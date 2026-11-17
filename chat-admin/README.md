# ChatSystem Admin — User Management (`chat-admin`)

Individual university Java Swing UI assignment (due **12 November 2026**): **Admin — User Management (`Quản lý người dùng`)** single-screen desktop interface.

---

## 1. Requirements

- **Java:** JDK 21 LTS (`maven.compiler.release` = `21`)
- **Desktop GUI:** Java Swing
- **Look & Feel:** FlatLaf `3.7.2` (Light theme)
- **Build Tool:** Apache Maven 3.6+ / 3.9+
- **Character Encoding:** `UTF-8`
- **Window Dimensions:** `1280 x 800` px default; `1100 x 700` px minimum; resizable

---

## 2. Build & Run Instructions

Run the following commands from the `chat-admin/` directory:

### Compile and package
```bash
mvn -DskipTests clean package
```

### Run the Swing application via Maven
```bash
mvn exec:java
```

Or run the compiled classes directly with Java 21:
```bash
mvn compile exec:java -Dexec.mainClass="com.chatsystem.admin.AdminApp"
```

---

## 3. Module Structure

```text
chat-admin/
  pom.xml
  README.md
  src/main/java/com/chatsystem/admin/
    AdminApp.java                  # EDT-safe entry point; initializes FlatLaf Light first
    theme/
      AppTheme.java                # Shared color tokens, Segoe UI/SansSerif fonts, and spacing constants
    components/
      SidebarPanel.java            # 240px dark sidebar with brand header, nav items, and profile/logout footer
      HeaderPanel.java             # 64px top header with breadcrumb, section title, and admin avatar badge
      StatCard.java                # Summary statistic card component
    model/
      User.java                    # Typed User model with independent online and locked boolean states
    data/
      MockUserRepository.java      # 24 deterministic Vietnamese mock user records and partition counts
    table/
      UserTableModel.java          # Non-editable AbstractTableModel (STT, Tên đăng nhập, Họ tên, Email, Ngày tạo, Trạng thái, Thao tác)
    ui/
      AdminMainFrame.java          # Resizable 1280x800 (min 1100x700) dashboard shell frame
      UserManagementPanel.java     # Main User Management screen (intro, 4 stat cards, toolbar, JTable, pagination)
      UserFormDialog.java          # Modal UI-only Add/Edit user dialog
```

---

## 4. Scope & Honest Disclosure of UI-Only Controls

This deliverable is strictly a **Java Swing UI-only implementation** backed by in-memory deterministic mock data (`MockUserRepository` with 24 Vietnamese sample users):

- **Implemented & Functional UI Behavior:**
  - Full Swing layout using `BorderLayout`, `GridBagLayout`, `GridLayout`, `BoxLayout`, and `CardLayout` (no `null` layouts or `setBounds`).
  - Summary statistic cards (`Tổng người dùng: 24`, `Đang hoạt động: 11`, `Ngoại tuyến: 10`, `Tài khoản bị khóa: 3`) computed directly from the 24 mock user records (`online` and `locked` are stored as independent boolean fields).
  - Non-editable `JTable` (`44px` row height) with custom status badge pills (`Đang hoạt động`, `Ngoại tuyến`, `Đã khóa`) and row action pills (`Sửa` and `Thao tác`).
  - Clicking `+ Thêm người dùng` opens `UserFormDialog` in Add mode; clicking `Sửa` on any row (or inside the row's `Thao tác` popup menu) opens `UserFormDialog` pre-populated with that user's data; clicking `Hủy` closes the dialog.
  - Clicking `Thao tác` on any table row opens a popup menu displaying `Sửa`, `Xóa`, `Khóa/Mở khóa`, `Đổi mật khẩu`, `Lịch sử đăng nhập`, and `Danh sách bạn bè`.
  - Clicking other sidebar navigation items (`Quản lý nhóm chat`, `Báo cáo spam`, `Lịch sử đăng nhập`, `Thống kê`, `Cài đặt`) displays a `"Tính năng đang phát triển"` placeholder view, while clicking `Quản lý người dùng` returns to the User Management screen.

- **Static / UI-Only Placeholders (No Backend or Data Mutation):**
  - Search input (`Tìm theo tên hoặc tên đăng nhập...`), status filter (`Tất cả trạng thái / Đang hoạt động / Ngoại tuyến / Đã khóa`), sort dropdown (`Tên A–Z / Tên Z–A / Mới nhất / Cũ nhất`), and `Làm mới` button are **static visual controls** and do not filter or resort the table data.
  - Pagination controls (`« Trước`, `1`, `2`, `3`, `Sau »`) are **visual-only placeholders**.
  - Row action menu items (`Xóa`, `Khóa/Mở khóa`, `Đổi mật khẩu`, `Lịch sử đăng nhập`, `Danh sách bạn bè`), the `Lưu thông tin` button inside `UserFormDialog`, and the sidebar `Đăng xuất` button are **visual-only affordances** and do not modify or persist data.
  - No database (JDBC/PostgreSQL), network sockets, Spring Boot, or authentication services are included.
