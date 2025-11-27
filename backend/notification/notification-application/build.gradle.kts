plugins {
    `java-library`
}

dependencies {
    implementation(project(":notification:notification-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
