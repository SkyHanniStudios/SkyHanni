import at.skyhanni.sharedvariables.ProjectTarget
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.github.davidarthurcole.daetekt")
}

val moulconfigTarget = ProjectTarget.MODERN_26200.minecraftVersion

dependencies {
    compileOnly(libs.detekt.api)
    ksp(libs.autoservice.ksp)
    implementation(libs.autoservice.annotations)
    implementation(libs.detektrules.ktlint)
    implementation(libs.gson)
    implementation("org.notenoughupdates.moulconfig:modern-${moulconfigTarget.versionName}:${libs.versions.moulconfig.get()}") {
        isTransitive = false
    }
    testImplementation(libs.detekt.test)
    detektPlugins(libs.detektrules.authors)
    detektPlugins(libs.detektrules.ktlint)
    detektPlugins(libs.detektrules.neu)
    detektPlugins(project(":detekt"))
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(layout.projectDirectory.file("detekt.yml"))
}

val detektIdeJars = tasks.register<Sync>("detektIdeJars") {
    description = "Map dependencies into Detekt"
    from(tasks.named("jar"))
    from(configurations.named("runtimeClasspath").map { files -> files.filter { it.name.startsWith("modern-") } })
    into(layout.buildDirectory.dir("detekt-ide"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget("25"))
}

tasks.withType<Detekt>().configureEach {
    val isDetektMain = name == "detektMain"
    onlyIf { isDetektMain }
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    onlyIf { false }
}
