# SQL Spark Connector for Databricks Runtime 17.3 LTS (Community Fork)

> Community-maintained fork of the Microsoft SQL Spark Connector updated for **Databricks Runtime 17.3 LTS**, **Apache Spark 4.0.0**, **Scala 2.13**, and **Java 17**.

---

## Overview

The original **Microsoft SQL Spark Connector** was built for earlier Apache Spark releases and has since been archived by Microsoft.

Modern Databricks runtimes introduced several internal Spark API changes that prevent the original connector from compiling or running without modifications.

This repository provides a community-maintained compatibility fork that has been updated to support:

- Databricks Runtime 17.3 LTS
- Apache Spark 4.0.0
- Scala 2.13
- Java 17
- Microsoft JDBC Driver 12.8.2

The goal is to help developers and organizations continue using the SQL Server Bulk Copy connector on modern Databricks runtimes.

---

# Why This Fork?

The original Microsoft repository is archived and no longer actively maintained.

Several Spark internal JDBC APIs changed between Spark 3.x and Spark 4.x, making the original connector incompatible with modern Databricks runtimes.

This project updates the connector to restore compatibility with Databricks Runtime 17.3 LTS while preserving the original bulk copy functionality.

---

# Original Microsoft Repository

This project is based on the original Microsoft SQL Spark Connector.

Reference:

https://github.com/microsoft/sql-spark-connector

All credit for the original connector implementation belongs to Microsoft.

This repository only contains compatibility updates required for newer Databricks runtimes.

---

# Supported Runtime

| Component | Version |
|-----------|---------|
| Databricks Runtime | 17.3 LTS |
| Apache Spark | 4.0.0 |
| Scala | 2.13.16 |
| Java | 17 |
| SQL Server JDBC Driver | 12.8.2.jre11 |

---

# Repository Structure

```
sql-spark-connector-dbx17.3LTS/
│
├── src/
├── project/
├── samples/
├── pom.xml
├── README.md
├── document.md
└── target/
```

---

# Prebuilt JAR

A prebuilt connector JAR is included in this repository.

```
17.3LTS_v4_spark-mssql-connector-1.4.0.jar
```

This JAR can be uploaded directly as a Databricks cluster library.

---

# Building

Requirements

- Java 17
- Maven 3.9+
- Scala 2.13

Build:

```bash
mvn clean package -DskipTests
```

Generated JAR:

```
target/spark-mssql-connector-1.4.0.jar
```

---

# Databricks Installation

1. Remove any previous SQL Spark Connector JARs from the cluster.

2. Upload

```
17.3LTS_v4_spark-mssql-connector-1.4.0.jar
```

3. Restart the cluster.

4. Use the connector normally.

---

# Example Usage

## Scala

```scala
df.write
 .format("com.microsoft.sqlserver.jdbc.spark")
 .option("url", jdbcUrl)
 .option("dbtable", "dbo.Employee")
 .option("user", username)
 .option("password", password)
 .mode("append")
 .save()
```

## Python

```python
df.write \
    .format("com.microsoft.sqlserver.jdbc.spark") \
    .option("url", jdbcUrl) \
    .option("dbtable", "dbo.Employee") \
    .option("user", username) \
    .option("password", password) \
    .mode("append") \
    .save()
```

---

# Major Changes

This fork includes compatibility updates for modern Databricks runtimes.

### Build Updates

- Spark upgraded to **4.0.0**
- Scala upgraded to **2.13.16**
- Java upgraded to **17**
- Updated Maven plugins
- Updated ScalaTest
- Updated Microsoft SQL Server JDBC Driver
- Dependency cleanup

---

### Spark 4 Compatibility

Updated connector implementation for Spark 4 JDBC API changes.

Including:

- JdbcUtils.getSchema
- JdbcUtils.schemaString

Updated connector logic to match the new Spark 4 signatures.

---

### Scala 2.13 Migration

Migrated deprecated Java/Scala collection APIs.

Updated:

```
scala.collection.JavaConversions
```

to

```
scala.jdk.javaapi.CollectionConverters
```

---

### Databricks Runtime Compatibility

Several Databricks Runtime binary incompatibilities were resolved.

Main improvements include:

- Removed dependency on Spark internal `JdbcOptionsInWrite`
- Replaced Spark internal JDBC helper methods
- Added connector-owned JDBC helper utilities
- Added direct Microsoft SQL Server JDBC connection creation for Bulk Copy
- Reduced dependency on Spark internal APIs that change between runtime releases

---

# Files Modified

| File | Description |
|------|-------------|
| pom.xml | Spark 4 / Scala 2.13 / Java 17 upgrade |
| SQLServerBulkJdbcOptions.scala | Removed Spark internal JdbcOptionsInWrite dependency |
| JdbcUtils.scala | Added connector-owned JDBC helper methods |
| BulkCopyUtils.scala | Updated Spark 4 JDBC APIs |
| Connector.scala | Updated table existence logic |
| SingleInstanceConnector.scala | Updated drop table logic |
| ReliableSingleInstanceStrategy.scala | Updated staging table cleanup |
| DataPoolConnector.scala | Updated connector options |
| DataSourceUtilsTest.java | Scala 2.13 migration |

---

# Validation

The connector was successfully built using:

```bash
mvn clean package
```

and

```bash
mvn clean test
```

Generated artifact:

```
target/spark-mssql-connector-1.4.0.jar
```

The generated connector JAR was validated on Databricks Runtime 17.3 LTS during development.

---

# Security

Security validation performed includes:

- No credentials embedded inside the generated JAR.
- Updated Microsoft JDBC Driver to 12.8.2.jre11.
- Connector JAR is a thin JAR.
- No Spark runtime libraries bundled.
- No Databricks libraries bundled.
- No passwords, tokens or JDBC URLs stored inside the artifact.
- Connector forwards credentials only through runtime connection properties.

Production recommendations:

- Use `encrypt=true`
- Prefer `trustServerCertificate=false`
- Store secrets in Databricks Secrets
- Do not hardcode credentials
- Keep Microsoft JDBC Driver updated

---

# Upgrade Notes

Detailed technical documentation is available in:

```
document.md
```

It contains:

- Build changes
- Spark 4 migration
- Scala 2.13 migration
- Databricks Runtime compatibility fixes
- SQLServerBulkCopy changes
- Security validation
- Dependency updates
- Deployment guidance

---

# Contributing

Community contributions are welcome.

If you validate this connector on newer Databricks runtimes or newer Spark releases, feel free to submit:

- Pull Requests
- Bug Reports
- Performance Improvements
- Documentation Updates

---

# Disclaimer

This project is an independent community-maintained compatibility fork.

It is **not affiliated with, maintained by, or officially supported by Microsoft or Databricks**.

Please validate the connector in your own environment before using it in production.

---

# Acknowledgements

Special thanks to the original Microsoft SQL Spark Connector team for creating the connector.

Original repository:

https://github.com/microsoft/sql-spark-connector

This fork exists solely to maintain compatibility with modern Databricks Runtime releases after the original project was archived.
