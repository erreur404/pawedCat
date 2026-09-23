# 12-github-ci-release-pipeline

Type: task
Status: resolved
Blocked by: 01

## Question

How to automate APK builds and release artifacts on GitHub Actions?

## Answer

Created `.github/workflows/build-apk.yml` with:
1. **Triggers**: On push to `main`/`master`, version tags (`v*`, `[0-9]*`), pull requests, manual `workflow_dispatch`, and GitHub release events (`published`, `created`).
2. **Build**: Sets up JDK 17 (Temurin), configures Gradle caching, runs `./gradlew assembleRelease`.
3. **Artifacts**: Uploads workflow artifacts (`PawedCat-APK`) and generates SHA-256 checksums.
4. **GitHub Releases**: Automatically publishes or attaches `PawedCat.apk`, `PawedCat-<version>.apk`, and `SHA256SUMS.txt` to the release.
5. **Direct Download Link**: Permanent shareable link `https://github.com/erreur404/pawedCat/releases/latest/download/PawedCat.apk` documented in `README.md`.

## Comments

- Enhanced pipeline to support GitHub UI releases (`github.event_name == 'release'`), numeric/v tags (`tags: [ 'v*', '[0-9]*' ]`), and `workflow_dispatch` releases.
- Configured clean standardized naming `PawedCat.apk` so direct download URLs are permanent and easily shareable without version string drift in the link.
