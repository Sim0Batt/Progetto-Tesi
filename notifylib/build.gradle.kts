plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "1.9.10"
    `maven-publish`
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("com.google.code.gson:gson:2.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.3")
    implementation("io.ktor:ktor-serialization-gson:3.4.3")

    implementation("info.faljse:SDNotify:1.6")

    implementation("com.kohlschutter.junixsocket:junixsocket-core:2.11.1")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}