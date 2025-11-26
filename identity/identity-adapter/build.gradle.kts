plugins {
    `java-library`
}

dependencies {
    implementation(project(":identity:identity-application"))
    implementation(project(":identity:identity-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}
