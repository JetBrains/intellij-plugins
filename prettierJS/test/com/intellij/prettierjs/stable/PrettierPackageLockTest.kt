// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.prettierjs.stable

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptionsBase
import com.intellij.codeInsight.actions.onSave.OptimizeImportsOnSaveOptions
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileTypes.FileType
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.modules.TestNpmPackageInstaller
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.WriteAction
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
import com.intellij.util.ThrowableRunnable
import org.junit.Assert
import java.io.File
import java.nio.file.Path

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
 * Base class for Prettier tests using cached package-lock.json files.
 *
 * Subclasses must be annotated with [@TestNpmPackage][TestNpmPackage]("prettier@X.Y.Z").
 */
abstract class PrettierPackageLockTest : JSTempDirWithNodeInterpreterTest() {

  protected var localNodePackage: NodePackage? = null
  protected var packagesInstalled: Boolean = false

  override fun getGlobalPackageVersionsToInstall(): Map<String, String> = emptyMap()

  override fun setUpForTempRoot(rootDir: Path) {
    // Skip parent implementation that installs global packages
    // We'll install packages locally on-demand in tests
  }

  override fun setUp() {
    super.setUp()
    myFixture.testDataPath = PrettierJSTestUtil.getTestDataPath() + "reformat"
    packagesInstalled = false
  }


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
   * fun testWithoutConfig() = withInstallation {
   *   doReformatFile("js")
   * }
   * ```
   */
  protected fun <T> withInstallation(block: () -> T): T {
    // Copy test data first
    val dirName = getTestName(true)
    myFixture.copyDirectoryToProject(dirName, "")
    
    // Replace version placeholders in package.json files before installation
    val projectDir = myFixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
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
   * fun testAutoconfigured() = withSubdirInstallation("autoconfigured", "subdir") {
   *   myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("subdir/formatted.js"))
   *   runReformatAction()
   *   myFixture.checkResultByFile("autoconfigured/subdir/formatted_after.js")
   * }
   * ```
   */
  protected fun <T> withSubdirInstallation(
    testDataSubdir: String,
    installSubdir: String,
    block: () -> T
  ): T {
    // Copy test data first so subdirectory exists
    myFixture.copyDirectoryToProject(testDataSubdir, "")

    // Replace version placeholders in the subdirectory before installation
    val projectDir = myFixture.tempDirFixture.getFile(".") ?: error("Project directory not found")
    val subdir = projectDir.findChild(installSubdir) ?: error("Subdirectory not found: $installSubdir")
    replacePrettierVersionPlaceholders(subdir)
    installPackages(installSubdir)

    return block()
  }

  /**
   * Installs prettier packages and configures the Prettier plugin.
   * This is called automatically by test wrapper functions, or can be called manually if needed.
   * 
   * @param installSubdir Optional subdirectory where packages should be installed (relative to project root)
   */
  private fun installPackages(installSubdir: String? = null) {
    if (packagesInstalled) {
      error("Packages already installed. Call this only once per test.")
    }
    packagesInstalled = true

    val projectDir = myFixture.tempDirFixture.getFile(".")
                     ?: error("Project directory not found")

    TestNpmPackageInstaller(project, myFixture, nodeJsAppRule, copyPackageJson = false)
      .installForTest(this::class.java, projectDir, installSubdir)

    // Determine where prettier was installed
    val installDir = if (installSubdir != null) {
      projectDir.findChild(installSubdir) ?: error("Install subdirectory not found: $installSubdir")
    } else {
      projectDir
    }

    // Set up NodePackage from locally installed prettier
    val prettierPath = installDir.toNioPath()
      .resolve(NodeModuleUtil.NODE_MODULES)
      .resolve(PrettierUtil.PACKAGE_NAME)

    check(File(prettierPath.toString()).exists()) {
      "Expected prettier package to exist at $prettierPath"
    }

    val nodePackage = NodePackage(prettierPath.toString())
    localNodePackage = nodePackage
    VfsRootAccess.allowRootAccess(myFixture.testRootDisposable, nodePackage.systemDependentPath)

    // Configure Prettier to use the locally installed package
    PrettierConfiguration.getInstance(project)
      .withLinterPackage(NodePackageRef.create(nodePackage))
  }

  override fun tearDown() {
    localNodePackage = null
    super.tearDown()
  }

  protected fun getNodePackage(): NodePackage {
    return localNodePackage ?: error("NodePackage not initialized. Call setUp() first.")
  }

  /**
   * Warms up the Prettier language service by calling resolveConfig without a timeout.
   * On Windows, the Node.js process cold start can exceed the 500ms timeout used by
   * [com.intellij.prettierjs.codeStyle.PrettierCodeStyleSettingsModifier],
   * causing it to silently return default settings.
   * Call this before querying code style settings to ensure the service is ready.
   */
  protected fun warmUpPrettierService(contextFile: VirtualFile) {
    val nodePackage = getNodePackage()
    val service = PrettierLanguageService.getInstance(project, contextFile, nodePackage)
    // Just wait for the result — the goal is to start the Node.js process, not to validate the config.
    service.resolveConfig(contextFile.path, nodePackage).get()
  }

