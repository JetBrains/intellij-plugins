// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.install

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.openapi.util.io.NioFiles
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.io.Decompressor
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.RequestBuilder
import com.sun.jna.platform.win32.*
import kotlinx.coroutines.CoroutineScope
import org.intellij.terraform.hcl.HCLBundle
import org.jetbrains.annotations.Nls
import java.io.IOException
import java.nio.file.FileSystems
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

@Service(Service.Level.PROJECT)
internal class TfBinaryInstaller(val scope: CoroutineScope) {

  @RequiresBackgroundThread
  fun runInstallation(binaryName: String, urlProvider: String, installDirectory: Path?): InstallationResult {
    if (binaryName.isEmpty()) {
      logger<TfBinaryInstaller>().error("No binary name provided")
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.failed"))
    }

    val download = download(urlProvider)
                   ?: return FailedInstallation(HCLBundle.messagePointer("binary.installation.download.failed", binaryName))
    val (folder, binaryPath) = findBinary(download, binaryName)
                               ?: return FailedInstallation(HCLBundle.messagePointer("binary.installation.search.failed", binaryName))
    val normalizedBinary = if (binaryName != binaryPath.name) normalizeBinaryPath(binaryName, binaryPath) else binaryPath

    if (!SystemInfoRt.isWindows && !makeExecutable(normalizedBinary)) {
      cleanup(download, folder)
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.make.executable.failed", binaryName))
    }

    if (installDirectory == null) {
      cleanup(download, folder)
      logger<TfBinaryInstaller>().error("No binary installation dir provided")
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.failed"))
    }
    if (!moveToInstallationDir(normalizedBinary, installDirectory)) {
      cleanup(download, folder)
      return FailedInstallation(HCLBundle.messagePointer("binary.installation.move.to.installation.dir.failed", installDirectory))
    }
    cleanup(download, folder)

    return SuccessfulInstallation(Paths.get(installDirectory.absolutePathString(), binaryName))
  }

  private fun download(urlProvider: String): Path? {
    if (urlProvider.isEmpty()) {
      logger<TfBinaryInstaller>().error("No binary download URL provided")
      return null
    }

    val file = Paths.get(PathManager.getTempPath(), urlProvider.substringAfterLast('/'))
    try {
      createRequest(urlProvider).saveToFile(file, null)
    }
    catch (e: Exception) {
      logger<TfBinaryInstaller>().error("Failed to download binary from: $urlProvider", e)
      return null
    }
    return file
  }

  private fun findBinary(file: Path, binaryName: String): Pair<Path, Path>? {
    if (file.name == binaryName) return file to file

    val folder = unpackArchiveIfNeeded(file) ?: return null
    if (Files.isRegularFile(folder)) return folder to folder
    val binary: Path = Files.walk(folder)
                         .filter { it.fileName.toString() == binaryName }
                         .findFirst()
                         .getOrNull() ?: return null

    return folder to binary
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
      logger<TfBinaryInstaller>().error("Unsupported archive type: $downloadFileName")
    }

    return null
  }

  private fun normalizeBinaryPath(binaryName: String, binaryPath: Path): Path {
    val normalizedBinaryPath = binaryPath.parent.resolve(binaryName)
    Files.move(binaryPath, normalizedBinaryPath)
    return normalizedBinaryPath
  }

  private fun makeExecutable(binary: Path): Boolean {
    try {
      binary.setPosixFilePermissions(setOf(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OWNER_EXECUTE))
    }
    catch (e: Exception) {
      logger<TfBinaryInstaller>().error("Failed to make '${binary.absolutePathString()}' executable", e)
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
      if (!output.checkSuccess(logger<TfBinaryInstaller>())) {
        logger<TfBinaryInstaller>().error(output.stderr)
        return false
      }
    }
    catch (e: Exception) {
      logger<TfBinaryInstaller>().error(e)
      return false
    }

    return !SystemInfoRt.isWindows || addInstallationDirToPath(installationDir)
  }

  private fun addInstallationDirToPath(installationDir: Path): Boolean {
    val locationToAdd = installationDir.absolutePathString()
    val userPath = readPathFromRegistry() ?: ""
    val newUserPath = appendToPath(userPath, locationToAdd)
    if (newUserPath == null) {
      logger<TfBinaryInstaller>().debug("The '$locationToAdd' location is already in the user PATH (`$userPath`)")
      return true
    }

    if (!updatePathInRegistry(newUserPath))
      return false

    logger<TfBinaryInstaller>().debug("The '$locationToAdd' location is added to the user PATH (`$userPath`)")
    return true
  }

  private fun cleanup(download: Path?, folder: Path?) {
    try {
      if (download != null) NioFiles.deleteRecursively(download)
      if (folder != null) NioFiles.deleteRecursively(folder)
    }
    catch (e: IOException) {
      logger<TfBinaryInstaller>().error("An exception thrown during cleanup after binary installation", e)
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
      logger<TfBinaryInstaller>().error(
        "Unable to read registry key 'WinReg.HKEY_CURRENT_USER\\$USER_PATH_SUB_KEY' valueName '$USER_PATH_VALUE_NAME': ${t.message}")
      null
    }
  }

  private fun appendToPath(oldPath: String, newEntry: String): String? {
    val pathSeparator = FileSystems.getDefault().separator
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
      logger<TfBinaryInstaller>().error("Unable to write registry key 'HKEY_CURRENT_USER\\$USER_PATH_SUB_KEY' valueName '$USER_PATH_VALUE_NAME': ${t.message}")
      return false
    }
  }

  private fun createRequest(url: String): RequestBuilder {
    return HttpRequests.request(url).productNameAsUserAgent()
  }
}

internal sealed class InstallationResult(
  open val binary: Path?,
  open val errorMsg: Supplier<@Nls String>?,
)

internal class SuccessfulInstallation(override val binary: Path) : InstallationResult(binary, null)

internal class FailedInstallation(override val errorMsg: Supplier<@Nls String>) : InstallationResult(null, errorMsg)