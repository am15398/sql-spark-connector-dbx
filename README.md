# Apache Spark SQL Server Connector for Databricks Runtime 17.3 LTS

> Community-maintained compatibility fork of the archived Microsoft SQL Spark Connector, updated for **Databricks Runtime 17.3 LTS**, **Apache Spark 4.0.0**, **Scala 2.13**, and **Java 17**.

![Databricks](https://img.shields.io/badge/Databricks-17.3_LTS-red)
![Spark](https://img.shields.io/badge/Apache_Spark-4.0.0-orange)
![Scala](https://img.shields.io/badge/Scala-2.13-blue)
![Java](https://img.shields.io/badge/Java-17-green)
![License](https://img.shields.io/badge/License-Apache_2.0-blue)

---

# Overview

The original **Microsoft SQL Spark Connector** has been archived and is no longer actively maintained.

Recent Databricks Runtime releases introduced Spark internal API changes that make the original connector incompatible with Spark 4.x.

This repository provides a community-maintained compatibility fork that restores support for modern Databricks runtimes while preserving the SQL Server Bulk Copy functionality.

---

# Features

- ✅ Databricks Runtime 17.3 LTS
- ✅ Apache Spark 4.0.0
- ✅ Scala 2.13.16
- ✅ Java 17
- ✅ Microsoft JDBC Driver 12.8.2
- ✅ SQL Server Bulk Copy Support
- ✅ Spark 4 Compatible
- ✅ Community Maintained

---

# Supported Versions

| Component | Version |
|-----------|---------|
| Databricks Runtime | 17.3 LTS |
| Apache Spark | 4.0.0 |
| Scala | 2.13.16 |
| Java | 17 |
| Microsoft JDBC Driver | 12.8.2.jre11 |

---

# Why This Fork?

The original connector was designed for older Spark versions.

Since Spark 4.0 introduced multiple JDBC API changes, the archived connector no longer builds or executes successfully on Databricks Runtime 17.3 LTS.

This fork updates the connector while preserving its original design and functionality.

Major compatibility updates include:

- Spark 4 JDBC API migration
- Scala 2.13 migration
- Java 17 compatibility
- Removal of deprecated Spark internal APIs
- Databricks Runtime 17.3 compatibility fixes
- Dependency updates

---

# Original Microsoft Repository

This project is based on the archived Microsoft SQL Spark Connector.

https://github.com/microsoft/sql-spark-connector

All credit for the original implementation belongs to Microsoft.

This repository only contains compatibility updates required for newer Databricks Runtime releases.

---

# Repository Structure

```
sql-spark-connector-dbx17.3LTS
│
├── src/
├── project/
├── samples/
├── test/
├── pom.xml
├── document.md
├── README.md
└── target/
```

---

# Build Requirements

- Java 17
- Maven 3.9+
- Scala 2.13

---

# Build

```bash
mvn clean package -DskipTests
```

Generated artifact:

```
target/spark-mssql-connector-1.4.0.jar
```

---

# Databricks Installation

Upload the generated connector JAR as a cluster library.

Restart the cluster after installation.

Example library:

```
17.3LTS_v4_spark-mssql-connector-1.4.0.jar
```

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

## Build & Dependency Updates

- Apache Spark 4.0.0
- Scala 2.13.16
- Java 17
- Updated Maven plugins
- Updated Microsoft JDBC Driver
- Updated ScalaTest dependencies
- General dependency cleanup

---

## Spark 4 Compatibility

Updated the connector to support Spark 4 JDBC API changes.

Examples include:

- JdbcUtils.getSchema()
- JdbcUtils.schemaString()

Connector implementations were updated to match the Spark 4 method signatures.

---

## Scala 2.13 Migration

Migrated deprecated collection APIs.

From

```scala
scala.collection.JavaConversions
```

To

```scala
scala.jdk.javaapi.CollectionConverters
```

---

## Databricks Runtime Compatibility

Resolved multiple binary incompatibilities introduced in recent Databricks Runtime releases.

Changes include:

- Removed dependency on Spark internal JdbcOptionsInWrite
- Added connector-owned JDBC helper utilities
- Updated Bulk Copy implementation
- Updated table existence logic
- Updated staging table cleanup
- Reduced dependency on Spark internal APIs

---

# Files Updated

| File | Purpose |
|------|---------|
| pom.xml | Spark 4 / Scala 2.13 / Java 17 |
| SQLServerBulkJdbcOptions.scala | Removed internal Spark dependency |
| JdbcUtils.scala | Added connector JDBC helpers |
| BulkCopyUtils.scala | Spark 4 migration |
| Connector.scala | Updated table existence logic |
| SingleInstanceConnector.scala | Updated drop table logic |
| ReliableSingleInstanceStrategy.scala | Updated cleanup |
| DataPoolConnector.scala | Connector option updates |
| DataSourceUtilsTest.java | Scala 2.13 migration |

---

# Validation

The connector has been built using:

```bash
mvn clean package
```

and

```bash
mvn test
```

Generated artifact:

```
target/spark-mssql-connector-1.4.0.jar
```

---

# Security Notes

- No credentials embedded in the JAR
- Thin JAR (Spark libraries not bundled)
- Updated Microsoft JDBC Driver
- Compatible with Databricks Secrets
- Supports encrypted SQL Server connections

Production recommendations:

- encrypt=true
- trustServerCertificate=false
- Store credentials using Databricks Secrets

---

# Documentation

Additional implementation details are available in:

```
document.md
```

Topics include:

- Spark 4 migration
- Scala 2.13 migration
- Databricks compatibility fixes
- Dependency updates
- Build process
- Deployment guidance

---

# Contributing

Contributions are welcome.

Feel free to submit:

- Pull Requests
- Bug Reports
- Performance Improvements
- Documentation Enhancements

---

# Disclaimer

This is an independent community-maintained compatibility fork.

It is **not affiliated with, endorsed by, or supported by Microsoft or Databricks**.

Always validate the connector in your own environment before deploying to production.

---

# Acknowledgements

Thanks to the Microsoft SQL Spark Connector team for the original implementation.

Original repository:

https://github.com/microsoft/sql-spark-connector
