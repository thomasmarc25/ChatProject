-- =============================================================================
-- File: script/03_admin_queries.sql
-- Project: Chat System (Version 1 — Runnable Administrative SQL Queries)
-- Target Engine: PostgreSQL 15+ (UTF-8)
-- Prerequisite: Execute script/01_schema.sql and script/02_seed.sql first.
--
-- DESIGN & QUERY CONVENTIONS:
-- 1. Every query is 100% self-contained and runnable in psql or any SQL client
--    without JDBC '?' or ':named' placeholders. Editable query inputs are
--    isolated in a top `params` CTE in each query.
-- 2. All calendar dates, months, and years are evaluated in time zone
--    'Asia/Ho_Chi_Minh' using half-open intervals [start_ts, end_ts).
-- 3. Online/offline status is NOT derived from login_history (real-time
--    presence belongs to Version 2 live TCP socket sessions).
-- 4. Per-user hidden message rows (direct_message_hidden, group_message_hidden)
--    are intentionally NOT subtracted from administrative activity counts.
-- 5. Historical queries (login history, new registrations, registration chart,
--    activity list, activity chart) retain soft-deleted accounts, whereas
--    current directory and friendship queries exclude soft-deleted accounts
--    (deleted_at IS NULL).
-- =============================================================================

SET client_encoding = 'UTF8';
SET TIME ZONE 'Asia/Ho_Chi_Minh';


-- =============================================================================
-- QUERY 1: User Management List — Filter by Name/Username/Status & Sort
-- (Assignment Item 1a)
-- Excludes soft-deleted accounts (deleted_at IS NULL).
-- Status filter options: 'ALL', 'ACTIVE' (is_locked = FALSE), 'LOCKED' (is_locked = TRUE).
-- Sort options: 'NAME_ASC', 'NAME_DESC', 'CREATED_DESC', 'CREATED_ASC'.
-- =============================================================================
WITH params AS (
    SELECT
        ''::TEXT        AS search_keyword,  -- e.g. 'nguyễn' or 'nguyen'; '' for all
        'ALL'::TEXT     AS status_filter,   -- 'ALL' | 'ACTIVE' | 'LOCKED'
        'CREATED_ASC'::TEXT AS sort_by      -- 'NAME_ASC' | 'NAME_DESC' | 'CREATED_DESC' | 'CREATED_ASC'
)
SELECT
    u.id,
    u.username,
    u.full_name,
    u.address,
    u.date_of_birth,
    u.gender,
    u.email,
    u.role,
    u.is_locked,
    CASE
        WHEN u.is_locked THEN 'LOCKED'
        ELSE 'ACTIVE'
    END AS account_status,
    u.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS created_at_vn
FROM users u
CROSS JOIN params p
WHERE u.deleted_at IS NULL
  AND u.role = 'USER'
  AND (
      p.search_keyword = ''
      OR u.username ILIKE '%' || p.search_keyword || '%'
      OR u.full_name ILIKE '%' || p.search_keyword || '%'
  )
  AND (
      p.status_filter = 'ALL'
      OR (p.status_filter = 'ACTIVE' AND u.is_locked = FALSE)
      OR (p.status_filter = 'LOCKED' AND u.is_locked = TRUE)
  )
ORDER BY
    CASE WHEN p.sort_by = 'NAME_ASC'     THEN u.full_name END ASC,
    CASE WHEN p.sort_by = 'NAME_DESC'    THEN u.full_name END DESC,
    CASE WHEN p.sort_by = 'CREATED_ASC'  THEN u.created_at END ASC,
    CASE WHEN p.sort_by = 'CREATED_DESC' THEN u.created_at END DESC,
    u.id ASC;


-- =============================================================================
-- QUERY 2A: Single User's Login History
-- (Assignment Item 1e — e.g. for 'nguyenvanan', user_id = 1)
-- =============================================================================
WITH params AS (
    SELECT 'nguyenvanan'::TEXT AS target_username
)
SELECT
    lh.id AS login_id,
    u.id AS user_id,
    u.username,
    u.full_name,
    lh.login_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS login_at_vn,
    lh.logout_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS logout_at_vn,
    lh.ip_address
