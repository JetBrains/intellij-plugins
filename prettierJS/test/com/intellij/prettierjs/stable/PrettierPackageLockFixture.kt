// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptionsBase
import com.intellij.codeInsight.actions.onSave.OptimizeImportsOnSaveOptions
import com.intellij.javascript.debugger.NodeJsAppRule
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.modules.JSNodeInterpreterFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.modules.TestNpmPackageInstaller
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.prettierjs.PrettierLanguageService
import com.intellij.prettierjs.PrettierLanguageServiceManager
import com.intellij.prettierjs.PrettierUtil
import com.intellij.prettierjs.ReformatWithPrettierAction
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.util.ThrowableRunnable
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.lang.reflect.Method

/**
 * Prettier version constants for tests.
 *
 * If you change a version number, please sync the stored lock files for the corresponding version here:
 * $PROJECT_ROOT/contrib/prettierJS/testData/reformat/_package-locks-store
 */
const val PRETTIER_3_8_1_TEST_PACKAGE_SPEC: String = "prettier@3.8.1"

// next versions
const val PRETTIER_LATEST_TEST_PACKAGE_SPEC: String = "prettier@latest"

/**
 * Placeholder for prettier version in package.json files.
 * This placeholder will be replaced with the actual version from @TestNpmPackage annotation at test runtime.
 */
const val PRETTIER_VERSION_PLACEHOLDER: String = "\$PRETTIER_VERSION\$"

/**
 * The fixture of the Prettier tests that install packages per test from a cached `package-lock.json`.
 * Create it with [prettierPackageLockFixture].
 *
 * The test class must be annotated with [@TestNpmPackage][TestNpmPackage]("prettier@X.Y.Z").
 * The fixture installs no global package.
 */
