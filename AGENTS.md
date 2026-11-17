# AGENTS.md — ChatSystem Admin / User Management (Swing UI-Only)

> Repository instructions for Google Antigravity and any other coding assistant. **Read this file before every phase.** The individual assignment due **12 November 2026** is **one Java Swing screen**, not the complete Chat System.

## 1. Mission, deliverables, and scope

Build one professional, runnable **Admin — User Management** screen, with an optional add/edit dialog as part of that screen, for the university individual Swing assignment.

Required submission:
- `source_code/` — the complete runnable Java Swing project, including `pom.xml` and source files.
- `screenshots/` — **real screenshots** captured from the application actually running.

Also keep `AI-LOG.md` in the repository root for the AI usage declaration. If the instructor specifies a different packaging layout, follow that layout while preserving the files and evidence.

**IN SCOPE:** visual components, Java Swing layout, representative mock data, Vietnamese labels, a running entry point, minimal click behavior to open a UI-only dialog, quality assurance, authentic screenshots, and AI usage documentation.

**OUT OF SCOPE:** real CRUD, saving data, login/authentication, real password handling, network requests, TCP sockets, WebSocket, Spring Boot, JDBC/PostgreSQL, SQL scripts, database integration, charts, real groups/spam/admin activity modules, complex business logic, and production-ready pagination/filter engines. Show appropriate controls on screen, but they need not perform their final business functions.

**Do not add features from future phases.** Follow only the current phase prompt and STOP after reporting results.

## 2. Technology decisions

| Item | Decision |
|---|---|
| Language | Java 21 LTS |
| Desktop GUI | Java Swing (not JavaFX/web) |
| Look & Feel | FlatLaf 3.7.2, light |
| Build | Apache Maven |
| Encoding | UTF-8 |
| UI labels | Vietnamese with diacritics |
| Packages / Java identifiers | English |
| Default frame | 1280 x 800 px; minimum 1100 x 700 px; resizable |
| Font | Segoe UI with SansSerif fallback |
| Layout | BorderLayout, GridBagLayout, BoxLayout or other real layout managers; no null layout or `setBounds` |

Suggested Maven dependency: `com.formdev:flatlaf:3.7.2`. Use `SwingUtilities.invokeLater` for initialization. Set Look & Feel before creating the UI. Avoid unnecessary external dependencies, icons that require internet access at runtime, and absolute paths.

## 3. Shared visual design

| Color token | Hex |
|---|---|
| Primary | `#2563EB` |
| Primary hover | `#1D4ED8` |
| Sidebar | `#111827` |
| Page background | `#F5F7FB` |
| Surface/card | `#FFFFFF` |
| Primary text | `#1F2937` |
| Secondary text | `#6B7280` |
| Border | `#E5E7EB` |
| Success | `#16A34A` |
| Warning | `#F59E0B` |
| Danger | `#DC2626` |

- Sidebar width: approximately 240 px; header height: approximately 64 px; content padding: 24 px.
- Font sizes: page title 24, section title 18, body 14, caption 12 (Java Swing font point units).
- Target heights: button/text input 40 px, table row 44 px; consistent 8/16/24 px spacing.
- Rounded corners (8–12 px) where FlatLaf supports them; readable contrast and aligned controls.
- Optimize for 1280x800 and 1100x700, including Windows text scaling when feasible.

## 4. UI requirements (one screen only)

The single **User Management** screen displays:
1. Dark admin sidebar, top header, selected "Quản lý người dùng" navigation item; other sections are nonfunctional placeholders.
2. Page title, a short description, and a prominent `+ Thêm người dùng` button.
3. Summary cards: total, online, offline, and locked user counts calculated from the mock list.
4. A search input; status filter; name/creation-date sorting controls; refresh button (visual presence only is acceptable).
5. A JTable of at least **20 deterministic Vietnamese sample users**, with username, full name, email, created date, status, and actions.
6. A clearly visible row action control or action menu for edit, delete, lock/unlock, change password, login history, and friends. These controls need not change underlying data.
7. A pagination bar (visual only; no pagination engine required).
8. An optional `UserFormDialog` opened from the Add/Edit control with fields for **username, full name, address, date of birth, gender, and email**. Save/Cancel buttons are visual only; Cancel should close the dialog.

Use a `User` model with `id`, `username`, `fullName`, `address`, `dateOfBirth`, `gender`, `email`, `createdAt`, `online`, `locked`. Treat account lock and online presence as separate properties. Display locked users as `Đã khóa`, otherwise online users as `Đang hoạt động`, others as `Ngoại tuyến`.

Do not claim any function works unless actually implemented and tested. A static search box is a valid UI deliverable but must be described honestly as static.

## 5. Project boundaries

**Inspect the workspace first.** If an existing ChatSystem repository already exists, preserve teammates' code, naming, Maven parent, and other modules. Do not blindly reinitialize Git or create nested duplicate modules. If the workspace is empty, create a standalone `chat-admin` Maven project, portable into the shared repository later.

Suggested package root: `com.chatsystem.admin`.

```text
chat-admin/
  pom.xml
  src/main/java/com/chatsystem/admin/
    AdminApp.java
    theme/AppTheme.java
    ui/AdminMainFrame.java
    ui/UserManagementPanel.java
    ui/UserFormDialog.java           # if included in Phase 3
    components/SidebarPanel.java
    components/HeaderPanel.java
    components/StatCard.java
    model/User.java
    data/MockUserRepository.java
    table/UserTableModel.java
```

