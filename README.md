# ChatSystem — Course Project Version 1 (User & Administrator Swing UIs + Shared PostgreSQL Database)

**Two-Member Course Project — Version 1 Deliverable (20% Weight):**
1. **Member 1 (`src/chat-user/`) — User Desktop Application:**
   - Java 21 Swing + FlatLaf `3.7.2` desktop application (`com.chatsystem.user.UserApp`) implementing the User subsystem screens (Main Chat workspace with personal/group/E2EE conversation list, message search, LLM composition assistant dialog, spam report dialog, group creation & management dialogs, Friends directory, Friend Requests, People Search, and Account login/registration/forgot-password/profile/change-password tabs) using deterministic in-memory mock data (`ChatData.java`, `MockData.java`).
2. **Member 2 (`chat-admin/` & `script/`) — Administrator Desktop Application & Shared PostgreSQL Database:**
   - Java 21 Swing + FlatLaf `3.7.2` desktop application (`com.chatsystem.admin.AdminApp`) implementing all **9 Administrator requirement groups** and 7 modal dialogs using deterministic in-memory mock data (`MockUserRepository.java`).
   - Shared PostgreSQL 15+ database contract (`script/01_schema.sql`, `script/02_seed.sql`, `script/03_admin_queries.sql`, `script/04_verify.sql`, plus reconciled User-subsystem query/verification companions `script/01_person1.sql`, `script/verify_person1.sql`, and `script/check_person1.mjs`).

---

## 1. Technology Stack & Requirements

- **Language / Runtime:** Java 21 LTS (`maven.compiler.release` = `21` in both `src/chat-user/pom.xml` and `chat-admin/pom.xml`)
- **Desktop Framework:** Java Swing + FlatLaf `3.7.2` (Light theme), pure `java.awt.Graphics2D` custom charts, avatars, and icons
- **Build System:** Apache Maven 3.6+ / 3.9+ (`UTF-8` encoding)
- **Window Dimensions:**
  - User Application (`src/chat-user/`): `1440 x 900` px default; `1100 x 700` px minimum; resizable
  - Administrator Application (`chat-admin/`): `1280 x 800` px default; `1100 x 700` px minimum; resizable
- **Database Deliverables (`script/`):** PostgreSQL 15+ (verified on PostgreSQL 18.4, `Asia/Ho_Chi_Minh`)

---

## 2. Build & Launch Instructions (Windows PowerShell / CMD)

> [!TIP]
> In Windows PowerShell, always wrap Maven `-D` arguments in double quotes (`"-DskipTests"`, `"-Dexec.mainClass=..."`) so PowerShell does not split on the period `.`.

### 2.1. Integrated ChatSystem Admin Launch (All 6 Personal User Pages + All 9 Admin Screens in One Window)

`chat-admin` depends one-way on `chat-user` (zero circular Maven dependency) and embeds Member 1's actual Swing panels (`UserWorkspaceHost`) under the **Cá nhân** sidebar section alongside all 9 **Quản trị** screens in `AdminMainFrame`.

Build and launch from the repository root in Windows PowerShell:

```powershell
mvn -f src/chat-user/pom.xml "-DskipTests" clean install
mvn -f chat-admin/pom.xml "-DskipTests" clean package
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" -cp "chat-admin\target\classes;src\chat-user\target\classes;$HOME\.m2\repository\com\formdev\flatlaf\3.7.2\flatlaf-3.7.2.jar" com.chatsystem.admin.AdminApp
```

Or start from the unified Version 1 mock login window (`nguyenvanan` → User workspace, `admin` → Integrated Admin workspace):

```powershell
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" -cp "src\chat-user\target\classes;chat-admin\target\classes;$HOME\.m2\repository\com\formdev\flatlaf\3.7.2\flatlaf-3.7.2.jar" com.chatsystem.user.UserApp
```

### 2.2. Launch the User Application Independently (`src/chat-user/` — Member 1)

From the repository root in Windows PowerShell (pass `--direct-user` to skip the login window and open the chat workspace directly):

```powershell
mvn -f src/chat-user/pom.xml "-DskipTests" clean package
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" -cp "src\chat-user\target\classes;$HOME\.m2\repository\com\formdev\flatlaf\3.7.2\flatlaf-3.7.2.jar" com.chatsystem.user.UserApp --direct-user
```

Or run the Windows launcher script directly from the repository root:

```powershell
.\run-user.cmd
```

### 2.3. Launch the Administrator Application Independently (`chat-admin/` — Member 2)

From the repository root in Windows PowerShell (recommended fast direct JDK 21 launch after compiling):

