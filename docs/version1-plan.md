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
| **1. User Management** | **Partially Implemented (~80%)** | **Implemented:** `UserManagementPanel` (4 `StatCard` summary cards, search/status-filter/sort toolbar, 24-row mock user `JTable`, pagination bar, row action popup menu) and `UserFormDialog` (Add/Edit dialog with all 6 required profile fields: username, full name, address, date of birth, gender, email).<br>**Missing:** **Password Update Dialog** (1d), **User Login History Dialog** (1e), **User Friends Dialog** (1f), **Delete / Lock-Unlock Confirmation Dialogs** (1b, 1c), and in-memory filtering/sorting event handlers on the toolbar controls. |
| **2. System-Wide Login History** | **Missing** | The `"Lịch sử đăng nhập"` (Login History) navigation item exists in `SidebarPanel` but currently opens the `"Tính năng đang phát triển"` (Under Development) placeholder card. |
| **3. Chat-Group List** | **Missing** | The `"Quản lý nhóm chat"` (Chat Group Management) navigation item exists in `SidebarPanel` (currently showing the placeholder card). Needs the group list panel (filter by name, sort by name/creation time) and dialogs to view **Group Members** and **Group Administrators**. |
| **4. Spam-Report List** | **Missing** | The `"Báo cáo spam"` (Spam Reports) navigation item exists in `SidebarPanel` (currently showing the placeholder card). Needs the spam report list panel with filtering/sorting by time and username, plus an **Account Lock** action. |
| **5. Newly Registered Users** | **Missing** | Needs a screen with a date/time range selector (`From Date` – `To Date`), name filter, and sorting by name or creation time. |
| **6. Annual Registration Chart** | **Missing** | Needs a year selector and a 12-month bar chart (horizontal axis: months 1–12; vertical axis: newly registered user counts). |
| **7. User Friend Statistics** | **Missing** | Needs a table displaying **Direct Friends Count** and **Friends Including Friends-of-Friends Count**, sorting by name/creation time, filtering by name, and filtering direct-friend count using `=`, `<`, or `>`. |
| **8. Active Users & Activity Metrics** | **Missing** | Needs a date/time range selector, table displaying 3 activity metrics (**App Opens**, **Distinct Users Chatted With**, **Distinct Groups Chatted With**), sorting by name/creation time, filtering by name, and filtering by activity count using `=`, `<`, or `>`. |
| **9. Annual Activity Chart** | **Missing** | Needs a year selector and a 12-month bar chart (horizontal axis: months 1–12; vertical axis: number of distinct users who opened the application). |
| **PostgreSQL Database (`script/`)** | **Missing** | Needs SQL scripts for schema creation, constraints, indexes, deterministic seed data, and administrative queries shared by both subsystems. |

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

### C. Seed Data and Admin Queries (`script/02_seed_data.sql` & `script/03_admin_queries.sql`)

1. **`script/02_seed_data.sql`**:
   - Inserts the 24 deterministic sample user accounts matching `MockUserRepository.java` (distributed across months 1–12 in 2025–2026 so annual charts display rich data), plus an `admin` account and 1 soft-deleted account to verify soft-deletion filtering rules.
   - Inserts sample records for `login_history`, `app_open_events` (with distinct `launch_id` UUIDs across months 1–12), `friend_requests`, `friendships` (multi-hop social graph to verify 1-hop and 2-hop friend counts), `user_blocks`, `chat_groups` (both standard and encrypted groups), `group_members`, `user_public_keys`, `group_key_versions`, `group_key_envelopes`, `direct_messages`, `direct_message_hidden`, `group_messages`, `group_message_hidden`, and `spam_reports`.
2. **`script/03_admin_queries.sql`**:
   - Reference SQL queries covering all 9 Admin requirements:
     - User list filtering/sorting, single-user login history, and single-user direct friends list.
     - System-wide chronological login history.
     - Chat-group list with filtering/sorting, active group members list (`left_at IS NULL`), and active group administrators list (`role = 'ADMIN' AND left_at IS NULL`).
     - Spam-report list with time-range and username filtering/sorting, plus account-lock resolution update.
     - Newly registered users in `[start, end)` with name filter and name/creation-time sorting.
     - Annual 12-month registration counts using `generate_series(1, 12)` in `Asia/Ho_Chi_Minh`.
     - User friend statistics CTE computing `direct_friends_count` and `friends_with_fof_count` (`DISTINCT` 1-hop $\cup$ 2-hop active users excluding self).
     - User activity statistics in `[start, end)` computing `app_opens`, `distinct_direct_recipients`, and `distinct_groups_messaged`, with metric-specific comparison filtering (`=`, `<`, `>`).
     - Annual 12-month active user counts (`COUNT(DISTINCT user_id)` in `app_open_events`) using `generate_series(1, 12)` in `Asia/Ho_Chi_Minh`.

---

### D. Database Verification

- Validate SQL syntax, foreign key dependency order, constraint enforcement, and query execution for `script/01_schema.sql`, `script/02_seed_data.sql`, and `script/03_admin_queries.sql`.
- Run live verification against PostgreSQL if `psql` / a local PostgreSQL server or container is available; if no local PostgreSQL server is running, perform static verification of all table/column references and verify that the Java in-memory mock data produces identical results.
- Document execution instructions in `script/README.md`.

