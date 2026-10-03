plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val logbackVersion = "1.4.12"
dependencies {

    implementation("org.simpleframework:simple-xml:2.7.1")

    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.3")
    implementation("io.ktor:ktor-serialization-gson:3.4.3")

    implementation("com.kohlschutter.junixsocket:junixsocket-core:2.11.1")


    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}