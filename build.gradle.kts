import java.lang.System.getenv

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
}

val gitSha = "git rev-parse --short HEAD".runCommand(project.rootDir) ?: "dev"
val refName = getenv("GITHUB_REF_NAME") ?: ""
val isTag = getenv("GITHUB_REF")?.startsWith("refs/tags/") == true

version = if (isTag) {
    refName.removePrefix("v")
} else {
    "0.0.0-SNAPSHOT+$gitSha"
}

fun String.runCommand(workingDir: File): String? = try {
    val proc = ProcessBuilder(*split(" ").toTypedArray()).directory(workingDir)
        .redirectErrorStream(true).start()
    proc.inputStream.bufferedReader().readText().trim().takeIf { proc.waitFor() == 0 }
} catch (_: Exception) {
    null
}

group = "de.dicecup"
version = "0.0.1-SNAPSHOT"
description = "Classlink"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

node {
    download = true
    version.set("22.18.0")
    workDir = file("${project.projectDir}/.gradle/nodejs")
    yarnWorkDir = file("${project.projectDir}/.gradle/yarn")
    nodeProjectDir = file("${project.projectDir}/classlink-frontend")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.11")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.scalar.maven:scalar:0.1.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("org.mapstruct:mapstruct:1.6.2")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("net.datafaker:datafaker:2.4.4")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    implementation("com.github.librepdf:openpdf:1.3.43")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")
    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<Test> {
    useJUnitPlatform()
    doFirst {
        val agentJar = configurations.testRuntimeClasspath.get().files
            .firstOrNull { it.name.startsWith("byte-buddy-agent") }
        if (agentJar != null) {
            jvmArgs("-javaagent:${agentJar.absolutePath}")
        }
    }
}

tasks.register("printVersion") {
    doLast { println("project.version=${project.version}") }
}


