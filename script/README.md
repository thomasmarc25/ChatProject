# PostgreSQL Database Scripts (`script/`)

This directory contains the SQL scripts for the **Chat System** shared PostgreSQL database (used by both the User Application and the Administrator Application).

---

## 1. Prerequisites

- **Database Engine:** PostgreSQL 15 or newer (tested against PostgreSQL 18).
- **Encoding & Time Zone:** `UTF-8` client/database encoding; application queries and calendar aggregations use time zone `Asia/Ho_Chi_Minh`.
- **Extensions:** None required. All constraints and case-insensitive uniqueness checks use standard built-in PostgreSQL expressions.
- **Target Database State:** `01_schema.sql` must be executed inside an **already-created, empty PostgreSQL database**.
  - It runs inside a single transaction (`BEGIN; ... COMMIT;`).
  - It intentionally omits `DROP DATABASE`, `DROP SCHEMA CASCADE`, `TRUNCATE`, and `CREATE TABLE IF NOT EXISTS` so that running it against a non-empty or incompatible schema fails fast without destroying existing data.

---

## 2. Script Execution Order

| Order | File | Purpose |
|---|---|---|
| **1** | `01_schema.sql` | Creates all 16 tables (13 core tables + 3 group-encryption storage preparation tables), primary keys, foreign keys (`ON DELETE RESTRICT`), row-level `CHECK` constraints, and indexes. |
| **2** | `02_seed.sql` | Inserts deterministic synthetic seed data (admin, active, locked, soft-deleted, and zero-activity/zero-friend accounts; social graph with 1-hop and 2-hop overlaps; blocks; multi-login single-launch and multi-launch events; direct & group messages including encrypted storage fixtures and hidden messages; and spam reports) and synchronizes identity sequences. |
| **3** | `03_admin_queries.sql` | Runnable reference SQL queries with editable `params` CTEs covering all 10 Administrator subsystem query patterns. |

---

## 3. How to Execute

1. Create a fresh, empty database (replace `<db_user>` and `<db_name>` with your local PostgreSQL role and target database name):

   ```bash
   createdb -U <db_user> -E UTF8 <db_name>
   ```

2. Run `01_schema.sql` with `ON_ERROR_STOP=1` enabled so execution halts immediately if any error occurs:

   ```bash
   psql -U <db_user> -d <db_name> -v ON_ERROR_STOP=1 -f script/01_schema.sql
   ```

3. Load deterministic seed data and run the administrative verification queries:

   ```bash
   psql -U <db_user> -d <db_name> -v ON_ERROR_STOP=1 -f script/02_seed.sql
   psql -U <db_user> -d <db_name> -v ON_ERROR_STOP=1 -f script/03_admin_queries.sql
   ```

---

## 4. Schema Maintenance Notes

- **Restrictive Foreign Keys (`ON DELETE RESTRICT`):** All historical and relationship tables use `ON DELETE RESTRICT`. User account removal is modeled as soft deletion (`users.deleted_at = now()`) so historical login logs, app-open events, messages, and spam reports are preserved.
- **Application-Maintained `updated_at`:** `users.updated_at` defaults to `now()` on `INSERT`, but PostgreSQL `DEFAULT` does not automatically update the column on subsequent `UPDATE` statements. The Version 2 service layer (`JdbcTemplate`) is responsible for setting `updated_at = now()` whenever a user profile row is updated.
- **Password Storage:** Password creation, reset, and update operations write a password hash to `users.password_hash`; plaintext passwords are never stored.
