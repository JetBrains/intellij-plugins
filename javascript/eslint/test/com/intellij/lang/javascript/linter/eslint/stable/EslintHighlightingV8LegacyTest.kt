// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint.stable

import com.intellij.lang.javascript.linter.eslint.ESLINT_8_57_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_TEST_DATA_RELATIVE_PATH
import com.intellij.lang.javascript.linter.eslint.EslintPackageLockTestBase
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.util.ThrowableRunnable
import java.io.File

/**
 * Legacy `.eslintrc`-only ESLint scenarios, pinned to eslint 8.57.0 -- the last release that supports
 * eslintrc configs (removed in eslint 9/10). These exercise behavior with no flat-config equivalent
 * (string parser resolution, eslintrc parse/fatal errors), so they stay on a frozen eslint 8 rather
 * than migrating to flat config. eslint 8.57.0 never changes, so these goldens are stable.
 */
@TestNpmPackage(ESLINT_8_57_0_TEST_PACKAGE_SPEC)
class EslintHighlightingV8LegacyTest : EslintPackageLockTestBase() {
  override fun getBasePath(): String = "$ESLINT_TEST_DATA_RELATIVE_PATH/linter/eslint/highlighting/"

  private fun doTest(mainFile: String) {
    doHighlightingTestWithInstallation(mainFile)
    doBatchInspectionTest()
  }

  fun testReportAboutWrongParser() = doTest("test.js")

  fun testESLintLocalFatalError() = doTest("test.js")

  fun testESLintGlobalFatalError() {
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("ESLint: Error: Failed to load parser 'babel'", true, false)
    doHighlightingTestWithInstallation("test.jsx")
  }

  // Legacy ignore mechanisms removed in flat config: .eslintignore and package.json "eslintIgnore".
  fun testFileIgnored() = doTest("testIgnored.js")

  fun testFileIgnoredByCommandLineOption() =
    doHighlightingTestWithInstallation("testIgnored.js") {
      updateConfiguration { it.setExtraOptions("--ignore-pattern '*.js'") }
    }

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

  // No flat config present -> ESLint falls back to the legacy .eslintrc config.
  fun testFallbackToLegacyConfig() = doHighlightingTestWithInstallation("index.js")

  // Package autodetection from the project-local node_modules (eslintConfig in package.json).
  fun testCanAutodetectLocalPackage() = doHighlightingTestWithAutodetectInstallation("js.js")

  // Autodetect ESLint from a parent directory's node_modules for a file in a sub-workspace.
  fun testCanAutodetectLocalPackageInParentNodeModules() =
    doHighlightingTestWithAutodetectInstallation("workspaces/a/js.js")

  // .eslintrc.js that `extends` a sibling config via a cwd-relative path.resolve() -- eslintrc only.
  fun testConfigReferencesLocalFiles() = doHighlightingTestWithAutodetectInstallation("packages/a/js.js")

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

  // A sub-package with no ESLint of its own resolves the parent's, found by walking up node_modules.
  fun testSubpackageContainsOnlyLinkToParentEslint() =
    doHighlightingTestWithAutodetectInstallation("packages/inner/js.js")

  // The sub-package has no explicit ESLint dependency but its eslintConfig implies one, so ESLint is
  // resolved from packages/inner/node_modules -- proven by the fact that it loads that package's
  // eslintConfig and reports its unresolved "react-app" extends. (The exact resolution path in the
  // message is separator- and eslint-version-specific, so we assert the stable error text instead.)
  fun testImplicitDependencyButEslintConfigInSubpackage() {
    myExpectedGlobalAnnotation = ExpectedGlobalAnnotation("Failed to load config \"react-app\" to extend from", false, true)
    installEslintForTestInSubdir("packages/inner")
    doEditorHighlightingTestWithoutCopy("packages/inner/js.js", null as ThrowableRunnable<Throwable>?)
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
