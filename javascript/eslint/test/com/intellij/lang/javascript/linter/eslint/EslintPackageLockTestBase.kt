// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.eslint

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.linter.AutodetectLinterPackage
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult
import com.intellij.lang.javascript.linter.LinterHighlightingTest
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.modules.TestNpmPackageInstaller
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.psi.PsiFile
import com.intellij.testFramework.InspectionTestUtil
import com.intellij.testFramework.createGlobalContextForTool
import com.intellij.testFramework.fixtures.impl.GlobalInspectionContextForTests
import com.intellij.util.ThrowableRunnable
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Base class for ESLint tests that install packages per test from a committed `package-lock.json` in
 * `testData/.../_package-locks-store`, mirroring
 * [com.intellij.prettierjs.stable.PrettierPackageLockTest] but adapted to the [LinterHighlightingTest]
 * highlighting flow.
 *
 * Subclasses must be annotated with `@`[TestNpmPackage]`("eslint@X.Y.Z")`. The shared global install is
 * neutralized (see [setUpForTempRoot]); each test copies its testData directory, replaces the
 * [ESLINT_VERSION_PLACEHOLDER] in its `package.json` files with the annotated version, installs from the
 * stored lock (strictly — a store miss for a pinned spec fails, see [TestNpmPackageInstaller]), and
 * points the ESLint configuration at the project-local `node_modules/eslint`.
 */
abstract class EslintPackageLockTestBase : LinterHighlightingTest() {

  private var packagesInstalled = false

  // Packages come from the lock store per test, so nothing is installed globally.
  override fun getGlobalPackageVersionsToInstall(): Map<String, String?> = emptyMap()

  // Skip the shared global install. The Node interpreter is still set up in the base setUp(), which
  // runs before setUpForTempRoot(); only the global npm install is skipped here.
  override fun setUpForTempRoot(rootDir: Path) {
    // no-op: local install happens in doHighlightingTestWithInstallation
  }

  override fun getPackageName(): String = "eslint"

  override fun getInspection(): InspectionProfileEntry = EslintInspection()

  // getNodePackage() is unset until the per-test install, so linter configuration is deferred to
  // doHighlightingTestWithInstallation, which points it at the project-local package.
  override fun configureLinter() {
    // no-op
  }

  /**
   * ESLint routes file-level annotations through [EslintLanguageServiceManager], not the
   * `JSLinterEditorNotifications` channel that [LinterHighlightingTest.getAnnotationText] reads (see
   * `ESLintAnnotationsBuilder.applyFileLevelAnnotation`). Without this override the tearDown
   * global-annotation check would be vacuous and file-level-error scenarios could not be asserted
   * (WEB-67129 lineage).
   */
  override fun getAnnotationText(): String? =
    getEslintFileLevelAnnotationText(project, myFixture.file.virtualFile)

  /** [installEslintForTest] followed by editor highlighting on [mainFileRelativePath]. Call once per test. */
  protected fun doHighlightingTestWithInstallation(
    mainFileRelativePath: String,
    configureFixture: ThrowableRunnable<Throwable>? = null,
  ) {
    installEslintForTest()
    doEditorHighlightingTestWithoutCopy(mainFileRelativePath, configureFixture)
  }

  /**
   * 1) copies `testData/<TestName>` into the project, 2) replaces [ESLINT_VERSION_PLACEHOLDER] in its
   * `package.json` files, 3) installs from the stored `package-lock.json`, 4) points the ESLint
   * configuration at `<project>/node_modules/eslint`. Call once per test, before highlighting or the
   * fake-service helper.
   */
  protected fun installEslintForTest() {
    copyTestDataToProject()
    installEslintFromProjectRoot()
  }

  /** Like [installEslintForTest], but leaves the linter set to auto-detect the project-local ESLint
   *  (AutodetectLinterPackage) rather than a fixed package -- for the autodetection tests. */
  protected fun installEslintForTestWithAutodetect() {
    copyTestDataToProject()
    installEslintFromProjectRoot(autodetect = true)
  }

  /** Installs ESLint into `<project>/<subdir>/node_modules` (from that sub-directory's `package.json` +
   *  stored lock) and auto-detects it -- for tests asserting resolution from a specific sub-package. */
  protected fun installEslintForTestInSubdir(subdir: String) {
    copyTestDataToProject()
    installEslintFromProjectRoot(autodetect = true, installSubdir = subdir)
  }

  /** [installEslintForTestWithAutodetect] followed by editor highlighting on [mainFileRelativePath]. */
  protected fun doHighlightingTestWithAutodetectInstallation(
    mainFileRelativePath: String,
    configureFixture: ThrowableRunnable<Throwable>? = null,
  ) {
    installEslintForTestWithAutodetect()
    doEditorHighlightingTestWithoutCopy(mainFileRelativePath, configureFixture)
  }

