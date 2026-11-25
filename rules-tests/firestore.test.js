import { after, before, beforeEach, describe, test } from 'node:test';
import { assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { deleteDoc, doc, getDoc, runTransaction, serverTimestamp, setDoc, updateDoc } from 'firebase/firestore';
import { createEnv, photo, request, seed, transaction, user } from './setup.js';

let env;
const as = (uid) => env.authenticatedContext(uid).firestore();

before(async () => { env = await createEnv(); });
beforeEach(async () => { await seed(env); });
after(async () => { await env.cleanup(); });

describe('users', () => {
  test('register creates a FAN profile', () =>
    assertSucceeds(setDoc(doc(as('new'), 'users/new'), user('new', 'FAN'))));

  test('cannot register pre-verified', () =>
    assertFails(setDoc(doc(as('new'), 'users/new'), user('new', 'CELEBRITY', { verificationStatus: 'VERIFIED' }))));

  test('cannot register with a rating', () =>
    assertFails(setDoc(doc(as('new'), 'users/new'), user('new', 'FAN', { rating: 5 }))));

  test('profile setup: FAN -> CELEBRITY', () =>
    assertSucceeds(updateDoc(doc(as('fan'), 'users/fan'),
      { displayName: 'F', bio: 'b', profileImageUrl: null, role: 'CELEBRITY' })));

  test('CELEBRITY -> FAN denied', () =>
    assertFails(updateDoc(doc(as('celeb'), 'users/celeb'), { role: 'FAN' })));

  test('cannot self-verify', () =>
    assertFails(updateDoc(doc(as('wannabe'), 'users/wannabe'), { verificationStatus: 'VERIFIED' })));

  test('verified celebrity edits profile (partial update)', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'users/celeb'),
      { displayName: 'C', bio: 'new', profileImageUrl: null, role: 'CELEBRITY' })));

  test('celebrity sets price and pauses requests', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'users/celeb'), { autographPrice: 250, isAcceptingRequests: false })));

  test('negative price denied', () =>
    assertFails(updateDoc(doc(as('celeb'), 'users/celeb'), { autographPrice: -1 })));

  for (const [field, value] of [['rating', 5], ['totalAutographs', 9999], ['totalPurchases', 50], ['collectionCount', 50]]) {
    test(`cannot edit own ${field}`, () =>
      assertFails(updateDoc(doc(as('celeb'), 'users/celeb'), { [field]: value })));
  }

  test('full overwrite that nulls verification denied', () =>
    assertFails(setDoc(doc(as('celeb'), 'users/celeb'), user('celeb', 'CELEBRITY'))));

  test('cannot edit someone else', () =>
    assertFails(updateDoc(doc(as('fan'), 'users/celeb'), { bio: 'hacked' })));

  test('can delete own profile', () =>
    assertSucceeds(deleteDoc(doc(as('fan'), 'users/fan'))));

  test('cannot delete someone else', () =>
    assertFails(deleteDoc(doc(as('fan'), 'users/celeb'))));
});

describe('account_deletions', () => {
  const marker = (uid, extra = {}) => ({ uid, role: 'FAN', requestedAt: serverTimestamp(), ...extra });

  test('user can mark own deletion', () =>
    assertSucceeds(setDoc(doc(as('fan'), 'account_deletions/fan'), marker('fan'))));

  test('retry after a failed deletion can overwrite the marker', async () => {
    await assertSucceeds(setDoc(doc(as('fan'), 'account_deletions/fan'), marker('fan')));
    await assertSucceeds(setDoc(doc(as('fan'), 'account_deletions/fan'), marker('fan')));
  });

  test('cannot mark someone else', () =>
    assertFails(setDoc(doc(as('fan'), 'account_deletions/celeb'), marker('celeb'))));

  test('uid must match the doc', () =>
    assertFails(setDoc(doc(as('fan'), 'account_deletions/fan'), marker('celeb'))));

  test('client-chosen timestamp denied', () =>
    assertFails(setDoc(doc(as('fan'), 'account_deletions/fan'), marker('fan', { requestedAt: 1 }))));

  test('extra fields denied', () =>
    assertFails(setDoc(doc(as('fan'), 'account_deletions/fan'), marker('fan', { email: 'x@example.com' }))));

  test('signed-out cannot write', () =>
    assertFails(setDoc(doc(env.unauthenticatedContext().firestore(), 'account_deletions/fan'), marker('fan'))));

  test('nobody can read markers from the app', async () => {
    await setDoc(doc(as('fan'), 'account_deletions/fan'), marker('fan'));
    await assertFails(getDoc(doc(as('fan'), 'account_deletions/fan')));
  });
});

