-- =============================================================================
-- File: script/verify_person1.sql
-- Project: Chat System (Version 1 — User-Subsystem SQL Verification)
-- Target Engine: PostgreSQL 15+ (UTF-8)
--
-- Prerequisite:
--   Execute `script/01_schema.sql` and `script/02_seed.sql` first.
--
-- Purpose:
--   Reconciled User-subsystem constraint and visibility assertions adapted to
--   the single authoritative schema in `script/01_schema.sql` (`users`,
--   `friendships`, `friend_requests`, `user_blocks`, `direct_messages`,
--   `direct_message_hidden`, `chat_groups`, `group_members`, `group_messages`,
--   `group_message_hidden`, `user_public_keys`, `group_key_versions`,
--   `group_key_envelopes`).
--   Executes inside a rollback-safe transaction (`BEGIN; ... ROLLBACK;`) so
--   seed fixtures remain untouched.
-- =============================================================================

BEGIN;

SET client_encoding = 'UTF8';
SET TIME ZONE 'Asia/Ho_Chi_Minh';

DO $$
DECLARE
    v_user1_visible_dm_count BIGINT;
    v_user2_visible_dm_count BIGINT;
BEGIN
    -- 1. Canonical friendship ordering and self-friendship rejection
    BEGIN
        INSERT INTO friendships (user_low_id, user_high_id) VALUES (2, 1);
        RAISE EXCEPTION 'FAIL: Reversed friendship (2, 1) was not rejected';
    EXCEPTION
        WHEN check_violation THEN NULL;
    END;

    BEGIN
        INSERT INTO friendships (user_low_id, user_high_id) VALUES (1, 1);
        RAISE EXCEPTION 'FAIL: Self-friendship (1, 1) was not rejected';
    EXCEPTION
        WHEN check_violation THEN NULL;
    END;

    -- 2. Self-block rejection
    BEGIN
        INSERT INTO user_blocks (blocker_id, blocked_id) VALUES (1, 1);
        RAISE EXCEPTION 'FAIL: Self-block (1, 1) was not rejected';
    EXCEPTION
        WHEN check_violation THEN NULL;
    END;

    -- 3. Opposite-direction simultaneous PENDING friend request rejection
    --    (Seed data already has PENDING request 1 -> 10)
    BEGIN
        INSERT INTO friend_requests (sender_id, receiver_id, status)
        VALUES (10, 1, 'PENDING');
        RAISE EXCEPTION 'FAIL: Opposite-direction simultaneous PENDING request (10 -> 1) was not rejected';
    EXCEPTION
        WHEN unique_violation THEN NULL;
    END;

    -- 4. Group message plaintext vs. encrypted payload constraint check
    BEGIN
        INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version)
        VALUES (2, 1, 'Plaintext and ciphertext mixed', decode('abcd', 'hex'), decode('0123', 'hex'), 1);
        RAISE EXCEPTION 'FAIL: Mixed plaintext/ciphertext group message was not rejected';
    EXCEPTION
        WHEN check_violation THEN NULL;
    END;

    -- 5. Per-user message hiding isolation (`direct_message_hidden`):
    --    In seed data, message #1 (1 -> 2) is hidden by user 1, and message #3 (1 -> 2)
    --    is hidden by user 2. Total DMs between user 1 and user 2 = 4 (IDs 1, 2, 3, 4).
    --    Therefore, user 1 sees 3 messages (2, 3, 4) and user 2 sees 3 messages (1, 2, 4).
    SELECT COUNT(*)
    INTO v_user1_visible_dm_count
    FROM direct_messages dm
    LEFT JOIN direct_message_hidden dmh
      ON dmh.message_id = dm.id AND dmh.user_id = 1
    WHERE ((dm.sender_id = 1 AND dm.receiver_id = 2) OR (dm.sender_id = 2 AND dm.receiver_id = 1))
      AND dmh.message_id IS NULL;

    SELECT COUNT(*)
    INTO v_user2_visible_dm_count
    FROM direct_messages dm
    LEFT JOIN direct_message_hidden dmh
      ON dmh.message_id = dm.id AND dmh.user_id = 2
    WHERE ((dm.sender_id = 1 AND dm.receiver_id = 2) OR (dm.sender_id = 2 AND dm.receiver_id = 1))
      AND dmh.message_id IS NULL;

    IF v_user1_visible_dm_count <> 3 OR v_user2_visible_dm_count <> 3 THEN
        RAISE EXCEPTION 'FAIL: Expected 3 visible DMs for user 1 (got %) and 3 for user 2 (got %)',
            v_user1_visible_dm_count, v_user2_visible_dm_count;
    END IF;

    RAISE NOTICE 'PASS [verify_person1.sql]: All User-subsystem constraints and visibility checks passed on authoritative schema.';
END;
$$;

ROLLBACK;
