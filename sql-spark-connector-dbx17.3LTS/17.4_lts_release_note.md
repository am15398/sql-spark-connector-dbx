# SQL Spark Connector Upgrade Notes for Databricks 17.3 LTS

## Purpose

This document records the changes made to upgrade the SQL Spark Connector from the Databricks 14.x/Spark 3.x line to Databricks Runtime 17.3 LTS with Spark 4.0.0 and Scala 2.13.

The final jar produced by the Maven build is:

```text
target/spark-mssql-connector-1.4.0.jar
```

For deployment, this jar was renamed for the project use case as:

```text
17.3LTS_v3_spark-mssql-connector-1.4.0.jar
```

## Target Runtime

| Component | Upgraded Value |
| --- | --- |
| Databricks Runtime | 17.3 LTS |
| Apache Spark | 4.0.0 |
| Scala binary version | 2.13 |
| Scala version | 2.13.16 |
| Java release target | 17 |
| SQL Server JDBC driver | 12.8.2.jre11 |

## Build Changes

Updated `pom.xml` to make the default Maven profile target Spark 4 and Scala 2.13.

Before:

```xml
<id>spark34</id>
<scala.binary.version>2.12</scala.binary.version>
<scala.version>2.12.11</scala.version>
<spark.version>3.4.0</spark.version>
```

After:

```xml
<id>spark40</id>
<scala.binary.version>2.13</scala.binary.version>
<scala.version>2.13.16</scala.version>
<spark.version>4.0.0</spark.version>
```

Other build updates:

- Set Java build target to release 17.
- Updated `scala-maven-plugin` to `4.9.2`.
- Updated `maven-compiler-plugin` to `3.13.0`.
- Updated `scalatest-maven-plugin` to `2.2.0`.
- Updated ScalaTest to `3.2.19`.
- Updated Microsoft JDBC driver to `mssql-jdbc:12.8.2.jre11`.
- Removed duplicate Maven javadoc execution because the Scala plugin already creates the javadoc classifier jar.

## Spark 4 API Changes

Updated `BulkCopyUtils.scala` for Spark 4 JDBC helper signatures.

Spark 4 changed the internal JDBC helper signatures for:

- `JdbcUtils.getSchema`
- `JdbcUtils.schemaString`

The connector now passes:

- JDBC connection
- JDBC dialect
- timestamp NTZ preference
- create table column type options

This keeps schema validation and SQL Server table creation compatible with Spark 4.0.0.

## Scala 2.13 Compatibility

Scala 2.13 removed old Java/Scala collection conversion APIs.

Updated test code from:

```java
scala.collection.JavaConversions
```

to:

```java
scala.jdk.javaapi.CollectionConverters
```

## Databricks Runtime Compatibility Fixes

### 1. Removed dependency on `JdbcOptionsInWrite`

Initial DBR 17.3 run failed with:

```text
java.lang.NoSuchMethodError:
org.apache.spark.sql.execution.datasources.jdbc.JdbcOptionsInWrite.<init>(CaseInsensitiveMap)
```

Reason:

The connector compiled against open-source Spark 4.0.0, but Databricks Runtime 17.3 has a binary-incompatible internal Spark JDBC class.

Fix:

`SQLServerBulkJdbcOptions` no longer extends Spark's internal `JdbcOptionsInWrite`.

It now owns connector option parsing locally while preserving:

- case-insensitive option lookup
- JDBC URL
- table name
- user/password
- batch size
- truncate option
- isolation level
- `mssqlIsolationLevel`
- table lock
- reliability level
- data pool options
- schema check options
- SQL Server connection properties

This removes the direct dependency on the unstable Spark internal constructor.

### 2. Replaced Spark internal table helpers

The connector previously used Spark internal JDBC helpers for table existence and drop-table logic.

Those calls were replaced with local helpers in:

```text
src/main/scala/com/microsoft/sqlserver/jdbc/spark/utils/JdbcUtils.scala
```

Local helpers now provide:

- `createConnection`
- `tableExists`
- `dropTable`

