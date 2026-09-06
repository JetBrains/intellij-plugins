# MDX Remote Development Migration

## Target

- Kind: plugin
- Plugin id: `mdx.js`
- Plan root: `contrib/mdx`
- Root descriptor: `contrib/mdx/src/main/resources/META-INF/plugin.xml`
- Root JPS module: `intellij.mdx`
- Last verified: 2026-09-09. The optional React Emmet module is shared. MDX passed 4/4 tests. JSX passed 37/37 tests.

## Current Split State

- Loading mode: one plugin that supports monolith, backend, and frontend product modes.
- Architecture: migration complete with a metadata-only root descriptor and five implementation modules:
  - `intellij.mdx.shared`
  - `intellij.mdx.backend`
  - `intellij.mdx.frontend`
  - `intellij.mdx.frontend.split`
  - `intellij.mdx.react`
- Root descriptor state: metadata-only; the empty `intellij.mdx` legacy content descriptor remains for compatibility and is embedded in the top-level `lib/intellij.mdx.jar` so the plugin has a root classpath entry.
- Module visibility: the legacy root, MDX shared, MDX React, and React shared content modules are `internal`.
- Full Line has an explicit dependency on the MDX shared module.
- Recognized module kinds:
  - root descriptor: `shared`
  - shared descriptor: `shared`
  - React integration descriptor: `shared`
  - React shared descriptor: `shared`
  - backend descriptor: `backend`
  - frontend descriptor: `frontend`
  - frontend-split descriptor: `frontend`

## Refactoring Overview

- Goal: provide frontend PSI and responsive MDX editor behavior while keeping indexing, resolve, completion, imports, and formatting on the backend.
- Architecture: one plugin with a metadata-only root, required shared content, conditional backend/frontend content, and a minimal thin-client-only adapter.
- Compatibility: preserve the plugin id, MDX/MdxJS language identities, file type, and existing `org.intellij.plugin.mdx` shared API packages.
- Line wrapping: register the MDX-specific strategy from shared ownership, as required by the split-mode API restriction.
- Parser boundaries: keep Markdown AST ownership and the JSX, expression, ESM, fence, and opacity scanners in shared; the backend import optimizer reuses the shared ESM scanner.
- Emmet: load the JSX and MDX generators from shared modules so expansion can run on each side.
- React Emmet: load an optional shared integration module when React is available. Its MDX filter normalizes token attribute names.
- Product compatibility: DataGrip does not bundle the JavaScript plugin required by MDX, so it explicitly excludes `intellij.mdx` from compatible non-bundled plugin publication; this is independent of Full Line's optional MDX content module.
- Completed milestone checklist:
  - [x] Normalize the root descriptor and establish the canonical content-module layout.
  - [x] Move Language, FileType, parser, lexer, template-data view provider, PSI, highlighting, typing, Enter, paste, and quote handling to shared ownership.
  - [x] Keep stubs, resolve, imports, completion, inspections, formatting, and backend listeners in backend ownership.
  - [x] Move Markdown editor routing to frontend ownership.
  - [x] Add the documented thin-client highlighting suppression and editor/comment action routing adapter in `frontend.split`.
  - [x] Remove product-level MDX dependency exceptions, verify the optional Full Line MDX module remains valid when MDX is not bundled, and exclude MDX from DataGrip compatible-plugin publication.
  - [x] Complete compilation, lint, focused tests, project-structure checks, API checks, and product packaging validation.
  - [x] Restore a non-empty root plugin classpath entry for dev-build and packaged runtime loading.
  - [x] Resolve post-migration descriptor dependencies, frontend editor-action ownership, project-structure allowlisting, and inspection findings.
  - [x] Move the MDX line-wrap registration to shared ownership and add an MDX-owned strategy there.
  - [x] Reapply the AST-first rescanner and parser-boundary redesign to the split shared/backend layout.

## Current In-Progress Task

- Status: complete
- Active milestone: move React Emmet support into an optional MDX integration module.
- Current worker skill: code-style, module dependency, remote-dev module layout, and code placement.
- Verified starting evidence: a shared MDX filter can recognize `MdxFileViewProvider` without a platform Emmet API change.
- Completed outcome: `intellij.mdx.react` owns the filter and loads in each product mode only when React is available.
- Last completed step: module generation, lint, module recognition, focused tests, and the format check passed.
- Next optional step: verify MDX Emmet in a running split IDE with at least 500 ms Direct Ping.
- Handoff condition: no automated follow-up work remains.

