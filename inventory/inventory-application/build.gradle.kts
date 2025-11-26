plugins {
    java-library
}

dependencies {
    implementation(project(":inventory:inventory-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
