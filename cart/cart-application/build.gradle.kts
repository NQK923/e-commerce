plugins {
    java-library
}

dependencies {
    implementation(project(":cart:cart-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