## Module And Descriptor Inventory

- `intellij.mdx`
  - Descriptor: `src/main/resources/META-INF/plugin.xml` plus empty `src/main/resources/intellij.mdx.xml`
  - Kind: shared
  - Role: plugin metadata and content composition; the legacy module is embedded in `lib/intellij.mdx.jar` to preserve its module identity and provide the root plugin classpath entry

- `intellij.mdx.shared`
  - Descriptor: `shared/resources/intellij.mdx.shared.xml`
  - Kind: shared
  - Role: language/file type, parser/lexer, PSI/template-data core, scanners, editor behavior, Emmet, and shared Markdown integration
  - Constraint: no frontend-, backend-, split-, or monolith-only platform dependency

- `intellij.mdx.backend`
  - Descriptor: `backend/resources/intellij.mdx.backend.xml`
  - Kind: backend
  - Role: stubs, resolve/imports, completion, formatting, inspections, injected-highlighting policy, and backend editor listeners
  - Dependency: shared MDX plus backend JavaScript/Markdown/XML/platform APIs

- `intellij.mdx.frontend`
  - Descriptor: `frontend/resources/intellij.mdx.frontend.xml`
  - Kind: frontend
  - Role: completion-indent restoration, paste action wrapping, and the frontend Markdown action-promoter delegate
  - Dependency: shared MDX plus frontend Markdown/platform APIs

- `intellij.mdx.frontend.split`
  - Descriptor: `frontend.split/resources/intellij.mdx.frontend.split.xml`
  - Kind: frontend
  - Role: minimal RD-only highlighting suppression and editor/comment action routing
  - Justification: the official language-split guide assigns these RD-protocol-aware extension points to `.frontend.split`; Markdown's adapters match `MarkdownFileType` exactly and do not cover MDX

- `intellij.mdx.react`
  - Descriptor: `react/resources/intellij.mdx.react.xml`
  - Kind: shared
  - Role: optional MDX and React Emmet integration
  - Dependency: shared MDX, shared React attribute mapping, and shared Emmet APIs

- `intellij.mdx.tests`
  - Role: parser, formatting, completion, highlighting, typing, and editor regression tests
  - Dependency: root/shared/backend/frontend MDX modules plus the existing platform test modules

## Feature Inventory

- PSI and core language model
  - Owner: shared
  - Registrations: file type, view provider, MDX/MdxJS parser definitions, outer-language range patcher
  - State: complete; the AST-first scanner redesign is shared-owned, shared compilation passes, and parser/editor regressions are covered by the full MDX suite

- Highlighting and color settings
  - Owner: shared for syntax highlighting and settings; backend for injected-highlighting policy; frontend-split for duplicate suppression
  - State: complete by source placement and automated checks; visual duplicate-highlighting behavior still needs manual split verification

- Latency-sensitive editor behavior
  - Owner: shared for typing, Enter, quote handling, line wrapping, and the code-fence editing sandbox; frontend for paste wrapping and Markdown routing; frontend-split for RD action strategy/customization
  - State: complete by source placement and automated tests; latency/rollback behavior still needs manual split verification

- Formatting, folding, and structure
  - Owner: backend for formatting/post-format/indent implementations; shared for frontend-safe Markdown folding and structure registrations
  - State: complete; focused formatting tests pass

- Resolve, imports, stubs, completion, and inspections
  - Owner: backend
  - State: complete; backend compilation and focused completion/resolve tests pass

- Emmet
  - Owner: shared generator plus the optional shared MDX and React integration
  - State: complete; the integration filter recognizes MDX directly and maps React attribute names without a platform API change

## Remaining Work