describe('signed_photos create', () => {
  test('verified celebrity signs their own photo', () =>
    assertSucceeds(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p'))));

  test('unverified celebrity cannot sign (forgery)', () =>
    assertFails(setDoc(doc(as('wannabe'), 'signed_photos/p'),
      photo('p', { celebrityId: 'wannabe', ownerId: 'wannabe', celebrityName: 'Taylor Swift' }))));

  test('fan cannot sign (forgery)', () =>
    assertFails(setDoc(doc(as('fan'), 'signed_photos/p'),
      photo('p', { celebrityId: 'fan', ownerId: 'fan', celebrityName: 'Taylor Swift' }))));

  test('cannot create with fake likes', () =>
    assertFails(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { likeCount: 99999 }))));

  test('cannot create already LISTED', () =>
    assertFails(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { status: 'LISTED', price: 1 }))));

  test('signed for an open request -> owned by the fan', () =>
    assertSucceeds(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { ownerId: 'fan', requestId: 'accepted' }))));

  test('cannot plant a photo in a fan collection without a request', () =>
    assertFails(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { ownerId: 'fan' }))));

  test('request already completed', () =>
    assertFails(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { ownerId: 'fan', requestId: 'done' }))));

  test('request exists but owner is not its fan', () =>
    assertFails(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { ownerId: 'buyer', requestId: 'accepted' }))));

  test('request does not exist', () =>
    assertFails(setDoc(doc(as('celeb'), 'signed_photos/p'), photo('p', { ownerId: 'fan', requestId: 'nope' }))));
});

describe('signed_photos update', () => {
  test('celebrity finishes a draft', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'signed_photos/draft'),
      { status: 'SIGNED', signedPhotoUrl: 'u', thumbnailUrl: 't', signedAt: 2 })));

  test('celebrity cannot relist a photo a fan owns (item 7)', () =>
    assertFails(updateDoc(doc(as('celeb'), 'signed_photos/sold'), { status: 'LISTED' })));

  test('celebrity cannot swap the image on a sold photo', () =>
    assertFails(updateDoc(doc(as('celeb'), 'signed_photos/sold'), { signedPhotoUrl: 'evil' })));

  test('celebrity cannot edit a signed photo', () =>
    assertFails(updateDoc(doc(as('celeb'), 'signed_photos/signed'), { signedPhotoUrl: 'evil' })));

  test('owner lists a photo (full-doc set, as the app does)', () =>
    assertSucceeds(setDoc(doc(as('celeb'), 'signed_photos/signed'),
      photo('signed', { status: 'LISTED', price: 25, title: 'Signed poster', description: 'd' }))));

  test('owner cannot list for $0', () =>
    assertFails(updateDoc(doc(as('celeb'), 'signed_photos/signed'), { status: 'LISTED', price: 0 })));

  test('owner delists', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'signed_photos/listed'), { status: 'SIGNED' })));

  test('owner cannot bump likes', () =>
    assertFails(updateDoc(doc(as('celeb'), 'signed_photos/signed'), { likeCount: 500 })));

  test('non-owner cannot list', () =>
    assertFails(updateDoc(doc(as('buyer'), 'signed_photos/signed'), { status: 'LISTED', price: 1 })));

  test('buyer purchases a listing', () =>
    assertSucceeds(updateDoc(doc(as('buyer'), 'signed_photos/listed'), { status: 'SOLD', ownerId: 'buyer' })));

  test('purchase cannot change the price', () =>
    assertFails(updateDoc(doc(as('buyer'), 'signed_photos/listed'), { status: 'SOLD', ownerId: 'buyer', price: 0.01 })));

  test('cannot buy an unlisted photo', () =>
    assertFails(updateDoc(doc(as('buyer'), 'signed_photos/signed'), { status: 'SOLD', ownerId: 'buyer' })));

  test('owner cannot "buy" their own listing', () =>
    assertFails(updateDoc(doc(as('celeb'), 'signed_photos/listed'), { status: 'SOLD', ownerId: 'celeb' })));
});

