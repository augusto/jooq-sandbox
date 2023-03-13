import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Logging
import kotlin.math.max

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
    id("java")
    id("org.liquibase.gradle") version "2.0.4"
    id("nu.studer.jooq") version "8.1"
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = max(Runtime.getRuntime().availableProcessors() / 2, 1)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    val junitVersion = "5.9.2"

    implementation("org.postgresql:postgresql:42.5.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.jooq:jooq:3.17.7")
    implementation("org.jooq:jooq-postgres-extensions:3.17.7")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.assertj:assertj-core:3.24.2")

    liquibaseRuntime("org.postgresql:postgresql:42.5.1")
    liquibaseRuntime("org.liquibase:liquibase-core:4.4.3")
    liquibaseRuntime("org.yaml:snakeyaml:1.29")
    jooqGenerator("org.postgresql:postgresql:42.5.1")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "changeLogFile" to "src/main/resources/liquibase-root-changelog.xml",
            "url" to "jdbc:postgresql://localhost:5432/java",
            "username" to "postgres",
            "password" to "postgres",
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
                    url = "jdbc:postgresql://localhost:5432/java"
                    user = "postgres"
                    password = "postgres"
                }
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
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
                        isRecords = true
                        isPojos = true
                        isImmutablePojos = false
                        isDaos = true

                        // Can generate spring annotations for the DAOs
                        isSpringAnnotations = false

                        isFluentSetters = true
                        isPojosToString = true
                        isPojosAsJavaRecordClasses = true
                        isPojosEqualsAndHashCode = true
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
