-- =============================================================================
-- File: script/01_person1.sql
-- Project: Chat System (Version 1 — Reconciled User-Subsystem SQL Queries)
-- Target Engine: PostgreSQL 15+ (UTF-8)
--
-- IMPORTANT INTEGRATION NOTICE (Reconciled Database Contract):
-- 1. `script/01_schema.sql` is the SINGLE authoritative schema creation script
--    for the entire project (covering both the User and Administrator apps).
-- 2. Do NOT create competing tables (`app_users`, `conversations`,
--    `conversation_members`, `messages`, `message_user_states`,
--    `conversation_member_keys`).
-- 3. Schema mapping from Member 1's initial draft to the authoritative schema
--    in `script/01_schema.sql` and `docs/database-design.md`:
--      - `app_users`                -> `users`
--      - `friendships(user_low, user_high)`
--                                   -> `friendships(user_low_id, user_high_id)`
--      - `friend_requests` status   -> uppercase `'PENDING'`, `'ACCEPTED'`,
--                                      `'REJECTED'`, `'CANCELLED'`
--      - `user_blocks`              -> `user_blocks(blocker_id, blocked_id)`
--      - Direct `conversations` & `messages`
--                                   -> `direct_messages(id, sender_id, receiver_id, content, sent_at)`
--      - Direct `message_user_states(hidden_at)`
--                                   -> `direct_message_hidden(message_id, user_id, hidden_at)`
--      - Group `conversations`      -> `chat_groups(id, name, created_by, is_encrypted, created_at)`
--      - `conversation_members`     -> `group_members(group_id, user_id, role, joined_at, left_at)`
--      - Group `messages`           -> `group_messages(id, group_id, sender_id, content, ciphertext, nonce, key_version, sent_at)`
--      - Group `message_user_states(hidden_at)`
--                                   -> `group_message_hidden(message_id, user_id, hidden_at)`
--      - `conversation_member_keys` -> `user_public_keys`, `group_key_versions`,
--                                      `group_key_envelopes`
--
-- Prerequisite: Execute `script/01_schema.sql` and `script/02_seed.sql` first.
-- This script contains read-only reference queries for the User application.
-- =============================================================================

SET client_encoding = 'UTF8';
SET TIME ZONE 'Asia/Ho_Chi_Minh';

-- -----------------------------------------------------------------------------
-- USER QUERY 1: Unified Conversation Sidebar List for a Logged-In User
-- Combines 1-on-1 direct message partners (`direct_messages`) and active group
-- memberships (`chat_groups` + `group_members WHERE left_at IS NULL`) into the
-- unified conversation list displayed by `ChatData` / `ConversationListPanel`.
-- -----------------------------------------------------------------------------
WITH params AS (
    SELECT 1::BIGINT AS current_user_id
),
direct_threads AS (
    SELECT
        'DIRECT'::TEXT AS conversation_kind,
        partner.id AS target_id,
        partner.full_name AS display_name,
        FALSE AS is_encrypted,
        MAX(dm.sent_at) AS last_activity_at
    FROM direct_messages dm
    CROSS JOIN params p
    JOIN users partner
      ON partner.id = CASE
          WHEN dm.sender_id = p.current_user_id THEN dm.receiver_id
          ELSE dm.sender_id
      END
    LEFT JOIN direct_message_hidden dmh
      ON dmh.message_id = dm.id
     AND dmh.user_id = p.current_user_id
    WHERE (dm.sender_id = p.current_user_id OR dm.receiver_id = p.current_user_id)
      AND dmh.message_id IS NULL
      AND partner.deleted_at IS NULL
      AND NOT EXISTS (
          SELECT 1
          FROM user_blocks ub
          WHERE (ub.blocker_id = p.current_user_id AND ub.blocked_id = partner.id)
             OR (ub.blocker_id = partner.id AND ub.blocked_id = p.current_user_id)
      )
    GROUP BY partner.id, partner.full_name
),
group_threads AS (
    SELECT
        'GROUP'::TEXT AS conversation_kind,
        cg.id AS target_id,
        cg.name AS display_name,
        cg.is_encrypted,
        COALESCE(MAX(gm_msg.sent_at), cg.created_at) AS last_activity_at
    FROM group_members gm
    CROSS JOIN params p
    JOIN chat_groups cg ON cg.id = gm.group_id
    LEFT JOIN group_messages gm_msg ON gm_msg.group_id = cg.id
    LEFT JOIN group_message_hidden gmh
      ON gmh.message_id = gm_msg.id
     AND gmh.user_id = p.current_user_id
    WHERE gm.user_id = p.current_user_id
      AND gm.left_at IS NULL
      AND gmh.message_id IS NULL
    GROUP BY cg.id, cg.name, cg.is_encrypted, cg.created_at
)
SELECT
    conversation_kind,
    target_id,
    display_name,
    is_encrypted,
    last_activity_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS last_activity_vn
