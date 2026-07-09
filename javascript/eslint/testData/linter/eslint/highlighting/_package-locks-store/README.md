# ESLint test package-lock store

Committed `package.json` + `package-lock.json` pairs that make the `stable`/`next`
ESLint tests install exact, deterministic dependency trees (direct **and** transitive),
instead of resolving floating versions from the registry on every run.

`TestNpmPackageInstaller` (in `plugins/JavaScriptLanguage/testFramework`) locates the
combo directory by name, copies its `package-lock.json` into the test project, and runs
`npm install --force`. The version-agnostic testData `package.json` files keep a
`$ESLINT_VERSION$` placeholder that `EslintPackageLockTestBase` substitutes at runtime
from the class-level `@TestNpmPackage` annotation, so one testData tree serves every
pinned version.

## Combo directory naming

The directory name is `<annotation-spec><each dependency of the test's package.json>`,
each token sanitized by replacing `@`, `.` and `/` with `_`
(`convertNpmPackageSpecToFileSystemName`). Example: annotation `eslint@10.6.0` plus a
testData `package.json` declaring `"eslint": "10.6.0"` →
`eslint_10_6_0eslint_10_6_0`.

## Combos

| Directory | package.json devDependencies | Used by |
|---|---|---|
| `eslint_10_6_0eslint_10_6_0` | `eslint 10.6.0` | core `stable` highlighting + fix tests |
| `eslint_8_57_0eslint_8_57_0` | `eslint 8.57.0` | `EslintHighlightingV8LegacyTest` (`.eslintrc` coverage) |

The full TS/Vue/HTML combo (eslint + typescript + `@typescript-eslint/parser` +
`vue-eslint-parser` + `eslint-plugin-vue`/`html`/`react`) is added alongside the
TS/Vue/HTML batch.

## Upgrade procedure

1. Change the version constant in
   `contrib/javascript/eslint/test/.../eslint/EslintTestPackages.kt`.
2. Regenerate the affected combo(s): in a scratch directory, write the `package.json`
   for the combo with the new exact versions, run
   `npm install --force --registry=https://repo.labs.intellij.net/api/npm/npm-all`,
   and copy the resulting `package.json` + `package-lock.json` into the matching combo
   directory here (and in `../../quickfix/_package-locks-store` if the fix suite uses it).
   Delete `node_modules`; commit only the two files.
3. Rerun the `stable` classes and re-record any goldens that legitimately changed.

## Triage

A `next.*` failure means an upstream ESLint release changed something — reproduce
locally, adapt the plugin or re-pin the `stable` matrix, and re-record goldens. It is
**not** a regression in whatever change happened to be in the merge queue.
