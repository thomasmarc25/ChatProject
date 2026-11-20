# PostgreSQL Database Scripts (`script/`)

This directory contains the unified SQL scripts for the **Chat System** shared PostgreSQL database (used by both the User Application in `src/chat-user/` and the Administrator Application in `chat-admin/`).

---

## 1. Prerequisites & Authoritative Contract

- **Authoritative Schema Contract:** [`script/01_schema.sql`](01_schema.sql) and [`docs/database-design.md`](../docs/database-design.md) define the **single authoritative 16-table schema** for the entire project.
- **Database Engine:** PostgreSQL 15 or newer (verified on PostgreSQL 18.4).
- **Encoding & Time Zone:** `UTF-8` client/database encoding; application queries and calendar aggregations use time zone `Asia/Ho_Chi_Minh`.
- **Extensions:** None required. All constraints and case-insensitive uniqueness checks use standard built-in PostgreSQL expressions.
- **Target Database State:** `01_schema.sql` must be executed inside an **already-created, empty PostgreSQL database** (`BEGIN; ... COMMIT;`).

---

## 2. Unified Script Execution Order

| Order | File | Purpose |
|---|---|---|
| **1** | `01_schema.sql` | **Single Authoritative DDL:** Creates all 16 tables (13 core tables + 3 group-encryption storage preparation tables), primary keys, foreign keys (`ON DELETE RESTRICT`), row-level `CHECK` constraints, and 32 indexes. |
| **2** | `02_seed.sql` | **Deterministic Seed Data:** Inserts 26 accounts (`PBKDF2-HMAC-SHA256` local test hashes), login history, app-open events, social graph (1-hop & 2-hop overlaps), directional blocks, direct & group messages (including encrypted storage fixtures and hidden messages), and spam reports; synchronizes identity sequences. |
| **3** | `03_admin_queries.sql` | **Admin Reference Queries:** Runnable SQL queries with editable `params` CTEs covering all 10 Administrator subsystem query patterns (`QUERY 1`..`QUERY 10`). |
| **4** | `04_verify.sql` | **Comprehensive Automated Verification:** Rollback-safe PL/pgSQL verification suite (`BEGIN; ... ROLLBACK;`) asserting all database-enforced constraints (`PASS [A1]`–`PASS [A9]`) and seed/query invariants (`PASS [B1]`–`PASS [B9]`). |
| **5** | `01_person1.sql` | **Reconciled User-Subsystem Reference Queries:** Read-only reference queries for the User subsystem (unified direct + group conversation list, visible message transcripts respecting `direct_message_hidden`/`group_message_hidden`, and block-filtered user search) adapted to `01_schema.sql`. Contains zero competing `CREATE TABLE` DDL statements. |
| **6** | `verify_person1.sql` | **Reconciled User-Subsystem Verification:** Rollback-safe PL/pgSQL verification block (`BEGIN; ... ROLLBACK;`) testing User-subsystem constraints and per-user message hiding isolation on top of `01_schema.sql` and `02_seed.sql`. |
| **7** | `check_person1.mjs` | **Optional Offline PGlite Runner:** Node.js script reconciled to load `01_schema.sql` + `02_seed.sql` + `verify_person1.sql` in PGlite (requires optional local `.tools` PGlite package). |

---

## 3. How to Execute (`psql`)

```powershell
createdb -h 127.0.0.1 -p 5432 -U postgres -E UTF8 chat_system_v1
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/01_schema.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/02_seed.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/03_admin_queries.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/04_verify.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/01_person1.sql
psql -h 127.0.0.1 -p 5432 -U postgres -d chat_system_v1 -v ON_ERROR_STOP=1 -f script/verify_person1.sql
```

---

## 4. Schema Reconciliation & Maintenance Notes

- **Reconciled User-Subsystem Table Mapping:**
  - `app_users` -> `users`
  - `friendships(user_low, user_high)` -> `friendships(user_low_id, user_high_id)`
  - `friend_requests.status` -> uppercase `'PENDING'`, `'ACCEPTED'`, `'REJECTED'`, `'CANCELLED'`
  - Direct conversations & messages -> `direct_messages` + `direct_message_hidden`
  - Group conversations, members & messages -> `chat_groups`, `group_members`, `group_messages`, `group_message_hidden`
  - Group encryption key envelopes -> `user_public_keys`, `group_key_versions`, `group_key_envelopes`
- **Restrictive Foreign Keys (`ON DELETE RESTRICT`):** All historical and relationship tables use `ON DELETE RESTRICT`. User account removal is modeled as soft deletion (`users.deleted_at = now()`).
- **Application-Maintained `updated_at`:** The Version 2 service layer (`JdbcTemplate`) is responsible for setting `updated_at = now()` whenever a `users` row is updated.
