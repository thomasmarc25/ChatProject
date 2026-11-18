# Version 1 Implementation Plan
**Final Course Project: Chat System (Java Swing + PostgreSQL)**  
**Owner:** Member 2 — Administrator Application (`chat-admin`) & Shared PostgreSQL Database Design (`script/`)

---

## 1. Repository & Architecture Audit

### 1.1. Repository Structure & Git Status
- **Current Git branch:** `main` (clean working tree prior to planning, with 2 existing commits: `b47ef10` and `e8b0fb1`).
- **Existing root-level directories and files:**
  - `AGENTS.md`: Swing UI engineering instructions (Java 21 LTS, FlatLaf 3.7.2 Light, shared color tokens, default frame size `1280x800` / minimum `1100x700`, standard layout managers without `null` layout, and the English Markdown / Vietnamese chat language policy).
  - `AI-LOG.md` & `evidence/chat_history/`: AI usage declaration log and exported session transcripts/screenshots.
  - `README.md` & `chat-admin/README.md`: Build and execution instructions for the Admin module.
  - `chat-admin/`: Standalone Maven module (`com.chatsystem:chat-admin:1.0.0-SNAPSHOT`) containing the Administrator Swing UI source code.
- **Course Assignment PDF:** The `.pdf` file is not stored directly in the Git working tree, but all 5 pages of the official course project specification (**Final Project — Chat System**, Department of Software Engineering, Faculty of Information Technology, VNUHCM - University of Science) were provided in the session context and audited in full.

### 1.2. Technical Configuration & How to Launch `chat-admin`
- **Java & Encoding:** JDK 21 LTS (`maven.compiler.release = 21`), `UTF-8` encoding.
- **UI Library:** `com.formdev:flatlaf:3.7.2` (FlatLaf Light). No external icon or charting dependencies are used; all badges, avatars, and charts are rendered using anti-aliased `java.awt.Graphics2D`.
- **Application Entry Point:** `com.chatsystem.admin.AdminApp` (initializes on the Swing EDT via `SwingUtilities.invokeLater`, calling `AppTheme.setupLookAndFeel()` before opening `AdminMainFrame`).
- **Build and Launch Commands:**
  ```powershell
  cd chat-admin
  mvn -DskipTests package
  mvn exec:java
  ```

### 1.3. Administrator Subsystem Requirement Checklist (9 Requirements)

| Assignment Item | Status | Detailed Current Status & Missing Parts |
|---|---|---|
| **1. User Management** | **Completed** | **Implemented:** `UserManagementPanel` (4 `StatCard` summary cards, live search by name/username, distinct Account Status and Online Presence filter dropdowns, sorting by name/creation date, 24-row mock user `JTable` with dual status badges, pagination bar, row action popup menu), `UserFormDialog` (Add/Edit user), `ConfirmActionDialog` (Delete & Lock/Unlock confirmations), `PasswordUpdateDialog` (masked password input), `UserLoginHistoryDialog` (1e), and `UserFriendsDialog` (1f). |
| **2. System-Wide Login History** | **Completed** | **Implemented:** `LoginHistoryPanel` (4 `StatCard` summary cards, distinction banner explaining `login_history` vs `app_open_events`, live search by username/full name, validated date-range filter, chronological sort order toggle, 18-row `JTable` with session status pills and business notes, empty-result card, and no-selection state bar). |
| **3. Chat-Group List** | **Completed** | **Implemented:** `ChatGroupManagementPanel` (read-only inspection badge, 4 `StatCard` summary cards, group-name filter, sorting by group name and creation time, 4-row `JTable` with encryption indicator pills and action controls, empty-result card, and no-selection state bar) and `GroupMembersDialog` (both Current Members `left_at IS NULL` and Group Admins `role = 'ADMIN'` modes, showing `group_members.role` separately from `users.role`). |
| **4. Spam-Report List** | **Completed** | **Implemented:** `SpamReportPanel` (4 `StatCard` summary cards, reported-username filter, validated date-range filter, sorting by report time and reported username, 5-row `JTable` with status & account-lock pills, empty-result card, and no-selection state bar), `SpamReportDetailDialog` (synthetic `message_snapshot` preview), and `ConfirmActionDialog.showSpamReportLockConfirm` targeting the reported user (in-memory mock mutation synchronized across screens). |
| **5. Newly Registered Users** | **Completed** | **Implemented:** `NewUserRegistrationPanel` (start/end date controls with validation, quick preset for Feb 2026 Test Case 6, name filter, sorting by name/creation time, 25-row `JTable` retaining historical soft-deleted account `#26`, empty-result card, and no-selection state bar). |
| **6. Annual Registration Chart** | **Completed** | **Implemented:** `RegistrationChartPanel` and custom `MonthlyBarChartPanel` (`Graphics2D` 12-month bar chart with year selector `2026`/`2025`/`2024`, visible X/Y axes, month labels `Tháng 1`..`Tháng 12`, integer numeric scale, zero-count months `7..12` in 2026, Empty-Year State in 2024, and 12-month breakdown strip). |
| **7. User Friend Statistics** | **Completed** | **Implemented:** `FriendStatisticsPanel` (table with name, username, account creation time, 1-hop direct-friend count, and 1-or-2-hop DISTINCT friend count with header/cell/banner tooltips explaining exclusion of self, duplicates, and soft-deleted paths; name filter; sorting by name/creation time; and direct-friend count filter with `=`, `<`, `>` and numeric input). |
| **8. Active Users & Activity Metrics** | **Completed** | **Implemented:** `UserActivityPanel` (start/end date controls defaulting to March 2026 Test Case 9, name filter, sorting by name/creation time, table with app opens, distinct DM recipients, and distinct groups messaged, and metric filter selector containing strictly the 3 concrete metrics plus `=`, `<`, `>` and numeric input). |
| **9. Annual Activity Chart** | **Completed** | **Implemented:** `ActivityChartPanel` and `MonthlyBarChartPanel` (`Graphics2D` 12-month bar chart with year selector `2026`/`2025`/`2024` plotting monthly DISTINCT users who opened the User app `COUNT(DISTINCT user_id)` from `app_open_events`, including zero-count Month 8 in 2026 and Empty-Year State in 2024). |
| **PostgreSQL Database (`script/`)** | **Completed** | Implemented and verified in `script/01_schema.sql`, `script/02_seed.sql`, `script/03_admin_queries.sql`, and `script/04_verify.sql`. |