FROM login_history lh
JOIN users u ON u.id = lh.user_id
CROSS JOIN params p
WHERE LOWER(u.username) = LOWER(p.target_username)
ORDER BY lh.login_at DESC, lh.id DESC;


-- =============================================================================
-- QUERY 2B: Single User's Accepted Direct Friends List
-- (Assignment Item 1f — e.g. for 'nguyenvanan', user_id = 1)
-- Excludes soft-deleted friends (friend.deleted_at IS NULL); retains locked
-- friends (such as user 4 'phamminhchau'). Pending requests are excluded.
-- =============================================================================
WITH params AS (
    SELECT 'nguyenvanan'::TEXT AS target_username
),
target_user AS (
    SELECT u.id
    FROM users u
    CROSS JOIN params p
    WHERE LOWER(u.username) = LOWER(p.target_username)
      AND u.deleted_at IS NULL
)
SELECT
    friend.id AS friend_user_id,
    friend.username AS friend_username,
    friend.full_name AS friend_full_name,
    friend.email AS friend_email,
    friend.is_locked AS friend_is_locked,
    f.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS friendship_created_at_vn
FROM target_user tu
JOIN friendships f
    ON f.user_low_id = tu.id OR f.user_high_id = tu.id
JOIN users friend
    ON friend.id = CASE
        WHEN f.user_low_id = tu.id THEN f.user_high_id
        ELSE f.user_low_id
    END
WHERE friend.deleted_at IS NULL
ORDER BY friend.full_name ASC, friend.id ASC;


-- =============================================================================
-- QUERY 3: System-Wide Login History in Chronological Order
-- (Assignment Item 2)
-- Displays login timestamp, username, and full name across all users,
-- including historical logins of accounts later soft-deleted.
-- =============================================================================
SELECT
    lh.id AS login_id,
    lh.login_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS login_at_vn,
    lh.logout_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS logout_at_vn,
    u.username,
    u.full_name,
    lh.ip_address,
    (u.deleted_at IS NOT NULL) AS is_user_soft_deleted
FROM login_history lh
JOIN users u ON u.id = lh.user_id
ORDER BY lh.login_at DESC, lh.id DESC;


-- =============================================================================
-- QUERY 4A: Chat-Group List — Filter by Name & Sort by Name or Creation Time
-- (Assignment Item 3a, 3b)
-- Counts active members and active admins (left_at IS NULL and user not deleted).
-- =============================================================================
WITH params AS (
    SELECT
        ''::TEXT            AS group_name_filter, -- e.g. 'Java'; '' for all
        'CREATED_ASC'::TEXT AS sort_by            -- 'NAME_ASC' | 'NAME_DESC' | 'CREATED_DESC' | 'CREATED_ASC'
),
active_membership_counts AS (
    SELECT
        gm.group_id,
        COUNT(*) AS active_member_count,
        COUNT(*) FILTER (WHERE gm.role = 'ADMIN') AS active_admin_count
    FROM group_members gm
    JOIN users u ON u.id = gm.user_id
    WHERE gm.left_at IS NULL
      AND u.deleted_at IS NULL
    GROUP BY gm.group_id
)
SELECT
    cg.id AS group_id,
    cg.name AS group_name,
    creator.username AS created_by_username,
    creator.full_name AS created_by_full_name,
    cg.is_encrypted,
    COALESCE(amc.active_member_count, 0) AS active_member_count,
    COALESCE(amc.active_admin_count, 0) AS active_admin_count,
    cg.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS created_at_vn
FROM chat_groups cg
JOIN users creator ON creator.id = cg.created_by
LEFT JOIN active_membership_counts amc ON amc.group_id = cg.id
CROSS JOIN params p
WHERE p.group_name_filter = ''
   OR cg.name ILIKE '%' || p.group_name_filter || '%'
ORDER BY
    CASE WHEN p.sort_by = 'NAME_ASC'     THEN cg.name END ASC,
    CASE WHEN p.sort_by = 'NAME_DESC'    THEN cg.name END DESC,
    CASE WHEN p.sort_by = 'CREATED_ASC'  THEN cg.created_at END ASC,
    CASE WHEN p.sort_by = 'CREATED_DESC' THEN cg.created_at END DESC,
    cg.id ASC;


