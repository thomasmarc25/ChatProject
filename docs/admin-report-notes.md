# Administrator Subsystem & Database Report Notes (`docs/admin-report-notes.md`)

**Project:** Chat System — Course Final Project (Version 1: UI & Database Design)  
**Author / Owner:** Member 2 (Administrator Desktop Application & Shared PostgreSQL Database Design)  
**Purpose:** Factual technical notes and screen descriptions for inclusion in **Section 4 (Database Design)** and **Section 5 (Designed Screens with Screenshots)** of the team's Version 1 Report.

> [!NOTE]
> **Factual Scope Disclosure for the Version 1 Report:**
> In accordance with the Version 1 milestone specification (`Final_Project.pdf`, Page 4: *"chỉ cần giao diện, chưa cần lập trình sự kiện"*), the Administrator desktop application (`chat-admin/`) is implemented in Java 21 Swing + FlatLaf 3.7.2 Light using deterministic in-memory mock data (`MockUserRepository.java`). Filter, sort, dialog, chart, and account-lock preview interactions operate strictly in memory to demonstrate all required UI states (`populated`, `empty-result`, `no-selection`, and `empty-year`) without connecting to a live Spring Boot backend, TCP socket server, or PostgreSQL JDBC datasource.

---

## 1. Summary of Shared PostgreSQL Database Work (`script/`)

The shared PostgreSQL 15+ database (verified on PostgreSQL 18.4, `UTF-8`, time zone `Asia/Ho_Chi_Minh`) consists of **16 tables** (13 core operational tables + 3 end-to-end group encryption storage preparation tables) defined across 4 scripts in `script/`:

1. **`script/01_schema.sql` — Transactional Schema Definition:**
   - Creates all 16 tables inside a single `BEGIN; ... COMMIT;` transaction on an empty database (`ON DELETE RESTRICT` across historical tables to prevent accidental erasure of audit history).
   - **Identity & Accounts (`users`):** Stores `username`, `password_hash`, `full_name`, `address`, `date_of_birth`, `gender`, `email`, `role` (`USER` / `ADMIN`), `is_locked`, `created_at`, `updated_at`, and `deleted_at` (soft deletion). Case-insensitive uniqueness is enforced via expression unique indexes on `LOWER(username)` and `LOWER(email)`.
   - **Authentication vs. Application Launches (`login_history` & `app_open_events`):**
     - `login_history` records every authentication session (`user_id`, `login_at`, `logout_at`, `ip_address`).
     - `app_open_events` records client JVM launches (`launch_id` UUID, `user_id`, `opened_at`) with `UNIQUE (launch_id, user_id)` so multiple logins within a single application launch count as `1` app open.
   - **Social Graph (`friend_requests`, `friendships`, `user_blocks`):**
     - `friend_requests` tracks request lifecycle (`PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`) with a partial unique index on `(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id)) WHERE status = 'PENDING'` preventing simultaneous opposite-direction pending requests.
     - `friendships` stores accepted friendships canonically with `PRIMARY KEY (user_low_id, user_high_id)` and `CHECK (user_low_id < user_high_id)`.
     - `user_blocks` stores directional blocks `(blocker_id, blocked_id)` separately from friendships.
   - **Direct & Group Messaging (`direct_messages`, `direct_message_hidden`, `chat_groups`, `group_members`, `group_messages`, `group_message_hidden`):**
     - Per-user message deletion is modeled via `direct_message_hidden` and `group_message_hidden`, preserving underlying message rows and historical activity statistics.
     - `group_members` tracks `role` (`ADMIN` / `MEMBER` — distinct from `users.role`), `joined_at`, and `left_at` (`NULL` for current members).
     - `group_messages` enforces a row-level `CHECK` constraint requiring either plaintext (`content IS NOT NULL` and encryption fields `NULL`) or encrypted payload (`content IS NULL` and `ciphertext`, `nonce`, `key_version` all `NOT NULL`), plus a composite foreign key `(group_id, key_version) REFERENCES group_key_versions(group_id, key_version)`.
   - **Group Encryption Storage Preparation (`user_public_keys`, `group_key_versions`, `group_key_envelopes`):** Schema tables prepared for storing per-user public keys, group key versions, and wrapped group key envelopes.
   - **Moderation (`spam_reports`):** Stores `reporter_id`, `reported_user_id`, optional `direct_message_id`, `reason`, `message_snapshot` (immutable snapshot at report time), `status` (`PENDING` / `RESOLVED`), `resolved_by`, and `resolved_at`.
