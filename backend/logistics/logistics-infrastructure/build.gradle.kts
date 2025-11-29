plugins {
    `java-library`
}

dependencies {
    implementation(project(":logistics:logistics-application"))
    implementation(project(":logistics:logistics-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    implementation(project(":common:common-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.postgresql:postgresql")
    implementation("org.elasticsearch.client:elasticsearch-rest-client")
}

