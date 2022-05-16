import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.6.20"
    `java-library`
    id("com.diffplug.spotless").version("6.6.1")
    jacoco
}

group = "de.rki.jfn"

val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val commit = "git rev-parse --short HEAD".runCommand()

version = "$versionMajor.$versionMinor.$versionPatch-SNAPSHOT-$commit"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.3.0")

    // jUnit5
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")

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
        ktlint("0.43.2").userData(mapOf("max_line_length" to "100"))
    }
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}
