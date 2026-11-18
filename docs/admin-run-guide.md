# Administrator Application Build & Run Guide (`docs/admin-run-guide.md`)

**Project:** Chat System — Course Final Project (Version 1: UI & Database Design)  
**Module:** `chat-admin` (`com.chatsystem:chat-admin:1.0.0-SNAPSHOT`)  
**Entry Point:** `com.chatsystem.admin.AdminApp`

---

## 1. Prerequisites

- **Operating System:** Microsoft Windows 10 / 11 (or any desktop OS with a graphical display server).
- **Java Development Kit:** **JDK 21 LTS** (`maven.compiler.release = 21`).
- **Build Tool:** **Apache Maven 3.6+ / 3.9+**.
- **UI Dependency:** `com.formdev:flatlaf:3.7.2` (resolved automatically by Maven).
- **Database Required to Launch Swing UI?** **No.** Version 1 of the Swing UI is a standalone in-memory mock-data preview (`MockUserRepository.java`) and launches immediately without a running PostgreSQL server.

---

## 2. Important Note on Windows PowerShell Quoting for Maven `-D` Arguments

In **Windows PowerShell**, unquoted arguments beginning with `-D` that contain a period `.` (such as `-Dexec.mainClass=...` or `-Dmaven.test.skip=true`) are split by the PowerShell parser at the period, causing Maven to fail with `Unknown lifecycle phase ".mainClass=..."`.

**Always wrap Maven `-D` flags in double quotes in Windows PowerShell:**
- Correct in PowerShell: `mvn "-DskipTests" clean package`
- Correct in PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.chatsystem.admin.AdminApp"`

---

## 3. Build and Launch Commands (Windows PowerShell)

### 3.1. Compile and Package the JAR

Open Windows PowerShell at the repository root and run:

```powershell
Set-Location chat-admin
mvn "-DskipTests" clean package
```

Expected output:
- Compiles all `34` Java source files under `chat-admin/src/main/java/com/chatsystem/admin/` with `javac [debug release 21]`.
- Produces `chat-admin/target/chat-admin-1.0.0-SNAPSHOT.jar` (`BUILD SUCCESS`).

### 3.2. Launch the Administrator Swing UI via Maven (`exec-maven-plugin`)

Because `chat-admin/pom.xml` already configures `<exec.mainClass>com.chatsystem.admin.AdminApp</exec.mainClass>`, you can launch the application directly from `chat-admin/`:

```powershell
Set-Location chat-admin
mvn exec:java
```

Or specify the main class explicitly (with PowerShell-safe quoting):

```powershell
Set-Location chat-admin
mvn compile exec:java "-Dexec.mainClass=com.chatsystem.admin.AdminApp"
```

### 3.3. Build and Launch the User Application (`src/chat-user/` — Member 1)

From the repository root in Windows PowerShell:

```powershell
mvn -f src/chat-user/pom.xml "-DskipTests" clean package
mvn -f src/chat-user/pom.xml exec:java
```

Or run the Windows launcher script directly from the repository root:

```powershell
.\run-user.cmd
```

### 3.4. If `JAVA_HOME` or `mvn` Is Not on Your Default PowerShell `PATH`

