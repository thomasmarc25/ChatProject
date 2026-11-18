# ChatSystem — Course Project Version 1 (Admin Swing UI & Shared PostgreSQL Database)

**Repository Scope (Member 2):**
1. **`chat-admin/`** — Java 21 Swing desktop application implementing all **9 Administrator requirement groups** and modal dialogs for Version 1 (`Final_Project.pdf`, Page 2), backed by deterministic in-memory mock data (`MockUserRepository.java`).
2. **`script/`** — Shared PostgreSQL 15+ database creation, seed, query, and automated verification scripts (`01_schema.sql`, `02_seed.sql`, `03_admin_queries.sql`, `04_verify.sql`) covering both the User and Administrator subsystems.

---

## 1. Technology Stack & Requirements

- **Language / Runtime:** Java 21 LTS (`maven.compiler.release` = `21`)
- **Desktop Framework:** Java Swing + FlatLaf `3.7.2` (Light theme), pure `java.awt.Graphics2D` custom charts & badges
- **Build System:** Apache Maven 3.6+ / 3.9+ (`UTF-8` encoding)
- **Window Dimensions:** `1280 x 800` px default; `1100 x 700` px minimum; resizable
- **Database Deliverables (`script/`):** PostgreSQL 15+ (verified on PostgreSQL 18.4, `Asia/Ho_Chi_Minh`)

---

## 2. Build & Launch Instructions (Windows PowerShell)

> [!TIP]
> In Windows PowerShell, always quote Maven `-D` arguments (`"-DskipTests"`, `"-Dexec.mainClass=..."`) so PowerShell does not split on the period `.`.

From the repository root in Windows PowerShell:

```powershell
Set-Location chat-admin
mvn "-DskipTests" clean package
mvn exec:java
```

Or specify the entry point explicitly:

```powershell
Set-Location chat-admin
mvn compile exec:java "-Dexec.mainClass=com.chatsystem.admin.AdminApp"
```

See [`docs/admin-run-guide.md`](docs/admin-run-guide.md) for full PowerShell environment setup and PostgreSQL verification commands.

---

## 3. Repository Structure & Documentation Index

- `chat-admin/` — Standalone Maven project containing `pom.xml`, `README.md`, and 21 Java classes (`34` compiled class units) under `src/main/java/com/chatsystem/admin/`.
- `script/` — Shared PostgreSQL 15+ database scripts:
  - [`script/01_schema.sql`](script/01_schema.sql) — Transactional DDL (`16` tables, PKs, `ON DELETE RESTRICT` FKs, row-level `CHECK`s, `32` indexes).
  - [`script/02_seed.sql`](script/02_seed.sql) — Deterministic synthetic seed data (`PBKDF2-HMAC-SHA256` local test password hashes and sequence synchronization).
  - [`script/03_admin_queries.sql`](script/03_admin_queries.sql) — Runnable SQL queries (`QUERY 1`..`QUERY 10`) with editable `params` CTEs.
  - [`script/04_verify.sql`](script/04_verify.sql) — Rollback-safe automated PL/pgSQL verification suite (`PASS [A1]`–`PASS [A9]` & `PASS [B1]`–`PASS [B9]`).
  - [`script/README.md`](script/README.md) — Clean-database prerequisites and execution order.
- `docs/` — Version 1 engineering and submission documentation:
  - [`docs/version1-plan.md`](docs/version1-plan.md) — Version 1 implementation plan and phase status.
  - [`docs/database-design.md`](docs/database-design.md) — Authoritative 16-table database specification, 10 business rules, ER diagrams, and Member 1 handoff guide.
  - [`docs/database-test-cases.md`](docs/database-test-cases.md) — Seed fixture documentation and expected query outputs.
  - [`docs/admin-screen-checklist.md`](docs/admin-screen-checklist.md) — Mapping of all 9 Admin requirement groups to Swing classes, SQL queries, and screenshots.
  - [`docs/admin-run-guide.md`](docs/admin-run-guide.md) — Windows PowerShell build and launch instructions.
  - [`docs/version1-submission-checklist.md`](docs/version1-submission-checklist.md) — Course submission structure (`src/`, `script/`, `git/`, and report checklist).
  - [`docs/admin-report-notes.md`](docs/admin-report-notes.md) — Factual descriptions of all 9 Admin screens, dialogs, and database design for the team report.
  - `docs/screenshots/admin/` — 19 authentic PNG screenshots captured from the running Administrator Swing application.
- [`AI-LOG.md`](AI-LOG.md) — Mandatory AI usage declaration log across all phases.

---

## 4. Honest Disclosure of Version 1 Scope

- **Version 1 Mock-Data Preview (`chat-admin/`):**
  - All 9 Administrator screens (`Quản lý người dùng`, `Lịch sử đăng nhập`, `Quản lý nhóm chat`, `Báo cáo spam`, `Người dùng đăng ký mới`, `Biểu đồ đăng ký mới`, `Thống kê bạn bè`, `Người dùng hoạt động`, `Biểu đồ hoạt động`) and 7 modal dialogs are implemented in Java Swing and operate on deterministic in-memory mock data (`MockUserRepository.java`).
  - Filter, search, sort, date-range validation, 12-month bar charts, and account-lock preview mutations execute in memory so every required UI state (`populated`, `empty-result`, `no-selection`, and `empty-year`) can be inspected.
  - No live Spring Boot server, TCP socket networking, authentication service, or JDBC/PostgreSQL connection is claimed or included in the Version 1 Swing UI.
