import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        content {
            includeGroupAndSubgroups("com.github")
        }
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlin.stdlib)
    implementation(libs.changelog.builder)
    implementation(files("../sharedVariables/build/libs/sharedVariables.jar"))
    implementation(libs.methanol)
    compileOnly(libs.kotlin.compiler.embeddable) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
    compileOnly(libs.detekt.api) {
        exclude(group = "org.jetbrains.kotlin")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget("25"))
        languageVersion.set(KotlinVersion.KOTLIN_2_4)
        apiVersion.set(KotlinVersion.KOTLIN_2_4)
        optIn.add("kotlin.concurrent.atomics.ExperimentalAtomicApi")
    }
}