Only create files as they are needed. Use PascalCase for classes, camelCase for methods/fields, uppercase underscores for constants, and lowercase packages. Keep UI, model and mock-data responsibilities separate.

## 6. Four-phase workflow

1. **Phase 1 — Setup:** inspect workspace, configure Maven/Java 21/FlatLaf and theme, create a runnable shell.
2. **Phase 2 — Main layout:** sidebar, header and main content shell; keep content placeholder.
3. **Phase 3 — User Management screen:** table, mock data, summary cards, search/filter/sort UI, pagination UI, action affordances and optionally a UI-only add/edit dialog.
4. **Phase 4 — Polish and submit:** compile, run and inspect when possible, fix layout, document and prepare authentic screenshots/submission structure.

Never perform later phases early. Preserve working code between phases.

## 7. MANDATORY AI usage logging — `AI-LOG.md`

**This is a submission compliance requirement, not an optional note.** On starting any Antigravity session (and after each new substantial prompt or iteration), **create or append to `AI-LOG.md` at the repository root**. If it already exists, **do not replace or erase prior entries**. Log every phase, revision, agent-generated file, and validation outcome, including unsuccessful attempts.

Each record must contain all fields below, matching the university's AI Usage Notes:

1. **AI tool name, version/model, and platform:** e.g., `Google Antigravity; model: [read actual UI/config]; platform: [desktop/IDE + OS]`. Never assume a model/version; if not exposed, write `Not available to agent — student must verify`.
2. **Access date and time (with time zone):** ideally `YYYY-MM-DD HH:mm:ss UTC+07:00 (Asia/Ho_Chi_Minh)`. Record the tool session time as observed. If shell is available, use `TZ=Asia/Ho_Chi_Minh date '+%Y-%m-%d %H:%M:%S %z'`. If unavailable, mark `To be verified by student`. Never invent timestamps or use file modification time as proof of session start.
3. **Prompt(s) used:** paste the **exact prompt actually sent**, preserving wording, or cite a tracked prompt file **plus any user-added text**. A file path alone does not prove what was pasted; mark whether the agent observed the actual chat prompt or only the checked-in source prompt. For long prompts, put the verbatim text in `ai_prompts/` and link it from the log, with any differences noted.
4. **Purpose:** state why AI was used for that phase.
5. **AI-generated content:** list actual files/classes/layouts/text created or changed by AI, with enough specificity to identify contributions.
6. **Student's independent work and edits/validation:** **never fabricate this**. Add `STUDENT TO COMPLETE` fields for manual code changes, decisions, review, tests, and how they were verified. If the student explicitly reports actions, log those with attribution; never claim them on their behalf.
7. **Screenshots or chat history:** list real relative paths under `screenshots/` or `evidence/` only if the files exist. If not present, use `Pending capture/export by student`. Never fabricate screenshots, claim to save the conversation history, or insert imaginary references.
8. **Verification and outcomes:** commands actually executed, pass/fail results, issues, and anything not testable. Clearly distinguish build success from visual inspection.

### Logging procedure — perform in every phase

- **Before editing:** inspect/initialize `AI-LOG.md`, append a session record with the current phase, tool/platform (if known), timestamp status, exact prompt evidence, and intended purpose. Include `AI-LOG.md` itself in the change summary.
- **During work:** append a brief dated revision/sub-entry for additional prompts or significant generated changes; do not overwrite earlier records.
- **At end of work:** update only the current record with actual generated/modified files, commands/output, blockers, genuine screenshot references if created, and `STUDENT TO COMPLETE` fields.
- **Before submission (Phase 4):** check that each recorded AI-assisted phase has all required fields; flag incomplete items in a checklist. The student must confirm actual version/platform/time, manually supplied independent contributions, and chat history/screenshots. **Do not mark these as complete without proof.**

**Important:** AI logging instructions are best-effort agent behavior, not a background logging service. If a tool has no reliable way to see its own version, precise access time, or chat export, leave it explicitly pending so the student can complete the evidence truthfully. Record the use of **ChatGPT to prepare `AGENTS.md` and prompts** as a separate planning entry (provided as a template in `AI-LOG.md`), not as work performed by Antigravity.

Keep logs free of passwords, API keys and personal account secrets. Screenshots should avoid exposing sensitive data.

## 8. Required validation and finish protocol

For each phase:
1. Read `AGENTS.md` and the appropriate `prompts/0X_*.md` completely.
2. Inspect the current project. Briefly outline changes limited to the requested phase.
3. **Open/append `AI-LOG.md` before coding**, logging what can actually be established.
4. Implement the phase with small, cohesive edits; do not touch teammate modules.
5. Try to compile using a command appropriate to the structure (`mvn -DskipTests package` inside module, or `mvn -pl chat-admin -am -DskipTests package` from multi-module root). Record exact commands, results or why they could not run.
6. Inspect the running UI only if the environment supports it; report honestly. Do not invent screenshots.
7. Complete the current log entry; include a list of file changes, verification results and remaining manual tasks.
8. Stop and report. **Do not proceed to the next phase on your own.**

## 9. Non-negotiable constraints

- One Admin User Management Swing screen, not the whole final project.
- No real backend, sockets, DB, CRUD business logic, JavaFX, web UI or null layouts.
- No editing unrelated teammate code or removing prior working work.
- No false claims about compiling, execution, screenshots, student contributions, model versions, or timestamps.
- No fake AI evidence or backdated logs. Missing values remain clearly marked for manual completion.
