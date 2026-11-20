# ChatSystem Admin (`chat-admin`) — Version 1 Mock-Data Preview

Java 21 Swing desktop application for the **Administrator Subsystem** (`Final_Project.pdf`, Page 2 — Requirements 1 through 9), backed by deterministic in-memory mock data (`MockUserRepository.java`) aligned 1:1 with `script/02_seed.sql` and `script/03_admin_queries.sql`.

---

## 1. Requirements

- **Java:** JDK 21 LTS (`maven.compiler.release` = `21`)
- **Desktop GUI:** Java Swing + FlatLaf `3.7.2` (Light theme)
- **Build Tool:** Apache Maven 3.6+ / 3.9+ (`UTF-8` encoding)
- **Window Dimensions:** `1280 x 800` px default; `1100 x 700` px minimum; resizable

---

## 2. Build & Run Instructions (Windows PowerShell)

Run the following commands from the `chat-admin/` directory in Windows PowerShell (always quote Maven `-D` flags):

### Compile and package
```powershell
mvn "-DskipTests" clean package
```

### Run the Swing application via Maven
```powershell
mvn exec:java
```

Or specify the main class explicitly with PowerShell-safe quoting:
```powershell
mvn compile exec:java "-Dexec.mainClass=com.chatsystem.admin.AdminApp"
```

---

## 3. Module Source Structure

```text
chat-admin/
  pom.xml
  README.md
  src/main/java/com/chatsystem/admin/
    AdminApp.java                       # EDT-safe entry point; initializes FlatLaf Light first
    theme/
      AppTheme.java                     # Shared color tokens, Segoe UI/SansSerif fonts, and spacing constants
    components/
      SidebarPanel.java                 # 240px dark sidebar with all 9 Admin navigation destinations
      HeaderPanel.java                  # 64px top header with breadcrumb, section title, and Version 1 Mock Preview badge
      StatCard.java                     # Summary statistic card component
      MonthlyBarChartPanel.java         # Custom Graphics2D 12-month bar chart with visible axes, zero months & empty-year state
    model/
      User.java                         # User account model with separated account lock and online presence states
      LoginRecord.java                  # Authentication session model (login_history)
      UserFriendRecord.java             # Accepted direct friend model (1-hop)
      ChatGroupRecord.java              # Chat group summary model (read-only inspection)
      GroupMemberRecord.java            # Group member model separating group role (ADMIN/MEMBER) from system role (USER/ADMIN)
      SpamReportRecord.java             # Spam report model with synthetic message_snapshot
      RegistrationRecord.java           # Historical registration record model (retaining soft-deleted accounts)
      FriendStatRecord.java             # 1-hop and 1-or-2-hop DISTINCT friend statistics model
      UserActivityRecord.java           # 3-metric user activity model (app opens, distinct DM recipients, distinct groups)
    data/
      MockUserRepository.java           # Deterministic in-memory mock dataset aligned 1:1 with script/02_seed.sql
    table/
      UserTableModel.java               # Table model for User Management directory
    ui/
      AdminMainFrame.java               # Resizable 1280x800 (min 1100x700) dashboard shell hosting all 9 screens
      UserManagementPanel.java          # Screen 1: User Management directory with separated Account & Presence filters
      UserFormDialog.java               # Dialog 1b: Add / Edit user profile dialog
      ConfirmActionDialog.java          # Dialogs 1b, 1c, 4d: Delete and Lock/Unlock confirmation dialogs
      PasswordUpdateDialog.java         # Dialog 1d: Masked JPasswordField password update dialog (char[] zeroed)
      UserLoginHistoryDialog.java       # Dialog 1e: Selected user's chronological login history
      UserFriendsDialog.java            # Dialog 1f: Selected user's accepted direct friends list
      LoginHistoryPanel.java            # Screen 2: System-wide login history (distinguished from app_open_events)
      ChatGroupManagementPanel.java     # Screen 3: Read-only chat group directory
      GroupMembersDialog.java           # Dialogs 3c, 3d: Current group members & group admins (group vs. system roles)
      SpamReportPanel.java              # Screen 4: Spam report management with date-range & username filters
      SpamReportDetailDialog.java       # Dialog 4: Spam report detail & synthetic message_snapshot viewer
      NewUserRegistrationPanel.java     # Screen 5: Newly registered users by date range
      RegistrationChartPanel.java       # Screen 6: Annual 12-month registration bar chart
      FriendStatisticsPanel.java        # Screen 7: Direct & 1-or-2-hop friend statistics with tooltips & =, <, > filter
      UserActivityPanel.java            # Screen 8: Active users & 3-metric activity filter (=, <, >)
      ActivityChartPanel.java           # Screen 9: Annual 12-month DISTINCT app-opening users bar chart
```

---

## 4. Honest Disclosure of Version 1 Scope

This module is a **Version 1 in-memory mock-data preview** (`MockUserRepository.java`):
- All 9 screens, 7 modal dialogs, search/filter/sort controls, date-range validators, and 12-month bar charts run in memory on the Swing EDT.
- In-memory mutations (adding/editing/deleting/locking a user or locking a reported spam account) update the in-memory preview state for the current JVM run only and are not persisted to disk or a server.
- No Spring Boot backend, TCP socket networking, or JDBC/PostgreSQL database connection is included in Version 1.
