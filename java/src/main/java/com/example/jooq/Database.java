package com.example.jooq;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class Database {
    private final static HikariDataSource datasource = initDataSource();


    public static Settings defaultSettings() {
        return new Settings()
                .withUpdateRecordVersion(true) // Defaults to true
                .withUpdateRecordTimestamp(true) // Defaults to true
                .withExecuteWithOptimisticLocking(true) // Defaults to false
                .withExecuteWithOptimisticLockingExcludeUnversioned(false); // Defaults to false
    }

    private static HikariDataSource initDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/java");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(5));

        return new HikariDataSource(config);
    }

    public static void withJooq(Consumer<DSLContext> fn) {
        withJooq(fn, defaultSettings());
    }

    public static void withJooq(Consumer<DSLContext> fn, Settings settings) {
        try (var conn = datasource.getConnection()) {
            var dsl = DSL.using(conn, SQLDialect.POSTGRES, settings);
            dsl.transaction((tx) -> fn.accept(tx.dsl()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> R withJooq(Function<DSLContext, R> fn) {
        return withJooq(fn, defaultSettings());
    }

    public static <R> R withJooq(Function<DSLContext, R> fn, Settings settings) {
        try (var conn = datasource.getConnection()) {
            var dsl = DSL.using(conn, SQLDialect.POSTGRES, settings);
            return dsl.transactionResult((tx) -> fn.apply(tx.dsl()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
