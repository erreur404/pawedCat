# 14-weekly-dependency-and-security-cron

Type: task
Status: resolved
Blocked by: 12, 13

## Question

How to automate weekly dependency update verification while enforcing supply-chain security (7-day quarantine cooldown) and automatically raising issues on breaking changes?

## Answer

1. **Supply Chain Defense (7-Day Quarantine Policy)**: Configured weekly automation schedule (Monday 04:00 UTC) allowing time for zero-day supply chain attacks, poisoned dependencies, or compromised maintainer credentials to be discovered and revoked by repository registries before being adopted.
2. **Scheduled CI Pipeline (`.github/workflows/dependency-check.yml`)**:
   - Runs `./gradlew testDebugUnitTest` and `./gradlew assembleRelease`.
   - On build or test failure, automatically uses the GitHub CLI (`gh issue create`) with `GITHUB_TOKEN` to file a new GitHub Issue titled `⚠️ Weekly Dependency / Build Alert: YYYY-MM-DD` attaching logs.
3. **Dependabot Configuration (`.github/dependabot.yml`)**: Groups dependencies logically (Compose, Media3, Room, Android Core) with weekly syncs.