- [x] Complete canonical root/shared/backend/frontend/frontend-split layout.
- [x] Complete shared PSI/core and latency-sensitive editor ownership.
- [x] Complete backend analysis ownership.
- [x] Verify the optional Full Line MDX dependency without product-level MDX exceptions or bundling changes, and keep MDX out of DataGrip's compatible non-bundled plugins.
- [x] Complete generated-model, build, lint, focused-test, API, and packaging validation.
- [x] Embed the legacy root content module in a top-level plugin JAR so dev-build descriptor loading has a non-empty classpath.
- [x] Resolve the reported post-migration descriptor, frontend action ownership, Full Line allowlist, and code-inspection findings.
- [x] Move the MDX line-wrap registration from frontend to shared ownership and add `MdxLineWrapPositionStrategy` to the shared module.
- [x] Reapply the rescanner and parser-boundary redesign without moving language-core scanning into backend or frontend modules.
- [x] Move the MDX Emmet generator and registration to shared ownership.
- [x] Load the optional MDX and React Emmet filter in each product mode.

### Manual split-runtime verification

- [ ] Compare frontend and backend MDX PSI trees and confirm the frontend stub tree is empty.
- [ ] Test typing, Enter, paste, quotes, comments, and editor actions with at least 500 ms Direct Ping.
- [ ] Verify MDX Emmet expansion with at least 500 ms Direct Ping.
- [ ] Confirm highlighting and error popups are not duplicated.
- [ ] Confirm undo, caret position, and editor changes do not roll back after backend synchronization.

## Blockers

- None for the code migration.
- The remaining checklist requires a running split IDE and interactive visual/editor inspection; it was not available in the command-line validation environment.

## Validation

- Rescanner rebase: the seven-commit series is based directly on `mdx` at `0e956f9fe2655`; parser/scanner sources are in `intellij.mdx.shared`, while `MdxJSImportOptimizer` remains in `intellij.mdx.backend`.
- Rescanner validation: the test build compiled shared, backend, frontend, and test modules; 364/364 MDX tests passed, and the split-mode compatibility inspection reported zero issues across 48 shared files.
- Line-wrap ownership: `85d46456e9bbe` moves the MDX registration from `intellij.mdx.frontend.xml` to the shared descriptor and adds `MdxLineWrapPositionStrategy`; Markdown's own frontend registration is unchanged.
- Line-wrap regression tests: `MarkdownSoftWrapTest` passed 2/2 and `MdxLiveEditingTest` passed 46/46.
- Generated metadata: `./build/jpsModelToBazel.cmd` completed successfully with no generated-file changes.
- Inspection-version note: the running IDE defaults `devkit.split.mode.analysis.api.restrictions.source` to `bundled`, and its older bundled data still classifies line wrapping as `frontend`. For monorepo development, set that registry key to `project` and restart the IDE; local inspection then reads the same newer `shared` policy as CI.
- Post-migration descriptor dependencies: both shared and frontend explicitly depend on the Markdown plugin; the Product Layout generator completed with all files unchanged, with content-module suppressions preserving those required runtime dependencies.
- Post-migration editor ownership: the `EditorPaste` wrapper and frontend Markdown action-promoter delegate remain in `intellij.mdx.frontend`; the current line-wrap change does not alter their ownership.
- Post-migration project structure: `LlmConfigurationSmokeTest#test external dependencies inside the llm modules` passed 1/1 with the two intentional Full Line MDX edges allowlisted.
- Post-migration compilation and tests: shared, backend, and frontend Bazel targets build; the affected MDX suite passed 203/203 tests.
- Supplied inspection report: all applicable findings were fixed. The remaining `super.findElementAt(offset, lang)` call in `MdxFileViewProvider` is a `FileViewProvider` call, not the reported `PsiFile.findElementAt(offset)` pattern.
- Descriptor recognition: root/shared are `shared`, backend is `backend`, and frontend/frontend-split are `frontend`.
- Compilation: shared, backend, frontend, frontend-split, and the Full Line MDX backend consumer build successfully together.
- IDE lint: 85 changed Kotlin, Java, XML, and IML files analyzed with zero warnings.
- MDX regression tests:
  - 295 unaffected focused tests passed.
  - Four unaffected `MdxMarkdownActionsTest` methods passed.
  - `testMarkdownStylingActionsApplyInMdx` and `testMarkdownStylingActionsApplyToMixedMdxSelections` still fail because the fixtures expect underscore italics while `MarkdownCodeInsightSettings` defaults to asterisks; this is unrelated to the split.
