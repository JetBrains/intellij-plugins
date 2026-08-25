# Dart/Qodana test maintenance

Dart analysis tests must pass with both the native Dart/Flutter executable and the recorded mock runner.

## Why both modes are required

- The native mode verifies the integration with the real SDK, analyzer output, process lifecycle, and diagnostic parsing.
- The mock mode is deterministic and runs without a locally installed SDK, so it covers the same Qodana/SARIF behavior in CI and during focused development.
- Both modes must produce the same result signature and `equal/1` and `equal/2` fingerprints. A mock-only change can hide a regression in the native analyzer integration; a native-only change can make the fixture stale.

## Selecting a backend

`DartAnalyzeTestSupport.kt` uses the native runner when the corresponding environment variable points to an executable file:

- `QODANA_FLUTTER_PATH` must contain the Dart or Flutter SDK `bin` directory, with the required executable inside it.

If the required executable is unavailable, the test uses `RecordedDartAnalyzeProcessExecutor` and fixtures from `test-data/dart-analyze`. Force mock mode explicitly with:

```text
QODANA_FLUTTER_PATH=/nonexistent
```

Run the focused class with:

```text
./tests.cmd --module intellij.qodana.dart.tests --test org.jetbrains.qodana.dart.DartAnalyzeGlobalInspectionQodanaTest
```

Run the same command with the environment variable above to verify mock mode. Run it with valid SDK `bin` paths to verify native mode.

## Updating a scenario

When changing a source file, enabled inspection, or expected diagnostic:

1. Update the source/configuration and assertions in the Kotlin test.
2. Run native mode and use its actual diagnostic code, severity, message, URI, and region as the contract.
3. Update the matching JSON fixture under `test-data/dart-analyze` so the mock describes the same diagnostic. Keep fields that affect fingerprint calculation identical, including location and message data.
4. Run the focused class in both modes. For fingerprint scenarios, also verify that an unchanged retrigger keeps the full result and both equal indicators stable, while a different inspection produces different values.

Do not treat a fixture-only test run as validation of the native analyzer integration.