---

## 2. Detailed Implementation Sequence (A – H)

### A. Shared Database Specification (COMPLETED in `docs/database-design.md`)

The authoritative shared database specification is documented in [`docs/database-design.md`](./database-design.md), establishing 10 standardized business rules for both the User and Admin applications:

1. **Direct Friends (`friendships`):** Only accepted friendships count as direct friends, stored as a single canonical row with `user_low_id < user_high_id`. Friend requests are tracked separately in `friend_requests` (`PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`).
2. **Friends Including Friends-of-Friends (PDF Item 7):** Counts `COUNT(DISTINCT ...)` active users (`deleted_at IS NULL`) reachable in 1 or 2 edges in `friendships`, excluding the target user and excluding paths through soft-deleted accounts.
3. **Directional Blocking (`user_blocks`):** Stored separately as `(blocker_id, blocked_id)`. A block in either direction prevents direct messaging and friend requests in both directions, hides both users from each other's search results, removes any existing row in `friendships`, and cancels any `PENDING` rows in `friend_requests` between the two users; shared group memberships remain intact.
4. **Activity Count Filter Metric Selection (PDF Item 8c):** Filters select **1 of the 3 concrete metrics** (`app_opens`, `distinct_direct_recipients`, or `distinct_groups_messaged`) for comparison with `=`, `<`, or `>`. No undefined aggregate total metric is invented.
5. **Active User Definition & Annual Activity Chart (PDF Items 8 & 9):** An active user in `[start, end)` has at least one `app_open_events` row OR sends at least one message in `direct_messages` or `group_messages`. The annual activity chart counts `COUNT(DISTINCT user_id)` from `app_open_events` for each month (1..12).
6. **App Opens (`app_open_events`) Separate from Logins (`login_history`):** Each User-app JVM launch generates a client `launch_id` (`UUID`); once authenticated, at most one row is recorded per `UNIQUE (launch_id, user_id)`.
7. **Per-User Message Hiding (`direct_message_hidden`, `group_message_hidden`):** Deleting chat messages or chat history hides messages for that specific user only without deleting the underlying rows or reducing activity counts.
8. **Soft Account Deletion (`users.deleted_at`) & Password Handling:** Historical registration, login, and activity counts retain soft-deleted accounts; current friend lists and friend statistics exclude soft-deleted accounts. Password reset/update replaces `users.password_hash` (no plaintext password column exists).
9. **Time Intervals & Time Zone:** Half-open intervals `[start, end)` evaluated in `Asia/Ho_Chi_Minh`.
10. **Online Presence:** Managed by live TCP socket sessions / heartbeat in Version 2; `users.is_online` is not persisted in PostgreSQL.

---

### B. PostgreSQL Schema (COMPLETED in `script/01_schema.sql` & `script/README.md`)