  /**
   * Replaces [PRETTIER_VERSION_PLACEHOLDER] in all package.json files under the given directory
   * with the actual prettier version from the @TestNpmPackage annotation.
   */
  protected fun replacePrettierVersionPlaceholders(rootDir: VirtualFile) {
    val annotation = this::class.java.getAnnotation(TestNpmPackage::class.java)
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
      WriteAction.run<Throwable> {
        for ((file, newContent) in filesToUpdate) {
          VfsUtil.saveText(file, newContent)
        }
      }
    }
  }

  protected fun doReformatFile(extension: String) {
    doReformatFile("toReformat", extension)
  }

  protected fun doReformatFile(fileNamePrefix: String, extension: String) {
    doReformatFile<Throwable>(fileNamePrefix, extension, null)
  }

  protected fun <T : Throwable> doReformatFile(
    fileNamePrefix: String,
    extension: String,
    configureFixture: ThrowableRunnable<T>?,
  ) {
    val dirName = getTestName(true)
    val extensionWithDot = if (StringUtil.isEmpty(extension)) "" else ".$extension"
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(fileNamePrefix + extensionWithDot))
    configureFixture?.run()
    runReformatAction()
    myFixture.checkResultByFile("$dirName/$fileNamePrefix" + "_after" + extensionWithDot)
  }

  protected fun runReformatAction() {
    myFixture.testAction(ReformatWithPrettierAction())
  }

  protected fun assertError(checkException: Condition<String>, runnable: Runnable) {
    runnable.run()

    val manager = PrettierLanguageServiceManager.getInstance(project)
    val service = manager.jsLinterServices.values.firstOrNull()
    val error = service?.service?.error
    Assert.assertTrue(
      "Expected condition to be valid for exception: ${error?.message}",
      checkException.value(error?.message)
    )
  }

  protected fun configureRunOnSave(runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val runOnSave = configuration.state.runOnSave
    val runOnReformat = configuration.state.runOnReformat
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnSave = true
    configuration.state.runOnReformat = false
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC

    try {
      // Copy prettier to node_modules (test data already copied by withInstallation)
      myFixture.tempDirFixture.copyAll(getNodePackage().systemIndependentPath, "node_modules/prettier")

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
   * picks it up at construction time. The extension is removed via `myFixture.testRootDisposable`.
   */
  protected fun configureRunOptimizeImportsAndPrettierOnSave(runnable: Runnable) {
    OPTIMIZE_IMPORTS_DEFAULTS_EP.point.registerExtension(JsOptimizeImportsOnSaveDefaults, myFixture.testRootDisposable)

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
      myFixture.tempDirFixture.copyAll(getNodePackage().systemIndependentPath, "node_modules/prettier")

      runnable.run()
    }
    finally {
      configuration.state.runOnSave = origRunOnSave
      configuration.state.runOnReformat = origRunOnReformat
      configuration.state.configurationMode = origConfigMode

      oiOptions.isRunOnSaveEnabled = origOiEnabled
    }
  }

  private companion object {
    val OPTIMIZE_IMPORTS_DEFAULTS_EP = ExtensionPointName.create<FormatOnSaveOptionsBase.DefaultsProvider>(
      "com.intellij.formatOnSaveOptions.defaultsProvider"
    )

    val JsOptimizeImportsOnSaveDefaults = object : FormatOnSaveOptionsBase.DefaultsProvider {
      override fun getFileTypesWithOptimizeImportsOnSaveByDefault(): Collection<FileType> =
        listOf(JavaScriptFileType)
    }
  }

  protected fun doTestSaveAction(actionId: String, subDir: String) {
    doTestSaveAction(actionId, subDir, null)
  }

  protected fun doTestSaveAction(actionId: String, subDir: String, configureFile: Runnable?) {
    val dirName = getTestName(true)
    myFixture.configureFromTempProjectFile(subDir + "toReformat.js")
    configureFile?.run()
    myFixture.type(' ')
    myFixture.performEditorAction(actionId)
    ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(myFixture.project)
    myFixture.checkResultByFile("$dirName/$subDir" + "toReformat_after.js")
  }

  protected fun configureRunOnReformat(runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val origRunOnReformat = configuration.state.runOnReformat
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnReformat = true
    configuration.state.configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC

    try {
      // Copy prettier to node_modules (test data already copied by withInstallation)
      myFixture.tempDirFixture.copyAll(getNodePackage().systemIndependentPath, "node_modules/prettier")

      runnable.run()
    }
    finally {
      configuration.state.runOnReformat = origRunOnReformat
      configuration.state.configurationMode = configurationMode
    }
  }

  protected fun configureMode(mode: PrettierConfiguration.ConfigurationMode, runnable: Runnable) {
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

  protected fun doTestEditorReformat(subDir: String) {
    doTestEditorReformat(subDir, null)
  }

  protected fun doTestEditorReformat(subDir: String, configureFile: Runnable?) {
    val dirName = getTestName(true)
    myFixture.configureFromTempProjectFile(subDir + "toReformat.js")
    configureFile?.run()
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
    myFixture.checkResultByFile("$dirName/$subDir" + "toReformat_after.js")
  }

  protected fun configureFormatFilesOutsideDependencyScope(enabled: Boolean, runnable: Runnable) {
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

  override fun getFile(): PsiFile = myFixture.file
}