If you use a local JDK 21 installation and the Maven Wrapper distribution cached under `$HOME\.m2\wrapper\dists`, run:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$HOME\.m2\wrapper\dists\apache-maven-3.6.3-bin\1iopthnavndlasol9gbrbg6bf2\apache-maven-3.6.3\bin;$env:PATH"
Set-Location chat-admin
mvn "-DskipTests" clean package
mvn exec:java
```

---

## 4. Running the PostgreSQL Database Scripts (`script/`)

The SQL deliverables in `script/` are standalone PostgreSQL 15+ scripts (verified on PostgreSQL 18.4). They are not required to launch the Version 1 Swing UI, but can be executed and verified on any clean PostgreSQL database (`chat_system_v1_test`) using `psql`:

```powershell
createdb -h 127.0.0.1 -p 5432 -U postgres -E UTF8 chat_system_v1_test
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1_test -v ON_ERROR_STOP=1 -f script/01_schema.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1_test -v ON_ERROR_STOP=1 -f script/02_seed.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1_test -v ON_ERROR_STOP=1 -f script/03_admin_queries.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1_test -v ON_ERROR_STOP=1 -f script/04_verify.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1_test -v ON_ERROR_STOP=1 -f script/01_person1.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1_test -v ON_ERROR_STOP=1 -f script/verify_person1.sql
```

---

## 5. Navigating and Previewing All 9 Screens and Modal Dialogs

Once `AdminMainFrame` opens (`1280x800` default, resizable down to `1100x700` minimum):

1. **Quản lý người dùng (`UserManagementPanel`):**
   - Test live search, separated `Tài khoản:` (`Hoạt động` / `Đã khóa`) and `Phiên:` (`Trực tuyến` / `Ngoại tuyến`) dropdown filters, and sorting (`Cũ nhất`, `Mới nhất`, `Tên A–Z`, `Tên Z–A`).
   - Click `+ Thêm người dùng` or `Sửa` on any row to open `UserFormDialog`.
   - Click `Thao tác ▾` on any row to open the popup menu and preview:
     - `Xóa tài khoản` (`ConfirmActionDialog`)
     - `Khóa tài khoản` / `Mở khóa tài khoản` (`ConfirmActionDialog`)
     - `Đổi mật khẩu` (`PasswordUpdateDialog` with masked `JPasswordField`)
     - `Lịch sử đăng nhập` (`UserLoginHistoryDialog`)
     - `Danh sách bạn bè` (`UserFriendsDialog`)
2. **Lịch sử đăng nhập (`LoginHistoryPanel`):**
   - Inspect chronological authentication sessions (`login_history`), test date-range validation (`Từ ngày` – `Đến ngày`), toggle `Mới nhất trước` / `Cũ nhất trước`, and test `Bỏ chọn` (no-selection state).
3. **Quản lý nhóm chat (`ChatGroupManagementPanel`):**
   - Filter by group name, sort by creation date or group name, and click `Thành viên` / `Xem thành viên hiện tại` or `Admin` / `Xem Admin nhóm` to open `GroupMembersDialog` showing **Group Role** (`group_members.role`) separately from **System Role** (`users.role`).
4. **Báo cáo spam (`SpamReportPanel`):**
   - Filter by reported username and validated date range, sort by time or username, click `Chi tiết` / `Xem chi tiết & Snapshot` to inspect `SpamReportDetailDialog` (`message_snapshot`), and click `Khóa TK` / `Khóa/Mở TK bị báo cáo` to preview in-memory account lock synchronization.
5. **Người dùng đăng ký mới (`NewUserRegistrationPanel`):**
   - Click `Mẫu Tháng 02/2026 (QUERY 6)` to view the 5 February 2026 registrations (including historical soft-deleted account `@deleteduser01`), or filter by custom date range and name.
6. **Biểu đồ đăng ký mới (`RegistrationChartPanel`):**
   - Inspect the 12-month bar chart for `2026` (populated months `1..6` and zero-count months `7..12`) and switch to `2024` to inspect the **Empty-Year State**.
7. **Thống kê bạn bè (`FriendStatisticsPanel`):**
   - Hover over the `Bạn bè + Bạn của bạn (i)` column header or cells to view the tooltip explaining 1-or-2-hop `DISTINCT` deduplication and self-exclusion; test the `=`, `<`, `>` direct-friend count filter.
8. **Người dùng hoạt động (`UserActivityPanel`):**
   - Inspect the 7 active users of March 2026 (`QUERY 9A/9B`) across the 3 concrete metrics (`Số lần mở ứng dụng`, `Số người đã chat`, `Số nhóm đã chat`), and filter by selecting 1 of the 3 metrics with `=`, `<`, or `>`.
9. **Biểu đồ hoạt động (`ActivityChartPanel`):**
   - Inspect the 12-month bar chart of monthly **DISTINCT users who opened the User app** (`COUNT(DISTINCT aoe.user_id)`) for `2026` (including zero-count `Tháng 8 = 0`) and `2024` (**Empty-Year State**).
