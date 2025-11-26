plugins {
    java-library
}

dependencies {
    implementation(project(":logistics:logistics-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
