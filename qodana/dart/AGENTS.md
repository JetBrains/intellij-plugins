# Qodana Dart integration

This plugin reports Dart analyzer diagnostics through Qodana for Dart and Flutter projects.
`DartAnalyze` is the inspection used by the Qodana profile.
The platform reads its short name from the XML registration; do not override `getShortName()` or duplicate it in a constant.
The final SARIF report identifies each diagnostic with a derived rule such as `dart_undefined_identifier`.

## SDK and project requirements

- The scan environment must provide a Dart executable and the project dependencies.
- `QODANA_FLUTTER_PATH` accepts an SDK `bin` directory or an executable path.
  A Flutter SDK can supply the Dart executable.
- Without an explicit path, the runner looks for `dart` on `PATH`.
  An unusable explicit path produces an error; it does not fall back to `PATH`.
- The plugin does not install or pin an SDK, run `pub get`, or change project configuration.
  Dependency preparation belongs to the scan environment.

## Analysis behavior

- Run only when `DartAnalyze` is enabled and the Qodana analysis scope contains an enabled Dart file.
  Excluded files and non-Dart files must not trigger analysis.
- Run `dart analyze --format=json .` once per inspection invocation.
  Check the SDK with `dart --version` first, and skip analysis if that check fails.
  Select the common ancestor of the parent directories of the Dart files in the inspection scope.
  Use `FilenameIndex` to select those files and `VfsUtil.getCommonAncestor` to select the directory.
  Skip files in hidden directories under the project directory when selecting the analysis directory.
