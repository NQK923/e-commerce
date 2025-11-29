plugins {
    `java-library`
}

dependencies {
    implementation(project(":order:order-application"))
    implementation(project(":order:order-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}

