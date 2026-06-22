plugins {
    `java-library`
}

dependencies {
    implementation(project(":chat:chat-application"))
    implementation(project(":chat:chat-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation(project(":common:common-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}