describe('autograph_requests', () => {
  test('fan requests a verified celebrity at their price', () =>
    assertSucceeds(setDoc(doc(as('fan'), 'autograph_requests/r'), request('r'))));

  test('request below the listed price denied', () =>
    assertFails(setDoc(doc(as('fan'), 'autograph_requests/r'), request('r', { price: 0.01 }))));

  test('request to an unverified celebrity denied', () =>
    assertFails(setDoc(doc(as('fan'), 'autograph_requests/r'), request('r', { celebrityId: 'wannabe', price: 5 }))));

  test('request to a fan denied', () =>
    assertFails(setDoc(doc(as('buyer'), 'autograph_requests/r'), request('r', { fanId: 'buyer', celebrityId: 'fan', price: 0 }))));

  test('request to a celebrity who is not accepting denied', () =>
    assertFails(setDoc(doc(as('fan'), 'autograph_requests/r'), request('r', { celebrityId: 'closed', price: 100 }))));

  test('fan the celebrity blocked cannot request', () =>
    assertFails(setDoc(doc(as('buyer'), 'autograph_requests/r'), request('r', { fanId: 'buyer' }))));

  test('cannot request on behalf of another fan', () =>
    assertFails(setDoc(doc(as('fan'), 'autograph_requests/r'), request('r', { fanId: 'buyer' }))));

  test('cannot create pre-accepted', () =>
    assertFails(setDoc(doc(as('fan'), 'autograph_requests/r'), request('r', { status: 'ACCEPTED' }))));

  test('celebrity accepts', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'autograph_requests/pending'), { status: 'ACCEPTED', updatedAt: 2 })));

  test('celebrity rejects (REJECTED, as the app writes)', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'autograph_requests/pending'), { status: 'REJECTED', updatedAt: 2 })));

  test('celebrity completes with the photo made for it', () =>
    assertSucceeds(updateDoc(doc(as('celeb'), 'autograph_requests/accepted'),
      { status: 'COMPLETED', signedPhotoId: 'forAccepted', updatedAt: 2 })));

  test('cannot complete with an unrelated photo', () =>
    assertFails(updateDoc(doc(as('celeb'), 'autograph_requests/accepted'),
      { status: 'COMPLETED', signedPhotoId: 'signed', updatedAt: 2 })));

  test('cannot jump PENDING -> COMPLETED', () =>
    assertFails(updateDoc(doc(as('celeb'), 'autograph_requests/pending'),
      { status: 'COMPLETED', signedPhotoId: 'forAccepted' })));

  test('celebrity cannot change the price', () =>
    assertFails(updateDoc(doc(as('celeb'), 'autograph_requests/pending'), { status: 'ACCEPTED', price: 9999 })));

  test('celebrity cannot rewrite the message', () =>
    assertFails(updateDoc(doc(as('celeb'), 'autograph_requests/pending'), { status: 'ACCEPTED', message: 'x' })));

  test('fan cancels a pending request', () =>
    assertSucceeds(updateDoc(doc(as('fan'), 'autograph_requests/pending'), { status: 'CANCELLED', updatedAt: 2 })));

  test('fan cannot accept their own request', () =>
    assertFails(updateDoc(doc(as('fan'), 'autograph_requests/pending'), { status: 'ACCEPTED' })));

  test('outsider cannot read a request', () =>
    assertFails(import('firebase/firestore').then(({ getDoc }) => getDoc(doc(as('buyer'), 'autograph_requests/pending')))));
});