---

### E. Admin Navigation and User-Management Dialogs (`UserManagementPanel`)

- **Preserve 100% of the existing visual layout** of `UserManagementPanel` and `UserFormDialog`.
- **Enhance preview navigation and dialogs on `UserManagementPanel`:**
  1. Wire in-memory filtering and sorting on the `UserManagementPanel` toolbar (search by name/username, filter by status, sort by name `A–Z`/`Z–A` and creation date `Newest`/`Oldest`).
  2. Create modal dialogs (`JDialog`) in `com.chatsystem.admin.ui` launched from the row action menu:
     - `PasswordUpdateDialog.java` (Requirement 1d — Update / reset password).
     - `UserLoginHistoryDialog.java` (Requirement 1e — View selected user's login history).
     - `UserFriendsDialog.java` (Requirement 1f — View selected user's direct friends list).
     - Confirmation dialogs for `Delete` (Requirement 1b) and `Lock/Unlock` (Requirement 1c).

---

### F. Login History, Groups, and Spam Reports

Replace the placeholder views for the 3 sidebar items with complete Swing panels:

1. **`LoginHistoryPanel.java` (Requirement 2 — bound to `SidebarPanel.NAV_LOGIN_HISTORY`):**
   - Displays system-wide login history ordered chronologically (login timestamp, username, full name, IP address), with search and sort controls.
2. **`ChatGroupManagementPanel.java` & `GroupMembersDialog.java` (Requirement 3 — bound to `SidebarPanel.NAV_GROUPS`):**
   - Toolbar: Filter by group name, sort by group name (`A–Z`, `Z–A`) and creation time (`Newest`, `Oldest`).
   - Group `JTable`: index, group name, creator, creation date, active member count, active admin count, encryption badge, and row actions to open `GroupMembersDialog` in **View Members** mode (3c) or **View Admins** mode (3d).
3. **`SpamReportPanel.java` (Requirement 4 — bound to `SidebarPanel.NAV_SPAM`):**
   - Toolbar: Filter by time range (`From Date` – `To Date`) and reported username; sort by report timestamp or username.
   - Spam report `JTable`: index, report timestamp, reported username & full name, reporter username, reason, message snapshot, status badge, and an **Account Lock** action button (4d).

---

### G. Registration and Activity Statistics and Charts

Bind `SidebarPanel.NAV_STATISTICS` to `StatisticsWorkspacePanel.java` containing 5 sub-tabs corresponding to Requirements 5, 6, 7, 8, and 9:

1. **Tab 1 — `NewUserRegistrationPanel.java` (Requirement 5: Newly Registered Users):**
   - Select a time range (`From Date` – `To Date`), filter by name, sort by name or creation time, and display matching users.
2. **Tab 2 — `RegistrationChartPanel.java` (Requirement 6: Annual Registration Chart):**
   - Select a year (`JComboBox`: `2026`, `2025`, `2024`) and render a 12-month bar chart via `MonthlyBarChartPanel.java` (`Graphics2D`: horizontal axis = Months 1..12, vertical axis = newly registered user count).
3. **Tab 3 — `FriendStatisticsPanel.java` (Requirement 7: User Friend Statistics):**
   - Display each active user with **Direct Friends Count** and **Friends Including Friends-of-Friends Count**.
   - Filter by name, sort by name or creation time, and filter direct-friend count using `=`, `<`, or `>` with a numeric input.
4. **Tab 4 — `UserActivityPanel.java` (Requirement 8: Active Users & Activity Statistics):**
   - Select a time range (`From Date` – `To Date`) and display active users with 3 metric columns: **App Opens**, **Distinct Users Chatted With**, and **Distinct Groups Chatted With**.
   - Sort by name or creation time, filter by name, and filter by activity count using `=`, `<`, or `>` after selecting **1 of the 3 concrete metrics** (`App Opens`, `Distinct Users Chatted With`, or `Distinct Groups Chatted With`; no undefined aggregate total metric).
5. **Tab 5 — `ActivityChartPanel.java` (Requirement 9: Annual Activity Chart):**
   - Select a year (`JComboBox`: `2026`, `2025`, `2024`) and render a 12-month bar chart via `MonthlyBarChartPanel.java` (`Graphics2D`: horizontal axis = Months 1..12, vertical axis = distinct users who opened the application).

---

### H. Final Verification and Submission Preparation

1. **Build & Visual QA:**
   - Run `mvn -DskipTests clean package` on JDK 21 LTS and verify zero compilation errors.
   - Launch the Swing application and inspect all 9 screens and dialogs at both `1280x800` and `1100x700`.
2. **Authentic Screenshots (`screenshots/`):**
   - Capture live screenshots of all 9 Admin screens and modal dialogs for the Version 1 report.
3. **Version 1 Submission Package Preparation:**
   - Organize `src/`, `script/`, `git/` commit screenshot instructions, and the Version 1 report draft (`docs/report-version1-draft.md`), and finalize `AI-LOG.md`.
