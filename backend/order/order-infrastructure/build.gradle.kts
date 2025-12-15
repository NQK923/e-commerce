plugins {
    `java-library`
}

dependencies {
    implementation(project(":order:order-application"))
    implementation(project(":order:order-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation(project(":common:common-infrastructure"))
    implementation(project(":promotion:promotion-domain"))
    implementation(project(":promotion:promotion-application"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.postgresql:postgresql")
    implementation("org.elasticsearch.client:elasticsearch-rest-client")
    implementation("com.fasterxml.jackson.core:jackson-databind")
}

