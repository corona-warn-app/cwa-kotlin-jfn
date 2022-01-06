import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    kotlin("jvm") version "1.6.10"
    `java-library`
    id("com.diffplug.spotless").version("6.0.0")
}

group = "de.rki.jfn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt", "**/*.gradle.kts")
        ktlint("0.43.2")
    }
}