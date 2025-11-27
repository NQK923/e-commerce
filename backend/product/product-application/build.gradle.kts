plugins {
    `java-library`
}

dependencies {
    implementation(project(":product:product-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