The `script/01_schema.sql` script implements all 16 tables specified in [`docs/database-design.md`](./database-design.md) (13 core tables + 3 group encryption storage preparation tables) inside a single transaction (`BEGIN; ... COMMIT;`) for an empty PostgreSQL database, with `ON DELETE RESTRICT` across historical tables and zero optional extensions required:

- **13 Core Tables:**
  1. `users` (`id`, `username`, `password_hash`, `full_name`, `address`, `date_of_birth`, `gender`, `email`, `role`, `is_locked`, `created_at`, `updated_at`, `deleted_at`)
  2. `login_history` (`id`, `user_id`, `login_at`, `logout_at`, `ip_address`)
  3. `app_open_events` (`id`, `launch_id`, `user_id`, `opened_at`) — `UNIQUE (launch_id, user_id)`
  4. `friend_requests` (`id`, `sender_id`, `receiver_id`, `status`, `created_at`, `responded_at`) — Partial Unique Index for `PENDING` pairs
  5. `friendships` (`user_low_id`, `user_high_id`, `created_at`) — `PRIMARY KEY (user_low_id, user_high_id)`, `CHECK (user_low_id < user_high_id)`
  6. `user_blocks` (`blocker_id`, `blocked_id`, `created_at`) — `PRIMARY KEY (blocker_id, blocked_id)`
  7. `direct_messages` (`id`, `sender_id`, `receiver_id`, `content`, `sent_at`)
  8. `direct_message_hidden` (`message_id`, `user_id`, `hidden_at`)
  9. `chat_groups` (`id`, `name`, `created_by`, `is_encrypted`, `created_at`)
  10. `group_members` (`group_id`, `user_id`, `role`, `joined_at`, `left_at`)
  11. `group_messages` (`id`, `group_id`, `sender_id`, `content`, `ciphertext`, `nonce`, `key_version`, `sent_at`) — per-message plaintext/ciphertext `CHECK` and composite FK `(group_id, key_version)`
  12. `group_message_hidden` (`message_id`, `user_id`, `hidden_at`)
  13. `spam_reports` (`id`, `reporter_id`, `reported_user_id`, `direct_message_id`, `reason`, `message_snapshot`, `status`, `created_at`, `resolved_by`, `resolved_at`)
- **3 Encryption Storage Preparation Tables:**
  14. `user_public_keys` (`id`, `user_id`, `public_key`, `created_at`, `revoked_at`)
  15. `group_key_versions` (`group_id`, `key_version`, `created_at`)
  16. `group_key_envelopes` (`group_id`, `key_version`, `user_public_key_id`, `encrypted_group_key`)

---

### C. Seed Data and Admin Queries (COMPLETED in `script/02_seed.sql` & `script/03_admin_queries.sql`)

1. **`script/02_seed.sql`**:
   - Inserts 24 deterministic sample user accounts matching `MockUserRepository.java`, plus 1 `admin` account (`id = 25`) and 1 soft-deleted account (`id = 26`, `deleteduser01`), using standard `PBKDF2-HMAC-SHA256` (`310,000` iterations) synthetic local test password hashes.
   - Inserts deterministic fixtures for `login_history` (including 3 logins inside a single app launch), `app_open_events` (including multiple opens in a single month, a UTC-to-Vietnam year-boundary event, and a zero-activity month in August 2026), `friend_requests` (`PENDING`, `ACCEPTED`, `REJECTED`, `CANCELLED`), `friendships` (overlapping 1-hop and 2-hop triangles/diamonds, locked friend, and soft-deleted bridge), `user_blocks`, `chat_groups` (standard and encrypted), `group_members` (admins, active members, and a departed member), synthetic encryption storage fixtures (`user_public_keys`, `group_key_versions`, `group_key_envelopes`), `direct_messages`, `direct_message_hidden`, `group_messages`, `group_message_hidden`, and `spam_reports`, and synchronizes all 9 identity sequences.
2. **`script/03_admin_queries.sql`**:
   - Runnable SQL queries with editable `params` CTEs covering all 10 Admin query patterns (user list filtering/sorting, single-user login history & direct friends, system-wide login history, group list/members/admins, spam reports with reporter & reported usernames, newly registered users, 12-month registration counts, 1-hop and 2-hop friend statistics, per-user activity metrics with `=`, `<`, `>` filters across all 3 metrics, and 12-month distinct active user counts).

---

### D. Database Verification (COMPLETED — Verified via `script/04_verify.sql` on PostgreSQL 18.4 & Documented in `docs/database-test-cases.md`)

