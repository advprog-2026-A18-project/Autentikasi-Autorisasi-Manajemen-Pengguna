plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "7.2.0.6526"
    id("com.google.protobuf") version "0.9.5"
    jacoco
}

group = "my_sawit"
version = "0.0.1-SNAPSHOT"
description = "authentication_manajemen_akun"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "advprog-2026-A18-project_Autentikasi-Autorisasi-Manajemen-Pengguna")
        property("sonar.organization", "advprog-2026-a18-project")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.exclusions", "**/seeder/**, **/exception/**, **/*Application.java, **/security/SecurityConfig.java, **/security/JwtAuthenticationFilter.java, **/dto/**, **/model/**")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.12.6"
val dotenvVersion = "4.0.0"
val googleApiClientVersion = "2.4.1"
val grpcVersion = "1.73.0"
val protobufVersion = "4.31.1"
val annotations_api = "6.0.53"

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    // --- IMPLEMENTATION ---
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("me.paulschwarz:spring-dotenv:$dotenvVersion")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    implementation("com.google.api-client:google-api-client:$googleApiClientVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")

    // --- COMPILE ONLY & ANNOTATION PROCESSOR ---
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.apache.tomcat:annotations-api:$annotations_api")
    annotationProcessor("org.projectlombok:lombok")

    // --- RUNTIME ONLY ---
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // --- DEVELOPMENT ONLY ---
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // --- TEST IMPLEMENTATION ---
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // --- TEST RUNTIME ONLY ---
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
}

tasks.test {
    filter{
        excludeTestsMatching("*FunctionalTest")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
            }
        }
    }
}
