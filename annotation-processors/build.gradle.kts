import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm")
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ksp.api)
    implementation(files(rootProject.layout.projectDirectory.file("buildSrc/build/libs/buildSrc.jar")))
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.matching { it.name == "kspTestKotlin" || it.name == "kspTestJava" }.configureEach {
    enabled = false
}
