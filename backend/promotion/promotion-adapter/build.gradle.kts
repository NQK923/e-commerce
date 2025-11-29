plugins {
    `java-library`
}

dependencies {
    implementation(project(":promotion:promotion-application"))
    implementation(project(":promotion:promotion-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}

