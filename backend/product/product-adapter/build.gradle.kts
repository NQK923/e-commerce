plugins {
    `java-library`
}

dependencies {
    implementation(project(":product:product-application"))
    implementation(project(":product:product-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation(project(":order:order-domain"))
    implementation(project(":inventory:inventory-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
}

