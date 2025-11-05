import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "2.1.20"

    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.plugin.spring") version "2.1.20"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.1.20"
    id("com.google.protobuf") version "0.9.4"
}

group = "ru.home.project"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.google.protobuf:protobuf-java-util:4.32.1")
    testImplementation("io.grpc:grpc-testing:1.71.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-agent-jvm:1.13.8") // For static mocking

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
//    testImplementation("com.github.tomakehurst:wiremock:3.0.0")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation ("org.projectlombok:lombok")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.apache.commons:commons-lang3")
    implementation("ru.tinkoff.piapi:java-sdk-spring-boot-starter:1.40")
    // Для поддержки стратегий
    implementation("ru.tinkoff.piapi:java-sdk-strategy:1.40")
    implementation("com.mysql:mysql-connector-j:9.4.0") {
        exclude("com.google.protobuf", "protobuf-java")
    }
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation ("com.google.protobuf:protobuf-java:4.32.1")

    implementation ("javax.annotation:javax.annotation-api:1.3.2")
    implementation ("io.github.wuhewuhe:bybit-java-api:1.2.7")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:4.32.1"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.71.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without
                // options. Note the braces cannot be omitted, otherwise the
                // plugin will not be added. This is because of the implicit way
                // NamedDomainObjectContainer binds the methods.
                id("grpc") { }
            }
        }
    }
}

springBoot {
    mainClass.value("ru.home.project.TinkoffScreenerAppKt")
}

