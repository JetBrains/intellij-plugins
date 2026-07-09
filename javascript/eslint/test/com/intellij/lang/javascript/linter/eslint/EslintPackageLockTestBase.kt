// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.linter.LinterHighlightingTest
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.modules.TestNpmPackageInstaller
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.util.ThrowableRunnable
import java.nio.file.Files

/**
 * Base class for ESLint tests that install packages per test from a committed
 * `package-lock.json` in `testData/.../_package-locks-store`, mirroring
 * [com.intellij.prettierjs.stable.PrettierPackageLockTest] but adapted to the
 * [LinterHighlightingTest] highlighting flow.
 *
 * Subclasses must be annotated with `@`[TestNpmPackage]`("eslint@X.Y.Z")`. The shared global
 * install is neutralized (see [setUpForTempRoot]); each test copies its testData directory,
 * replaces the [ESLINT_VERSION_PLACEHOLDER] in its `package.json` files with the annotated
 * version, installs from the stored lock, and points the ESLint configuration at the
 * project-local `node_modules/eslint`.
 */
abstract class EslintPackageLockTestBase : LinterHighlightingTest() {

  private var packagesInstalled = false

  /** Set by the per-test install; the fake-service helpers and assertions read it instead of the (null) global package. */
  protected var localNodePackage: NodePackage? = null

  // Packages come from the lock store per test, so nothing is installed globally.
  override fun getGlobalPackageVersionsToInstall(): Map<String, String?> = emptyMap()

  // Skip the shared global install. The Node interpreter is still set up in the base setUp(),
  // which runs before setUpForTempRoot(); only the global npm install is skipped here.
  override fun setUpForTempRoot(rootDir: java.nio.file.Path) {
    // no-op: local install happens in doHighlightingTestWithInstallation
  }

  override fun getPackageName(): String = "eslint"

  override fun getInspection(): InspectionProfileEntry = EslintInspection()

  // getNodePackage() is null in this flow (no global install); linter configuration is deferred
  // to doHighlightingTestWithInstallation, which points it at the project-local package.
  override fun configureLinter() {
    // no-op
  }

  override fun tearDown() {
    localNodePackage = null
    super.tearDown()
  }

  /**
   * Copies `testData/<TestName>` into the project, replaces [ESLINT_VERSION_PLACEHOLDER] in its
   * `package.json` files, installs from the stored `package-lock.json`, points the ESLint
   * configuration at `<project>/node_modules/eslint`, and runs editor highlighting on
   * [mainFileRelativePath]. Call once per test.
   */
  protected fun doHighlightingTestWithInstallation(
    mainFileRelativePath: String,
    configureFixture: ThrowableRunnable<Throwable>? = null,
  ) {
    check(!packagesInstalled) { "doHighlightingTestWithInstallation must be called once per test" }
    packagesInstalled = true

    WriteAction.run<RuntimeException> { FileDocumentManager.getInstance().saveAllDocuments() }
    myFixture.setCaresAboutInjection(false)
    myFixture.copyDirectoryToProject(getTestName(false), "")

    val projectDir = myFixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
    replaceEslintVersionPlaceholders(projectDir)

    TestNpmPackageInstaller(project, myFixture, nodeJsAppRule, copyPackageJson = false)
      .installForTest(this::class.java, projectDir)

    val eslintPath = projectDir.toNioPath().resolve(NodeModuleUtil.NODE_MODULES).resolve("eslint")
    check(Files.exists(eslintPath)) { "eslint was not installed at $eslintPath" }

    val nodePackage = NodePackage(eslintPath.toString())
    localNodePackage = nodePackage
    VfsRootAccess.allowRootAccess(myFixture.testRootDisposable, nodePackage.systemDependentPath)
    configureLinterForPackage(NodePackageRef.create(nodePackage))

    // EslintServiceTestBase sets this in setUp() from the global install; here the package only
    // exists after the per-test install, so the registry value is set at this point instead.
    Registry.get("eslint.service.node.path").setValue(eslintPath.parent.toString(), testRootDisposable)

    doEditorHighlightingTestWithoutCopy(mainFileRelativePath, configureFixture)
  }

  private fun replaceEslintVersionPlaceholders(rootDir: VirtualFile) {
    val annotation = this::class.java.getAnnotation(TestNpmPackage::class.java)
                     ?: error("Test class must be annotated with @TestNpmPackage")
    val version = annotation.packageSpec.substringAfter('@', "latest")

    val filesToUpdate = mutableListOf<Pair<VirtualFile, String>>()
    VfsUtil.processFileRecursivelyWithoutIgnored(rootDir) { file ->
      if (file.name == "package.json" && !file.path.contains(NodeModuleUtil.NODE_MODULES)) {
        val content = VfsUtil.loadText(file)
        if (content.contains(ESLINT_VERSION_PLACEHOLDER)) {
          filesToUpdate.add(file to content.replace(ESLINT_VERSION_PLACEHOLDER, version))
        }
      }
      true
    }

    if (filesToUpdate.isNotEmpty()) {
      WriteAction.run<Throwable> {
        for ((file, newContent) in filesToUpdate) {
          VfsUtil.saveText(file, newContent)
        }
      }
    }
  }
}
