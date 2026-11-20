# Version 1 Submission Checklist (`docs/version1-submission-checklist.md`)

**Course Project:** Java Application Programming — Final Project: Chat System  
**Milestone:** **Version 1 (20% Weight)** — UI Source Code & Database Design (`Final_Project.pdf`, Page 4)

> [!IMPORTANT]
> **Critical Grading Warnings from `Final_Project.pdf` (Page 4):**
> - The team must use Git for source control. **Any member without Git commits receives 0 points.**
> - The team must submit the UI source code for **both subsystems** (Administrator and User).
> - The team must submit the database creation script(s).
> - **An incorrect archive (ZIP) directory structure receives 0 points.**
> - **A report missing any required section receives 0 points.**

---

## 1. Official Version 1 Archive Layout Required by `Final_Project.pdf` (Page 4)

When packaging the final Version 1 submission archive (do **not** restructure or move the working Git repository directly; assemble a separate staging folder when both members are ready to package the ZIP), the archive must contain:

```text
<Submission_Archive_Root>/
  src/
    chat-admin/                     # Member 2: Complete runnable Maven module (pom.xml + src/main/java/...)
    <user-app-module>/              # Member 1: Complete runnable Maven module for the User subsystem
    pom.xml                         # (Optional parent POM if the team merges into a multi-module root)
  script/
    01_schema.sql                   # Member 2: Transactional PostgreSQL 15+ DDL (16 tables, 32 indexes)
    02_seed.sql                     # Member 2: Deterministic synthetic seed data & sequence sync
    03_admin_queries.sql            # Member 2: Runnable administrative & analytical SQL queries (Queries 1..10)
    04_verify.sql                   # Member 2: Rollback-safe automated PL/pgSQL verification suite
    README.md                       # Member 2: Execution order and clean-database instructions
  git/
    member1_commits.png             # Screenshot(s) showing Member 1's real Git commit history
    member2_commits.png             # Screenshot(s) showing Member 2's real Git commit history
  <Report_File>.pdf (or .docx)      # Team Version 1 Report containing all 5 mandatory sections below
```

> [!NOTE]
> **Preserving Maven Structure Inside `src/`:**
> - `chat-admin/` is currently a self-contained Maven project (`chat-admin/pom.xml`, `com.chatsystem:chat-admin:1.0.0-SNAPSHOT`) with zero machine-specific paths.
> - When copying `chat-admin/` into the submission archive's `src/` directory, preserve `chat-admin/pom.xml` and the full `chat-admin/src/main/java/com/chatsystem/admin/...` directory tree intact, and exclude build output (`chat-admin/target/`).
> - If the team uses a shared parent `pom.xml` in `src/pom.xml`, ensure `<modules>` lists both `chat-admin` and Member 1's user module with matching relative paths.

---

## 2. Deliverable Status Breakdown (Member 2 vs. Team / Member 1)

### 2.1. `src/` — UI Source Code for Both Subsystems

| Item | Owner | Current Status in This Repository | Remaining Action Before Submission |
|---|---|---|---|
| **Administrator Subsystem UI (`chat-admin/`)** | **Member 2** | **COMPLETED & VERIFIED** (`34` Java classes covering all 9 Admin requirement groups and 7 modal dialogs; compiles cleanly on JDK 21 with `mvn "-DskipTests" clean package`). | Copy `chat-admin/` (excluding `target/`) into the submission `src/` folder. |
| **User Subsystem UI (12 requirement groups on Page 3 of `Final_Project.pdf`)** | **Member 1** | **INTEGRATED FROM `origin/Phuc` (`src/chat-user/`)** (`13` Java classes under `src/chat-user/src/main/java/com/chatsystem/user/`, entry point `com.chatsystem.user.UserApp`, plus `run-user.cmd` and 17 UI screenshots in `git/screenshots/`). | Copy `src/chat-user/` (excluding `target/`) alongside `chat-admin/` in the submission `src/` folder. |

### 2.2. `script/` — PostgreSQL Database Creation & Verification Scripts

