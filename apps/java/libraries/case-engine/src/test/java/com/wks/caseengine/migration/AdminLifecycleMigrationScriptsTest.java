package com.wks.caseengine.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AdminLifecycleMigrationScriptsTest {

	@Test
	void shouldApplyH2SchemaAndMigrationBackfillsIdempotently() throws Exception {
		try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:adminlifecycle;DB_CLOSE_DELAY=-1");
				Statement statement = connection.createStatement()) {
			executeSql(statement, resource("schema-h2.sql"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "EXTERNAL_PARTY_REF"));

			statement.executeUpdate(
					"INSERT INTO case_instance (business_key, status, stage, admin_state, resume_to_state) VALUES ('BK-1', 'WIP_CASE_STATUS', 'Open', 'Open', 'Open')");

			executeSql(statement, resource("migration-case-instance-admin-lifecycle-h2.sql"));
			executeSql(statement, resource("migration-case-instance-opened-backfill-h2.sql"));
			executeSql(statement, resource("migration-case-instance-opened-backfill-h2.sql"));
			executeSql(statement, resource("migration-case-instance-opened-stage-backfill-h2.sql"));
			executeSql(statement, resource("migration-case-instance-opened-stage-backfill-h2.sql"));

			try (ResultSet rs = statement.executeQuery(
					"SELECT admin_state, resume_to_state, stage FROM case_instance WHERE business_key = 'BK-1'")) {
				assertTrue(rs.next());
				assertEquals("Opened", rs.getString("admin_state"));
				assertEquals("Opened", rs.getString("resume_to_state"));
				assertEquals("Opening", rs.getString("stage"));
			}

			statement.executeUpdate(
					"INSERT INTO case_instance (business_key, status, stage, admin_state) VALUES ('BK-2', 'WIP_CASE_STATUS', 'Maintenance', 'Opened')");
			executeSql(statement, resource("migration-case-instance-opened-stage-backfill-h2.sql"));

			try (ResultSet rs = statement.executeQuery(
					"SELECT stage FROM case_instance WHERE business_key = 'BK-2'")) {
				assertTrue(rs.next());
				assertEquals("Opening", rs.getString("stage"));
			}
		}
	}

	@Test
	void shouldKeepPostgresqlAndH2MigrationStatementsAlignedForStep2A() throws Exception {
		String postgresqlMigration = resource("migration-case-instance-admin-lifecycle-postgresql.sql");
		String h2Migration = resource("migration-case-instance-admin-lifecycle-h2.sql");

		assertTrue(postgresqlMigration.contains("external_party_ref"));
		assertTrue(h2Migration.contains("external_party_ref"));
		assertTrue(postgresqlMigration.contains("opened_at"));
		assertTrue(h2Migration.contains("opened_at"));
		assertTrue(resource("migration-case-instance-opened-stage-backfill-postgresql.sql").contains("stage = 'Opening'"));
		assertTrue(resource("migration-case-instance-opened-stage-backfill-h2.sql").contains("stage = 'Opening'"));
	}

	@Test
	void shouldApplyAccountsFoundationMigrationIdempotently() throws Exception {
		try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:accountsfoundation;DB_CLOSE_DELAY=-1");
				Statement statement = connection.createStatement()) {
			statement.execute("CREATE TABLE case_instance (business_key VARCHAR(255) PRIMARY KEY)");

			executeSql(statement, resource("migration-case-instance-accounts-foundation-h2.sql"));
			executeSql(statement, resource("migration-case-instance-accounts-foundation-h2.sql"));

			assertTrue(hasColumn(statement, "CASE_INSTANCE", "MATTER_TYPE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "BILLING_PARTY_MODEL"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "BILLING_MODE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_PROFILE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "BILLING_SETUP_COMPLETE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "FLAT_FEE_AMOUNT"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "PAYMENT_METHOD_AUTHORIZED"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "PAYMENT_METHOD_REF"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "RETAINER_AMOUNT"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "RETAINER_FUNDS_RECEIVED"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "SUBSCRIPTION_PLAN_ID"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "SUBSCRIPTION_PLAN_NAME"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "SUBSCRIPTION_ACTIVE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "INSTRUCTING_FIRM_ID"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "INSTRUCTING_FIRM_NAME"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "COUNSEL_BILLING_MODE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "COUNSEL_BILLING_PARTY_OVERRIDE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_STAGE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_STATE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_HEALTH"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_HEALTH_REASON_CODES"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_HEALTH_EVALUATED_AT"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_STALE_SINCE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_MALFORMED_CASE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_READINESS_STATUS"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_READINESS_REASON_CODES"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_READINESS_EVALUATED_AT"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_READINESS_SUMMARY"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_QUEUE_ID"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_NEXT_ACTION_OWNER_TYPE"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_NEXT_ACTION_SUMMARY"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_NEXT_ACTION_DUE_AT"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_WORK_BLOCKED"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_WORK_PRIORITY"));
			assertTrue(hasColumn(statement, "CASE_INSTANCE", "ACCOUNTS_EVENTS"));
		}
	}

	@Test
	void shouldKeepPostgresqlAndH2AccountsFoundationMigrationsAligned() throws Exception {
		String postgresqlMigration = resource("migration-case-instance-accounts-foundation-postgresql.sql");
		String h2Migration = resource("migration-case-instance-accounts-foundation-h2.sql");

		assertTrue(postgresqlMigration.contains("matter_type"));
		assertTrue(h2Migration.contains("matter_type"));
		assertTrue(postgresqlMigration.contains("billing_party_model"));
		assertTrue(h2Migration.contains("billing_party_model"));
		assertTrue(postgresqlMigration.contains("billing_mode"));
		assertTrue(h2Migration.contains("billing_mode"));
		assertTrue(postgresqlMigration.contains("accounts_profile"));
		assertTrue(h2Migration.contains("accounts_profile"));
		assertTrue(postgresqlMigration.contains("billing_setup_complete"));
		assertTrue(h2Migration.contains("billing_setup_complete"));
		assertTrue(postgresqlMigration.contains("flat_fee_amount"));
		assertTrue(h2Migration.contains("flat_fee_amount"));
		assertTrue(postgresqlMigration.contains("payment_method_authorized"));
		assertTrue(h2Migration.contains("payment_method_authorized"));
		assertTrue(postgresqlMigration.contains("payment_method_ref"));
		assertTrue(h2Migration.contains("payment_method_ref"));
		assertTrue(postgresqlMigration.contains("retainer_amount"));
		assertTrue(h2Migration.contains("retainer_amount"));
		assertTrue(postgresqlMigration.contains("retainer_funds_received"));
		assertTrue(h2Migration.contains("retainer_funds_received"));
		assertTrue(postgresqlMigration.contains("subscription_plan_id"));
		assertTrue(h2Migration.contains("subscription_plan_id"));
		assertTrue(postgresqlMigration.contains("subscription_plan_name"));
		assertTrue(h2Migration.contains("subscription_plan_name"));
		assertTrue(postgresqlMigration.contains("subscription_active"));
		assertTrue(h2Migration.contains("subscription_active"));
		assertTrue(postgresqlMigration.contains("instructing_firm_id"));
		assertTrue(h2Migration.contains("instructing_firm_id"));
		assertTrue(postgresqlMigration.contains("instructing_firm_name"));
		assertTrue(h2Migration.contains("instructing_firm_name"));
		assertTrue(postgresqlMigration.contains("counsel_billing_mode"));
		assertTrue(h2Migration.contains("counsel_billing_mode"));
		assertTrue(postgresqlMigration.contains("counsel_billing_party_override"));
		assertTrue(h2Migration.contains("counsel_billing_party_override"));
		assertTrue(postgresqlMigration.contains("accounts_stage"));
		assertTrue(h2Migration.contains("accounts_stage"));
		assertTrue(postgresqlMigration.contains("accounts_state"));
		assertTrue(h2Migration.contains("accounts_state"));
		assertTrue(postgresqlMigration.contains("accounts_health"));
		assertTrue(h2Migration.contains("accounts_health"));
		assertTrue(postgresqlMigration.contains("accounts_health_reason_codes"));
		assertTrue(h2Migration.contains("accounts_health_reason_codes"));
		assertTrue(postgresqlMigration.contains("accounts_health_evaluated_at"));
		assertTrue(h2Migration.contains("accounts_health_evaluated_at"));
		assertTrue(postgresqlMigration.contains("accounts_stale_since"));
		assertTrue(h2Migration.contains("accounts_stale_since"));
		assertTrue(postgresqlMigration.contains("accounts_malformed_case"));
		assertTrue(h2Migration.contains("accounts_malformed_case"));
		assertTrue(postgresqlMigration.contains("accounts_readiness_status"));
		assertTrue(h2Migration.contains("accounts_readiness_status"));
		assertTrue(postgresqlMigration.contains("accounts_readiness_reason_codes"));
		assertTrue(h2Migration.contains("accounts_readiness_reason_codes"));
		assertTrue(postgresqlMigration.contains("accounts_readiness_evaluated_at"));
		assertTrue(h2Migration.contains("accounts_readiness_evaluated_at"));
		assertTrue(postgresqlMigration.contains("accounts_readiness_summary"));
		assertTrue(h2Migration.contains("accounts_readiness_summary"));
		assertTrue(postgresqlMigration.contains("accounts_queue_id"));
		assertTrue(h2Migration.contains("accounts_queue_id"));
		assertTrue(postgresqlMigration.contains("accounts_next_action_owner_type"));
		assertTrue(h2Migration.contains("accounts_next_action_owner_type"));
		assertTrue(postgresqlMigration.contains("accounts_next_action_summary"));
		assertTrue(h2Migration.contains("accounts_next_action_summary"));
		assertTrue(postgresqlMigration.contains("accounts_next_action_due_at"));
		assertTrue(h2Migration.contains("accounts_next_action_due_at"));
		assertTrue(postgresqlMigration.contains("accounts_work_blocked"));
		assertTrue(h2Migration.contains("accounts_work_blocked"));
		assertTrue(postgresqlMigration.contains("accounts_work_priority"));
		assertTrue(h2Migration.contains("accounts_work_priority"));
		assertTrue(postgresqlMigration.contains("accounts_events"));
		assertTrue(h2Migration.contains("accounts_events"));
	}

	private String resource(String path) throws IOException {
		return new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
	}

	private void executeSql(Statement statement, String sql) throws Exception {
		String normalizedSql = sql.lines().filter(line -> !line.strip().startsWith("--"))
				.collect(Collectors.joining("\n"));
		for (String rawStatement : normalizedSql.split(";")) {
			String candidate = rawStatement.strip();
			if (candidate.isBlank()) {
				continue;
			}
			statement.execute(candidate);
		}
	}

	private boolean hasColumn(Statement statement, String tableName, String columnName) throws Exception {
		try (ResultSet rs = statement.executeQuery(
				"SELECT COUNT(*) AS count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName
						+ "' AND COLUMN_NAME = '" + columnName + "'")) {
			assertTrue(rs.next());
			return rs.getInt("count") == 1;
		}
	}
}
