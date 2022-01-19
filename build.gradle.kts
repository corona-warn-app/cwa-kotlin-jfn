import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    `java-library`
    id("com.diffplug.spotless").version("6.0.0")
    jacoco
}

group = "de.rki.jfn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")

    // Joda time
    implementation("joda-time:joda-time:2.10.13")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")

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

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt", "**/*.gradle.kts")
        ktlint("0.43.2").userData(mapOf("max_line_length" to "100"))
    }
}