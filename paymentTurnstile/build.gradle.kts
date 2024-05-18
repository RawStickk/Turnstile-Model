plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("io.github.nsk90:kstatemachine:0.27.0")
    implementation("io.github.nsk90:kstatemachine-coroutines:0.27.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}