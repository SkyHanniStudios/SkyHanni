import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
    compileOnly(libs.detekt.api) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler")
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