- Executed `script/01_schema.sql`, `script/02_seed.sql`, `script/03_admin_queries.sql`, and `script/04_verify.sql` sequentially with `psql -v ON_ERROR_STOP=1` against an isolated PostgreSQL 18.4 test database.
- `script/04_verify.sql` asserts all database-enforced constraints (Part A) and seed/query invariants (Part B) inside rollback-safe blocks (`BEGIN; ... ROLLBACK;`) without leaving fixture changes behind.
- Documented expected and verified outputs in [`docs/database-test-cases.md`](./database-test-cases.md), Member 1 handoff instructions in [`docs/database-design.md`](./database-design.md), and execution steps in [`script/README.md`](../script/README.md).

---

### E. Admin Navigation and User-Management Dialogs (COMPLETED in `chat-admin/`)

- **Expanded `SidebarPanel` & `AdminMainFrame` navigation to all 9 required Admin destinations:**
  - Group 1 (`QUẢN LÝ HỆ THỐNG`): `Quản lý người dùng`, `Lịch sử đăng nhập`, `Quản lý nhóm chat`, `Báo cáo spam`.
  - Group 2 (`THỐNG KÊ & BIỂU ĐỒ`): `Người dùng đăng ký mới`, `Biểu đồ đăng ký mới`, `Thống kê bạn bè`, `Người dùng hoạt động`, `Biểu đồ hoạt động`.
  - Clicking `Quản lý người dùng` opens the completed `UserManagementPanel`; clicking the remaining 8 destinations displays clearly marked placeholder views until Steps F and G are implemented.
- **Completed `UserManagementPanel` and all sub-dialogs:**
  1. Wired live search by name/username, distinct **Account Status** (`Tài khoản: Tất cả TK / Hoạt động / Đã khóa`) and **Online Presence** (`Phiên: Tất cả phiên / Trực tuyến / Ngoại tuyến`) filter dropdowns, and Vietnamese locale-aware sorting (`Cũ nhất`, `Mới nhất`, `Tên A–Z`, `Tên Z–A`).
  2. Rendered dual status badges in the table (`Hoạt động` / `Đã khóa` paired with `Online` / `Offline`).
  3. Implemented `ConfirmActionDialog.java` (Delete and Lock/Unlock confirmations), `PasswordUpdateDialog.java` (masked `JPasswordField` input with immediate buffer zeroing), `UserLoginHistoryDialog.java` (Requirement 1e), and `UserFriendsDialog.java` (Requirement 1f), and wired `UserFormDialog.java` for Add/Edit user preview.

---

### F. Login History, Groups, and Spam Reports (COMPLETED in `chat-admin/`)

Replaced the placeholder views for the 3 operational administration destinations (`Lịch sử đăng nhập`, `Quản lý nhóm chat`, `Báo cáo spam`) with complete Swing panels while keeping the remaining 5 statistics/chart destinations (`Người dùng đăng ký mới`, `Biểu đồ đăng ký mới`, `Thống kê bạn bè`, `Người dùng hoạt động`, `Biểu đồ hoạt động`) as clearly marked placeholders for Step G:

1. **`LoginHistoryPanel.java` (Requirement 2 — bound to `SidebarPanel.NAV_LOGIN_HISTORY`):**
   - Displays system-wide login history (`login_history`: login timestamp, username, full name, logout timestamp, IP address, session status badge, and business note) ordered chronologically (`Mới nhất trước` / `Cũ nhất trước`), with live username/name search, validated date-range filtering (`Từ ngày` – `Đến ngày`), a prominent banner distinguishing authentication events (`login_history`) from client application-open statistics (`app_open_events`), and populated, empty-result, and no-selection states.
2. **`ChatGroupManagementPanel.java` & `GroupMembersDialog.java` (Requirement 3 — bound to `SidebarPanel.NAV_GROUPS`):**
   - Read-only group inspection screen (no group edit/delete mutations): displays group name, creation time, creator, current member count (`left_at IS NULL`), and encryption indicator badge (`Đã bật mã hóa (E2EE)` vs `Tiêu chuẩn (Plaintext)`).
   - Supports filtering by group name, sorting by creation time (`Cũ nhất`, `Mới nhất`) and Vietnamese collated group name (`A–Z`, `Z–A`), populated/empty-result/no-selection states, and opens `GroupMembersDialog` in **Current Members** mode (3c) or **Group Admins** mode (3d) showing **Group Role** (`group_members.role`) in a separate column from **System Role** (`users.role`).
