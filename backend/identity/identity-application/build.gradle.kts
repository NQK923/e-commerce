plugins {
    `java-library`
}

dependencies {
    implementation(project(":identity:identity-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}

