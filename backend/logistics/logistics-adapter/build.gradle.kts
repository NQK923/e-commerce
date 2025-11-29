plugins {
    `java-library`
}

dependencies {
    implementation(project(":logistics:logistics-application"))
    implementation(project(":logistics:logistics-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}