3. **`SpamReportPanel.java` & `SpamReportDetailDialog.java` (Requirement 4 — bound to `SidebarPanel.NAV_SPAM`):**
   - Displays reporter, reported user, report timestamp, reason, resolution status badge (`PENDING` / `RESOLVED`), and reported account lock badge.
   - Supports filtering by validated date range (`Từ ngày` – `Đến ngày`) and reported username, sorting by report time (`Mới nhất`, `Cũ nhất`) and reported username (`A–Z`, `Z–A`), populated/empty-result/no-selection states, `SpamReportDetailDialog` showing the synthetic direct-message snapshot (`message_snapshot`), and `ConfirmActionDialog.showSpamReportLockConfirm` targeting the reported user (mock-only in-memory mutation synchronized with `UserManagementPanel`).

---

### G. Registration and Activity Statistics and Charts (COMPLETED in `chat-admin/`)

Replaced the remaining 5 placeholder views with complete Swing panels and a reusable custom `Graphics2D` 12-month bar chart (`MonthlyBarChartPanel.java`), and added an unobtrusive `"Version 1 • Mock Data Preview"` indicator badge in `HeaderPanel.java`:

1. **`NewUserRegistrationPanel.java` (Requirement 5 — bound to `SidebarPanel.NAV_NEW_REGISTRATIONS`):**
   - Start/end date controls (`Từ ngày` – `Đến ngày`) with `YYYY-MM-DD` and range validation, name filter, sorting by name (`A–Z`, `Z–A`) or creation time (`Cũ nhất`, `Mới nhất`), and table displaying full name, username, and account creation timestamp (retaining historical soft-deleted account `#26` aligned with `QUERY 6`).
2. **`RegistrationChartPanel.java` & `MonthlyBarChartPanel.java` (Requirement 6 — bound to `SidebarPanel.NAV_REGISTRATION_CHART`):**
   - Year selector (`JComboBox`: `2026`, `2025`, `2024`), 4 summary `StatCard`s, custom `Graphics2D` 12-month bar chart (`Tháng 1`..`Tháng 12` on horizontal axis, registration count on vertical axis with integer numeric scale), zero-count months (`7..12` in 2026), and an explicit **Empty-Year State** (`2024`), aligned with `QUERY 7`.
3. **`FriendStatisticsPanel.java` (Requirement 7 — bound to `SidebarPanel.NAV_FRIEND_STATS`):**
   - Displays each active user with full name, username, creation time, **Direct Friends Count** (`1-hop`), and **Friends Including Friends-of-Friends Count** (`1-or-2-hop DISTINCT`), with header/cell/banner tooltips explaining that the second count excludes self, duplicates, and soft-deleted accounts (aligned with `QUERY 8`).
   - Supports name filter, sorting by name or creation time, and direct-friend count filtering using `=`, `<`, or `>` with validated numeric input.
4. **`UserActivityPanel.java` (Requirement 8 — bound to `SidebarPanel.NAV_USER_ACTIVITY`):**
   - Start/end date controls (`Từ ngày` – `Đến ngày`, defaulting to March 2026 `QUERY 9A/9B`) and table displaying active users with 3 metric columns: **Số lần mở ứng dụng** (`app_opens`), **Số người đã chat** (`distinct_direct_recipients`), and **Số nhóm đã chat** (`distinct_groups_messaged`).
   - Supports sorting by name or creation time, filtering by name, and filtering by activity count using `=`, `<`, or `>` after selecting **strictly 1 of the 3 concrete metrics** (no undefined "total activity" metric).
5. **`ActivityChartPanel.java` & `MonthlyBarChartPanel.java` (Requirement 9 — bound to `SidebarPanel.NAV_ACTIVITY_CHART`):**
   - Year selector (`JComboBox`: `2026`, `2025`, `2024`), 4 summary `StatCard`s, and custom `Graphics2D` 12-month bar chart plotting monthly **DISTINCT users who opened the User app** (`COUNT(DISTINCT aoe.user_id)` from `app_open_events`, never login count, message count, or total app-open count), including zero-count Month 8 in 2026 and an **Empty-Year State** in 2024 (aligned with `QUERY 10`).

---

### H. Final Verification and Submission Preparation

1. **Build & Visual QA:**
   - Run `mvn -DskipTests clean package` on JDK 21 LTS and verify zero compilation errors.
   - Launch the Swing application and inspect all 9 screens and dialogs at both `1280x800` and `1100x700`.
2. **Authentic Screenshots (`screenshots/`):**
   - Capture live screenshots of all 9 Admin screens and modal dialogs for the Version 1 report.
3. **Version 1 Submission Package Preparation:**
   - Organize `src/`, `script/`, `git/` commit screenshot instructions, and the Version 1 report draft (`docs/report-version1-draft.md`), and finalize `AI-LOG.md`.
