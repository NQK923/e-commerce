plugins {
    `java-library`
}

dependencies {
    implementation(project(":promotion:promotion-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
    
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
}