2. **`script/02_seed.sql` — Deterministic Synthetic Fixtures:**
   - Seeds 26 user accounts (`IDs 1..24` matching the Swing UI, `ID 25` `admin`, and `ID 26` soft-deleted `deleteduser01`) using standard `PBKDF2-HMAC-SHA256` (`310,000` iterations) synthetic local test password hashes.
   - Seeds all edge cases: 3 logins inside 1 app launch (`2026-03-05`), multiple app opens in March 2026, a zero-activity month (August 2026), a UTC-to-Vietnam year-boundary timestamp (`2025-12-31 23:45:00Z` = `2026-01-01 06:45:00+07`), overlapping 1-hop and 2-hop friendships, departed group member (`user 6`), hidden messages, and pending/resolved spam reports.
3. **`script/03_admin_queries.sql` & `script/04_verify.sql`:**
   - `03_admin_queries.sql` provides 10 runnable SQL queries (`QUERY 1` through `QUERY 10`, including `QUERY 9B` demonstrating `=`, `<`, `>` across all 3 activity metrics) using editable `params` CTEs.
   - `04_verify.sql` runs 18 automated PL/pgSQL assertions (`PASS [A1]`–`PASS [A9]` negative constraint tests and `PASS [B1]`–`PASS [B9]` positive seed/query invariant tests) inside a rollback-safe transaction (`BEGIN; ... ROLLBACK;`).

---

## 2. Factual Descriptions of Completed Administrator Screens & Dialogs

All 19 authentic screenshots below were captured directly from the running Java 21 Swing application (`com.chatsystem.admin.AdminApp`) and are stored in `docs/screenshots/admin/`.

### Screen 1: User Management (`Quản lý người dùng` — `UserManagementPanel`)
- **Screenshot:** `docs/screenshots/admin/01_user_management.png`
- **Features Implemented:**
  - Displays 4 summary `StatCard` components (`Tổng người dùng: 24`, `Đang hoạt động: 11`, `Ngoại tuyến: 10`, `Tài khoản bị khóa: 3`).
  - Toolbar provides live search by full name or username, **separated Account Status (`Tài khoản: Tất cả TK / Hoạt động / Đã khóa`) and Online Presence (`Phiên: Tất cả phiên / Trực tuyến / Ngoại tuyến`) filter dropdowns**, Vietnamese locale-aware sorting (`Cũ nhất`, `Mới nhất`, `Tên A–Z`, `Tên Z–A`), and a `Làm mới` reset button.
  - The user table renders dual side-by-side status pills (`Hoạt động` / `Đã khóa` paired with `Online` / `Offline`) and never exposes password hashes.
- **Associated Modal Dialogs (Requirements 1b–1f):**
  1. **Add / Edit User Dialog (`UserFormDialog`)** — `docs/screenshots/admin/01a_user_add_edit_dialog.png`: Form for `username`, `fullName`, `email`, `dateOfBirth`, `gender`, and `address` with input validation and in-memory table preview updates.
  2. **Lock / Unlock Account Confirmation (`ConfirmActionDialog`)** — `docs/screenshots/admin/01b_user_lock_confirm_dialog.png`: Confirms locking or unlocking the selected user account in memory.
  3. **Delete Account Confirmation (`ConfirmActionDialog`)** — `docs/screenshots/admin/01c_user_delete_confirm_dialog.png`: Confirms soft-deletion (`deleted_at`) of the selected user account in memory.
  4. **Password Update Dialog (`PasswordUpdateDialog`)** — `docs/screenshots/admin/01d_user_password_dialog.png`: Uses masked `JPasswordField` controls for new password and confirmation, includes a temporary show/hide checkbox, validates minimum length and match, and immediately zeroes `char[]` buffers (`Arrays.fill(pw, '\0')`) without persisting passwords.
  5. **Single-User Login History Dialog (`UserLoginHistoryDialog`)** — `docs/screenshots/admin/01e_user_login_history_dialog.png`: Displays chronological login sessions (`login_at`, `logout_at`, `ip_address`, session state) for the selected user (`QUERY 2A`).
  6. **Single-User Direct Friends Dialog (`UserFriendsDialog`)** — `docs/screenshots/admin/01f_user_friends_dialog.png`: Displays accepted direct friends (`ACCEPTED`) of the selected user (`QUERY 2B`).