- Product DSL: the generator completed with all files unchanged, and the `Plugin DSL Tests` run configuration passed with exit code 0 after removing the product allowlists.
- Product packaging: `AllProductsPackagingTest` passed 35/35 without MDX `allowMissingDependencies`; after DataGrip added `intellij.mdx` to `compatiblePluginsToIgnore`, the test no longer proposed MDX in DataGrip's `nonBundled` plugin content.
- Root runtime packaging: `intellij.mdx` uses `loading="embedded"` and is generated as `lib/intellij.mdx.jar`; `jpsModelToBazel`, Product Layout generation, Plugin DSL tests, the MDX root Bazel build, and `AllProductsPackagingTest` pass. The original interactive WebStorm configuration was not relaunched in this validation run.
- Patronus snapshot consistency: 53/53 passed.
- Focused API check: 163/163 applicable existing dumps passed. Empty scaffold `api-dump.txt` files were removed because MDX was not API-dump-managed before the split; the migration does not publish a new implementation API contract.
- `IdeaUltimatePluginModuleDependenciesTest`: both cases were skipped by the test because their checks moved to `AllProductsPackagingTest`, which passed.
- Fast project-structure checks: no MDX failures; two unrelated local `Permanent Script Dependencies` failures remain outside this migration.
- Generated metadata and formatting: `jpsModelToBazel` and `//:format.check` pass.
- React integration kind: `intellij.mdx.react` is shared. The root and existing MDX modules keep their previous recognized kinds.
- React integration model: the plugin model added `intellij.mdx.react` to the MDX dev distribution.
- Plugin model note: the run also found nine pre-existing stale JavaScript and React build files. These unrelated generated changes were discarded.
- Emmet ownership: `MdxZenCodingGenerator` and its registration moved from backend to shared.
- Emmet behavior: the optional MDX filter handles tokens with and without an XML tag. The generator consumes the filtered attribute names.
- React Emmet regression tests: shorthand, `class`, and `className` cases pass. `MdxEmmetTest` passed 4/4. `JSXEmmetTest` passed 37/37.
- Emmet compilation: the focused test build compiled the root, shared, React integration, backend, frontend, and test modules.
- Emmet lint: the changed Kotlin, XML, and IML files have no IDE warnings.
- Packaging: 69/78 checks passed. Nine unrelated JavaScript and React generated-file checks failed. No MDX check failed.
- Split-mode proof: manual frontend/backend PSI comparison, simulated-latency interaction, visual duplicate-highlighting inspection, and rollback/caret checks remain unverified.

## Change Log

- 2026-08-19: created the whole-plugin migration plan and established the canonical root/shared/backend/frontend layout.
- 2026-08-19: moved core PSI/editor behavior to shared, analysis features to backend, and Markdown editor routing to frontend.
- 2026-08-19: added the documented minimal frontend-split highlighting/action adapter.
- 2026-08-19: synchronized Full Line/product dependencies and packaging snapshots; completed automated validation and marked the migration complete.
- 2026-08-19: removed unnecessary product-level MDX missing-dependency allowlists and verified optional Full Line loading across all product packaging tests.
- 2026-08-19: excluded MDX from DataGrip compatible-plugin publication because DataGrip does not bundle the required JavaScript plugin; verified that `AllProductsPackagingTest` no longer adds MDX to DataGrip's `nonBundled` plugin content.
- 2026-08-19: embedded the legacy `intellij.mdx` content module in `lib/intellij.mdx.jar`, restoring the root classpath entry required by dev-build plugin descriptor loading while preserving Full Line compatibility.
- 2026-08-19: fixed the post-migration Markdown descriptor dependencies, moved the paste action wrapper and Markdown promoter registration to frontend ownership, allowlisted the intentional Full Line MDX dependencies, and resolved the supplied inspection findings.
- 2026-08-20: `85d46456e9bbe` moved the MDX line-wrap registration from frontend to shared ownership and introduced the MDX-owned `MdxLineWrapPositionStrategy`; Markdown's frontend strategy and registration were unchanged.
- 2026-08-25: rebased the seven-commit rescanner and parser-boundary redesign onto the split MDX layout, keeping language-core scanners shared and import optimization backend-owned; 364/364 MDX tests passed.
- 2026-09-09: moved MDX Emmet to shared ownership; 3/3 focused tests passed.
- 2026-09-09: moved React Emmet mapping into an optional shared MDX integration module without a platform Emmet API change.
