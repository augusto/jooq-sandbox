package com.example.jooq.app

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.TransactionalRunnable
import org.jooq.impl.DSL
import java.util.concurrent.TimeUnit


class Database {
    private val datasource = let {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/jooqliquibase"
        config.username = "postgres"
        config.password = "postgres"
        config.maximumPoolSize = 2
        config.minimumIdle = 1
        config.maxLifetime = TimeUnit.MINUTES.toMillis(5)

        HikariDataSource(config)
    }

    fun <T> withJooq(block: (DSLContext) -> T): T {
        datasource.connection.use { conn ->
            val dsl = DSL.using(conn, SQLDialect.POSTGRES)

            return dsl.transactionResult { tx:Configuration ->
                block(tx.dsl())
            }
        }
    }
}