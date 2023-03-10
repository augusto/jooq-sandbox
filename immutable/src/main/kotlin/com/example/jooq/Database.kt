package com.example.jooq

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.util.concurrent.TimeUnit


object Database {
    private val settings = defaultSettings()

    private val datasource = let {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/immutable"
        config.username = "postgres"
        config.password = "postgres"
        config.maximumPoolSize = 2
        config.minimumIdle = 1
        config.maxLifetime = TimeUnit.MINUTES.toMillis(5)

        HikariDataSource(config)
    }

    fun <T> withJooq(block: (DSLContext) -> T): T {
        datasource.connection.use { conn ->
            val dsl = DSL.using(conn, SQLDialect.POSTGRES, settings)

            return dsl.transactionResult { tx:Configuration ->
                block(tx.dsl())
            }
        }
    }

    fun <T> withJooq(customSettings: Settings, block: (DSLContext) -> T): T {
        datasource.connection.use { conn ->
            val dsl = DSL.using(conn, SQLDialect.POSTGRES, customSettings)

            return dsl.transactionResult { tx:Configuration ->
                block(tx.dsl())
            }
        }
    }

    fun defaultSettings(): Settings {
        // Requiered to enable optimistic locking as it's disabled by default
        return Settings()
            .withUpdateRecordVersion(true) // Defaults to true
            .withUpdateRecordTimestamp(true) // Defaults to true
            .withExecuteWithOptimisticLocking(true) // Defaults to false
            .withExecuteWithOptimisticLockingExcludeUnversioned(false) // Defaults to false
    }
}