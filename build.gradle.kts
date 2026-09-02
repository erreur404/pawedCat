plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    id("com.github.ben-manes.versions") version "0.51.0"
}


tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        val nonStableKeywords = listOf("alpha", "beta", "rc", "cr", "m", "preview", "dev", "eap")
        val isNonStable = nonStableKeywords.any { candidate.version.lowercase().contains(it) }
        val currentIsStable = !nonStableKeywords.any { currentVersion.lowercase().contains(it) }
        isNonStable && currentIsStable
    }
}


