# Database Seed Fixtures & Query Test Cases (`docs/database-test-cases.md`)

**Project:** Chat System (Version 1 — Shared PostgreSQL Database Verification)  
**Scripts Verified:** [`script/01_schema.sql`](../script/01_schema.sql), [`script/02_seed.sql`](../script/02_seed.sql), [`script/03_admin_queries.sql`](../script/03_admin_queries.sql)  
**Target Engine:** PostgreSQL 15+ (Verified on PostgreSQL 18.4, time zone `Asia/Ho_Chi_Minh`)

---

## 1. Synthetic Seed Fixtures Overview (`script/02_seed.sql`)

### 1.1. Accounts & Password Hashing (`users`, IDs `1..26`)
- **Total rows in `users`:** `26`
  - **Regular Active Accounts (`role = 'USER'`, `is_locked = FALSE`, `deleted_at IS NULL`):** `21` users (IDs `1..3`, `5..7`, `9..12`, `14..24`).
  - **Locked Accounts (`role = 'USER'`, `is_locked = TRUE`, `deleted_at IS NULL`):** `3` users:
    - `id = 4` (`phamminhchau` — Phạm Minh Châu)
    - `id = 8` (`buithanhha` — Bùi Thanh Hà)
    - `id = 13` (`truongducthang` — Trương Đức Thắng)
  - **Zero-Activity & Zero-Friend Accounts:** `3` users (`id = 21` `caothevinh`, `id = 23` `luonggiahan`, `id = 24` `taquocdat`) have `0` logins, `0` app opens, `0` messages, and `0` friendships.
  - **Administrator Account (`role = 'ADMIN'`):** `1` account (`id = 25`, `username = 'admin'`, registered `2025-12-31 23:00:00+07`).
  - **Soft-Deleted Account (`role = 'USER'`, `deleted_at IS NOT NULL`):** `1` account (`id = 26`, `username = 'deleteduser01'`, registered `2026-02-10 09:00:00+07`, soft-deleted `2026-05-20 10:00:00+07`).
- **Year-Boundary Time Zone Fixture (`Asia/Ho_Chi_Minh`):**
  - User `1` (`nguyenvanan`) has `created_at = TIMESTAMPTZ '2025-12-31 23:30:00+00'`, which converts to **`2026-01-01 06:30:00+07`** in `Asia/Ho_Chi_Minh` (counted in **January 2026**, not December 2025).
- **Synthetic Demo Password Hashes:**
  - Generated using standard `PBKDF2-HMAC-SHA256` (`310,000` iterations, 256-bit derived key, salt `chatsystem-seed-2026`):
    - Regular accounts (`1..24`, `26`): synthetic local test password `UserDemo#2026` -> `{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$cvAxwqWazA1AqdbLFzVnx31KJ+gelwqoWLMNdpt3YYw=}`
    - Admin account (`25`): synthetic local test password `AdminDemo#2026` -> `{pbkdf2@sha256$310000$Y2hhdHN5c3RlbS1zZWVkLTIwMjY=$Jo+AWDZ/vFsq865Ss8+JsNs1IFlijhr1LqvivDVwQFk=}`
  - These are **synthetic local test credentials only**.

---

## 2. Expected Results for Key Administrative Queries (`script/03_admin_queries.sql`)

### Test Case 1 — User Management Directory (`QUERY 1`)
- **Rule Verified:** Excludes soft-deleted user `26` (`deleteduser01`) and filters `role = 'USER'`; retains locked accounts (`4`, `8`, `13`).
- **Expected Output (`status_filter = 'ALL'`):** `24` rows (IDs `1..24`).
  - Filtering `status_filter = 'LOCKED'` returns `3` rows (`4`, `8`, `13`).
  - Filtering `status_filter = 'ACTIVE'` returns `21` rows.

---

### Test Case 2 — Single User's Login History & Accepted Friends (`QUERY 2A` & `QUERY 2B`)
- **Target User:** `nguyenvanan` (`id = 1`).
- **`QUERY 2A` (Single User Login History):**
  - **Expected Output:** `6` rows (`login_id` `13, 10, 7, 6, 5, 2`) ordered chronologically descending.
  - Note: Rows `5, 6, 7` all occurred on `2026-03-05` (`08:00:06+07`, `09:30:00+07`, `11:15:00+07`) within a **single application launch** (`launch_id = 11111111-1111-4111-8111-111111110301`).
- **`QUERY 2B` (Single User Accepted Direct Friends):**
  - Raw `friendships` edges touching user `1`: `(1, 2)`, `(1, 3)`, `(1, 4)`, `(1, 26)`.
  - **Expected Output:** `3` rows:
    1. `lehoangnam` (`id = 3`, active)
    2. `phamminhchau` (`id = 4`, locked — retained per Rule 8)
    3. `tranthibich` (`id = 2`, active)
  - Excluded from output:
    - `deleteduser01` (`id = 26`) because `deleted_at IS NOT NULL`.
    - `ngothuyduong` (`id = 10`) and `duongtuananh` (`id = 11`) because their requests in `friend_requests` are `PENDING`.
    - `dangquanghuy` (`id = 7`) because request `16` is `REJECTED`.

