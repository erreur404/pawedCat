# 13-core-user-flows-test-suite

Type: task
Status: resolved
Blocked by: 01, 04, 05

## Question

How to construct a fast, robust testing strategy securing user flows and catching regressions if upstream UI frameworks or libraries change?

## Answer

Built a multi-tiered test suite:
1. **Robolectric & Compose Testing (`MainScreenUiTest.kt`)**: Runs directly on JVM in ~7 seconds without emulator overhead. Validates tab switching across Podcasts, Queue, Downloads, and Settings, asserting UI node clickability and rendering.
2. **RSS Feed Parser Tests (`RssFeedParserTest.kt`)**: Tests XML feeds against edge cases (such as bare `<link>` text URLs and CDATA content) to prevent feed ingestion crashes.
3. **OPML Round-Trip Test (`OpmlRoundTripTest.kt`)**: Tests exporting podcasts to OPML 2.0 and re-importing to guarantee 100% fidelity.
4. **Auto-Download Rule Tests (`AutoDownloadRuleTest.kt`)**: Tests positive regex evaluation and title filtering.
5. **Database Transaction Flow Tests (`RoomFlowIntegrationTest.kt`)**: In-memory SQLite Room tests covering cascading foreign keys, downloaded status queries, title search, and volume boost persistence.
6. **Android Emulator Runner in CI (`.github/workflows/e2e-emulator.yml`)**: Uses `reactivecircus/android-emulator-runner@v2` on hardware-accelerated macOS runners to run connected UI tests against API 34.
