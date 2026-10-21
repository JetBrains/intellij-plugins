// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.linter.eslint.ESLINT_10_6_0_TEST_PACKAGE_SPEC
import com.intellij.lang.javascript.linter.eslint.ESLINT_VERSION_PLACEHOLDER
import com.intellij.lang.javascript.linter.eslint.EslintConfiguration
import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.lang.javascript.linter.eslint.EslintState
import com.intellij.lang.javascript.linter.eslint.getEslintTestDataPath
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.modules.TestNpmPackageInstaller
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import java.io.File
import java.nio.file.Files

/**
 * Runs the Actions on Save chain of WEB-75109 with the real tools. 'Optimize imports' reformats the import
 * block through the Prettier formatting service, `eslint --fix` runs next, and the Prettier on-save action
 * runs last. Every result must survive, and the document must end up saved.
 */
@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class OptimizeImportsEslintPrettierOnSaveTest : PrettierPackageLockTest() {
  private var eslintPackage: NodePackage? = null

  override fun tearDown() {
    eslintPackage = null
    super.tearDown()
  }

  fun testEslintFixSurvivesOptimizeImportsAndPrettier() = withSubdirInstallation("optimizeImportsEslintAndPrettierOnSave", "app") {
    installEslintAtProjectRoot()
    withEslintRunOnSave {
      configureRunOptimizeImportsAndPrettierOnSave {
        myFixture.configureFromTempProjectFile("app/toReformat.js")
        myFixture.type(' ')
        myFixture.performEditorAction("SaveDocument")
        ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(project)
        val document = myFixture.editor.document
        val unsaved = FileDocumentManager.getInstance().isDocumentUnsaved(document)
        val expected = FileUtil.loadFile(File(myFixture.testDataPath, "optimizeImportsEslintAndPrettierOnSave/app/toReformat_after.js"))
        assertEquals("Document unsaved after the Actions on Save: $unsaved", StringUtil.convertLineSeparators(expected), document.text)
        assertFalse("The document must be saved when all Actions on Save are done", unsaved)
      }
    }
  }

  /**
   * Installs ESLint into `<project>/node_modules` from the stored lock of the ESLint quick-fix test data,
   * the same way as `EslintPackageLockTestBase` does.
   */
  private fun installEslintAtProjectRoot() {
    val projectDir = myFixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
    val packageJson = projectDir.findChild("package.json") ?: error("package.json not found in the project root")
    val eslintVersion = ESLINT_10_6_0_TEST_PACKAGE_SPEC.substringAfter('@')
    WriteAction.run<Throwable> {
      VfsUtil.saveText(packageJson, VfsUtil.loadText(packageJson).replace(ESLINT_VERSION_PLACEHOLDER, eslintVersion))
    }

    val prettierTestDataPath = myFixture.testDataPath
    myFixture.testDataPath = getEslintTestDataPath() + "/linter/eslint/quickfix"
    try {
      TestNpmPackageInstaller(project, myFixture, nodeJsAppRule, copyPackageJson = false, requireStoredLock = true)
        .installForTest(EslintPackageSpec::class.java, projectDir)
    }
    finally {
      myFixture.testDataPath = prettierTestDataPath
    }

    val eslintPath = projectDir.toNioPath().resolve(NodeModuleUtil.NODE_MODULES).resolve("eslint")
    check(Files.exists(eslintPath)) { "eslint was not installed at $eslintPath" }
    val nodePackage = NodePackage(eslintPath.toString())
    VfsRootAccess.allowRootAccess(testRootDisposable, nodePackage.systemDependentPath)
    Registry.get("eslint.service.node.path").setValue(eslintPath.parent.toString(), testRootDisposable)
    eslintPackage = nodePackage
  }

  private fun withEslintRunOnSave(runnable: Runnable) {
    val nodePackage = eslintPackage ?: error("ESLint is not installed")
    // The linter configuration toggles its inspection, so the test profile must know the tool.
    myFixture.enableInspections(EslintInspection())
    val configuration = EslintConfiguration.getInstance(project)
    val originalEnabled = configuration.isEnabled
    val originalState = configuration.extendedState.state
    val state = EslintState.Builder(originalState)
      .setEslintPackage(NodePackageRef.create(nodePackage))
      .setRunOnSave(true)
      .build()
    configuration.setExtendedState(true, state)
    try {
      runnable.run()
    }
    finally {
      configuration.setExtendedState(originalEnabled, originalState)
    }
  }
}

/** Carries the ESLint package spec for [TestNpmPackageInstaller], which reads it from a class annotation. */
@TestNpmPackage(ESLINT_10_6_0_TEST_PACKAGE_SPEC)
private class EslintPackageSpec