describe('transactions', () => {
  // The app buys with one Firestore transaction: record + ownership change together.
  const buy = (db, tx) => runTransaction(db, async (t) => {
    t.set(doc(db, `transactions/${tx.id}`), tx);
    t.update(doc(db, 'signed_photos/listed'), { status: 'SOLD', ownerId: tx.buyerId });
  });

  test('atomic purchase succeeds', () =>
    assertSucceeds(buy(as('buyer'), transaction('t1'))));

  test('purchase recorded at the wrong amount denied', () =>
    assertFails(buy(as('buyer'), transaction('t1', { amount: 0.01 }))));

  test('fake transaction against a victim seller denied', () =>
    assertFails(setDoc(doc(as('buyer'), 'transactions/t1'),
      transaction('t1', { sellerId: 'fan', amount: 1e6, relatedItemId: 'sold' }))));

  test('transaction for an unlisted photo denied', () =>
    assertFails(setDoc(doc(as('buyer'), 'transactions/t1'), transaction('t1', { relatedItemId: 'signed', amount: 0 }))));

  test('cannot record a purchase on behalf of someone else', () =>
    assertFails(setDoc(doc(as('buyer'), 'transactions/t1'), transaction('t1', { buyerId: 'fan' }))));

  test('transactions are append-only', async () => {
    await assertSucceeds(buy(as('buyer'), transaction('t1')));
    await assertFails(updateDoc(doc(as('buyer'), 'transactions/t1'), { amount: 0 }));
  });
});

describe('blocked', () => {
  const block = (name = 'Fan') => ({ displayName: name, blockedAt: serverTimestamp() });

  test('user blocks someone', () =>
    assertSucceeds(setDoc(doc(as('fan'), 'users/fan/blocked/celeb'), block('Celeb'))));

  test('cannot write to someone else\'s block list', () =>
    assertFails(setDoc(doc(as('fan'), 'users/celeb/blocked/fan'), block())));

  test('cannot block yourself', () =>
    assertFails(setDoc(doc(as('fan'), 'users/fan/blocked/fan'), block())));

  test('client timestamp rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'users/fan/blocked/celeb'), { displayName: 'C', blockedAt: new Date() })));

  test('extra fields rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'users/fan/blocked/celeb'), { ...block(), note: 'x' })));

  test('owner reads and unblocks', async () => {
    await assertSucceeds(getDoc(doc(as('celeb'), 'users/celeb/blocked/buyer')));
    await assertSucceeds(deleteDoc(doc(as('celeb'), 'users/celeb/blocked/buyer')));
  });

  test('others cannot see a block list', () =>
    assertFails(getDoc(doc(as('buyer'), 'users/celeb/blocked/buyer'))));

  test('others cannot remove a block', () =>
    assertFails(deleteDoc(doc(as('buyer'), 'users/celeb/blocked/buyer'))));

  test('after unblocking, the fan can request again', async () => {
    await deleteDoc(doc(as('celeb'), 'users/celeb/blocked/buyer'));
    await assertSucceeds(setDoc(doc(as('buyer'), 'autograph_requests/r'), request('r', { fanId: 'buyer' })));
  });
});

describe('reports', () => {
  const report = (extra = {}) => ({
    reporterId: 'fan', targetType: 'PHOTO', targetId: 'listed', targetUserId: 'celeb',
    reason: 'SPAM', details: '', createdAt: serverTimestamp(), ...extra,
  });

  test('user reports a photo', () =>
    assertSucceeds(setDoc(doc(as('fan'), 'reports/x'), report())));

  test('user reports another user with details', () =>
    assertSucceeds(setDoc(doc(as('fan'), 'reports/x'),
      report({ targetType: 'USER', targetId: 'celeb', reason: 'HARASSMENT', details: 'Rude messages' }))));

  test('cannot report as someone else', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ reporterId: 'buyer' }))));

  test('cannot report yourself', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ targetUserId: 'fan' }))));

  test('unknown reason rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ reason: 'BORED' }))));

  test('unknown target type rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ targetType: 'LISTING' }))));

  test('details over 500 characters rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ details: 'x'.repeat(501) }))));

  test('client timestamp rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ createdAt: new Date() }))));

  test('extra fields rejected', () =>
    assertFails(setDoc(doc(as('fan'), 'reports/x'), report({ priority: 'high' }))));

  test('signed-out users cannot report', () =>
    assertFails(setDoc(doc(env.unauthenticatedContext().firestore(), 'reports/x'), report())));

  test('reports are unreadable, even to the reporter', async () => {
    await setDoc(doc(as('fan'), 'reports/x'), report());
    await assertFails(getDoc(doc(as('fan'), 'reports/x')));
  });

  test('reports cannot be changed or deleted', async () => {
    await setDoc(doc(as('fan'), 'reports/x'), report());
    await assertFails(updateDoc(doc(as('fan'), 'reports/x'), { reason: 'OTHER' }));
    await assertFails(deleteDoc(doc(as('fan'), 'reports/x')));
  });
});
