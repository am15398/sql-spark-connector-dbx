package com.microsoft.sqlserver.jdbc.spark.utils

import com.microsoft.sqlserver.jdbc.spark.SQLServerBulkJdbcOptions
import com.microsoft.sqlserver.jdbc.SQLServerDriver

import java.sql.{Connection, DriverManager, SQLException}

object JdbcUtils {
  /**
   * Creates a JDBC connection using the input JDBC options.
   * @param options The options which are used to create the connection.
   * @return A JDBC connection.
   */
  def createConnection(options: SQLServerBulkJdbcOptions): Connection = {
    if (options.url != null && options.url.startsWith("jdbc:sqlserver")) {
      val conn = new SQLServerDriver().connect(options.url, options.asConnectionProperties)
      if (conn == null) {
        throw new SQLException(s"Microsoft SQL Server JDBC driver did not accept URL ${options.url}")
      }
      conn
    } else if (options.driverClass != null && options.driverClass.nonEmpty) {
      Class.forName(options.driverClass)
      DriverManager.getConnection(options.url, options.asConnectionProperties)
    } else {
      DriverManager.getConnection(options.url, options.asConnectionProperties)
    }
  }

  /**
   * Checks table existence without depending on Spark's internal JDBC option classes.
   */
  def tableExists(conn: Connection, table: String): Boolean = {
    val stmt = conn.createStatement()
    try {
      stmt.executeQuery(s"SELECT 1 FROM $table WHERE 1=0")
      true
    } catch {
      case _: SQLException => false
    } finally {
      stmt.close()
    }
  }

  /**
   * Drops a database table without depending on Spark's internal JDBC option classes.
   */
  def dropTable(conn: Connection, table: String): Unit = {
    val stmt = conn.createStatement()
    try {
      stmt.executeUpdate(s"DROP TABLE $table")
    } finally {
      stmt.close()
    }
  }
}
