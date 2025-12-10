plugins {
    `java-library`
}

dependencies {
    implementation(project(":chat:chat-application"))
    implementation(project(":chat:chat-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
}