-- =============================================================================
-- QUERY 4B: View Current Members of a Selected Chat Group
-- (Assignment Item 3c — e.g. Group 1 'Cộng đồng Java Việt Nam')
-- Excludes departed members (gm.left_at IS NOT NULL, such as user 6).
-- =============================================================================
WITH params AS (
    SELECT 1::BIGINT AS target_group_id
)
SELECT
    cg.id AS group_id,
    cg.name AS group_name,
    u.id AS user_id,
    u.username,
    u.full_name,
    u.email,
    gm.role AS group_role,
    gm.joined_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS joined_at_vn
FROM group_members gm
JOIN chat_groups cg ON cg.id = gm.group_id
JOIN users u ON u.id = gm.user_id
CROSS JOIN params p
WHERE gm.group_id = p.target_group_id
  AND gm.left_at IS NULL
  AND u.deleted_at IS NULL
ORDER BY
    CASE WHEN gm.role = 'ADMIN' THEN 0 ELSE 1 END ASC,
    u.full_name ASC,
    u.id ASC;


-- =============================================================================
-- QUERY 4C: View Current Administrators of a Selected Chat Group
-- (Assignment Item 3d — e.g. Group 1 'Cộng đồng Java Việt Nam')
-- =============================================================================
WITH params AS (
    SELECT 1::BIGINT AS target_group_id
)
SELECT
    cg.id AS group_id,
    cg.name AS group_name,
    u.id AS admin_user_id,
    u.username AS admin_username,
    u.full_name AS admin_full_name,
    u.email AS admin_email,
    gm.joined_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS joined_at_vn
FROM group_members gm
JOIN chat_groups cg ON cg.id = gm.group_id
JOIN users u ON u.id = gm.user_id
CROSS JOIN params p
WHERE gm.group_id = p.target_group_id
  AND gm.role = 'ADMIN'
  AND gm.left_at IS NULL
  AND u.deleted_at IS NULL
ORDER BY u.full_name ASC, u.id ASC;


-- =============================================================================
-- QUERY 5: Spam-Report List — Filter & Sort by Time Range and Reported Username
-- (Assignment Item 4a, 4b, 4c)
-- Includes both reporter_username and reported_username.
-- =============================================================================
WITH params AS (
    SELECT
        TIMESTAMPTZ '2026-01-01 00:00:00+07' AS start_ts,
        TIMESTAMPTZ '2027-01-01 00:00:00+07' AS end_ts,
        ''::TEXT                             AS reported_username_filter,
        'TIME_DESC'::TEXT                    AS sort_by -- 'TIME_DESC' | 'TIME_ASC' | 'USERNAME_ASC' | 'USERNAME_DESC'
)
SELECT
    sr.id AS report_id,
    sr.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS reported_at_vn,
    reporter.id AS reporter_user_id,
    reporter.username AS reporter_username,
    reporter.full_name AS reporter_full_name,
    reported.id AS reported_user_id,
    reported.username AS reported_username,
    reported.full_name AS reported_full_name,
    reported.is_locked AS reported_user_is_locked,
    sr.direct_message_id,
    sr.reason,
    sr.message_snapshot,
    sr.status,
    resolver.username AS resolved_by_username,
    sr.resolved_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS resolved_at_vn
FROM spam_reports sr
JOIN users reporter ON reporter.id = sr.reporter_id
JOIN users reported ON reported.id = sr.reported_user_id
LEFT JOIN users resolver ON resolver.id = sr.resolved_by
CROSS JOIN params p
WHERE sr.created_at >= p.start_ts
  AND sr.created_at < p.end_ts
  AND (
      p.reported_username_filter = ''
      OR reported.username ILIKE '%' || p.reported_username_filter || '%'
  )
ORDER BY
    CASE WHEN p.sort_by = 'TIME_DESC'     THEN sr.created_at END DESC,
    CASE WHEN p.sort_by = 'TIME_ASC'      THEN sr.created_at END ASC,
    CASE WHEN p.sort_by = 'USERNAME_ASC'  THEN reported.username END ASC,
    CASE WHEN p.sort_by = 'USERNAME_DESC' THEN reported.username END DESC,
    sr.id DESC;


