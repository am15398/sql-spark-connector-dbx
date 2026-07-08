/**
* Copyright 2020 and onwards Microsoft Corporation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.microsoft.sqlserver.jdbc.spark

import java.sql.Connection
import java.util.{Locale, Properties}

import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap

class SQLServerBulkJdbcOptions(inputParams: Map[String, String]) extends Serializable {

  // Save original parameters for when a JdbcBulkOptions instance is passed
  // from the Spark driver to an executor, which loses the reference to the
  // params input in memory
  val params: CaseInsensitiveMap[String] = CaseInsensitiveMap(inputParams)
  val parameters: Map[String, String] = inputParams.map {
    case (key, value) => key.toLowerCase(Locale.ROOT) -> value
  }

  private def get(key: String): Option[String] = parameters.get(key.toLowerCase(Locale.ROOT))
  private def getOrElse(key: String, defaultValue: String): String = get(key).getOrElse(defaultValue)
  private def getBoolean(key: String, defaultValue: Boolean): Boolean =
    get(key).map(_.toBoolean).getOrElse(defaultValue)
  private def getInt(key: String, defaultValue: Int): Int =
    get(key).map(_.toInt).getOrElse(defaultValue)

  val url: String = getOrElse("url", null)
  val dbtable: String = getOrElse("dbtable", null)

  val user: String = getOrElse("user", null)
  val password: String = getOrElse("password", null)
  val driverClass: String = get("driver")
    .orElse(get("driverClass"))
    .orElse(Option(url).filter(_.startsWith("jdbc:sqlserver")).map(_ =>
      "com.microsoft.sqlserver.jdbc.SQLServerDriver"))
    .orNull
  val lowerBound: Option[String] = get("lowerBound")
  val upperBound: Option[String] = get("upperBound")
  val numPartitions: Option[Int] = get("numPartitions").map(_.toInt)
  val queryTimeout: Int = getInt("queryTimeout", 0)
  val fetchSize: Int = getInt("fetchSize", 0)
  val isTruncate: Boolean = getBoolean("truncate", false)
  val createTableOptions: String = getOrElse("createTableOptions", "")
  val createTableColumnTypes: Option[String] = get("createTableColumnTypes")
  val customSchema: Option[String] = get("customSchema")
  val batchSize: Int = {
    val size = getInt("batchsize", 1000)
    require(size >= 1, s"Invalid value `$size` for parameter `batchsize`. The minimum value is 1.")
    size
  }
  val sessionInitStatement: Option[String] = get("sessionInitStatement")
  val pushDownPredicate: Boolean = getBoolean("pushDownPredicate", true)
  val preferTimestampNTZ: Boolean = getBoolean("preferTimestampNTZ", false)

  // If no value is provided, then we write to a single SQL Server instance.
  // A non-empty value indicates the name of a data source whose location is
  // the data pool that the user wants to write to. This data source will
  // contain the user's external table.
  val dataPoolDataSource: String = getOrElse("dataPoolDataSource", null)

  // In the standard Spark JDBC implementation, the default isolation level is
  // "READ_UNCOMMITTED," but for SQL Server, the default is "READ_COMMITTED"
  val isolationLevel: Int =
    get("mssqlIsolationLevel").orElse(get("isolationLevel")).getOrElse("READ_COMMITTED") match {
      case "NONE"             => Connection.TRANSACTION_NONE
      case "READ_UNCOMMITTED" => Connection.TRANSACTION_READ_UNCOMMITTED
      case "READ_COMMITTED"   => Connection.TRANSACTION_READ_COMMITTED
      case "REPEATABLE_READ"  => Connection.TRANSACTION_REPEATABLE_READ
      case "SERIALIZABLE"     => Connection.TRANSACTION_SERIALIZABLE
      case "SNAPSHOT"         => Connection.TRANSACTION_READ_COMMITTED + 4094
    }

  val reliabilityLevel: Int =
    getOrElse("reliabilityLevel", "BEST_EFFORT") match {
      case "BEST_EFFORT"   => SQLServerBulkJdbcOptions.BEST_EFFORT
      case "NO_DUPLICATES" => SQLServerBulkJdbcOptions.NO_DUPLICATES
    }

  val checkConstraints = getBoolean("checkConstraints", false)
  val fireTriggers = getBoolean("fireTriggers", false)
  val keepIdentity = getBoolean("keepIdentity", false)
  val keepNulls = getBoolean("keepNulls", false)
  val tableLock = getBoolean("tableLock", false)
  val allowEncryptedValueModifications =
    getBoolean("allowEncryptedValueModifications", false)


  val schemaCheckEnabled =
    getBoolean("schemaCheckEnabled", true)

  // Not a feature
  // Only used for internally testing data idempotency
  val testDataIdempotency =
    getBoolean("testDataIdempotency", false)

  val dataPoolDistPolicy = getOrElse("dataPoolDistPolicy", "ROUND_ROBIN")

  def asConnectionProperties: Properties = {
    val properties = new Properties()
    parameters.foreach {
      case (key, value) if !SQLServerBulkJdbcOptions.connectionPropertyExclusions.contains(key) =>
        properties.setProperty(SQLServerBulkJdbcOptions.canonicalConnectionProperty(key), value)
      case _ =>
    }
    properties
  }
}

object SQLServerBulkJdbcOptions {
  val BEST_EFFORT = 0
  val NO_DUPLICATES = 1

  private val connectionPropertyExclusions = Set(
    "allowencryptedvaluemodifications",
    "batchsize",
    "cascadetruncate",
    "checkconstraints",
    "createtablecolumntypes",
    "createtableoptions",
    "customschema",
    "datapooldatasource",
    "datapooldistpolicy",
    "dbtable",
    "driver",
    "driverclass",
    "fetchsize",
    "firetriggers",
    "hint",
    "isolationlevel",
    "keepidentity",
    "keepnulls",
    "keytab",
    "lowerbound",
    "mssqlisolationlevel",
    "numpartitions",
    "partitioncolumn",
    "preferTimestampNTZ".toLowerCase(Locale.ROOT),
    "preparequery",
    "principal",
    "pushdownaggregate",
    "pushdownlimit",
    "pushdownoffset",
    "pushdownpredicate",
    "pushdowntablesample",
    "query",
    "querytimeout",
    "reliabilitylevel",
    "schemacheckenabled",
    "sessioninitstatement",
    "tablelock",
    "testdataidempotency",
    "truncate",
    "upperbound",
    "url"
  )

  private val canonicalConnectionProperties = Map(
    "accesstoken" -> "accessToken",
    "applicationname" -> "applicationName",
    "databasename" -> "databaseName",
    "hostnameincertificate" -> "hostNameInCertificate",
    "servercertificate" -> "serverCertificate",
    "trustservercertificate" -> "trustServerCertificate"
  )

  private def canonicalConnectionProperty(key: String): String =
    canonicalConnectionProperties.getOrElse(key, key)
}