- Analyze the selected directory as a whole, including nested packages.
  Do not discover packages and start a separate analyzer for each package.
  The [Dart analyzer](https://dart.dev/tools/dart-analyze) discovers the Dart files under that directory.
  It applies the relevant [analysis options](https://dart.dev/tools/analysis#the-analysis-options-file), including exclusions and lint rules.
  It looks for `analysis_options.yaml` at each package root, then in parent directories, and uses standard checks if none exists.
  Flutter projects use the same Dart command.
- Qodana scope and inspection exclusions control which diagnostics reach the report.
  An incremental scan analyzes the common directory of its Dart files but reports only results within its scope.
- Preserve diagnostic severity, message, correction text, and source location.
- Accept a nonzero analyzer exit code when the output contains valid diagnostics.
  Report SDK check failures as `DartAnalyzeSanity` issues, while other inspections continue.
  `DartAnalyzeSanity` also checks the SDK independently and belongs to the sanity profile.
  Check explicitly whether its scope contains Dart files before checking the SDK.
  Use the project directory as the execution context for SDK checks and as the SDK failure location.
  Both inspections use the same failure message, directory, and inspection ID so Qodana can remove duplicate results.
  When sanity is disabled, log SDK check failures without adding a report result.
  Throw analysis failures so Qodana reports them through `toolExecutionNotifications` as `toolError` notifications.
  Include the analysis directory and failure stage in the exception, and preserve the cause.
  Log bounded process output with escaped line endings. The notification collector does not redact exception text.
  Propagate cancellation.

## SARIF and fingerprints

Fingerprint generation uses `dart_<diagnosticCode>` as the `ruleId`.
Both `equal/1` and `equal/2` include the `ruleId`, so diagnostics at the same location stay distinct.
The results carry no additional fingerprint data.

Profile routing and the final diagnostic identity use these values:

| Data | Expected value and purpose |
|---|---|
| Inspection ID during consumption | `DartAnalyze`, which controls filtering and thresholds. |
| Result `ruleId` during database writing and comparison | `dart_<diagnosticCode>`, assigned at descriptor export and registered in `ProfileState` before database writing. |
| Derived rule `properties.parentRuleId` | `DartAnalyze`. The definition links the diagnostic rule to its parent. |

- Each diagnostic descriptor implements `ExportedInspectionsResultModifier` and assigns its derived rule ID to the exported element.
  `DartAnalyzeGlobalOutputConsumer` registers derived rules from that ID.
  The standard XML converter creates the result and its fingerprints with the derived rule ID.
  Do not write diagnostics to an aggregate XML file.
- Register observed diagnostics in `ProfileState` before the writer receives their results.
  Map each derived inspection to the state of `DartAnalyze`; do not extend the profile.
  Early registration lets results use derived IDs before baseline comparison, including matching when fingerprints change.
- Create derived definitions in `populateRunWithInspectionResults()`, before any baseline comparison, beside the `DartAnalyze` definition.
  Copy the parent's default configuration and taxonomy relationships; preserve each result's severity.
  Create definitions for codes present in the collected results.
  Baseline comparison copies missing definitions from the baseline when it retains absent results.
  Current definitions must exist before comparison so baseline does not copy rules already reported by the scan.
- Do not register derived diagnostics as profile inspections or maintain a generated diagnostic catalog.
- Add `parentRuleId` to derived definitions, not to new individual results.
  Resolve an existing derived result's parent through its rule definition when needed.
  Do not remove or clean properties imported from a baseline.
- Repeated scans must keep `equal/1` and `equal/2` stable.
  A saved final report must match the same diagnostics in a later scan.

## Implementation map

Source files are under `src/org/jetbrains/qodana/dart/`.

| File | Responsibility |
|---|---|
| `DartAnalyzeRunner.kt` | Resolve the executable, run the process, and handle analyzer failures. |
| `DartProblem.kt` | Parse analyzer JSON and convert source ranges. |
| `DartAnalyzeGlobalInspection.kt` | Check the scope, run analysis, and register diagnostics with derived rule IDs. |
| `DartAnalyzeSanityInspection.kt` | Check the SDK and report SDK failures. |
| `DartAnalyzeGlobalOutputConsumer.kt` | Register derived rules and consume the exported XML results. |

## Verification

Unit tests should be fast, and an external linter can be difficult to include in their environment.
The mock runner supports development without that dependency, but its recorded responses can quickly become outdated.
The shared native/mock scenarios and parity requirements help identify differences between recorded responses and actual analyzer behavior.

Dart analysis tests must pass with both the native executable and the recorded mock runner.
Native tests verify the SDK integration and actual analyzer output.
Mock tests use fixtures under `test-data/dart-analyze` and cover the same Qodana reporting behavior.
Both modes must produce the same result signature and `equal/1` and `equal/2` fingerprints.

Run commands from the repository root.
Replace `/path/to/sdk/bin` with an installed Dart or Flutter SDK `bin` directory:

```sh
QODANA_FLUTTER_PATH=/path/to/sdk/bin ./tests.cmd --module intellij.qodana.dart.tests --test 'org.jetbrains.qodana.dart.DartAnalyze*'
QODANA_FLUTTER_PATH=/nonexistent ./tests.cmd --module intellij.qodana.dart.tests --test 'org.jetbrains.qodana.dart.DartAnalyze*'
```

The test runner uses recorded responses when it cannot resolve an executable.
Check the selected backend; a fixture-only run does not verify native integration.
Use a focused class FQN when changing one scenario.
Also run `org.jetbrains.qodana.dart.DartAnalyzeRunnerTest` when changing SDK resolution.

When changing a diagnostic scenario:

1. Update the source, configuration, and final SARIF assertions in the Kotlin test.
2. Run native mode. Use its diagnostic code, severity, message, URI, and region as the expected behavior.
3. Update the matching JSON fixture. Keep location and message data consistent with native output.
4. Run both modes. Verify stable fingerprints on an unchanged retrigger and correct matching against a saved baseline.

Prefer assertions about final SARIF behavior over internal registration or execution details.
Cover scope filtering, disabled inspections, SDK failures, diagnostic severity, derived rule IDs, and parent metadata on definitions.

Real SDK integration tests are under `org.jetbrains.qodana.dart.integration` and use the QDPOLY launcher.
They belong to the `QODANA_DART_INTEGRATION` test group and do not use the recorded mock runner.
Run them with an available SDK and prepared fixture dependencies when changing the launcher or SDK integration:

```sh
./tests.cmd --module intellij.qodana.dart.tests --test 'org.jetbrains.qodana.dart.integration.*'
```