This reduces dependency on Spark internal classes that can differ between open-source Spark and Databricks Runtime.

### 3. Forced Microsoft SQL Server driver connection for bulk copy

Second DBR 17.3 run failed with:

```text
com.microsoft.sqlserver.jdbc.SQLServerException:
Destination connection must be a connection from the Microsoft JDBC Driver for SQL Server.
```

Reason:

`SQLServerBulkCopy` requires a connection created by the Microsoft SQL Server JDBC driver. On Databricks, `DriverManager` can return a wrapped or non-Microsoft driver connection.

Fix:

For `jdbc:sqlserver` URLs, the connector now creates the connection directly using:

```scala
new SQLServerDriver().connect(options.url, options.asConnectionProperties)
```

This guarantees the connection is compatible with:

```scala
new SQLServerBulkCopy(conn)
```

## Main Files Changed

| File | Change |
| --- | --- |
| `pom.xml` | Upgraded Maven build to Spark 4.0.0, Scala 2.13.16, Java 17, newer plugins and JDBC driver. |
| `SQLServerBulkJdbcOptions.scala` | Replaced inheritance from Spark internal `JdbcOptionsInWrite` with connector-owned option parsing. |
| `JdbcUtils.scala` | Added local connection, table-exists, and drop-table helpers. SQL Server URLs now use `SQLServerDriver` directly. |
| `BulkCopyUtils.scala` | Updated Spark 4 JDBC schema helper calls. |
| `Connector.scala` | Switched table existence checks to local helper. |
| `SingleInstanceConnector.scala` | Switched drop-table handling to local helper. |
| `ReliableSingleInstanceStrategy.scala` | Switched staging table cleanup to local helper. |
| `DataPoolConnector.scala` | Updated drop-table signature to use connector-owned options. |
| `DataSourceUtilsTest.java` | Updated Java/Scala collection conversion for Scala 2.13. |

## Validation Performed

The following commands were run successfully:

```bash
mvn -q clean test
mvn -q -DskipTests package
```

Result:

- Main source compiles against Spark 4.0.0.
- Test suite passes.
- Package build succeeds.
- Final jar created at `target/spark-mssql-connector-1.4.0.jar`.

## Deployment Note

When deploying to Databricks Runtime 17.3 LTS:

1. Remove older connector jars from the cluster.
2. Upload/install the renamed jar:

   ```text
   17.3LTS_v3_spark-mssql-connector-1.4.0.jar
   ```

3. Restart the cluster so executors do not keep stale classes loaded.
4. Ensure the Microsoft SQL Server JDBC driver is available. The build uses:

   ```text
   com.microsoft.sqlserver:mssql-jdbc:12.8.2.jre11
   ```

## Summary

The upgrade mainly handled three compatibility areas:

- Spark 4 / Scala 2.13 build changes.
- Databricks Runtime 17.3 binary differences in Spark internal JDBC classes.
- SQL Server bulk copy requiring a direct Microsoft JDBC driver connection.

After these changes, the connector works on Databricks Runtime 17.3 LTS with Spark 4.0.0 and Scala 2.13.

## Security Validation Notes

Security validation was done for the connector changes and generated jar.

Validation commands used:

```bash
mvn dependency:tree -Dscope=runtime
mvn -DskipTests package
jar tf target/spark-mssql-connector-1.4.0.jar
strings target/spark-mssql-connector-1.4.0.jar | rg -i "password=|accessToken=|secret=|jdbc:sqlserver://|trustServerCertificate=true"
```

### Connector Jar Contents

The built connector jar is a thin jar. It contains connector classes and Maven metadata only.

It does not bundle:

- Databricks runtime libraries
- Spark runtime libraries
- Microsoft SQL Server JDBC driver classes
- credentials
- JDBC URLs
- access tokens
- passwords

The secret scan against the built jar did not find embedded credentials or connection strings.

### Security Impact of Code Changes

No new credential logging was added.

The connector still receives credentials and tokens only from Spark write options at runtime. The code passes those values to the SQL Server JDBC driver through connection properties and does not print them.