-- =============================================================================
-- QUERY 6: Newly Registered Users in a Date/Time Range [start_ts, end_ts)
-- (Assignment Item 5a, 5b)
-- Retains soft-deleted accounts (such as user 26 in Feb 2026) for accurate
-- historical registration reporting.
-- =============================================================================
WITH params AS (
    SELECT
        TIMESTAMPTZ '2026-02-01 00:00:00+07' AS start_ts,
        TIMESTAMPTZ '2026-03-01 00:00:00+07' AS end_ts,
        ''::TEXT                             AS name_filter,
        'CREATED_ASC'::TEXT                  AS sort_by -- 'NAME_ASC' | 'NAME_DESC' | 'CREATED_ASC' | 'CREATED_DESC'
)
SELECT
    u.id,
    u.username,
    u.full_name,
    u.email,
    u.gender,
    u.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS created_at_vn,
    u.is_locked,
    (u.deleted_at IS NOT NULL) AS is_soft_deleted
FROM users u
CROSS JOIN params p
WHERE u.role = 'USER'
  AND u.created_at >= p.start_ts
  AND u.created_at < p.end_ts
  AND (
      p.name_filter = ''
      OR u.full_name ILIKE '%' || p.name_filter || '%'
      OR u.username ILIKE '%' || p.name_filter || '%'
  )
ORDER BY
    CASE WHEN p.sort_by = 'NAME_ASC'     THEN u.full_name END ASC,
    CASE WHEN p.sort_by = 'NAME_DESC'    THEN u.full_name END DESC,
    CASE WHEN p.sort_by = 'CREATED_ASC'  THEN u.created_at END ASC,
    CASE WHEN p.sort_by = 'CREATED_DESC' THEN u.created_at END DESC,
    u.id ASC;


-- =============================================================================
-- QUERY 7: Annual Registration Chart — Monthly Registration Counts (Months 1..12)
-- (Assignment Item 6)
-- Uses explicit Asia/Ho_Chi_Minh month boundaries [month_start, month_end) and
-- generate_series(1, 12) so zero-count months (Months 7..12) return 0.
-- Includes user 1 (2025-12-31 23:30:00Z = 2026-01-01 06:30:00+07 -> Month 1)
-- and soft-deleted user 26 (2026-02-10+07 -> Month 2).
-- =============================================================================
WITH params AS (
    SELECT 2026::INT AS target_year
),
months AS (
    SELECT
        m.month_num,
        make_timestamptz(p.target_year, m.month_num, 1, 0, 0, 0, 'Asia/Ho_Chi_Minh') AS month_start,
        make_timestamptz(p.target_year, m.month_num, 1, 0, 0, 0, 'Asia/Ho_Chi_Minh') + INTERVAL '1 month' AS month_end
    FROM params p
    CROSS JOIN generate_series(1, 12) AS m(month_num)
)
SELECT
    m.month_num AS month,
    COUNT(u.id) AS new_registrations_count
FROM months m
LEFT JOIN users u
    ON u.role = 'USER'
   AND u.created_at >= m.month_start
   AND u.created_at < m.month_end
GROUP BY m.month_num
ORDER BY m.month_num ASC;


