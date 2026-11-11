// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.AutodetectLinterPackage
import com.intellij.lang.javascript.linter.LinterHighlightingFixture.ExpectedGlobalAnnotation
import com.intellij.lang.javascript.linter.eslint.ESLINT_8_57_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockFixture.Companion.eslintPackageLockFixture
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Legacy ESLint scenarios pinned to eslint 8.57.0 -- the last release supporting the mechanisms these
 * exercise, all removed in eslint 9/10 and with no flat-config equivalent: `.eslintignore` and
 * package.json `eslintIgnore`, `--rulesdir`, the eslintrc cascade across sub-packages, custom
 * (non-standard-named) eslintrc files, a string `parser` path, and `eslintConfig`-implied resolution.
 * Each test's comment states which legacy mechanism it covers. eslint 8.57.0 never changes, so these
 * goldens are stable. (Fatal/parse-error and flat-ignore scenarios are version-agnostic and live in
 * the generic tier; only genuinely eslintrc-era behavior belongs here.)
 */
@TestApplication
@TestNpmPackage(ESLINT_8_57_0_TEST_PACKAGE_SPEC)
class EslintHighlightingV8LegacyTest {
  private val eslint by eslintPackageLockFixture("$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/highlighting/")

  // Legacy ignore mechanism with no flat-config equivalent: package.json "eslintIgnore".
  @Test
  fun testFileIgnoredWithPackageJsonOption(): Unit = eslint.doHighlightingTestWithInstallation("src/ignoredDir/test.js")

  @Test
  fun testCanDisableIgnoreFilesWithCommandLineOption(): Unit =
    eslint.doHighlightingTestWithInstallation("test.js") {
      eslint.updateConfiguration { it.setExtraOptions("--no-ignore") }
    }

  // eslintrc cascade across sub-packages (nearest .eslintrc, root:true, .eslintrc.js resolution) --
  // no flat-config equivalent.
  @Test
  fun testEslintRcInSubpackage(): Unit = eslint.doHighlightingTestWithInstallation("packages/inner/js.js")

  @Test
  fun testEslintignoreInSubpackageAndParent(): Unit = eslint.doHighlightingTestWithInstallation("packages/inner/js.js")

  @Test
  fun testEslintIgnoreWithRelativePathInProjectSubDirectory(): Unit =
    eslint.doHighlightingTestWithInstallation("packages/foo/bar/src/ignored.js")

  // A custom (non-standard-named) eslintrc selected via the "custom config file" setting.
  @Test
  fun testCustomLegacyConfig(): Unit = eslint.doHighlightingTestWithInstallation("index.js") {
    eslint.useCustomConfigFile(".eslintrc.fast.json")
  }

  // --rulesdir (removed in eslint 9) + additional rules directory, custom rule plugins on disk.
  @Test
  fun testWithCustomRulesDirectories(): Unit = eslint.doHighlightingTestWithInstallation("js.js") {
    val tempDir = eslint.fixture.tempDirFixture
    eslint.updateConfiguration {
      it.setAdditionalRulesDirPath(tempDir.getFile("customRules1")!!.path)
        .setExtraOptions("--rulesdir " + tempDir.getFile("customRules2")!!.path)
    }
  }

  // Relative-path .eslintignore inside a sub-package ignores a file there (eslintrc + .eslintignore).
  @Test
  fun testEslintignoreWithRelativePathInProjectSubPackage(): Unit =
    eslint.doHighlightingTestWithAutodetectInstallation("packages/with-eslint-ignore/src/ignored.js")

  // The sub-package has no declared ESLint dependency, only an `eslintConfig` that implies one -- so
  // ESLint can be resolved from packages/inner/node_modules ONLY via AutodetectLinterPackage's
  // eslintConfig-implies-eslint branch (root's decoy `eslint` declaration blocks the sibling branch;
  // inner declares no eslint dep). A committed empty stub `node_modules/eslint` (no npm install) proves
  // that branch resolved it: the run fails trying to load the stub's lib/options and the error names
  // the inner path. Discrimination verified by inverting the eslintConfig branch locally (the test then
  // fails to resolve any package).
  @Test
  fun testImplicitDependencyButEslintConfigInSubpackage() {
    eslint.expectedGlobalAnnotation = ExpectedGlobalAnnotation("packages/inner/node_modules/eslint/lib/options", false, true)
    eslint.configureLinterForPackage(AutodetectLinterPackage.INSTANCE)
    eslint.doEditorHighlightingTest("packages/inner/js.js")
  }

  // .eslintrc referencing an ABSOLUTE parser path resolved from the installed package -- eslintrc only.
  @Test
  fun testTypescriptWithVueParserAbsolutePath() {
    eslint.installEslintForTest()
    val parserFile = File(eslint.nodePackage.systemDependentPath, "../@typescript-eslint/parser/dist/parser.js").canonicalFile
    assertTrue(parserFile.exists(), parserFile.toString())
    eslint.fixture.setCaresAboutInjection(false)
    eslint.fixture.configureByText(
      ".eslintrc.json",
      """
      {
        "parserOptions": {
          "parser": "${parserFile.path.replace('\\', '/')}"
        },
        "parser": "vue-eslint-parser",
        "rules": {
          "no-console": "error"
        }
      }
      """.trimIndent())
    eslint.fixture.configureByText("ts.ts", "<error descr=\"ESLint: Unexpected console statement. (no-console)\">console.log</error>('hello')")
    eslint.fixture.testHighlighting(true, false, true)
  }
}
