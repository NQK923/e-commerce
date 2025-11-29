plugins {
    `java-library`
}

dependencies {
    implementation(project(":notification:notification-application"))
    implementation(project(":notification:notification-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}

