import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "1.9.21"

    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.21"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.9.21"
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
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.google.protobuf:protobuf-java-util:3.21.5")
    testImplementation("io.grpc:grpc-testing:1.44.0")

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
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("ru.tinkoff.piapi:java-sdk-core:1.5")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("net.devh:grpc-client-spring-boot-starter:2.13.1.RELEASE") {
        exclude("io.grpc:grpc-core")
    }
    implementation ("javax.annotation:javax.annotation-api:1.3.2")
    implementation ("io.github.wuhewuhe:bybit-java-api:1.2.7")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.6.1"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
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