### Screen 2: System-Wide Login History (`Lịch sử đăng nhập` — `LoginHistoryPanel`)
- **Screenshot:** `docs/screenshots/admin/02_system_login_history.png`
- **Features Implemented:**
  - Displays all 18 authentication sessions (`login_history`: login timestamp, username, full name, logout timestamp, IP address, session badge, and business note) ordered chronologically (`Mới nhất trước` / `Cũ nhất trước`), aligned with `QUERY 3`.
  - Includes a prominent informational callout distinguishing authentication sessions (`login_history`, where `@nguyenvanan` logged in 3 times inside 1 app launch on `05/03/2026`) from application launch events (`app_open_events`).
  - Supports live username/name search, validated date-range filtering (`Từ ngày` – `Đến ngày`), empty-result view, and no-selection state (`Bỏ chọn`).

### Screen 3: Chat Groups — Read-Only Inspection (`Quản lý nhóm chat` — `ChatGroupManagementPanel`)
- **Screenshot:** `docs/screenshots/admin/03_chat_groups.png`
- **Features Implemented:**
  - Marked with a `CHẾ ĐỘ TRA CỨU CHỈ ĐỌC (READ-ONLY)` badge (no group edit/delete mutations).
  - Displays the 4 chat groups (`Cộng đồng Java Việt Nam`, `Nhóm Đồ Án Hệ Thống Chat`, `CLB Thuật Toán & AI`, `Nhóm Học Tập KHTN`) with creation timestamp, creator, current active member count (`left_at IS NULL`), and encryption indicator badge (`Đã bật mã hóa (E2EE)` vs `Tiêu chuẩn (Plaintext)`), aligned with `QUERY 4A`.
  - Supports live group-name filtering and sorting by creation date (`Cũ nhất`, `Mới nhất`) or Vietnamese collated group name (`A–Z`, `Z–A`).
- **Associated Modal Dialogs (Requirements 3c & 3d):**
  1. **Current Group Members Dialog (`GroupMembersDialog`, `adminsOnly = false`)** — `docs/screenshots/admin/03a_group_members_dialog.png`: Lists current active members (`left_at IS NULL`, excluding departed member `@vuthithao` in Group 1) and displays **Group Role** (`group_members.role`: `Quản trị nhóm (ADMIN)` / `Thành viên (MEMBER)`) in a separate column from **System Role** (`users.role`: `Người dùng (USER)` / `Quản trị hệ thống (ADMIN)`), aligned with `QUERY 4B`.
  2. **Current Group Admins Dialog (`GroupMembersDialog`, `adminsOnly = true`)** — `docs/screenshots/admin/03b_group_admins_dialog.png`: Filters to active group administrators (`role = 'ADMIN' AND left_at IS NULL`), aligned with `QUERY 4C`.

### Screen 4: Spam Reports (`Báo cáo spam` — `SpamReportPanel`)
- **Screenshot:** `docs/screenshots/admin/04_spam_reports.png`
- **Features Implemented:**
  - Displays the 5 spam reports (`spam_reports`: report timestamp, reporter, reported user, reason, resolution status `Chờ xử lý` / `Đã xử lý`, and reported user account lock pill `TK Mở` / `TK Đã khóa`), aligned with `QUERY 5`.
  - Supports filtering by reported username and validated date range (`Từ ngày` – `Đến ngày`), and sorting by report time (`Mới nhất`, `Cũ nhất`) or reported username (`Username A–Z`, `Username Z–A`).
- **Associated Modal Dialogs (Requirement 4d):**
  1. **Spam Report Detail & Message Snapshot Dialog (`SpamReportDetailDialog`)** — `docs/screenshots/admin/04a_spam_report_detail_dialog.png`: Displays reporter and reported user metadata, resolution status, and the immutable synthetic direct-message snapshot (`message_snapshot`), with a direct button to trigger lock-account confirmation targeting the reported user (synchronized in memory with `UserManagementPanel`).