  /**
   * Installs ESLint from the `package.json`/stored lock already present in the project root, then
   * points the ESLint configuration at `<project>/node_modules/eslint`. The project root must already
   * contain the `package.json` (and any config): [installEslintForTest] copies the per-test data
   * directory first, while the quick-fix suite copies a shared root install instead. Call once per test.
   */
  protected fun installEslintFromProjectRoot(autodetect: Boolean = false, installSubdir: String? = null) {
    check(!packagesInstalled) { "ESLint must be installed once per test" }
    packagesInstalled = true

    val projectDir = myFixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
    replaceEslintVersionPlaceholders(projectDir)

    TestNpmPackageInstaller(project, myFixture, nodeJsAppRule, copyPackageJson = false, requireStoredLock = true)
      .installForTest(this::class.java, projectDir, installSubdir)

    val installDir = if (installSubdir != null) projectDir.toNioPath().resolve(installSubdir) else projectDir.toNioPath()
    val eslintPath = installDir.resolve(NodeModuleUtil.NODE_MODULES).resolve("eslint")
    check(Files.exists(eslintPath)) { "eslint was not installed at $eslintPath" }

    val nodePackage = NodePackage(eslintPath.toString())
    setNodePackage(nodePackage)
    VfsRootAccess.allowRootAccess(myFixture.testRootDisposable, nodePackage.systemDependentPath)
    if (autodetect) {
      configureLinterForPackage(AutodetectLinterPackage.INSTANCE)
    }
    else {
      configureLinterForPackage(NodePackageRef.create(nodePackage))
    }

    // EslintServiceTestBase sets this in setUp() from the global install; here the package only exists
    // after the per-test install, so the registry value is set at this point instead.
    Registry.get("eslint.service.node.path").setValue(eslintPath.parent.toString(), testRootDisposable)
  }

  /**
   * Fabricates a stub `node_modules/eslint` (a package.json only -- no npm install) so [getNodePackage]
   * and `Files.exists` are satisfied for tests that never actually run ESLint, e.g. the never-responding
   * fake-service timeout test. Keeps the `@latest` canary offline: no registry install to hang on.
   */
  protected fun installStubEslintPackage() {
    // A real (supported) version string so the plugin's version check passes; the fake service never
    // loads this package, so no actual ESLint code is needed.
    myFixture.tempDirFixture.createFile("node_modules/eslint/package.json", """{"name":"eslint","version":"8.57.0"}""")
    val eslintPath = (myFixture.tempDirFixture.getFile(".") ?: error("Project directory not found"))
      .toNioPath().resolve(NodeModuleUtil.NODE_MODULES).resolve("eslint")
    val nodePackage = NodePackage(eslintPath.toString())
    setNodePackage(nodePackage)
    VfsRootAccess.allowRootAccess(myFixture.testRootDisposable, nodePackage.systemDependentPath)
    configureLinterForPackage(NodePackageRef.create(nodePackage))
  }

  /**
   * Runs the ESLint analysis pipeline against a fake service whose request never completes, so the
   * analysis times out deterministically without spawning a real node process (WEB-67172 rationale).
   * Call after [installEslintForTest]; set the timeout first
   * (e.g. `JSLanguageServiceUtil.setTimeout(1L, testRootDisposable)`). Shared with the global-install
   * tiers via [EslintTestUtil.highlightWithNeverRespondingService].
   */
  protected fun highlightWithNeverRespondingService(psiFile: PsiFile): JSLinterAnnotationResult =
    highlightWithNeverRespondingService(project, psiFile, getNodePackage())

  /**
   * Runs the batch (whole-project) ESLint inspection and compares against the current test dir's
   * `expected.xml`, exercising the Annotation -> ProblemDescriptor conversion path. Call after
   * [doHighlightingTestWithInstallation], which performs the per-test install.
   */
  protected fun doBatchInspectionTest() {
    val toolWrapper = LocalInspectionToolWrapper(EslintInspection())
    val scope = AnalysisScope(myFixture.project)
    scope.invalidate()
    val globalContext: GlobalInspectionContextForTests =
      createGlobalContextForTool(scope, project, listOf<InspectionToolWrapper<*, *>>(toolWrapper))
    InspectionTestUtil.runTool(toolWrapper, scope, globalContext)
    InspectionTestUtil.compareToolResults(globalContext, toolWrapper, false, File(testDataPath, getTestName(false)).path)
  }

  /**
   * Points ESLint at an explicit (non-standard-named) config file already present in the project root,
   * as the "custom config file" IDE setting does. [fileName] is resolved in the temp project (copied
   * there by [installEslintForTest]). Call after the install, before highlighting.
   */
  protected fun useCustomConfigFile(fileName: String) {
    val configFile = myFixture.findFileInTempDir(fileName) ?: error("Config file '$fileName' not found in project")
    updateConfiguration {
      it.setCustomConfigFileUsed(true)
        .setCustomConfigFilePath(VfsUtilCore.virtualToIoFile(configFile).absolutePath)
    }
  }

  /** Mutates the ESLint extended state (e.g. extra CLI options) for the current test. */
  protected fun updateConfiguration(configure: (EslintState.Builder) -> EslintState.Builder) {
    val configuration = EslintConfiguration.getInstance(project)
    val builder = configure(EslintState.Builder(configuration.extendedState.state))
    configuration.setExtendedState(true, builder.build())
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
