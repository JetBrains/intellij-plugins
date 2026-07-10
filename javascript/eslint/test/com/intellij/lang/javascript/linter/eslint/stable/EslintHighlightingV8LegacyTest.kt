// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.AutodetectLinterPackage
import com.intellij.lang.javascript.linter.eslint.ESLINT_8_57_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.modules.TestNpmPackage
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
@TestNpmPackage(ESLINT_8_57_0_TEST_PACKAGE_SPEC)
class EslintHighlightingV8LegacyTest : EslintPackageLockTestBase() {
  override fun getBasePath(): String = "$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/highlighting/"

  // Legacy ignore mechanism with no flat-config equivalent: package.json "eslintIgnore".
  fun testFileIgnoredWithPackageJsonOption() = doHighlightingTestWithInstallation("src/ignoredDir/test.js")

  fun testCanDisableIgnoreFilesWithCommandLineOption() =
    doHighlightingTestWithInstallation("test.js") {
      updateConfiguration { it.setExtraOptions("--no-ignore") }
    }

  // eslintrc cascade across sub-packages (nearest .eslintrc, root:true, .eslintrc.js resolution) --
  // no flat-config equivalent.
  fun testEslintRcInSubpackage() = doHighlightingTestWithInstallation("packages/inner/js.js")

  fun testEslintignoreInSubpackageAndParent() = doHighlightingTestWithInstallation("packages/inner/js.js")

  fun testEslintIgnoreWithRelativePathInProjectSubDirectory() =
    doHighlightingTestWithInstallation("packages/foo/bar/src/ignored.js")

  // A custom (non-standard-named) eslintrc selected via the "custom config file" setting.
  fun testCustomLegacyConfig() = doHighlightingTestWithInstallation("index.js") {
    useCustomConfigFile(".eslintrc.fast.json")
  }

  // --rulesdir (removed in eslint 9) + additional rules directory, custom rule plugins on disk.
  fun testWithCustomRulesDirectories() = doHighlightingTestWithInstallation("js.js") {
    val tempDir = myFixture.tempDirFixture
    updateConfiguration {
      it.setAdditionalRulesDirPath(tempDir.getFile("customRules1")!!.path)
        .setExtraOptions("--rulesdir " + tempDir.getFile("customRules2")!!.path)
    }
  }

  // Relative-path .eslintignore inside a sub-package ignores a file there (eslintrc + .eslintignore).
  fun testEslintignoreWithRelativePathInProjectSubPackage() =
    doHighlightingTestWithAutodetectInstallation("packages/with-eslint-ignore/src/ignored.js")

  // The sub-package has no declared ESLint dependency, only an `eslintConfig` that implies one -- so
  // ESLint can be resolved from packages/inner/node_modules ONLY via AutodetectLinterPackage's
  // eslintConfig-implies-eslint branch (root's decoy `eslint` declaration blocks the sibling branch;
  // inner declares no eslint dep). A committed empty stub `node_modules/eslint` (no npm install) proves
  // that branch resolved it: the run fails trying to load the stub's lib/options and the error names
  // the inner path. Discrimination verified by inverting the eslintConfig branch locally (the test then
  // fails to resolve any package).
  fun testImplicitDependencyButEslintConfigInSubpackage() {
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("packages/inner/node_modules/eslint/lib/options", false, true)
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE)
    doEditorHighlightingTest("packages/inner/js.js")
  }

  // .eslintrc referencing an ABSOLUTE parser path resolved from the installed package -- eslintrc only.
  fun testTypescriptWithVueParserAbsolutePath() {
    installEslintForTest()
    val parserFile = File(getNodePackage().systemDependentPath, "../@typescript-eslint/parser/dist/parser.js").canonicalFile
    assertTrue(parserFile.toString(), parserFile.exists())
    myFixture.setCaresAboutInjection(false)
    myFixture.configureByText(
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
    myFixture.configureByText("ts.ts", "<error descr=\"ESLint: Unexpected console statement. (no-console)\">console.log</error>('hello')")
    myFixture.testHighlighting(true, false, true)
  }
}
