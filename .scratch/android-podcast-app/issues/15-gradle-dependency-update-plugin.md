# 15-gradle-dependency-update-plugin

Type: task
Status: resolved
Blocked by: 14

## Question

How do we automate querying repositories for the latest versions of all project dependencies and plugins using a Gradle plugin (e.g. `com.github.ben-manes.versions`) instead of guessing outdated versions?

## Answer

1. **Gradle Versions Plugin**: Integrated `com.github.ben-manes.versions` (v0.51.0) into `build.gradle.kts`.
2. **Stability Rejection Filter**: Configured `DependencyUpdatesTask` to filter out unstable candidates (`alpha`, `beta`, `rc`, `cr`, `m`, `preview`, `dev`, `eap`) whenever the current dependency version is stable.
3. **Automated Live Querying**: Enabled running `./gradlew dependencyUpdates` which queries Maven Central, Google Maven, and the Gradle Plugin Portal to report latest verified upstream releases.
4. **Resolved Baseline Catalog**: Sanitized `gradle/libs.versions.toml` to verified stable dependencies and verified full clean test pass (`./gradlew testDebugUnitTest`).