-- =============================================================================
-- QUERY 8: User Friend Statistics — Direct Friends & 1-or-2-Hop Friends
-- (Assignment Item 7a, 7b, 7c)
-- Rules enforced:
--   - Only active (non-deleted) users are listed and traversed (deleted_at IS NULL).
--   - Direct friends = accepted friendships in `friendships` with active users.
--   - Friends including friends-of-friends = DISTINCT active users reachable
--     in 1 or 2 accepted friendship edges, excluding the user themselves.
--   - Supports filtering on direct_friends_count with 'ALL', 'EQ' (=),
--     'LT' (<), or 'GT' (>).
-- =============================================================================
WITH params AS (
    SELECT
        ''::TEXT            AS name_filter,
        'ALL'::TEXT         AS comparison_op, -- 'ALL' | 'EQ' | 'LT' | 'GT'
        3::BIGINT           AS comparison_val,
        'CREATED_ASC'::TEXT AS sort_by        -- 'NAME_ASC' | 'NAME_DESC' | 'CREATED_ASC' | 'CREATED_DESC'
),
active_users AS (
    SELECT id, username, full_name, created_at
    FROM users
    WHERE deleted_at IS NULL
      AND role = 'USER'
),
-- Bidirectional edges between active (non-deleted) users only
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
direct_counts AS (
    SELECT
        de.u_id AS user_id,
        COUNT(DISTINCT de.v_id) AS direct_friends_count
    FROM directed_edges de
    GROUP BY de.u_id
),
one_or_two_hop_edges AS (
    -- 1-hop reachable active users
    SELECT de.u_id AS user_id, de.v_id AS reachable_user_id
    FROM directed_edges de
    UNION
    -- 2-hop reachable active users through active intermediate friends, excluding self
    SELECT e1.u_id AS user_id, e2.v_id AS reachable_user_id
    FROM directed_edges e1
    JOIN directed_edges e2 ON e2.u_id = e1.v_id
    WHERE e2.v_id <> e1.u_id
),
fof_counts AS (
    SELECT
        h.user_id,
        COUNT(DISTINCT h.reachable_user_id) AS friends_with_fof_count
    FROM one_or_two_hop_edges h
    GROUP BY h.user_id
),
user_friend_stats AS (
    SELECT
        au.id,
        au.username,
        au.full_name,
        au.created_at,
        COALESCE(dc.direct_friends_count, 0) AS direct_friends_count,
        COALESCE(fc.friends_with_fof_count, 0) AS friends_with_fof_count
    FROM active_users au
    LEFT JOIN direct_counts dc ON dc.user_id = au.id
    LEFT JOIN fof_counts fc ON fc.user_id = au.id
)
SELECT
    s.id AS user_id,
    s.username,
    s.full_name,
    s.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS created_at_vn,
    s.direct_friends_count,
    s.friends_with_fof_count
FROM user_friend_stats s
CROSS JOIN params p
WHERE (
      p.name_filter = ''
      OR s.full_name ILIKE '%' || p.name_filter || '%'
      OR s.username ILIKE '%' || p.name_filter || '%'
  )
  AND (
      p.comparison_op = 'ALL'
      OR (p.comparison_op = 'EQ' AND s.direct_friends_count = p.comparison_val)
      OR (p.comparison_op = 'LT' AND s.direct_friends_count < p.comparison_val)
      OR (p.comparison_op = 'GT' AND s.direct_friends_count > p.comparison_val)
  )
ORDER BY
    CASE WHEN p.sort_by = 'NAME_ASC'     THEN s.full_name END ASC,
    CASE WHEN p.sort_by = 'NAME_DESC'    THEN s.full_name END DESC,
    CASE WHEN p.sort_by = 'CREATED_ASC'  THEN s.created_at END ASC,
    CASE WHEN p.sort_by = 'CREATED_DESC' THEN s.created_at END DESC,
    s.id ASC;


