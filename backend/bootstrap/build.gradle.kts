plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation(project(":common:common-infrastructure"))

    implementation(project(":identity:identity-adapter"))
    implementation(project(":identity:identity-infrastructure"))

    implementation(project(":product:product-adapter"))
    implementation(project(":product:product-infrastructure"))

    implementation(project(":order:order-adapter"))
    implementation(project(":order:order-infrastructure"))

    implementation(project(":inventory:inventory-adapter"))
    implementation(project(":inventory:inventory-infrastructure"))

    implementation(project(":promotion:promotion-adapter"))
    implementation(project(":promotion:promotion-infrastructure"))

    implementation(project(":logistics:logistics-adapter"))
    implementation(project(":logistics:logistics-infrastructure"))

    implementation(project(":notification:notification-adapter"))
    implementation(project(":notification:notification-infrastructure"))

    implementation(project(":report:report-adapter"))
    implementation(project(":report:report-infrastructure"))

    implementation(project(":cart:cart-adapter"))
    implementation(project(":cart:cart-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
}
