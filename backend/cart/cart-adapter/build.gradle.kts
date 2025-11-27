plugins {
    `java-library`
}

dependencies {
    implementation(project(":cart:cart-application"))
    implementation(project(":cart:cart-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
}
