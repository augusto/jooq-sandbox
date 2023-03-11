import io.zonky.test.db.postgres.embedded.EmbeddedPostgres

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("io.zonky.test:embedded-postgres:2.0.1")
    }
}


repositories {
    mavenCentral()
}


task<Task>("startPostgres") {
    doLast {
        val pg = EmbeddedPostgres.builder()
            .setPort(5432)
            .start()
        println("Setting up database")
        val datasource = pg.postgresDatabase
        datasource.connection.use { conn ->
            conn.createStatement().use { st ->
                st.execute("CREATE DATABASE record")
                st.execute("CREATE DATABASE immutable")
                st.execute("CREATE DATABASE sandbox")
            }
        }
    }
}
// Very unsafe
task<Exec>("stopPostgres") {
    commandLine("killall", "postgres")
    isIgnoreExitValue = true
}

task<Task>("generateJooq") {
    dependsOn("immutable:update")
    dependsOn("immutable:generateJooq")
    dependsOn("record:update")
    dependsOn("record:generateJooq")
    dependsOn("sandbox:update")
    dependsOn("sandbox:generateJooq")
}