class PrettierPackageLockFixture(
  fixture: CodeInsightTestFixture,
  testClass: Class<*>,
  testMethod: Method,
  options: JSTempDirFixtureOptions,
) : JSNodeInterpreterFixture(fixture, testClass, testMethod, options) {

  private var localNodePackage: NodePackage? = null
  private var packagesInstalled: Boolean = false

  /** The locally installed Prettier package. It is `getNodePackage()` of the JUnit 3 base. */
  val nodePackage: NodePackage
    get() = localNodePackage ?: error("NodePackage not initialized. Call setUp() first.")

  /** The file of [fixture]. It is the `getFile()` override of the JUnit 3 base. */
  val file: PsiFile get() = fixture.file

  /**
   * Wrapper for tests that need default installation (packages at root).
   * Most tests should use this wrapper.
   *
   * This wrapper:
   * 1. Copies test data directory (using getTestName(true))
   * 2. Installs packages at root
   * 3. Replaces version placeholders
   *
   * Example:
   * ```
   * fun testWithoutConfig() = prettier.withInstallation {
   *   prettier.doReformatFile("js")
   * }
   * ```
   */
  fun <T> withInstallation(block: () -> T): T {
    // Copy test data first
    val dirName = getTestName(true)
    fixture.copyDirectoryToProject(dirName, "")

    // Replace version placeholders in package.json files before installation
    val projectDir = fixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
    replacePrettierVersionPlaceholders(projectDir)
    installPackages()

    return block()
  }

  /**
   * Wrapper for tests that need subdirectory installation (packages in subdir).
   *
   * @param testDataSubdir Name of the test data directory to copy (usually getTestName(true))
   * @param installSubdir Name of the subdirectory where packages should be installed
   *
   * Example:
   * ```
   * fun testAutoconfigured() = prettier.withSubdirInstallation("autoconfigured", "subdir") {
   *   prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("subdir/formatted.js"))
   *   prettier.runReformatAction()
   *   prettier.fixture.checkResultByFile("autoconfigured/subdir/formatted_after.js")
   * }
   * ```
   */
  fun <T> withSubdirInstallation(
    testDataSubdir: String,
    installSubdir: String,
    block: () -> T,
  ): T {
    // Copy test data first so subdirectory exists
    fixture.copyDirectoryToProject(testDataSubdir, "")

    // Replace version placeholders in the subdirectory before installation
    val projectDir = fixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
    val subdir = projectDir.findChild(installSubdir) ?: error("Subdirectory not found: $installSubdir")
    replacePrettierVersionPlaceholders(subdir)
    installPackages(installSubdir)

    return block()
  }

  /**
   * Installs prettier packages and configures the Prettier plugin.
   * This is called automatically by test wrapper functions.
   *
   * @param installSubdir Optional subdirectory where packages should be installed (relative to project root)
   */
  private fun installPackages(installSubdir: String? = null) {
    if (packagesInstalled) {
      error("Packages already installed. Call this only once per test.")
    }
    packagesInstalled = true

    val projectDir = fixture.tempDirFixture.getFile(".")
                     ?: error("Project directory not found")

    runInEdtAndWait {
      TestNpmPackageInstaller(project, fixture, nodeJsAppRule, copyPackageJson = false)
        .installForTest(testClass, projectDir, installSubdir)
    }

    // Determine where prettier was installed
    val installDir = if (installSubdir != null) {
      projectDir.findChild(installSubdir) ?: error("Install subdirectory not found: $installSubdir")
    }
    else {
      projectDir
    }

    // Set up NodePackage from locally installed prettier
    val prettierPath = installDir.toNioPath()
      .resolve(NodeModuleUtil.NODE_MODULES)
      .resolve(PrettierUtil.PACKAGE_NAME)

    check(File(prettierPath.toString()).exists()) {
      "Expected prettier package to exist at $prettierPath"
    }

    val prettierPackage = NodePackage(prettierPath.toString())
    localNodePackage = prettierPackage
    VfsRootAccess.allowRootAccess(testRootDisposable, prettierPackage.systemDependentPath)

    // Configure Prettier to use the locally installed package
    PrettierConfiguration.getInstance(project)
      .withLinterPackage(NodePackageRef.create(prettierPackage))
  }

  /**
   * Warms up the Prettier language service by calling resolveConfig without a timeout.
   * On Windows, the Node.js process cold start can exceed the 500ms timeout used by
   * [com.intellij.prettierjs.codeStyle.PrettierCodeStyleSettingsModifier],
   * causing it to silently return default settings.
   * Call this before querying code style settings to ensure the service is ready.
   */
  fun warmUpPrettierService(contextFile: VirtualFile) {
    val prettierPackage = nodePackage
    val service = PrettierLanguageService.getInstance(project, contextFile, prettierPackage)
    // Just wait for the result — the goal is to start the Node.js process, not to validate the config.
    service.resolveConfig(contextFile.path, prettierPackage).get()
  }

  /**
   * Replaces [PRETTIER_VERSION_PLACEHOLDER] in all package.json files under the given directory
   * with the actual prettier version from the @TestNpmPackage annotation.
   */
  fun replacePrettierVersionPlaceholders(rootDir: VirtualFile) {
    val annotation = testClass.getAnnotation(TestNpmPackage::class.java)
                     ?: error("Test class must be annotated with @TestNpmPackage")

    val packageSpec = annotation.packageSpec
    // Extract version from spec like "prettier@3.2.5"
    val version = packageSpec.substringAfter('@', "latest")

    val filesToUpdate = mutableListOf<Pair<VirtualFile, String>>()

    VfsUtil.processFileRecursivelyWithoutIgnored(rootDir) { file ->
      if (file.name == "package.json" && !file.path.contains("node_modules")) {
        val content = VfsUtil.loadText(file)
        if (content.contains(PRETTIER_VERSION_PLACEHOLDER)) {
          val newContent = content.replace(PRETTIER_VERSION_PLACEHOLDER, version)
          filesToUpdate.add(file to newContent)
        }
      }
      true
    }

    if (filesToUpdate.isNotEmpty()) {
      WriteAction.runAndWait<Throwable> {
        for ((file, newContent) in filesToUpdate) {
          VfsUtil.saveText(file, newContent)
        }
      }
    }
  }

  /**
   * Moves an installed package from the root `node_modules` into the `node_modules` of [targetDir].
   * npm hoists every package into the root. The move gives a nested package its own copy, as pnpm does.
   */
  fun moveInstalledPackage(packageName: String, targetDir: String) {
    val installedPackage = fixture.findFileInTempDir("${NodeModuleUtil.NODE_MODULES}/$packageName")
                           ?: error("Package $packageName is not installed")
    val target = fixture.findFileInTempDir(targetDir) ?: error("Directory $targetDir not found")
    WriteAction.runAndWait<Throwable> {
      installedPackage.move(this, VfsUtil.createDirectoryIfMissing(target, NodeModuleUtil.NODE_MODULES))
    }
  }

  fun doReformatFile(extension: String) {
    doReformatFile("toReformat", extension)
  }

  fun doReformatFile(fileNamePrefix: String, extension: String) {
    doReformatFile<Throwable>(fileNamePrefix, extension, null)
  }

  fun <T : Throwable> doReformatFile(
    fileNamePrefix: String,
    extension: String,
    configureFixture: ThrowableRunnable<T>?,
  ) {
    val dirName = getTestName(true)
    val extensionWithDot = if (StringUtil.isEmpty(extension)) "" else ".$extension"
    fixture.configureFromExistingVirtualFile(fixture.findFileInTempDir(fileNamePrefix + extensionWithDot))
    configureFixture?.run()
    runReformatAction()
    fixture.checkResultByFile("$dirName/$fileNamePrefix" + "_after" + extensionWithDot)
  }

  fun runReformatAction() {
    runInEdtAndWait {
      fixture.testAction(ReformatWithPrettierAction())
    }
  }

  fun assertError(checkException: Condition<String>, runnable: Runnable) {
    runnable.run()

    val manager = PrettierLanguageServiceManager.getInstance(project)
    val service = manager.jsLinterServices.values.firstOrNull()
    val error = service?.service?.error
    assertTrue(checkException.value(error?.message), "Expected condition to be valid for exception: ${error?.message}")
  }

  fun configureRunOnSave(runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val runOnSave = configuration.state.runOnSave
    val runOnReformat = configuration.state.runOnReformat
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnSave = true
    configuration.state.runOnReformat = false
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC

    try {
      // Copy prettier to node_modules (test data already copied by withInstallation)
      fixture.tempDirFixture.copyAll(nodePackage.systemIndependentPath, "node_modules/prettier")

      runnable.run()
    }
    finally {
      configuration.state.runOnSave = runOnSave
      configuration.state.runOnReformat = runOnReformat
      configuration.state.configurationMode = configurationMode
    }
  }

  /**
   * Enables both Prettier-on-save and Optimize Imports-on-save for JavaScript files.
   * Used to exercise the WEB-76498 deadlock scenario: rapid saves chain OI's latch wait
   * with Prettier's BG write, which previously deadlocked.
   *
   * Registers a transient `FormatOnSaveOptionsBase.DefaultsProvider` that adds JavaScript
   * to the on-save 'Optimize imports' file-type set. The extension is registered BEFORE the
   * first `OptimizeImportsOnSaveOptions.getInstance(project)` call so the service's State
   * picks it up at construction time. The extension is removed via `testRootDisposable`.
   */
  fun configureRunOptimizeImportsAndPrettierOnSave(runnable: Runnable) {
    OPTIMIZE_IMPORTS_DEFAULTS_EP.point.registerExtension(JsOptimizeImportsOnSaveDefaults, testRootDisposable)

    val configuration = PrettierConfiguration.getInstance(project)
    val origRunOnSave = configuration.state.runOnSave
    val origRunOnReformat = configuration.state.runOnReformat
    val origConfigMode = configuration.state.configurationMode

    val oiOptions = OptimizeImportsOnSaveOptions.getInstance(project)
    val origOiEnabled = oiOptions.isRunOnSaveEnabled

    configuration.state.runOnSave = true
    configuration.state.runOnReformat = false
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC

    oiOptions.isRunOnSaveEnabled = true

    try {
      // Copy prettier to node_modules (test data already copied by withInstallation)
      fixture.tempDirFixture.copyAll(nodePackage.systemIndependentPath, "node_modules/prettier")

      runnable.run()
    }
    finally {
      configuration.state.runOnSave = origRunOnSave
      configuration.state.runOnReformat = origRunOnReformat
      configuration.state.configurationMode = origConfigMode

      oiOptions.isRunOnSaveEnabled = origOiEnabled
    }
  }

  fun doTestSaveAction(actionId: String, subDir: String) {
    doTestSaveAction(actionId, subDir, null)
  }

  fun doTestSaveAction(actionId: String, subDir: String, configureFile: Runnable?) {
    val dirName = getTestName(true)
    fixture.configureFromTempProjectFile(subDir + "toReformat.js")
    configureFile?.run()
    runInEdtAndWait {
      fixture.type(' ')
      fixture.performEditorAction(actionId)
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(fixture.project)
    }
    fixture.checkResultByFile("$dirName/$subDir" + "toReformat_after.js")
  }

  fun configureRunOnReformat(runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val origRunOnReformat = configuration.state.runOnReformat
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnReformat = true
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC

    try {
      // Copy prettier to node_modules (test data already copied by withInstallation)
      fixture.tempDirFixture.copyAll(nodePackage.systemIndependentPath, "node_modules/prettier")

      runnable.run()
    }
    finally {
      configuration.state.runOnReformat = origRunOnReformat
      configuration.state.configurationMode = configurationMode
    }
  }

  fun configureMode(mode: PrettierConfiguration.ConfigurationMode, runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val configurationMode = configuration.state.configurationMode

    configuration.state.configurationMode = mode

    try {
      runnable.run()
    }
    finally {
      configuration.state.configurationMode = configurationMode
    }
  }

  fun doTestEditorReformat(subDir: String) {
    doTestEditorReformat(subDir, null)
  }

  fun doTestEditorReformat(subDir: String, configureFile: Runnable?) {
    val dirName = getTestName(true)
    fixture.configureFromTempProjectFile(subDir + "toReformat.js")
    configureFile?.run()
    runInEdtAndWait {
      fixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
    }
    fixture.checkResultByFile("$dirName/$subDir" + "toReformat_after.js")
  }

  fun configureFormatFilesOutsideDependencyScope(enabled: Boolean, runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val runOnSave = configuration.state.runOnSave
    val runOnReformat = configuration.state.runOnReformat
    val configurationMode = configuration.state.configurationMode
    val formatFilesOutsideDependencyScope = configuration.state.formatFilesOutsideDependencyScope

    configuration.state.runOnSave = true
    configuration.state.runOnReformat = true
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
    configuration.state.formatFilesOutsideDependencyScope = enabled

    try {
      // Test data already copied by withInstallation
      runnable.run()
    }
    finally {
      configuration.state.runOnSave = runOnSave
      configuration.state.runOnReformat = runOnReformat
      configuration.state.configurationMode = configurationMode
      configuration.state.formatFilesOutsideDependencyScope = formatFilesOutsideDependencyScope
    }
  }

  companion object {
    private val OPTIMIZE_IMPORTS_DEFAULTS_EP = ExtensionPointName.create<FormatOnSaveOptionsBase.DefaultsProvider>(
      "com.intellij.formatOnSaveOptions.defaultsProvider"
    )

    private val JsOptimizeImportsOnSaveDefaults = object : FormatOnSaveOptionsBase.DefaultsProvider {
      override fun getFileTypesWithOptimizeImportsOnSaveByDefault(): Collection<FileType> =
        listOf(JavaScriptFileType)
    }

    /**
     * Creates a [PrettierPackageLockFixture] on a new project, for the test data in [testDataDir] of the Prettier test data.
     * [nodeJsAppRule] replaces an override of `configureInterpreterVersion()`; `null` keeps the default Node.js version.
     * Declare the result as an instance property, so that each test gets its own project.
     */
    fun prettierPackageLockFixture(
      testDataDir: String = "reformat",
      nodeJsAppRule: NodeJsAppRule? = null,
    ): TestFixture<PrettierPackageLockFixture> {
      val options = JSTempDirFixtureOptions(PrettierJSTestUtil.getTestDataPath() + testDataDir).withoutGlobalInstall()
      return jsTempDirFixture(
        "prettierPackageLockFixture",
        if (nodeJsAppRule != null) options.withNodeVersion(nodeJsAppRule) else options,
        ::PrettierPackageLockFixture,
      )
    }
  }
}
