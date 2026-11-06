import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("dev.detekt")
}

dependencies {
    compileOnly(libs.detekt.api)
    ksp(libs.autoservice.ksp)
    implementation(libs.autoservice.annotations)
    implementation(libs.detektrules.ktlint)
    testImplementation(libs.detekt.test)
    detektPlugins(libs.detektrules.authors)
    detektPlugins(libs.detektrules.ktlint)
}

val detektRuleClasses by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations.runtimeClasspath.get())
}

artifacts {
    val main = sourceSets.main.get().output
    main.classesDirs.forEach { add(detektRuleClasses.name, it) { builtBy(tasks.classes) } }
    main.resourcesDir?.let { add(detektRuleClasses.name, it) { builtBy(tasks.processResources) } }
}

tasks.withType<Detekt>().configureEach {
    onlyIf { false }
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    onlyIf { false }
}