FROM (
    SELECT * FROM direct_threads
    UNION ALL
    SELECT * FROM group_threads
) unified
ORDER BY last_activity_at DESC, conversation_kind ASC, target_id ASC;


-- -----------------------------------------------------------------------------
-- USER QUERY 2: Visible Direct Message Transcript Between Two Users
-- Excludes messages hidden by `current_user_id` in `direct_message_hidden`
-- while preserving them for the other participant and for historical stats.
-- -----------------------------------------------------------------------------
WITH params AS (
    SELECT
        1::BIGINT AS current_user_id,
        2::BIGINT AS partner_user_id
)
SELECT
    dm.id AS message_id,
    dm.sender_id,
    u.username AS sender_username,
    u.full_name AS sender_full_name,
    dm.content,
    dm.sent_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS sent_at_vn
FROM direct_messages dm
JOIN users u ON u.id = dm.sender_id
CROSS JOIN params p
LEFT JOIN direct_message_hidden dmh
  ON dmh.message_id = dm.id
 AND dmh.user_id = p.current_user_id
WHERE (
      (dm.sender_id = p.current_user_id AND dm.receiver_id = p.partner_user_id)
   OR (dm.sender_id = p.partner_user_id AND dm.receiver_id = p.current_user_id)
  )
  AND dmh.message_id IS NULL
ORDER BY dm.sent_at ASC, dm.id ASC;


-- -----------------------------------------------------------------------------
-- USER QUERY 3: Visible Group Message Transcript (Plaintext & Encrypted E2EE)
-- Excludes messages hidden by `current_user_id` in `group_message_hidden`.
-- -----------------------------------------------------------------------------
WITH params AS (
    SELECT
        1::BIGINT AS current_user_id,
        1::BIGINT AS target_group_id
)
SELECT
    gm.id AS message_id,
    gm.group_id,
    gm.sender_id,
    u.username AS sender_username,
    u.full_name AS sender_full_name,
    gm.content,
    gm.ciphertext IS NOT NULL AS is_encrypted_payload,
    gm.key_version,
    gm.sent_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS sent_at_vn
FROM group_messages gm
JOIN users u ON u.id = gm.sender_id
CROSS JOIN params p
LEFT JOIN group_message_hidden gmh
  ON gmh.message_id = gm.id
 AND gmh.user_id = p.current_user_id
WHERE gm.group_id = p.target_group_id
  AND gmh.message_id IS NULL
ORDER BY gm.sent_at ASC, gm.id ASC;


-- -----------------------------------------------------------------------------
-- USER QUERY 4: User Search Excluding Directional Blocks in Either Direction
-- (Business Rule 3 — a block in either direction hides both users from search)
-- -----------------------------------------------------------------------------
WITH params AS (
    SELECT
        2::BIGINT AS current_user_id,
        ''::TEXT  AS keyword
)
SELECT
    u.id AS user_id,
    u.username,
    u.full_name
FROM users u
CROSS JOIN params p
WHERE u.role = 'USER'
  AND u.deleted_at IS NULL
  AND u.id <> p.current_user_id
  AND (
      p.keyword = ''
      OR u.username ILIKE '%' || p.keyword || '%'
      OR u.full_name ILIKE '%' || p.keyword || '%'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM user_blocks ub
      WHERE (ub.blocker_id = p.current_user_id AND ub.blocked_id = u.id)
         OR (ub.blocker_id = u.id AND ub.blocked_id = p.current_user_id)
  )
ORDER BY u.full_name ASC, u.id ASC;
