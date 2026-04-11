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

			try (ResultSet rs = statement.executeQuery(
					"SELECT admin_state, resume_to_state FROM case_instance WHERE business_key = 'BK-1'")) {
				assertTrue(rs.next());
				assertEquals("Opened", rs.getString("admin_state"));
				assertEquals("Opened", rs.getString("resume_to_state"));
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
