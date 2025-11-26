plugins {
    `java-library`
}

dependencies {
    implementation(project(":identity:identity-application"))
    implementation(project(":identity:identity-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation(project(":common:common-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.postgresql:postgresql")
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.18")
}
