plugins {
    `java-library`
}

dependencies {
    implementation(project(":inventory:inventory-application"))
    implementation(project(":inventory:inventory-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}
