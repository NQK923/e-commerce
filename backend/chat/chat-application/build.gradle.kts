plugins {
    `java-library`
}

dependencies {
    implementation(project(":chat:chat-domain"))
    implementation(project(":common:common-domain"))
    implementation(project(":common:common-application"))
}
