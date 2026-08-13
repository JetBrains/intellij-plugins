# Qodana Dart: plugin and lifecycle

## Scope of the current iteration

1. Provide the Qodana Dart plugin without adding a standalone product or product bundling.
2. Use locally installed Dart and Flutter SDK `bin` directories configured with `QODANA_DART_PATH` and `QODANA_FLUTTER_PATH`.
3. Split dependency preparation (`dart pub get`) from analysis (`dart analyze --format=json`).

## Plugin

- Keep the local SDK path as an explicit environment-based prerequisite. Do not embed an SDK in the plugin in this iteration.

## Lifecycle

- Run a Dart preparation provider only when a Dart inspection is enabled.
- Discover Dart packages and run `dart pub get` before analysis.
- Store preparation results for the current run.
- Analyze only prepared packages and report preparation failures through `DartAnalyze`.

## Nested packages

- `dart analyze <directory>` and `flutter analyze <directory>` analyze their respective module types. Diagnostics discovered while
  analyzing a Dart parent are kept in the output unchanged.
- The same file can be reported by analyses of more than one Dart directory. If this becomes a problem, deduplicate SARIF output
  explicitly using a stable issue identity; do not filter results only because their path belongs to a Flutter subproject.

## Verification

- Make tests obtain the Dart executable from an environment variable or system property rather than an absolute user path.
- Cover preparation success and failure, disabled Dart inspections, and the analysis/SARIF path.

## Deferred

- Per-diagnostic routing and generated diagnostic catalog maintenance.
- Bundled and pinned Dart SDK.
