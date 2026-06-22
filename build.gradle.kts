import org.gradle.api.tasks.compile.JavaCompile

plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

allprojects {
    group = "com.learnfirebase"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.8")
        }
        dependencies {
            dependency("org.apache.commons:commons-lang3:3.20.0")
            dependency("org.flywaydb:flyway-core:11.17.2")
            dependency("org.flywaydb:flyway-database-postgresql:11.17.2")
            dependency("com.tngtech.archunit:archunit-junit5:1.3.0")
            dependency("org.testcontainers:testcontainers-bom:1.20.4")
            dependency("org.testcontainers:junit-jupiter:1.20.4")
            dependency("org.testcontainers:postgresql:1.20.4")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        compileOnly("org.slf4j:slf4j-api")

        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("com.tngtech.archunit:archunit-junit5")
        testImplementation("org.testcontainers:junit-jupiter")
        testImplementation("org.testcontainers:postgresql")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }
}

// Root project is an aggregator; disable bootJar here to avoid missing main class issues.
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
