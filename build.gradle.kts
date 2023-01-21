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
plugins {
    kotlin("jvm") version "1.8.0"
    id("org.liquibase.gradle") version "2.0.4"
    application
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    val junitVersion="5.9.2"
    val http4kVersion="4.37.0.0"
    val kotlinVersion="1.8.0"

    implementation("org.http4k:http4k-client-okhttp:${http4kVersion}")
    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.http4k:http4k-format-moshi:${http4kVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")

    liquibaseRuntime("org.postgresql:postgresql:42.5.1")
    liquibaseRuntime("org.liquibase:liquibase-core:4.4.3")
    liquibaseRuntime("org.yaml:snakeyaml:1.29")
}

application {
    mainClass.set("com.example.jooq.MainKt")
}


task<Task>("startPostgres") {
    doLast {
        val pg = EmbeddedPostgres.builder()
            .setPort(5432)
            .start()
        println("Setting up database")
        val datasource = pg.postgresDatabase
        datasource.connection.use {conn->
            conn.createStatement().use {st->
                st.execute("CREATE DATABASE jooqliquibase")
            }
        }
    }
}
// Very unsafe
task<Exec>("stopPostgres") {
    commandLine("killall", "postgres")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changeLogFile" to "src/main/resources/liquibase-root-changelog.xml",
            "url" to "jdbc:postgresql://localhost:5432/jooqliquibase",
            "username" to "postgres",
            "password" to "postgres"
        )
    }
}