---

### Test Case 3 — System-Wide Chronological Login History (`QUERY 3`)
- **Rule Verified:** Returns all `13` authentication sessions ordered by `login_at DESC`, including `login_id = 4` by soft-deleted user `deleteduser01` (`is_user_soft_deleted = true`) on `2026-02-12 14:00:05+07` and `login_id = 1` by `admin` on `2025-12-31 23:10:02+07`.

---

### Test Case 4 — Chat Groups, Active Members & Active Admins (`QUERY 4A`, `4B`, `4C`)
- **`QUERY 4A` (Chat-Group List):** Returns `4` groups:
  | `group_id` | `group_name` | `is_encrypted` | `active_member_count` | `active_admin_count` |
  |---|---|---|---|---|
  | `1` | `Cộng đồng Java Việt Nam` | `false` | `4` | `2` |
  | `2` | `Nhóm Đồ Án Hệ Thống Chat` | `true` | `3` | `1` |
  | `3` | `CLB Thuật Toán & AI` | `false` | `5` | `2` |
  | `4` | `Nhóm Học Tập KHTN` | `false` | `4` | `1` |
- **`QUERY 4B` (Current Members of Group 1):**
  - Returns `4` active members (`1` `nguyenvanan`, `2` `tranthibich`, `5` `hoangquocviet`, `3` `lehoangnam`).
  - Excludes departed member `6` (`vuthithao`, `left_at = '2026-03-20 18:00:00+07'`).
- **`QUERY 4C` (Current Admins of Group 1):**
  - Returns `2` active admins (`1` `nguyenvanan`, `2` `tranthibich`).

---

### Test Case 5 — Spam Reports with Reporter & Reported Usernames (`QUERY 5`)
- **Expected Output (`2026-01-01` to `2027-01-01`):** `5` rows (`report_id` `5, 4, 3, 2, 1`):
  - `2` `PENDING` reports (`report_id = 5`: `lehoangnam` -> `trinhhoanglong`; `report_id = 4`: `ngothuyduong` -> `doanduckien`) with `resolved_by_username IS NULL`.
  - `3` `RESOLVED` reports (`report_id = 3`: `vuthithao` -> `truongducthang`; `report_id = 2`: `tranthibich` -> `buithanhha`; `report_id = 1`: `nguyenvanan` -> `phamminhchau`) resolved by `admin`, where all 3 reported accounts have `reported_user_is_locked = true`.

---

### Test Case 6 — Newly Registered Users in February 2026 (`QUERY 6`)
- **Range:** `[2026-02-01 00:00:00+07, 2026-03-01 00:00:00+07)`.
- **Expected Output:** `5` rows (`id = 5` `hoangquocviet`, `id = 6` `vuthithao`, `id = 26` `deleteduser01` with `is_soft_deleted = true`, `id = 7` `dangquanghuy`, `id = 8` `buithanhha`).
- **Rule Verified:** Soft-deleted user `26` is retained in historical February 2026 registration results.

---

### Test Case 7 — Annual Registration Chart for 2026 (`QUERY 7`)
- **Expected Output (All 12 Months `1..12` in `Asia/Ho_Chi_Minh`):**
  | Month | `new_registrations_count` | Notes |
  |---|---|---|
  | `1` | `4` | Users `1, 2, 3, 4` (User `1` registered at `2025-12-31 23:30:00Z` = `2026-01-01 06:30:00+07`) |
  | `2` | `5` | Users `5, 6, 7, 8, 26` (includes soft-deleted User `26`) |
  | `3` | `5` | Users `9, 10, 11, 12, 13` |
  | `4` | `4` | Users `14, 15, 16, 17` |
  | `5` | `4` | Users `18, 19, 20, 21` |
  | `6` | `3` | Users `22, 23, 24` |
  | `7..12` | `0` | Zero-registration months preserved via `generate_series(1, 12)` |

---

### Test Case 8 — Friend Statistics: Direct Friends & 1-or-2-Hop Friends (`QUERY 8`)
- **Rules Verified:**
  1. Overlapping 1-hop and 2-hop paths are deduplicated via `DISTINCT`.
  2. Self (`u_id = v_id`) is never counted.
  3. Soft-deleted user `26` and the 2-hop bridge `1 -> 26 -> 9` through user `26` are excluded.
  4. Pending/rejected/cancelled requests in `friend_requests` are excluded.
