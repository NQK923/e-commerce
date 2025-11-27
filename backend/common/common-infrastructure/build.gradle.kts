plugins {
    `java-library`
}

dependencies {
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
