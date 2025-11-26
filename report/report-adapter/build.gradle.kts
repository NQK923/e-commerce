plugins {
    `java-library`
}

dependencies {
    implementation(project(":report:report-application"))
    implementation(project(":report:report-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}