### Screen 5: Newly Registered Users (`Người dùng đăng ký mới` — `NewUserRegistrationPanel`)
- **Screenshot:** `docs/screenshots/admin/05_new_registrations.png`
- **Features Implemented:**
  - Provides validated start/end date controls (`Từ ngày` – `Đến ngày`) plus a quick preset button `Mẫu Tháng 02/2026 (QUERY 6)`, live name/username filtering, and sorting by creation time or full name.
  - Displays full name, username, exact creation timestamp (`dd/MM/yyyy HH:mm:ss`), email, and account lifecycle status (retaining historical soft-deleted account `@deleteduser01` registered on `10/02/2026 09:00:00` per Rule 8 and `QUERY 6`).

### Screen 6: Annual Registration Chart (`Biểu đồ đăng ký mới` — `RegistrationChartPanel` & `MonthlyBarChartPanel`)
- **Screenshots:**
  - `docs/screenshots/admin/06_registration_chart_2026.png` (Populated Year `2026` with zero-count Months `7..12`)
  - `docs/screenshots/admin/06a_registration_chart_2024_empty.png` (Empty-Year State `2024`)
- **Features Implemented:**
  - Year selector (`2026`, `2025`, `2024`), 4 summary `StatCard`s, a custom `Graphics2D` 12-month bar chart (`Tháng 1`..`Tháng 12` on X-axis, new registration count on Y-axis with integer scale), and a 12-month numeric verification strip (`T1`..`T12`) matching `QUERY 7` (`[4, 5, 5, 4, 4, 3, 0, 0, 0, 0, 0, 0]` in 2026).

### Screen 7: Friend Statistics (`Thống kê bạn bè` — `FriendStatisticsPanel`)
- **Screenshot:** `docs/screenshots/admin/07_friend_statistics.png`
- **Features Implemented:**
  - Displays full name, username, account creation timestamp, **Direct Friends Count** (`1-hop`), and **Friends Including Friends-of-Friends Count** (`1-or-2-hop DISTINCT`), matching `QUERY 8` (`@nguyenvanan`: `3` direct, `5` 1-or-2-hop; `@tranthibich`: `3` direct, `6` 1-or-2-hop; `@lehoangnam`: `4` direct, `6` 1-or-2-hop).
  - Includes header, cell, and badge tooltips explaining that the 1-or-2-hop count deduplicates overlapping paths (`DISTINCT`), excludes the user themselves (`self`), and excludes soft-deleted accounts.
  - Supports live name filtering, sorting by name or creation time, and direct-friend count filtering with `=`, `<`, `>` and validated numeric input.

### Screen 8: Active Users & Activity Metrics (`Người dùng hoạt động` — `UserActivityPanel`)
- **Screenshot:** `docs/screenshots/admin/08_user_activity.png`
- **Features Implemented:**
  - Provides validated start/end date controls (defaulting to March 2026: `2026-03-01` to `2026-03-31`, matching the 7 active users of `QUERY 9A & 9B`), name filtering, and sorting by name or creation time.
  - Displays the 3 independent activity metric columns: `Số lần mở ứng dụng` (`app_opens`), `Số người đã chat` (`distinct_direct_recipients`), and `Số nhóm đã chat` (`distinct_groups_messaged`).
  - Provides a metric filter selector containing **strictly the 3 concrete metrics** (`Số lần mở ứng dụng`, `Số người đã chat`, `Số nhóm đã chat` — no undefined aggregate total metric) paired with `=`, `<`, `>` and validated numeric input.

### Screen 9: Annual Activity Chart (`Biểu đồ hoạt động` — `ActivityChartPanel` & `MonthlyBarChartPanel`)
- **Screenshot:** `docs/screenshots/admin/09_activity_chart_2026.png`
- **Features Implemented:**
  - Year selector (`2026`, `2025`, `2024`), 4 summary `StatCard`s, a definition banner, and a 12-month bar chart plotting monthly **DISTINCT users who opened the User app** (`COUNT(DISTINCT aoe.user_id)` from `app_open_events`, never login count, message count, or total app-open count), matching `QUERY 10` (`[3, 3, 5, 2, 2, 1, 1, 0, 1, 1, 2, 1]` in 2026, with `Tháng 8 = 0`).
