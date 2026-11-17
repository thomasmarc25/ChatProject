-- =============================================================================
-- File: script/04_verify.sql
-- Project: Chat System (Version 1 — Automated Database Verification Suite)
-- Target Engine: PostgreSQL 15+ (UTF-8)
-- Prerequisite: Execute script/01_schema.sql and script/02_seed.sql first
--               in a disposable test database.
--
-- PURPOSE & SCOPE DISTINCTION:
--   A. DATABASE-ENFORCED CONSTRAINTS & QUERY INVARIANTS (Verified by this script):
--      1. Case-insensitive uniqueness on users.username and users.email.
--      2. Rejection of self-relationships in friendships, friend_requests,
--         user_blocks, direct_messages, and spam_reports.
--      3. Rejection of reversed or duplicate friendships (user_low_id < user_high_id).
--      4. Rejection of simultaneous opposite-direction PENDING friend_requests.
--      5. Rejection of duplicate (launch_id, user_id) rows in app_open_events.
--      6. Foreign key enforcement (invalid references & ON DELETE RESTRICT on
--         historical records).
--      7. Rejection of invalid plaintext/ciphertext combinations in group_messages.
--      8. Rejection of group_messages referencing another group's key_version.
--      9. Accurate 1-hop and 2-hop friend counts eliminating overlap, self,
--         pending/rejected/cancelled requests, and soft-deleted accounts/paths.
--     10. Repeated direct messages to one recipient count as 1 chat partner.
--     11. Repeated group messages to one group count as 1 group.
--     12. Multiple app opens by one user in a month count as multiple opens
--         per user, but 1 distinct monthly user in the annual activity chart.
--     13. Multiple login_history rows within a single launch do not increase
--         app_open_events counts.
--     14. Per-user hidden messages (direct_message_hidden, group_message_hidden)
--         do not reduce historical activity counts.
--     15. Monthly registration and activity charts return all 12 rows (1..12),
--         including zero-count months (Month 8 = 0).
--     16. Vietnam-local (Asia/Ho_Chi_Minh) month and year boundaries classify
--         UTC boundary timestamps into the expected Vietnam calendar month/year.
--
--   B. FUTURE VERSION 2 SERVICE-LAYER BUSINESS RULES (NOT claimed to be
--      enforced by standalone SQL DDL):
--      - Checking user.role = 'ADMIN', is_locked = FALSE, and deleted_at IS NULL
--        before authorizing privileged commands.
--      - Checking user_blocks before allowing direct messages, friend requests,
--        or user search visibility, and atomically removing friendships /
--        cancelling PENDING requests when a block is created.
--      - Atomically updating friend_requests.status = 'ACCEPTED' and inserting
--        the canonical row into friendships.
--      - Verifying active group membership (left_at IS NULL) before allowing
--        a user to send group messages or manage a group.
--      - Maintaining at least one active ADMIN per chat group.
--      - Verifying message participation before inserting into
--        direct_message_hidden or group_message_hidden.
--      - Verifying that spam_reports.direct_message_id was sent by
--        reported_user_id to reporter_id.
--      - Enforcing chat_groups.is_encrypted mode consistency and rotating
--        group_key_versions / group_key_envelopes when membership changes.
--
-- ROLLBACK SAFETY:
--   Every negative constraint check runs inside a PL/pgSQL BEGIN ... EXCEPTION
--   subtransaction (implicit savepoint), and the entire verification script is
--   wrapped in BEGIN; ... ROLLBACK; so zero fixture changes are left behind.
-- =============================================================================

BEGIN;

SET client_encoding = 'UTF8';
SET TIME ZONE 'Asia/Ho_Chi_Minh';

