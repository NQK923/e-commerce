plugins {
    `java-library`
}

dependencies {
    implementation(project(":order:order-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
