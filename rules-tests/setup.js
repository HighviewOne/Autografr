import { initializeTestEnvironment } from '@firebase/rules-unit-testing';
import { readFileSync } from 'node:fs';
import { doc, setDoc } from 'firebase/firestore';

const rules = (name) => readFileSync(new URL(`../${name}`, import.meta.url), 'utf8');

export async function createEnv() {
  return initializeTestEnvironment({
    projectId: 'demo-autografr',
    firestore: { rules: rules('firestore.rules'), host: '127.0.0.1', port: 8181 },
    storage: { rules: rules('storage.rules'), host: '127.0.0.1', port: 9199 },
  });
}

// Shapes match what the app writes (see data/remote/dto/*.kt).
export const user = (id, role, extra = {}) => ({
  id, email: `${id}@example.com`, displayName: id, profileImageUrl: null, role, bio: '',
  createdAt: 1, verificationStatus: null, ...extra,
});

export const photo = (id, extra = {}) => ({
  id, celebrityId: 'celeb', celebrityName: 'Celeb', originalPhotoUrl: '', signedPhotoUrl: '',
  thumbnailUrl: '', title: '', description: '', price: 0, status: 'SIGNED', requestId: null,
  ownerId: 'celeb', createdAt: 1, signedAt: 1, tags: [], likeCount: 0, viewCount: 0, ...extra,
});

export const request = (id, extra = {}) => ({
  id, fanId: 'fan', fanName: 'fan', celebrityId: 'celeb', celebrityName: 'Celeb',
  photoUrl: 'https://example.com/p.jpg', message: '', status: 'PENDING', price: 200,
  signedPhotoId: null, createdAt: 1, updatedAt: 1, expiresAt: null, ...extra,
});

export const transaction = (id, extra = {}) => ({
  id, buyerId: 'buyer', sellerId: 'celeb', type: 'MARKETPLACE_PURCHASE', status: 'COMPLETED',
  amount: 50, currency: 'USD', relatedItemId: 'listed', description: '', createdAt: 1,
  completedAt: 1, ...extra,
});

/**
 * Standard world: a verified celebrity "celeb" ($200), an unverified "wannabe",
 * fans "fan" and "buyer", one photo of each status, and requests in each state.
 */
export async function seed(env) {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const put = (path, data) => setDoc(doc(db, path), data);
    await put('users/celeb', user('celeb', 'CELEBRITY', {
      verificationStatus: 'VERIFIED', autographPrice: 200, isAcceptingRequests: true, rating: 4.5,
      totalAutographs: 10,
    }));
    await put('users/wannabe', user('wannabe', 'CELEBRITY', { autographPrice: 5 }));
    await put('users/closed', user('closed', 'CELEBRITY', {
      verificationStatus: 'VERIFIED', autographPrice: 100, isAcceptingRequests: false,
    }));
    await put('users/fan', user('fan', 'FAN'));
    await put('users/buyer', user('buyer', 'FAN'));

    await put('signed_photos/draft', photo('draft', { status: 'DRAFT' }));
    await put('signed_photos/signed', photo('signed'));
    await put('signed_photos/listed', photo('listed', { status: 'LISTED', price: 50, title: 'T' }));
    await put('signed_photos/sold', photo('sold', { status: 'SOLD', price: 50, ownerId: 'fan' }));

    await put('autograph_requests/pending', request('pending'));
    await put('autograph_requests/accepted', request('accepted', { status: 'ACCEPTED' }));
    await put('autograph_requests/done', request('done', { status: 'COMPLETED' }));
    await put('signed_photos/forAccepted', photo('forAccepted', { ownerId: 'fan', requestId: 'accepted' }));
    // celeb has blocked buyer
    await put('users/celeb/blocked/buyer', { displayName: 'buyer', blockedAt: new Date(0) });
  });
}