DO $$
DECLARE
    v_caught        BOOLEAN;
    v_direct_cnt    BIGINT;
    v_fof_cnt       BIGINT;
    v_dm_total      BIGINT;
    v_dm_distinct   BIGINT;
    v_gm_total      BIGINT;
    v_gm_distinct   BIGINT;
    v_user1_opens   BIGINT;
    v_mar_open_rows BIGINT;
    v_mar_dist_usr  BIGINT;
    v_mar05_logins  BIGINT;
    v_mar05_opens   BIGINT;
    v_hidden_dm     BIGINT;
    v_hidden_gm     BIGINT;
    v_reg_months    BIGINT;
    v_reg_month1    BIGINT;
    v_reg_month2    BIGINT;
    v_reg_month8    BIGINT;
    v_act_months    BIGINT;
    v_act_month1    BIGINT;
    v_act_month2    BIGINT;
    v_act_month3    BIGINT;
    v_act_month8    BIGINT;
    v_dec2025_reg   BIGINT;
    v_dec2025_act   BIGINT;
BEGIN
    RAISE NOTICE '=== Starting Chat System PostgreSQL Verification Suite ===';

    -- =========================================================================
    -- PART A: DATABASE-ENFORCED CONSTRAINT VERIFICATION (NEGATIVE TESTS)
    -- =========================================================================

    -- A1. Duplicate username differing only in case ('NguyenVanAn' vs 'nguyenvanan')
    v_caught := FALSE;
    BEGIN
        INSERT INTO users (username, password_hash, full_name, gender, email, role)
        VALUES ('NguyenVanAn', '{pbkdf2@sha256$test}', 'Duplicate Username Case', 'MALE', 'unique_email_test@chatsystem.vn', 'USER');
    EXCEPTION
        WHEN unique_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A1]: Duplicate username differing only in case was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A1]: Case-insensitive username uniqueness enforced (uq_users_username_lower).';

    -- A2. Duplicate email differing only in case ('NGUYENVANAN@CHATSYSTEM.VN')
    v_caught := FALSE;
    BEGIN
        INSERT INTO users (username, password_hash, full_name, gender, email, role)
        VALUES ('unique_user_test', '{pbkdf2@sha256$test}', 'Duplicate Email Case', 'MALE', 'NGUYENVANAN@CHATSYSTEM.VN', 'USER');
    EXCEPTION
        WHEN unique_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A2]: Duplicate email differing only in case was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A2]: Case-insensitive email uniqueness enforced (uq_users_email_lower).';

    -- A3a. Self friendship (1, 1) rejected
    v_caught := FALSE;
    BEGIN
        INSERT INTO friendships (user_low_id, user_high_id) VALUES (1, 1);
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A3a]: Self-friendship (1, 1) was NOT rejected';
    END IF;

    -- A3b. Self friend request (1 -> 1) rejected
    v_caught := FALSE;
    BEGIN
        INSERT INTO friend_requests (sender_id, receiver_id, status) VALUES (1, 1, 'PENDING');
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A3b]: Self friend request (1 -> 1) was NOT rejected';
    END IF;

    -- A3c. Self block (1 -> 1) rejected
    v_caught := FALSE;
    BEGIN
        INSERT INTO user_blocks (blocker_id, blocked_id) VALUES (1, 1);
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A3c]: Self block (1 -> 1) was NOT rejected';
    END IF;

    -- A3d. Self direct message (1 -> 1) rejected
    v_caught := FALSE;
    BEGIN
        INSERT INTO direct_messages (sender_id, receiver_id, content) VALUES (1, 1, 'Self message');
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A3d]: Self direct message (1 -> 1) was NOT rejected';
    END IF;

    -- A3e. Self spam report (1 -> 1) rejected
    v_caught := FALSE;
    BEGIN
        INSERT INTO spam_reports (reporter_id, reported_user_id, reason, status)
        VALUES (1, 1, 'Self spam report', 'PENDING');
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A3e]: Self spam report (1 -> 1) was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A3]: Self-relationships rejected across friendships, friend_requests, user_blocks, direct_messages, and spam_reports.';

    -- A4a. Reversed friendship (2, 1) rejected by canonical order check (user_low_id < user_high_id)
    v_caught := FALSE;
    BEGIN
        INSERT INTO friendships (user_low_id, user_high_id) VALUES (2, 1);
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A4a]: Reversed friendship (2, 1) was NOT rejected by chk_friendships_canonical_order';
    END IF;

    -- A4b. Duplicate canonical friendship (1, 2) rejected by PK
    v_caught := FALSE;
    BEGIN
        INSERT INTO friendships (user_low_id, user_high_id) VALUES (1, 2);
    EXCEPTION
        WHEN unique_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A4b]: Duplicate canonical friendship (1, 2) was NOT rejected by pk_friendships';
    END IF;
    RAISE NOTICE 'PASS [A4]: Reversed and duplicate friendships rejected.';

    -- A5. Opposite-direction simultaneous PENDING friend request (10 -> 1 when 1 -> 10 is PENDING)
    v_caught := FALSE;
    BEGIN
        INSERT INTO friend_requests (sender_id, receiver_id, status)
        VALUES (10, 1, 'PENDING');
    EXCEPTION
        WHEN unique_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A5]: Opposite-direction simultaneous PENDING request (10 -> 1) was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A5]: Opposite-direction simultaneous PENDING friend request rejected (uq_friend_requests_pending_pair).';

    -- A6. Duplicate (launch_id, user_id) app-open event rejected
    v_caught := FALSE;
    BEGIN
        INSERT INTO app_open_events (launch_id, user_id, opened_at)
        VALUES (UUID '11111111-1111-4111-8111-111111110301', 1, now());
    EXCEPTION
        WHEN unique_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A6]: Duplicate (launch_id, user_id) in app_open_events was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A6]: Duplicate (launch_id, user_id) app-open event rejected (uq_app_open_events_launch_user).';

    -- A7a. Foreign key rejects non-existent user reference
    v_caught := FALSE;
    BEGIN
        INSERT INTO login_history (user_id, login_at, ip_address)
        VALUES (999999, now(), '127.0.0.1');
    EXCEPTION
        WHEN foreign_key_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A7a]: Foreign key did NOT reject non-existent user_id = 999999';
    END IF;

    -- A7b. Restrictive FK prevents physical deletion of user with historical records
    v_caught := FALSE;
    BEGIN
        DELETE FROM users WHERE id = 1;
    EXCEPTION
        WHEN restrict_violation OR foreign_key_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A7b]: ON DELETE RESTRICT did NOT prevent physical deletion of user 1';
    END IF;
    RAISE NOTICE 'PASS [A7]: Foreign keys reject invalid references (23503) and prevent cascading deletion of historical user records (23001 restrict_violation).';

    -- A8a. Invalid group_messages payload: both plaintext and ciphertext present
    v_caught := FALSE;
    BEGIN
        INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version)
        VALUES (2, 1, 'Plaintext', decode('0102', 'hex'), decode('0304', 'hex'), 2);
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A8a]: group_messages with BOTH plaintext and ciphertext was NOT rejected';
    END IF;

    -- A8b. Invalid group_messages payload: neither plaintext nor ciphertext present
    v_caught := FALSE;
    BEGIN
        INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version)
        VALUES (1, 1, NULL, NULL, NULL, NULL);
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A8b]: group_messages with ALL NULL payload fields was NOT rejected';
    END IF;

    -- A8c. Invalid group_messages payload: ciphertext present but nonce NULL
    v_caught := FALSE;
    BEGIN
        INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version)
        VALUES (2, 1, NULL, decode('0102', 'hex'), NULL, 2);
    EXCEPTION
        WHEN check_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A8c]: group_messages with missing nonce was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A8]: Invalid plaintext/ciphertext combinations in group_messages rejected (chk_group_messages_payload_mode).';

    -- A9. Group message referencing another group's key_version (Group 1 referencing Group 2's key_version = 2)
    v_caught := FALSE;
    BEGIN
        INSERT INTO group_messages (group_id, sender_id, content, ciphertext, nonce, key_version)
        VALUES (1, 1, NULL, decode('0102', 'hex'), decode('0304', 'hex'), 2);
    EXCEPTION
        WHEN foreign_key_violation THEN
            v_caught := TRUE;
    END;
    IF NOT v_caught THEN
        RAISE EXCEPTION 'FAIL [A9]: Group 1 message referencing Group 2 key_version = 2 was NOT rejected';
    END IF;
    RAISE NOTICE 'PASS [A9]: Composite FK (group_id, key_version) prevents cross-group key version references.';


    -- =========================================================================
    -- PART B: SEED FIXTURE & QUERY INVARIANT ASSERTIONS (POSITIVE TESTS)
    -- =========================================================================

    -- B1. Friend counts for User 1 ('nguyenvanan'):
    --     Direct active friends = 3 ({2, 3, 4}; excludes soft-deleted 26 and pending 10, 11)
    --     1-or-2-hop active friends = 5 ({2, 3, 4, 5, 6}; eliminates triangle/diamond overlap,
    --     excludes self 1, excludes 9 via soft-deleted 26, excludes 3-hop user 7)
    WITH active_users AS (
        SELECT id FROM users WHERE deleted_at IS NULL AND role = 'USER'
    ),
    directed_edges AS (
        SELECT f.user_low_id AS u_id, f.user_high_id AS v_id
        FROM friendships f
        JOIN active_users u1 ON u1.id = f.user_low_id
        JOIN active_users u2 ON u2.id = f.user_high_id
        UNION ALL
        SELECT f.user_high_id AS u_id, f.user_low_id AS v_id
        FROM friendships f
        JOIN active_users u1 ON u1.id = f.user_high_id
        JOIN active_users u2 ON u2.id = f.user_low_id
    ),
    reach AS (
        SELECT v_id AS reachable_id FROM directed_edges WHERE u_id = 1
        UNION
        SELECT e2.v_id AS reachable_id
        FROM directed_edges e1
        JOIN directed_edges e2 ON e2.u_id = e1.v_id
        WHERE e1.u_id = 1 AND e2.v_id <> 1
    )
    SELECT
        (SELECT COUNT(DISTINCT v_id) FROM directed_edges WHERE u_id = 1),
        (SELECT COUNT(DISTINCT reachable_id) FROM reach)
    INTO v_direct_cnt, v_fof_cnt;

    IF v_direct_cnt <> 3 OR v_fof_cnt <> 5 THEN
        RAISE EXCEPTION 'FAIL [B1]: Expected User 1 (direct=3, fof=5), got (direct=%, fof=%)', v_direct_cnt, v_fof_cnt;
    END IF;
    RAISE NOTICE 'PASS [B1]: User 1 friend counts verified (direct_friends_count = 3, friends_with_fof_count = 5).';

    -- B2. Repeated direct messages to one recipient count as one chat partner
    SELECT COUNT(*), COUNT(DISTINCT receiver_id)
    INTO v_dm_total, v_dm_distinct
    FROM direct_messages
    WHERE sender_id = 1
      AND sent_at >= TIMESTAMPTZ '2026-03-01 00:00:00+07'
      AND sent_at <  TIMESTAMPTZ '2026-04-01 00:00:00+07';

    IF v_dm_total <> 4 OR v_dm_distinct <> 2 THEN
        RAISE EXCEPTION 'FAIL [B2]: Expected User 1 March DMs (total=4, distinct_recipients=2), got (total=%, distinct=%)', v_dm_total, v_dm_distinct;
    END IF;
    RAISE NOTICE 'PASS [B2]: Repeated direct messages (4 total) counted as 2 distinct chat partners.';

    -- B3. Repeated group messages count as one group
    SELECT COUNT(*), COUNT(DISTINCT group_id)
    INTO v_gm_total, v_gm_distinct
    FROM group_messages
    WHERE sender_id = 1
      AND sent_at >= TIMESTAMPTZ '2026-03-01 00:00:00+07'
      AND sent_at <  TIMESTAMPTZ '2026-04-01 00:00:00+07';

    IF v_gm_total <> 5 OR v_gm_distinct <> 2 THEN
        RAISE EXCEPTION 'FAIL [B3]: Expected User 1 March group messages (total=5, distinct_groups=2), got (total=%, distinct=%)', v_gm_total, v_gm_distinct;
    END IF;
    RAISE NOTICE 'PASS [B3]: Repeated group messages (5 total) counted as 2 distinct groups.';

    -- B4. Multiple app opens in one month count as multiple opens per user, but 1 monthly user
    SELECT
        COUNT(*) FILTER (WHERE user_id = 1),
        COUNT(*),
        COUNT(DISTINCT user_id)
    INTO v_user1_opens, v_mar_open_rows, v_mar_dist_usr
    FROM app_open_events
    WHERE opened_at >= TIMESTAMPTZ '2026-03-01 00:00:00+07'
      AND opened_at <  TIMESTAMPTZ '2026-04-01 00:00:00+07';

    IF v_user1_opens <> 3 OR v_mar_open_rows <> 8 OR v_mar_dist_usr <> 5 THEN
        RAISE EXCEPTION 'FAIL [B4]: Expected March 2026 opens (user1=3, total_rows=8, distinct_users=5), got (%, %, %)',
            v_user1_opens, v_mar_open_rows, v_mar_dist_usr;
    END IF;
    RAISE NOTICE 'PASS [B4]: Multiple app opens counted as 3 opens for User 1, and 5 distinct monthly users (from 8 events).';

    -- B5. Multiple login events in a single launch do not increase app-open counts
    SELECT COUNT(*) INTO v_mar05_logins
    FROM login_history
    WHERE user_id = 1
      AND login_at >= TIMESTAMPTZ '2026-03-05 00:00:00+07'
      AND login_at <  TIMESTAMPTZ '2026-03-06 00:00:00+07';

    SELECT COUNT(*) INTO v_mar05_opens
    FROM app_open_events
    WHERE user_id = 1
      AND opened_at >= TIMESTAMPTZ '2026-03-05 00:00:00+07'
      AND opened_at <  TIMESTAMPTZ '2026-03-06 00:00:00+07';

    IF v_mar05_logins <> 3 OR v_mar05_opens <> 1 THEN
        RAISE EXCEPTION 'FAIL [B5]: Expected 2026-03-05 User 1 (logins=3, app_opens=1), got (logins=%, app_opens=%)',
            v_mar05_logins, v_mar05_opens;
    END IF;
    RAISE NOTICE 'PASS [B5]: 3 logins on 2026-03-05 within one launch recorded as 1 app-open event.';

    -- B6. Hidden messages do not reduce historical activity counts
    SELECT COUNT(*) INTO v_hidden_dm FROM direct_message_hidden WHERE user_id = 1;
    SELECT COUNT(*) INTO v_hidden_gm FROM group_message_hidden WHERE user_id = 1;

    IF v_hidden_dm < 1 OR v_hidden_gm < 1 OR v_dm_distinct <> 2 OR v_gm_distinct <> 2 THEN
        RAISE EXCEPTION 'FAIL [B6]: Hidden message rows failed to preserve historical activity counts';
    END IF;
    RAISE NOTICE 'PASS [B6]: Per-user hidden messages do not reduce historical activity counts.';

    -- B7. Monthly registration & activity charts contain 12 rows (including zero-count Month 8)
    --     and Vietnam-local month/year boundaries classify UTC boundary events accurately
    WITH months AS (
        SELECT
            m.month_num,
            make_timestamptz(2026, m.month_num, 1, 0, 0, 0, 'Asia/Ho_Chi_Minh') AS month_start,
            make_timestamptz(2026, m.month_num, 1, 0, 0, 0, 'Asia/Ho_Chi_Minh') + INTERVAL '1 month' AS month_end
        FROM generate_series(1, 12) AS m(month_num)
    ),
    reg_chart AS (
        SELECT m.month_num, COUNT(u.id) AS reg_cnt
        FROM months m
        LEFT JOIN users u
            ON u.role = 'USER'
           AND u.created_at >= m.month_start
           AND u.created_at <  m.month_end
        GROUP BY m.month_num
    ),
    act_chart AS (
        SELECT m.month_num, COUNT(DISTINCT aoe.user_id) AS act_cnt
        FROM months m
        LEFT JOIN app_open_events aoe
            ON aoe.opened_at >= m.month_start
           AND aoe.opened_at <  m.month_end
        GROUP BY m.month_num
    )
    SELECT
        (SELECT COUNT(*) FROM reg_chart),
        (SELECT reg_cnt FROM reg_chart WHERE month_num = 1),
        (SELECT reg_cnt FROM reg_chart WHERE month_num = 2),
        (SELECT reg_cnt FROM reg_chart WHERE month_num = 8),
        (SELECT COUNT(*) FROM act_chart),
        (SELECT act_cnt FROM act_chart WHERE month_num = 1),
        (SELECT act_cnt FROM act_chart WHERE month_num = 2),
        (SELECT act_cnt FROM act_chart WHERE month_num = 3),
        (SELECT act_cnt FROM act_chart WHERE month_num = 8)
    INTO
        v_reg_months, v_reg_month1, v_reg_month2, v_reg_month8,
        v_act_months, v_act_month1, v_act_month2, v_act_month3, v_act_month8;

    IF v_reg_months <> 12 OR v_reg_month1 <> 4 OR v_reg_month2 <> 5 OR v_reg_month8 <> 0 THEN
        RAISE EXCEPTION 'FAIL [B7-Reg]: Unexpected 2026 registration chart counts (months=%, m1=%, m2=%, m8=%)',
            v_reg_months, v_reg_month1, v_reg_month2, v_reg_month8;
    END IF;

    IF v_act_months <> 12 OR v_act_month1 <> 3 OR v_act_month2 <> 3 OR v_act_month3 <> 5 OR v_act_month8 <> 0 THEN
        RAISE EXCEPTION 'FAIL [B7-Act]: Unexpected 2026 activity chart counts (months=%, m1=%, m2=%, m3=%, m8=%)',
            v_act_months, v_act_month1, v_act_month2, v_act_month3, v_act_month8;
    END IF;

    -- Verify December 2025 boundary in Asia/Ho_Chi_Minh:
    -- User 1 ('2025-12-31 23:30:00+00' = '2026-01-01 06:30:00+07') must NOT be in Dec 2025.
    SELECT COUNT(*) INTO v_dec2025_reg
    FROM users
    WHERE created_at >= TIMESTAMPTZ '2025-12-01 00:00:00+07'
      AND created_at <  TIMESTAMPTZ '2026-01-01 00:00:00+07';

    SELECT COUNT(DISTINCT user_id) INTO v_dec2025_act
    FROM app_open_events
    WHERE opened_at >= TIMESTAMPTZ '2025-12-01 00:00:00+07'
      AND opened_at <  TIMESTAMPTZ '2026-01-01 00:00:00+07';

    IF v_dec2025_reg <> 1 OR v_dec2025_act <> 1 THEN
        RAISE EXCEPTION 'FAIL [B8]: Expected Dec 2025 Vietnam boundary counts (reg=1, act=1), got (reg=%, act=%)',
            v_dec2025_reg, v_dec2025_act;
    END IF;
    RAISE NOTICE 'PASS [B7 & B8]: 12-month charts (including zero-count Month 8) and Asia/Ho_Chi_Minh year boundaries verified.';

    -- B9. Verify fixture row counts remain untouched
    IF (SELECT COUNT(*) FROM users) <> 26
       OR (SELECT COUNT(*) FROM login_history) <> 13
       OR (SELECT COUNT(*) FROM app_open_events) <> 26
       OR (SELECT COUNT(*) FROM friend_requests) <> 19
       OR (SELECT COUNT(*) FROM friendships) <> 12
       OR (SELECT COUNT(*) FROM user_blocks) <> 3
       OR (SELECT COUNT(*) FROM direct_messages) <> 11
       OR (SELECT COUNT(*) FROM direct_message_hidden) <> 2
       OR (SELECT COUNT(*) FROM chat_groups) <> 4
       OR (SELECT COUNT(*) FROM group_members) <> 17
       OR (SELECT COUNT(*) FROM user_public_keys) <> 3
       OR (SELECT COUNT(*) FROM group_key_versions) <> 2
       OR (SELECT COUNT(*) FROM group_key_envelopes) <> 5
       OR (SELECT COUNT(*) FROM group_messages) <> 8
       OR (SELECT COUNT(*) FROM group_message_hidden) <> 2
       OR (SELECT COUNT(*) FROM spam_reports) <> 5 THEN
        RAISE EXCEPTION 'FAIL [B9]: Seed fixture row counts were modified during verification!';
    END IF;
    RAISE NOTICE 'PASS [B9]: All 16 seed table row counts remain 100%% intact.';
    RAISE NOTICE '=== ALL POSTGRESQL VERIFICATION ASSERTIONS PASSED ===';
END $$;

ROLLBACK;
