plugins {
    `java-library`
}

dependencies {
    implementation(project(":notification:notification-application"))
    implementation(project(":notification:notification-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}

