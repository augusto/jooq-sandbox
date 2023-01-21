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
}

application {
    mainClass.set("com.example.jooq.MainKt")
}