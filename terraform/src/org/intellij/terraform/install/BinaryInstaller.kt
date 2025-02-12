// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.install

import com.google.common.hash.Hashing
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.intellij.util.io.Decompressor
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.RequestBuilder
import com.sun.jna.platform.win32.*
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.runtime.TfProjectSettings
import org.jetbrains.annotations.Nls
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.function.Supplier
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.setPosixFilePermissions
import kotlin.jvm.optionals.getOrNull

private val SUPPORTED_ARCHIVE_TYPES: List<String> = listOf(".zip", ".tar.gz")

private const val USER_PATH_SUB_KEY = "Environment"
private const val USER_PATH_VALUE_NAME = "Path"

internal class BinaryInstaller private constructor(
  private val binaryNameProvider: () -> String?,
  private val downloadUrlProvider: (Configuration) -> String?,
  private val checksumProvider: (Configuration) -> String?,
  private val fileFilterProvider: (Configuration) -> (Path) -> Boolean,
  private val installDirProvider: (Configuration) -> Path?,
  private val resultHandler: ((InstallationResult) -> Unit)?,
  private val progressIndicator: ProgressIndicator?) {

  private var configuration: Configuration = Configuration(null, null)

  private fun install(project: Project) {
    runInstallationInBackground(project, ::installInternal, ::handleResult)
  }

  @RequiresBackgroundThread
  private fun installInternal(progressIndicator: ProgressIndicator): InstallationResult {
    val binaryName = binaryNameProvider().also {
      configuration = configuration.copy(binaryName = it)
    }
    if (binaryName.isNullOrEmpty()) {
      logger<BinaryInstaller>().error("No binary name provided")
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.failed"))
    }

    val download = download(progressIndicator)
    if (download == null) {
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.download.failed", binaryName))
    }

    var (folder, binary) = findBinary(download, binaryName) ?: return FailedInstallation(
      HCLBundle.messagePointer("binary.installation.search.failed", binaryName))

    if (binary.name != binaryName) {
      val normalizedBinaryPath = binary.parent.resolve(binaryName)
      Files.move(binary, normalizedBinaryPath)
      binary = normalizedBinaryPath
    }

    if (!verifyDownload(download, binary, progressIndicator)) {
      cleanup(download, folder)
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.verification.failed"))
    }

    if (!SystemInfoRt.isWindows && !makeExecutable(binary)) {
      cleanup(download, folder)
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.make.executable.failed", binaryName))
    }

    val installationDir = installDirProvider(configuration)
    if (installationDir == null) {
      cleanup(download, folder)
      logger<BinaryInstaller>().error("No binary installation dir provided")
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.failed"))
    }
    if (!moveToInstallationDir(binary, installationDir)) {
      cleanup(download, folder)
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.move.to.installation.dir.failed", installationDir))
    }

    cleanup(download, folder)

    return SuccessfulInstallation(Paths.get(installationDir.absolutePathString(), binaryName))
  }

  @RequiresEdt
  private fun handleResult(result: InstallationResult, project: Project) {
    if (ModalityState.current() == ModalityState.nonModal()) {
      if (result is FailedInstallation) {
        showNotification(result.errorMsg.get(), NotificationType.ERROR, project)
      }
      else {
        showNotification(HCLBundle.message("binary.installation.succeeded", configuration.binaryName!!, result.binary.toString()),
                         NotificationType.INFORMATION, project)
      }
    }

    resultHandler?.invoke(result)
  }

  private fun download(progressIndicator: ProgressIndicator): Path? {
    val downloadUrl = downloadUrlProvider(configuration).also {
      configuration = configuration.copy(downloadUrl = it)
    }
    if (downloadUrl.isNullOrEmpty()) {
      logger<BinaryInstaller>().error("No binary download URL provided")
      return null
    }

    val file = Paths.get(PathManager.getTempPath(), downloadUrl.substringAfterLast('/'))
    try {
      progressIndicator.text2 = HCLBundle.message("binary.installation.downloading.binary.progress.title", downloadUrl)

      createRequest(downloadUrl)
        .saveToFile(file, progressIndicator)
    }
    catch (e: Exception) {
      logger<BinaryInstaller>().error("Failed to download binary from: $downloadUrl", e)
      return null
    }
    return file
  }

  private fun findBinary(file: Path, binaryName: String): Pair<Path, Path>? {
    if (file.name == binaryName) return file to file

    val folder = unpackArchiveIfNeeded(file) ?: return null
    if (Files.isRegularFile(folder)) return folder to folder
    return Files.walk(folder)
      .filter { it.fileName.toString() == binaryName }
      .findFirst()
      .getOrNull()
      ?.let { folder to it }
  }

  private fun unpackArchiveIfNeeded(download: Path): Path? {
    val downloadFileName = download.name

    if (SUPPORTED_ARCHIVE_TYPES.none { downloadFileName.endsWith(it) }) return download

    if (downloadFileName.endsWith(".zip")) {
      val archiveContent = Paths.get(download.parent.toString(), downloadFileName.removeSuffix(".zip"))
      Decompressor.Zip(download)
        .withZipExtensions()
        .extract(archiveContent)
      return archiveContent
    }
    else if (downloadFileName.endsWith(".tar.gz")) {
      val archiveContent = Paths.get(download.parent.toString(), downloadFileName.removeSuffix(".tar.gz"))
      Decompressor.Tar(download)
        .extract(archiveContent)
      return archiveContent
    }
    else {
      logger<BinaryInstaller>().error("Unsupported archive type: $downloadFileName")
    }

    return null
  }

  private fun verifyDownload(download: Path, binary: Path, progressIndicator: ProgressIndicator): Boolean {
    val checksum = checksumProvider.invoke(configuration) ?: return true
    val fileFilter = fileFilterProvider.invoke(configuration)
    val fileToVerify = listOf(download, binary).first(fileFilter)

    val actualChecksum = try {
      progressIndicator.text2 = HCLBundle.message("binary.installation.calculating.hash.progress.title")

      com.google.common.io.Files.asByteSource(fileToVerify.toFile())
        .hash(Hashing.sha256())
        .toString()
    }
    catch (e: Exception) {
      logger<BinaryInstaller>().error("Failed to calculate hashcode for binary", e)
      return false
    }

    return actualChecksum.equals(checksum, ignoreCase = true)
  }

  private fun makeExecutable(binary: Path): Boolean {
    try {
      binary.setPosixFilePermissions(setOf(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OWNER_EXECUTE))
    }
    catch (e: Exception) {
      logger<BinaryInstaller>().error("Failed to make '${binary.absolutePathString()}' executable", e)
    }
    return Files.isExecutable(binary)
  }

  private fun moveToInstallationDir(binary: Path, installationDir: Path): Boolean {
    try {
      if (!installationDir.exists()) {
        Files.createDirectories(installationDir)
      }

      val commandLine = if (SystemInfoRt.isWindows) {
        GeneralCommandLine("cmd.exe", "/c", "move", binary.absolutePathString(), installationDir.absolutePathString())
      }
      else {
        ExecUtil.sudoCommand(GeneralCommandLine("mv", binary.absolutePathString(), installationDir.absolutePathString()),
                             HCLBundle.message("binary.installation.move.to.installation.dir", installationDir))
      }

      val output = ExecUtil.execAndGetOutput(commandLine)
      if (!output.checkSuccess(logger<BinaryInstaller>())) {
        logger<BinaryInstaller>().error(output.stderr)
        return false
      }
    }
    catch (e: Exception) {
      logger<BinaryInstaller>().error(e)
      return false
    }

    return !SystemInfoRt.isWindows || addInstallationDirToPath(installationDir)
  }

  private fun addInstallationDirToPath(installationDir: Path): Boolean {
    val locationToAdd = installationDir.absolutePathString()
    val userPath = readPathFromRegistry() ?: ""
    val newUserPath = appendToPath(userPath, locationToAdd)
    if (newUserPath == null) {
      logger<BinaryInstaller>().debug("The '$locationToAdd' location is already in the user PATH (`$userPath`)")
      return true
    }

    if (!updatePathInRegistry(newUserPath))
      return false

    logger<BinaryInstaller>().debug("The '$locationToAdd' location is added to the user PATH (`$userPath`)")
    return true
  }

  private fun cleanup(download: Path?, folder: Path?) {
    try {
      if (download != null) FileUtil.delete(download)
      if (folder != null) FileUtil.delete(folder)
    }
    catch (e: IOException) {
      logger<BinaryInstaller>().error("An exception thrown during cleanup after binary installation", e)
    }
  }

  private fun readPathFromRegistry(): String? {
    return try {
      val phkKey = WinReg.HKEYByReference()

      val rc = Advapi32.INSTANCE.RegOpenKeyEx(
        WinReg.HKEY_CURRENT_USER,
        USER_PATH_SUB_KEY,
        0,
        WinNT.KEY_READ or WinNT.KEY_WOW64_32KEY,
        phkKey)
      if (rc != W32Errors.ERROR_SUCCESS) throw Win32Exception(rc)

      try {
        return Advapi32Util.registryGetValue(phkKey.value, "", USER_PATH_VALUE_NAME)?.toString()
      }
      finally {
        Advapi32Util.registryCloseKey(phkKey.value)
      }
    }
    catch (t: Win32Exception) {
      logger<BinaryInstaller>().error(
        "Unable to read registry key 'WinReg.HKEY_CURRENT_USER\\$USER_PATH_SUB_KEY' valueName '$USER_PATH_VALUE_NAME': ${t.message}")
      null
    }
  }

  private fun appendToPath(oldPath: String, newEntry: String): String? {
    val pathSeparator = File.pathSeparator
    val pathElements = oldPath.split(pathSeparator)

    val alreadyAdded = pathElements.any { it.equals(newEntry, ignoreCase = true) }
    if (alreadyAdded) {
      return null
    }

    return (pathElements + newEntry).filter(String::isNotEmpty).joinToString("") { "$it$pathSeparator" }
  }

  private fun updatePathInRegistry(value: String): Boolean {
    try {
      val branch = WinNT.KEY_WOW64_32KEY
      Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, USER_PATH_SUB_KEY, branch)
      Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, USER_PATH_SUB_KEY, USER_PATH_VALUE_NAME, value, branch)
      return true
    }
    catch (t: Win32Exception) {
      logger<BinaryInstaller>().error("Unable to write registry key 'HKEY_CURRENT_USER\\$USER_PATH_SUB_KEY' valueName '$USER_PATH_VALUE_NAME': ${t.message}")
      return false
    }
  }

  private fun createRequest(url: String): RequestBuilder {
    return HttpRequests
      .request(url)
      .productNameAsUserAgent()
  }

  private fun runInstallationInBackground(project: Project,
                                          installation: (ProgressIndicator) -> InstallationResult,
                                          afterInstallation: (InstallationResult, Project) -> Unit) {
    val progressManager = ProgressManager.getInstance()
    val task = createInstallationTask(project, installation, afterInstallation)
    val indicator = progressIndicator ?: BackgroundableProcessIndicator(task)


    progressManager.runProcessWithProgressAsynchronously(task, indicator)
  }

  private fun createInstallationTask(project: Project,
                                     installation: (ProgressIndicator) -> InstallationResult,
                                     afterInstallation: (InstallationResult, Project) -> Unit) =
    object : Task.Backgroundable(project,
                                 HCLBundle.message("binary.installation.progress.title"),
                                 true) {

      private var installationResult: InstallationResult? = null

      override fun run(indicator: ProgressIndicator) {
        installationResult = installation(indicator)
      }

      override fun onSuccess() {
        if (installationResult != null) {
          afterInstallation(installationResult!!, project)
        }
      }
    }

  private fun showNotification(@NlsContexts.NotificationContent content: String,
                               type: NotificationType,
                               project: Project) {
    TfConstants.getNotificationGroup().createNotification(HCLBundle.message("terraform.name"), content, type)
      .notify(project)
  }

  /**
   * Builder to configure binary installation.
   */
  internal class Configurer(private val project: Project) {

    private var binaryNameProvider: (() -> String?)? = null
    private var downloadUrlProvider: ((Configuration) -> String?)? = null
    private var checksumProvider: ((Configuration) -> String?)? = null
    private var fileToVerifyFilter: ((Configuration) -> (Path) -> Boolean)? = null
    private var installDirProvider: ((Configuration) -> Path?)? = null
    private var resultHandler: ((InstallationResult) -> Unit)? = null
    private var progressIndicator: ProgressIndicator? = null

    fun withBinaryName(nameProvider: () -> String?): Configurer {
      this.binaryNameProvider = nameProvider
      return this
    }

    fun withDownloadUrl(urlProvider: (Configuration) -> String?): Configurer {
      this.downloadUrlProvider = urlProvider
      return this
    }

    fun withResultHandler(@RequiresEdt resultHandler: (InstallationResult) -> Unit): Configurer {
      this.resultHandler = resultHandler
      return this
    }

    fun withProgressIndicator(progressIndicator: ProgressIndicator?): Configurer {
      this.progressIndicator = progressIndicator
      return this
    }

    fun install() {
      assert(binaryNameProvider != null)
      assert(downloadUrlProvider != null)

      BinaryInstaller(binaryNameProvider!!,
                      downloadUrlProvider!!,
                      checksumProvider ?: { null },
                      fileToVerifyFilter ?: ::getDefaultFileFilter,
                      installDirProvider ?: ::getDefaultInstallationDirProvider,
                      resultHandler,
                      progressIndicator)
        .install(project)
    }

    private fun getDefaultFileFilter(configuration: Configuration): (Path) -> Boolean {
      return { path -> path.fileName.toString() == configuration.binaryName }
    }

    private fun getDefaultInstallationDirProvider(configuration: Configuration): Path? {
      return if (SystemInfoRt.isWindows) {
        val binaryFolderName = configuration.binaryName?.substringBefore('.').takeIf { !it.isNullOrEmpty() } ?: return null
        "${System.getProperty("user.home")}/.jetbrains/$binaryFolderName/"
          .let(FileUtil::toSystemIndependentName)
          .let(Paths::get)
      }
      else {
        Paths.get("/usr/local/bin/")
      }
    }
  }

  internal data class Configuration(val binaryName: String?, val downloadUrl: String?)

  companion object {
    fun create(project: Project): Configurer = Configurer(project)
  }
}

internal sealed class InstallationResult(open val binary: Path?,
                                         open val errorMsg: Supplier<@Nls String>?)

internal class SuccessfulInstallation(override val binary: Path)
  : InstallationResult(binary, null)

internal class FailedInstallation(override val errorMsg: Supplier<@Nls String>)
  : InstallationResult(null, errorMsg)

internal class InstallTfAction : DumbAwareAction() {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = e.project != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    installTFTool(project, type = TfToolType.TERRAFORM, resultHandler = { result -> handleInstall(project, result) })
  }

  private fun handleInstall(project: Project, result: InstallationResult) {
    when (result) {
      is SuccessfulInstallation -> {
        project.service<TfProjectSettings>().toolPath = result.binary.toString()
      }
      else -> {}
    }

  }
}