| File | Owner | Current Status in This Repository | Remaining Action Before Submission |
|---|---|---|---|
| `script/01_schema.sql` | **Member 2** | **COMPLETED & VERIFIED** on PostgreSQL 18.4 (`16` tables, PKs, `ON DELETE RESTRICT` FKs, row-level `CHECK`s, `32` indexes inside `BEGIN; ... COMMIT;`). | Copy directly into the submission `script/` folder. |
| `script/02_seed.sql` | **Member 2** | **COMPLETED & VERIFIED** on PostgreSQL 18.4 (deterministic synthetic fixtures, PBKDF2-HMAC-SHA256 local test hashes, sequence `setval` synchronization). | Copy directly into the submission `script/` folder. |
| `script/03_admin_queries.sql` | **Member 2** | **COMPLETED & VERIFIED** on PostgreSQL 18.4 (runnable SQL with `params` CTEs for all 10 Admin query patterns). | Copy directly into the submission `script/` folder. |
| `script/04_verify.sql` | **Member 2** | **COMPLETED & VERIFIED** on PostgreSQL 18.4 (`PASS [A1]`–`PASS [A9]` and `PASS [B1]`–`PASS [B9]` inside `BEGIN; ... ROLLBACK;`). | Copy directly into the submission `script/` folder. |
| `script/01_person1.sql` | **Member 1 & Member 2** | **RECONCILED & VERIFIED** on PostgreSQL 18.4 (read-only User-subsystem reference queries adapted to `01_schema.sql`; zero competing `CREATE TABLE` DDL). | Copy into the submission `script/` folder if desired, or keep `01_schema.sql` as the primary schema script. |
| `script/verify_person1.sql` | **Member 1 & Member 2** | **RECONCILED & VERIFIED** on PostgreSQL 18.4 (rollback-safe User-subsystem constraint and visibility assertions adapted to `01_schema.sql`). | Copy into the submission `script/` folder. |
| `script/README.md` | **Member 2** | **COMPLETED** (documents single unified database setup path and execution order). | Copy directly into the submission `script/` folder. |

### 2.3. `git/` — Screenshots of Each Member's Real Git Commits

| Item | Owner | Current Status | Remaining Action Before Submission |
|---|---|---|---|
| **Member 2 Git Commit Screenshots** | **Member 2** | **MANUAL CAPTURE REQUIRED** (Real commits exist on `main`, e.g., `f1087f9`..`af313b2`, plus the final documentation commit). | Capture a real screenshot of `git log --author="<Member 2>"` or the GitHub/GitLab commit history page showing Member 2's commits, and save it to `git/`. |
| **Member 1 Git Commit Screenshots** | **Member 1** | **INTEGRATED FROM `origin/Phuc` (`git/commits-phuc.png`)** | Included in `git/commits-phuc.png`. |

### 2.4. Version 1 Team Report (5 Mandatory Sections from `Final_Project.pdf`, Page 4)

| Report Section | Current Status | Source Material / Action Required |
|---|---|---|
| **1. Team Information (`Thông tin nhóm`)** | **TEAM TO COMPLETE MANUALLY** | Fill in real student full names, student IDs (MSSV), email addresses, and class/group metadata. *(Never fabricated by AI).* |
| **2. Task Allocation per Member (`Danh sách công việc từng thành viên`)** | **TEAM TO COMPLETE MANUALLY** | Document Member 1's ownership of the User Swing application and Member 2's ownership of the Administrator Swing application + shared PostgreSQL database design. |
| **3. Contribution Percentage Table (`Bảng đóng góp từng thành viên — tổng là 100`)** | **TEAM TO COMPLETE MANUALLY** | Both members must agree on and fill in their real contribution percentages totaling exactly `100%`. *(Never fabricated by AI).* |
| **4. System Database Design (`Thiết kế cơ sở dữ liệu của hệ thống`)** | **READY IN `docs/database-design.md` & `docs/admin-report-notes.md`** | Copy the ER diagrams, 10 standardized business rules, and 16-table Data Dictionary from [`docs/database-design.md`](./database-design.md) and [`docs/admin-report-notes.md`](./admin-report-notes.md). |
| **5. List of Designed Screens with Screenshots (`Danh sách các màn hình đã thiết kế — có hình chụp`)** | **ADMIN PART READY (`docs/screenshots/admin/` & `docs/admin-report-notes.md`); USER PART PENDING FROM MEMBER 1** | Insert Member 2's 19 real Admin screenshots from `docs/screenshots/admin/` along with the factual descriptions in [`docs/admin-report-notes.md`](./admin-report-notes.md), and combine with Member 1's User application screen descriptions and screenshots. |
