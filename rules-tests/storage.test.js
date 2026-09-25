import { after, before, describe, test } from 'node:test';
import { assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { deleteObject, ref, uploadBytes } from 'firebase/storage';
import { createEnv } from './setup.js';

let env;
const as = (uid) => env.authenticatedContext(uid).storage();
const jpg = new Uint8Array([0xff, 0xd8, 0xff, 0xd9]);
const JPEG = { contentType: 'image/jpeg' };
let n = 0;
// Fresh UUID-shaped name per test so create-only checks don't collide
const newId = () => `0b4c2f7e-1a2b-4c3d-8e9f-${String(++n).padStart(12, '0')}`;

before(async () => { env = await createEnv(); await env.clearStorage(); });
after(async () => { await env.cleanup(); });

describe('photo folders', () => {
  for (const folder of ['original_photos', 'signed_photos', 'thumbnails']) {
    test(`${folder}: upload to own folder`, () =>
      assertSucceeds(uploadBytes(ref(as('fan'), `${folder}/fan/${newId()}.jpg`), jpg, JPEG)));

    test(`${folder}: upload to someone else's folder denied`, () =>
      assertFails(uploadBytes(ref(as('fan'), `${folder}/celeb/${newId()}.jpg`), jpg, JPEG)));
  }

  test('overwriting an existing photo denied (create-only)', async () => {
    const path = `signed_photos/fan/${newId()}.jpg`;
    await assertSucceeds(uploadBytes(ref(as('fan'), path), jpg, JPEG));
    await assertFails(uploadBytes(ref(as('fan'), path), jpg, JPEG));
  });

  test('SVG denied', () =>
    assertFails(uploadBytes(ref(as('fan'), `thumbnails/fan/${newId()}.jpg`), jpg, { contentType: 'image/svg+xml' })));

  test('missing content type denied', () =>
    assertFails(uploadBytes(ref(as('fan'), `thumbnails/fan/${newId()}.jpg`), jpg)));

  test('non-UUID file name denied', () =>
    assertFails(uploadBytes(ref(as('fan'), 'thumbnails/fan/evil.html'), jpg, JPEG)));

  test('old flat path denied', () =>
    assertFails(uploadBytes(ref(as('fan'), `signed_photos/${newId()}.jpg`), jpg, JPEG)));

  test('owner deletes their photo', async () => {
    const path = `original_photos/fan/${newId()}.jpg`;
    await assertSucceeds(uploadBytes(ref(as('fan'), path), jpg, JPEG));
    await assertSucceeds(deleteObject(ref(as('fan'), path)));
  });

  test("cannot delete someone else's photo", async () => {
    const path = `original_photos/celeb/${newId()}.jpg`;
    await assertSucceeds(uploadBytes(ref(as('celeb'), path), jpg, JPEG));
    await assertFails(deleteObject(ref(as('fan'), path)));
  });
});

describe('profile images', () => {
  test('upload, replace and delete own avatar', async () => {
    const avatar = ref(as('fan'), 'profile_images/fan.jpg');
    await assertSucceeds(uploadBytes(avatar, jpg, JPEG));
    await assertSucceeds(uploadBytes(avatar, jpg, JPEG));
    await assertSucceeds(deleteObject(avatar));
  });

  test("cannot write someone else's avatar", () =>
    assertFails(uploadBytes(ref(as('fan'), 'profile_images/celeb.jpg'), jpg, JPEG)));

  test('signed-out users cannot upload', () =>
    assertFails(uploadBytes(ref(env.unauthenticatedContext().storage(), 'profile_images/fan.jpg'), jpg, JPEG)));
});