```powershell
mvn -f chat-admin/pom.xml "-DskipTests" compile
& "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" -cp "chat-admin\target\classes;$HOME\.m2\repository\com\formdev\flatlaf\3.7.2\flatlaf-3.7.2.jar" com.chatsystem.admin.AdminApp
```

Or launch via Maven (`exec-maven-plugin`):

```powershell
mvn -f chat-admin/pom.xml exec:java
```

See [`docs/admin-run-guide.md`](docs/admin-run-guide.md) for full PowerShell environment setup and PostgreSQL verification commands.

---

## 3. Unified PostgreSQL Database Setup Path (`script/`)

The project uses **one unified PostgreSQL schema contract** defined in [`docs/database-design.md`](docs/database-design.md) and implemented in [`script/01_schema.sql`](script/01_schema.sql). Execute the scripts in the following order on a clean, empty PostgreSQL database:

```powershell
createdb -h 127.0.0.1 -p 5432 -U postgres -E UTF8 chat_system_v1
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/01_schema.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/02_seed.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/03_admin_queries.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/04_verify.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/01_person1.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/verify_person1.sql
```

> [!NOTE]
> **Reconciled User-Subsystem SQL Scripts (`01_person1.sql`, `verify_person1.sql`, `check_person1.mjs`):**
> Member 1's initial SQL draft proposed a unified `conversations`/`conversation_members`/`messages`/`message_user_states` table layout. During branch integration, `01_person1.sql`, `verify_person1.sql`, and `check_person1.mjs` were reconciled and adapted to query and verify `main`'s authoritative 16-table schema (`users`, `friendships`, `friend_requests`, `user_blocks`, `direct_messages`, `direct_message_hidden`, `chat_groups`, `group_members`, `group_messages`, `group_message_hidden`, `user_public_keys`, `group_key_versions`, `group_key_envelopes`, `spam_reports`, `login_history`, `app_open_events`). Meanwhile, UI-only in-memory classes such as `ChatData.java` continue to present a unified conversation list in the User Swing UI.

---

## 4. Repository Structure & Evidence Index

- `src/chat-user/` — Member 1's standalone Maven module (`com.chatsystem:chat-user:1.0.0-SNAPSHOT`, 13 Java classes under `src/main/java/com/chatsystem/user/`).
- `chat-admin/` — Member 2's standalone Maven module (`com.chatsystem:chat-admin:1.0.0-SNAPSHOT`, 21 Java classes under `src/main/java/com/chatsystem/admin/`).
- `script/` — Unified PostgreSQL 15+ database scripts (`01_schema.sql`, `02_seed.sql`, `03_admin_queries.sql`, `04_verify.sql`, `01_person1.sql`, `verify_person1.sql`, `check_person1.mjs`, and `script/README.md`).
- `git/` — Member 1's Git commit evidence (`git/commits-phuc.png`) and 17 User UI screenshots (`git/screenshots/01-chat.png` through `14-message-search.png`).
- `docs/` — Version 1 engineering and submission documentation:
  - [`docs/version1-plan.md`](docs/version1-plan.md) — Version 1 implementation plan and phase status.
  - [`docs/database-design.md`](docs/database-design.md) — Authoritative 16-table database specification, 10 business rules, ER diagrams, and Member 1 handoff guide.
  - [`docs/database-test-cases.md`](docs/database-test-cases.md) — Seed fixture documentation and expected query outputs.
  - [`docs/admin-screen-checklist.md`](docs/admin-screen-checklist.md) — Mapping of all 9 Admin requirement groups to Swing classes, SQL queries, and screenshots.
  - [`docs/admin-run-guide.md`](docs/admin-run-guide.md) — Windows PowerShell build and launch instructions for both modules and PostgreSQL.
  - [`docs/version1-submission-checklist.md`](docs/version1-submission-checklist.md) — Course submission structure (`src/`, `script/`, `git/`, and report checklist).
  - [`docs/admin-report-notes.md`](docs/admin-report-notes.md) — Factual descriptions of all 9 Admin screens, dialogs, and database design for the team report.
  - `docs/screenshots/admin/` — 19 authentic PNG screenshots captured from the running Administrator Swing application.
- `evidence/chat_history/` & [`AI-LOG.md`](AI-LOG.md) — AI usage declaration log and session evidence.

---

## 5. Honest Disclosure of Version 1 Scope

Both desktop applications (`src/chat-user/` and `chat-admin/`) are **Version 1 in-memory mock-data previews**:
- All User and Administrator screens, modal dialogs, search/filter/sort controls, date-range validators, and 12-month bar charts run in memory on the Swing EDT without requiring a running database or network server.
- No live Spring Boot backend, TCP socket networking, authentication service, or JDBC/PostgreSQL connection is claimed or included in the Version 1 Swing UIs.