The change from `DriverManager.getConnection(...)` to:

```scala
new SQLServerDriver().connect(options.url, options.asConnectionProperties)
```

does not weaken authentication or TLS by itself. It only forces the Microsoft SQL Server JDBC driver to create the connection so `SQLServerBulkCopy` accepts it on Databricks Runtime 17.3.

TLS/security behavior is still controlled by the runtime JDBC options, such as:

- `encrypt`
- `trustServerCertificate`
- `hostNameInCertificate`
- `serverCertificate`
- authentication/access token options

For production, avoid weakening TLS settings unless explicitly required. In particular:

- Prefer `encrypt=true`.
- Prefer `trustServerCertificate=false`.
- Use `hostNameInCertificate` or `serverCertificate` when certificate validation requires it.
- Do not hardcode passwords or access tokens in notebooks, job parameters, or jar files.

### SQL Option Trust Boundary

The connector still builds SQL statements using table names provided in options such as `dbtable`.

This behavior already existed in the connector and is typical for Spark JDBC connectors, but it means the following options should be treated as trusted configuration, not raw end-user input:

- `dbtable`
- `dataPoolDataSource`
- `dataPoolDistPolicy`
- `createTableOptions`
- `createTableColumnTypes`

Do not pass unvalidated external user input into these options.

### Dependency Security Note

Initial runtime dependency check showed:

```text
com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11
```

Public advisory data lists `mssql-jdbc:12.8.1.jre11` as affected by CVE-2025-59250, an improper input validation/spoofing issue in the Microsoft JDBC Driver for SQL Server. OSV lists the fixed version for the 12.8 line as:

```text
12.8.2.jre11
```

Microsoft's JDBC driver release notes also show newer driver releases are available, including 13.4.0 as of March 13, 2026.

Recommendation:

- For lowest-risk security posture, use `mssql-jdbc:12.8.2.jre11` or a newer compatible Microsoft JDBC driver on the Databricks cluster.
- If the cluster provides its own SQL Server JDBC driver, confirm the actual installed driver version in the Databricks runtime/library list.
- Re-run functional write tests after changing the JDBC driver version because the connector relies on `SQLServerBulkCopy`.

Project action taken:

- Updated `pom.xml` from `mssql-jdbc:12.8.1.jre11` to `mssql-jdbc:12.8.2.jre11`.
- Rebuilt and retested the connector after the dependency update.

References:

- Microsoft JDBC Driver release notes: https://learn.microsoft.com/en-us/sql/connect/jdbc/release-notes-for-the-jdbc-driver
- NVD CVE-2025-59250: https://nvd.nist.gov/vuln/detail/CVE-2025-59250
- OSV GHSA-m494-w24q-6f7w: https://osv.dev/vulnerability/GHSA-m494-w24q-6f7w

### Spark Runtime Security Note

The connector jar does not bundle Spark. Spark is provided by Databricks Runtime 17.3 LTS.

Public advisory data lists CVE-2025-54920 for Apache Spark versions before 4.0.1. Because Databricks controls the Spark runtime in DBR 17.3 LTS, remediation for Spark runtime CVEs should follow Databricks runtime patch guidance rather than connector jar changes.

Reference:

- NVD CVE-2025-54920: https://nvd.nist.gov/vuln/detail/CVE-2025-54920

### Security Conclusion

No new direct security issue was identified from the connector code changes or the generated connector jar based on the checks performed above.

This is not a full application penetration test or a complete Databricks workspace security assessment. Remaining security posture still depends on:

- cluster library versions actually installed in Databricks;
- cluster policies and secret management;
- network controls between Databricks and SQL Server;
- TLS options used at runtime;
- validation of SQL-related options such as `dbtable`.

The main security action items are:

- use `mssql-jdbc:12.8.2.jre11` or newer;
- keep Databricks Runtime patched according to Databricks guidance;
- keep SQL table/options values trusted and validated;
- keep credentials in Databricks secrets or secure job configuration, not in code or jar files.