-- =============================================================================
-- QUERY 9A: Active Users & Activity Metrics in [start_ts, end_ts)
-- (Assignment Item 8a, 8b, 8c — General Configurable Query)
-- Rules enforced:
--   - Aggregates app_open_events, direct_messages, and group_messages in
--     separate CTEs BEFORE joining to users so joins never multiply counts.
--   - Hidden messages do NOT reduce activity counts.
--   - Metric filter selects one specific metric ('APP_OPENS',
--     'DIRECT_RECIPIENTS', or 'GROUPS_MESSAGED') and applies 'ALL', 'EQ' (=),
--     'LT' (<), or 'GT' (>).
-- =============================================================================
WITH params AS (
    SELECT
        TIMESTAMPTZ '2026-03-01 00:00:00+07' AS start_ts,
        TIMESTAMPTZ '2026-04-01 00:00:00+07' AS end_ts,
        ''::TEXT                             AS name_filter,
        'APP_OPENS'::TEXT                    AS filter_metric, -- 'APP_OPENS' | 'DIRECT_RECIPIENTS' | 'GROUPS_MESSAGED'
        'ALL'::TEXT                          AS comparison_op, -- 'ALL' | 'EQ' | 'LT' | 'GT'
        1::BIGINT                            AS comparison_val,
        'CREATED_ASC'::TEXT                  AS sort_by        -- 'NAME_ASC' | 'NAME_DESC' | 'CREATED_ASC' | 'CREATED_DESC'
),
open_counts AS (
    SELECT
        aoe.user_id,
        COUNT(*) AS app_opens
    FROM app_open_events aoe
    CROSS JOIN params p
    WHERE aoe.opened_at >= p.start_ts
      AND aoe.opened_at < p.end_ts
    GROUP BY aoe.user_id
),
dm_counts AS (
    SELECT
        dm.sender_id AS user_id,
        COUNT(DISTINCT dm.receiver_id) AS distinct_direct_recipients
    FROM direct_messages dm
    CROSS JOIN params p
    WHERE dm.sent_at >= p.start_ts
      AND dm.sent_at < p.end_ts
    GROUP BY dm.sender_id
),
gm_counts AS (
    SELECT
        gm.sender_id AS user_id,
        COUNT(DISTINCT gm.group_id) AS distinct_groups_messaged
    FROM group_messages gm
    CROSS JOIN params p
    WHERE gm.sent_at >= p.start_ts
      AND gm.sent_at < p.end_ts
    GROUP BY gm.sender_id
),
active_user_metrics AS (
    SELECT
        u.id AS user_id,
        u.username,
        u.full_name,
        u.created_at,
        COALESCE(oc.app_opens, 0) AS app_opens,
        COALESCE(dc.distinct_direct_recipients, 0) AS distinct_direct_recipients,
        COALESCE(gc.distinct_groups_messaged, 0) AS distinct_groups_messaged
    FROM users u
    LEFT JOIN open_counts oc ON oc.user_id = u.id
    LEFT JOIN dm_counts dc ON dc.user_id = u.id
    LEFT JOIN gm_counts gc ON gc.user_id = u.id
    WHERE u.role = 'USER'
      AND (
          COALESCE(oc.app_opens, 0) > 0
          OR COALESCE(dc.distinct_direct_recipients, 0) > 0
          OR COALESCE(gc.distinct_groups_messaged, 0) > 0
      )
),
filtered_metrics AS (
    SELECT
        m.*,
        CASE p.filter_metric
            WHEN 'APP_OPENS'         THEN m.app_opens
            WHEN 'DIRECT_RECIPIENTS' THEN m.distinct_direct_recipients
            WHEN 'GROUPS_MESSAGED'   THEN m.distinct_groups_messaged
        END AS selected_metric_value
    FROM active_user_metrics m
    CROSS JOIN params p
)
SELECT
    fm.user_id,
    fm.username,
    fm.full_name,
    fm.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh' AS created_at_vn,
    fm.app_opens,
    fm.distinct_direct_recipients,
    fm.distinct_groups_messaged
FROM filtered_metrics fm
CROSS JOIN params p
WHERE (
      p.name_filter = ''
      OR fm.full_name ILIKE '%' || p.name_filter || '%'
      OR fm.username ILIKE '%' || p.name_filter || '%'
  )
  AND (
      p.comparison_op = 'ALL'
      OR (p.comparison_op = 'EQ' AND fm.selected_metric_value = p.comparison_val)
      OR (p.comparison_op = 'LT' AND fm.selected_metric_value < p.comparison_val)
      OR (p.comparison_op = 'GT' AND fm.selected_metric_value > p.comparison_val)
  )
ORDER BY
    CASE WHEN p.sort_by = 'NAME_ASC'     THEN fm.full_name END ASC,
    CASE WHEN p.sort_by = 'NAME_DESC'    THEN fm.full_name END DESC,
    CASE WHEN p.sort_by = 'CREATED_ASC'  THEN fm.created_at END ASC,
    CASE WHEN p.sort_by = 'CREATED_DESC' THEN fm.created_at END DESC,
    fm.user_id ASC;


