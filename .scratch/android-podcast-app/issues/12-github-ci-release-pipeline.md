# 12-github-ci-release-pipeline

Type: task
Status: resolved
Blocked by: 01

## Question

How to automate APK builds and release artifacts on GitHub Actions?

## Answer

Created `.github/workflows/build-apk.yml` with:
1. **Triggers**: On push to `main`/`master`, pull requests, manual `workflow_dispatch`, and tag releases (`v*`).
2. **Build**: Sets up JDK 17 (Temurin), configures Gradle caching, runs `./gradlew assembleRelease`.
3. **Artifacts**: Uploads `PawedCat-release.apk` to workflow run artifacts.
4. **GitHub Releases**: Automatically creates a GitHub Release and attaches the downloadable APK when a git tag like `v1.0.0` is pushed.
