## Case Instance Admin Lifecycle JPA Migration

Use these artifacts when running the admin-lifecycle slice against an existing JPA database:

- Fresh H2 schema: `schema-h2.sql`
- Fresh PostgreSQL schema: `schema-postgresql.sql`
- Incremental H2 patch: `migration-case-instance-admin-lifecycle-h2.sql`
- Incremental PostgreSQL patch: `migration-case-instance-admin-lifecycle-postgresql.sql`
- Incremental `Open` -> `Opened` H2 backfill: `migration-case-instance-opened-backfill-h2.sql`
- Incremental `Open` -> `Opened` PostgreSQL backfill: `migration-case-instance-opened-backfill-postgresql.sql`
- Mongo compatibility backfill: `migration-case-instance-opened-backfill-mongo.js`

Expectation:

- Fresh databases use the updated base schema files.
- Existing databases must run the matching incremental patch before the admin-lifecycle fields are enabled.
- Existing databases with Step 1 data must also run the `Open` -> `Opened` backfill that matches their storage path.
- These scripts are the current migration evidence because this repo does not yet ship Flyway or Liquibase.