-- =============================================================================
-- QUERY 9B: Concrete Demonstration of =, <, > Filters for EACH Activity Metric
-- (March 2026: [2026-03-01 00:00:00+07, 2026-04-01 00:00:00+07))
-- Demonstrates all 9 (metric x operator) combinations over active March 2026 users:
--   - APP_OPENS:          = 1, < 2, > 1
--   - DIRECT_RECIPIENTS:  = 1, < 1, > 1
--   - GROUPS_MESSAGED:    = 1, < 1, > 1
-- =============================================================================
WITH bounds AS (
    SELECT
        TIMESTAMPTZ '2026-03-01 00:00:00+07' AS start_ts,
        TIMESTAMPTZ '2026-04-01 00:00:00+07' AS end_ts
),
open_counts AS (
    SELECT aoe.user_id, COUNT(*) AS app_opens
    FROM app_open_events aoe CROSS JOIN bounds b
    WHERE aoe.opened_at >= b.start_ts AND aoe.opened_at < b.end_ts
    GROUP BY aoe.user_id
),
dm_counts AS (
    SELECT dm.sender_id AS user_id, COUNT(DISTINCT dm.receiver_id) AS distinct_direct_recipients
    FROM direct_messages dm CROSS JOIN bounds b
    WHERE dm.sent_at >= b.start_ts AND dm.sent_at < b.end_ts
    GROUP BY dm.sender_id
),
gm_counts AS (
    SELECT gm.sender_id AS user_id, COUNT(DISTINCT gm.group_id) AS distinct_groups_messaged
    FROM group_messages gm CROSS JOIN bounds b
    WHERE gm.sent_at >= b.start_ts AND gm.sent_at < b.end_ts
    GROUP BY gm.sender_id
),
march_active AS (
    SELECT
        u.id AS user_id,
        u.username,
        COALESCE(oc.app_opens, 0) AS app_opens,
        COALESCE(dc.distinct_direct_recipients, 0) AS distinct_direct_recipients,
        COALESCE(gc.distinct_groups_messaged, 0) AS distinct_groups_messaged
    FROM users u
    LEFT JOIN open_counts oc ON oc.user_id = u.id
    LEFT JOIN dm_counts dc ON dc.user_id = u.id
    LEFT JOIN gm_counts gc ON gc.user_id = u.id
    WHERE u.role = 'USER'
      AND (
          COALESCE(oc.app_opens, 0) > 0
          OR COALESCE(dc.distinct_direct_recipients, 0) > 0
          OR COALESCE(gc.distinct_groups_messaged, 0) > 0
      )
)
SELECT '1a. APP_OPENS = 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE app_opens = 1
UNION ALL
SELECT '1b. APP_OPENS < 2' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE app_opens < 2
UNION ALL
SELECT '1c. APP_OPENS > 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE app_opens > 1
UNION ALL
SELECT '2a. DIRECT_RECIPIENTS = 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE distinct_direct_recipients = 1
UNION ALL
SELECT '2b. DIRECT_RECIPIENTS < 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE distinct_direct_recipients < 1
UNION ALL
SELECT '2c. DIRECT_RECIPIENTS > 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE distinct_direct_recipients > 1
UNION ALL
SELECT '3a. GROUPS_MESSAGED = 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE distinct_groups_messaged = 1
UNION ALL
SELECT '3b. GROUPS_MESSAGED < 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE distinct_groups_messaged < 1
UNION ALL
SELECT '3c. GROUPS_MESSAGED > 1' AS filter_case, user_id, username, app_opens, distinct_direct_recipients, distinct_groups_messaged
FROM march_active WHERE distinct_groups_messaged > 1
ORDER BY filter_case ASC, user_id ASC;


-- =============================================================================
-- QUERY 10: Annual Activity Chart — Monthly Distinct Users Opening App (1..12)
-- (Assignment Item 9)
-- Counts COUNT(DISTINCT aoe.user_id) directly per month in Asia/Ho_Chi_Minh
-- (never summing daily distinct counts), including zero-count months (Month 8).
-- =============================================================================
WITH params AS (
    SELECT 2026::INT AS target_year
),
months AS (
    SELECT
        m.month_num,
        make_timestamptz(p.target_year, m.month_num, 1, 0, 0, 0, 'Asia/Ho_Chi_Minh') AS month_start,
        make_timestamptz(p.target_year, m.month_num, 1, 0, 0, 0, 'Asia/Ho_Chi_Minh') + INTERVAL '1 month' AS month_end
    FROM params p
    CROSS JOIN generate_series(1, 12) AS m(month_num)
)
SELECT
    m.month_num AS month,
    COUNT(DISTINCT aoe.user_id) AS active_app_opening_users_count
FROM months m
LEFT JOIN app_open_events aoe
    ON aoe.opened_at >= m.month_start
   AND aoe.opened_at < m.month_end
GROUP BY m.month_num
ORDER BY m.month_num ASC;