- **Expected Counts for Key Users:**
  | `user_id` | `username` | `direct_friends_count` | `friends_with_fof_count` | Reachable Active Set (1 or 2 edges, excluding self) |
  |---|---|---|---|---|
  | `1` | `nguyenvanan` | `3` (`{2, 3, 4}`) | `5` | `{2, 3, 4, 5, 6}` (User `9` via deleted `26` is excluded; User `7` at 3 hops is excluded) |
  | `2` | `tranthibich` | `3` (`{1, 3, 5}`) | `6` | `{1, 3, 4, 5, 6, 7}` |
  | `3` | `lehoangnam` | `4` (`{1, 2, 5, 6}`) | `6` | `{1, 2, 4, 5, 6, 7}` |
  | `4` | `phamminhchau` | `1` (`{1}`) | `3` | `{1, 2, 3}` |
  | `5` | `hoangquocviet` | `3` (`{2, 3, 7}`) | `5` | `{1, 2, 3, 6, 7}` |
  | `6` | `vuthithao` | `1` (`{3}`) | `4` | `{1, 2, 3, 5}` |
  | `7` | `dangquanghuy` | `1` (`{5}`) | `3` | `{2, 3, 5}` |
  | `9` | `dinhgiabao` | `0` | `0` | Only had edge `(9, 26)` to soft-deleted user `26` |
  | `10, 12` | `ngothuyduong`, `lykhanhlinh` | `1` | `1` | Connected pair `(10, 12)` |
  | `14, 15` | `haquynhnhu`, `phanminhkhoi` | `1` | `1` | Connected pair `(14, 15)` |
  | `8, 11, 13, 16..24` | *(11 users)* | `0` | `0` | Zero accepted active friends |

---

### Test Case 9 — User Activity Statistics in March 2026 (`QUERY 9A` & `QUERY 9B`)
- **Range:** `[2026-03-01 00:00:00+07, 2026-04-01 00:00:00+07)`.
- **Rules Verified:**
  - Each activity table (`app_open_events`, `direct_messages`, `group_messages`) is aggregated in its own CTE before joining to `users`, preventing join multiplication.
  - User `1` (`nguyenvanan`) had `3` logins inside launch `11111111-1111-4111-8111-111111110301` on `2026-03-05`, plus launches on `2026-03-12` and `2026-03-25` -> `app_opens = 3` (not `5`).
  - User `1` sent `4` direct messages in March 2026 (3 to user `2`, 1 to user `3`) and hid 1 message -> `distinct_direct_recipients = 2` (hidden message does not reduce count).
  - User `1` sent `3` plaintext messages to Group `1` and `2` encrypted messages to Group `2`, and hid 1 group message -> `distinct_groups_messaged = 2`.
- **Expected Output of `QUERY 9A` (`7` active users in March 2026):**
  | `user_id` | `username` | `app_opens` | `distinct_direct_recipients` | `distinct_groups_messaged` |
  |---|---|---|---|---|
  | `1` | `nguyenvanan` | `3` | `2` | `2` |
  | `2` | `tranthibich` | `2` | `1` | `1` |
  | `3` | `lehoangnam` | `1` | `0` | `1` |
  | `4` | `phamminhchau` | `0` | `1` | `0` |
  | `5` | `hoangquocviet` | `1` | `0` | `0` |
  | `10` | `ngothuyduong` | `1` | `0` | `1` |
  | `13` | `truongducthang` | `0` | `1` | `0` |

---

### Test Case 10 — Annual Activity Chart for 2026 (`QUERY 10`)
- **Rules Verified:**
  - Counts `COUNT(DISTINCT aoe.user_id)` per calendar month in `Asia/Ho_Chi_Minh`.
  - In March 2026, `8` app-open events occurred across `5` distinct users (`1, 2, 3, 5, 10`) -> Month 3 returns **`5`** (not `8`).
  - In February 2026, soft-deleted user `26` is retained in historical activity -> Month 2 returns **`3`** (`1, 5, 26`).
  - In August 2026 (Month 8), zero events occurred -> Month 8 returns **`0`**.
- **Expected Output (All 12 Months `1..12`):**
  | Month | `active_app_opening_users_count` | Distinct User IDs |
  |---|---|---|
  | `1` | `3` | `1, 2, 3` (User `1` opened at `2025-12-31 23:45:00Z` = `2026-01-01 06:45:00+07`) |
  | `2` | `3` | `1, 5, 26` |
  | `3` | `5` | `1, 2, 3, 5, 10` |
  | `4` | `2` | `14, 15` |
  | `5` | `2` | `18, 20` |
  | `6` | `1` | `22` |
  | `7` | `1` | `1` |
  | `8` | `0` | *(Zero-activity month)* |
  | `9` | `1` | `2` |
  | `10` | `1` | `3` |
  | `11` | `2` | `1, 2` |
  | `12` | `1` | `1` |
