import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.7.10"
    `java-library`
    id("com.diffplug.spotless").version("6.11.0")
    jacoco
}

group = "de.rki.jfn"

val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionBuild: String by project

version = "$versionMajor.$versionMinor.$versionPatch-rc.$versionBuild"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.4.2")

    // jUnit5
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")

}

jacoco {
    toolVersion = "0.8.7"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

java {
    withSourcesJar()
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt", "**/*.gradle.kts")
        ktlint("0.47.1")
    }
}
