// Reconciled PGlite verification runner for the User subsystem against the
// single authoritative schema (`script/01_schema.sql` + `script/02_seed.sql` + `script/verify_person1.sql`).
// Prerequisite (optional offline PGlite check):
//   npm install --prefix .tools @electric-sql/pglite@0.3.14
//   node script/check_person1.mjs
import { PGlite } from '../.tools/node_modules/@electric-sql/pglite/dist/index.js';
import fs from 'node:fs/promises';

const db = new PGlite();
await db.exec(await fs.readFile('script/01_schema.sql', 'utf8'));
await db.exec(await fs.readFile('script/02_seed.sql', 'utf8'));
await db.exec(await fs.readFile('script/verify_person1.sql', 'utf8'));

let checks = 0;
async function reject(sql) {
    let failed = false;
    try {
        await db.exec(sql);
    } catch {
        failed = true;
    }
    if (!failed) {
        throw Error('Expected constraint rejection: ' + sql);
    }
    checks++;
}

await reject(`INSERT INTO friendships (user_low_id, user_high_id) VALUES (2, 1)`);
await reject(`INSERT INTO friendships (user_low_id, user_high_id) VALUES (1, 1)`);
await reject(`INSERT INTO user_blocks (blocker_id, blocked_id) VALUES (1, 1)`);
await reject(`INSERT INTO friend_requests (sender_id, receiver_id, status) VALUES (10, 1, 'PENDING')`);
await reject(`INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version) VALUES (2, 1, 'text', decode('ff', 'hex'), decode('0123', 'hex'), 1)`);
await reject(`INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version) VALUES (1, 1, NULL, decode('abcd', 'hex'), decode('0123', 'hex'), 2)`);

const visibleForUser2 = await db.query(`
    SELECT dm.id, dm.content
    FROM direct_messages dm
    LEFT JOIN direct_message_hidden dmh
      ON dmh.message_id = dm.id AND dmh.user_id = 2
    WHERE dm.id = 1 AND dmh.message_id IS NULL
`);
if (visibleForUser2.rows.length !== 1) {
    throw Error('Message #1 hidden by user 1 must remain visible to user 2');
}
checks++;

console.log(`PASS: Authoritative schema (01_schema.sql + 02_seed.sql + verify_person1.sql) verified with ${checks} User-subsystem checks.`);
await db.close();
