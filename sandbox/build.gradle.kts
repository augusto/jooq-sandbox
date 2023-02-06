import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Logging

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

}

repositories {
    mavenCentral()
}
plugins {
    kotlin("jvm") version "1.8.0"
    id("org.liquibase.gradle") version "2.0.4"
    id("nu.studer.jooq") version "8.1"
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    val junitVersion = "5.9.2"
    val http4kVersion = "4.37.0.0"
    val kotlinVersion = "1.8.0"

    implementation("org.http4k:http4k-client-okhttp:${http4kVersion}")
    implementation("org.http4k:http4k-core:${http4kVersion}")
    implementation("org.http4k:http4k-format-moshi:${http4kVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jooq:jooq:3.17.7")
    implementation("org.jooq:jooq-kotlin:3.17.7")
    implementation("org.jooq:jooq-postgres-extensions:3.17.7")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("io.strikt:strikt-core:0.34.0")

    liquibaseRuntime("org.postgresql:postgresql:42.5.1")
    liquibaseRuntime("org.liquibase:liquibase-core:4.4.3")
    liquibaseRuntime("org.yaml:snakeyaml:1.29")
    jooqGenerator("org.postgresql:postgresql:42.5.1")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changeLogFile" to "src/main/resources/liquibase-root-changelog.xml",
            "url" to "jdbc:postgresql://localhost:5432/sandbox",
            "username" to "postgres",
            "password" to "postgres"
        )
    }
}

jooq {
    version.set("3.17.6")
    edition.set(JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/sandbox"
                    user = "postgres"
                    password = "postgres"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = ".*_[0-9]+" // Ignore partitioned tables
                        withIncludeSequences(true)
                        withIncludeSystemSequences(true)
                        recordVersionFields = "version"
//                        forcedTypes = listOf(
//                            ForcedType().apply {
//                                name = "varchar"
//                                includeExpression = ".*"
//                                includeTypes = "JSONB?"
//                            },
//                            ForcedType().apply {
//                                name = "varchar"
//                                includeExpression = ".*"
//                                includeTypes = "INET"
//                            }
//                        )
                    }
                    generate.apply {
                        // remove UpdatableRecords and mutable pojos/pokos
                        isRecords = false
                        isPojos = true
                        isImmutablePojos = true
                        isPojosAsKotlinDataClasses = true
                        // disable equals and hashcode generation (just as an example)
                        isPojosEqualsAndHashCode = false
                    }
                    target.apply {
                        packageName = "com.example.jooq.db"
                        directory = "build/generated/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.findByPath("generateJooq")!!
    .dependsOn("update")
