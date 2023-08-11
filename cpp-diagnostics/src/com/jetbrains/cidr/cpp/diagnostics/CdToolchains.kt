package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.StringUtil
import com.intellij.remote.RemoteCredentials
import com.intellij.ssh.RSyncUtil
import com.intellij.ssh.rsync.RSyncOptions
import com.intellij.util.PathMapper
import com.intellij.util.PathMappingSettings
import com.intellij.util.io.delete
import com.intellij.util.io.readText
import com.intellij.util.io.write
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.WorkspaceWithEnvironment
import com.jetbrains.cidr.system.*
import com.jetbrains.cidr.toolchains.CidrExecutableTool
import com.jetbrains.cidr.toolchains.EnvironmentProblems
import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files

fun collectToolchains(project: Project?): String {
  val log = CdIndenter(indentSize = 4)
  processSystemInfo(log)
  log.put()
  getAllEnvironments(project).forEach {
    processCPPEnvironment(log, it)
    log.put()
  }
  return log.result
}

fun processSystemInfo(log: CdIndenter) {
  val appInfo = ApplicationInfo.getInstance()
  val namesInfo = ApplicationNamesInfo.getInstance()
  log.put("IDE: ${namesInfo.fullProductName} (build #${appInfo.build.asString()})")
  log.put("OS: ${SystemInfo.OS_NAME} (${SystemInfo.OS_VERSION}, ${SystemInfo.OS_ARCH})")

  val cppToolchains = CPPToolchains.getInstance()
  log.put("Default toolchain: ${cppToolchains.defaultToolchain?.name ?: "UNKNOWN"}")

  processDevOptions(log)
}

private fun processDevOptions(log: CdIndenter) {
  fun logOption(option: String, defaultValue: Boolean) =
    log.put("${option} = ${Registry.`is`(option, defaultValue)}")

  fun logIntOption(option: String, defaultValue: Int) =
    log.put("${option} = ${Registry.intValue(option, defaultValue)}")

  log.put("Options:")
  log.scope {
    // Note: keep default values in sync with actual values
    logOption("clion.remote.compress.tar", defaultValue = true)
    logIntOption("clion.remote.tar.timeout", defaultValue = 240000)
    logOption("clion.remote.resync.system.cache", defaultValue = false)
    logOption("clion.remote.upload.external.changes", defaultValue = true)
  }
}

fun processRemoteHost(log: CdIndenter, environment: CidrToolEnvironment) {
  if (!environment.hostMachine.isRemote) return

  val remoteHost = environment.hostMachine
  val hostId = remoteHost.hostId
  val isWsl = environment is CPPEnvironment && environment.toolSet.isWSL
  log.put("Remote Host: ${remoteHost.name} ($hostId)", if (isWsl) " is WSL" else "")

  log.scope {
    testRemoteHost(log, remoteHost)

    if (environment is CPPEnvironment) {
      processCPPEnvironment(log, environment)
    }
  }
}

private fun logTool(tool: CidrExecutableTool, toolName: String, log: CdIndenter) {
  val version = try {
    tool.readVersion()
  } catch (e: ExecutionException) {
    null
  }

  log.put("$toolName ($version): ${tool.executablePath}")
}

private fun processCPPEnvironment(log: CdIndenter, environment: CPPEnvironment) {
  ProgressManager.checkCanceled()
  val toolchain = environment.toolchain
  val hostMachine = environment.hostMachine

  log.put("Toolchain: ${toolchain.name}")
  log.scope {
    processGeneralToolchainInfo(log, toolchain)

    environment.cMake?.let { logTool(it, "cmake", log) }
    environment.make?.let { logTool(it, "make", log) }
    environment.gdb?.let { logTool(it, "cmake", log) }

    if (hostMachine is MappedHost) {
      log.put("Path Mappings:")

      var pathMapper: PathMapper = hostMachine.pathMapper
      if (pathMapper is PathMapperWrapper) pathMapper = pathMapper.original

      log.scope {
        if (pathMapper is PathMappingSettings) {
          pathMapper.pathMappings.forEach { log.put("${it.localRoot} -> ${it.remoteRoot}") }
        }
      }
    }

    var rootPath = RemoteDeploymentHelper.getRootPath(hostMachine.hostId)
    if (rootPath.isEmpty()) {
      rootPath = "not specified"
    }
    log.put("Root Path: ${rootPath}")

    if (hostMachine is RemoteHost) {
      log.put("Header-roots cache: ${hostMachine.cacheDirectory.absolutePath}")
    }
  }
}

private fun processGeneralToolchainInfo(log: CdIndenter, toolchain: CPPToolchains.Toolchain) {
  log.put("OS: ${toolchain.osType}")
  log.put("Kind: ${toolchain.toolSetKind}")

  toolchain.customCCompilerPath?.let { log.put("c: ${it}") }
  toolchain.customCXXCompilerPath?.let { log.put("cxx: ${it}") }
}

private fun testRemoteHost(log: CdIndenter, remoteHost: HostMachine) {
  val isConnected = test(log, remoteHost, "test connection", ::checkConnection)
  if (isConnected) {
    test(log, remoteHost, "run remote process", ::processCheck)
    test(log, remoteHost, "remote FS", ::checkRemoteFS)
    test(log, remoteHost, "tar", ::checkRemoteTar)

    test(log, remoteHost, "rsync local", { CidrRSyncUtil.canUseRSync(remoteHost.hostId) }, "[NOT CONFIGURED]")
    test(log, remoteHost, "rsync remote", ::checkRemoteRsync)
    test(log, remoteHost, "rsync download", ::checkRsyncDownload)

    test(log, remoteHost, "pigz remote", ::checkRemotePigz)
  }
}

private fun checkConnection(remoteHost: HostMachine) =
  RemoteUtil.checkConnection(getCredentials(remoteHost))

private fun processCheck(remoteHost: HostMachine): Boolean {
  val output: ProcessOutput = remoteHost.runProcess(
    GeneralCommandLine("pwd"), ProgressManager.getInstance().progressIndicator, 5000)
  return output.exitCode == 0 && StringUtil.isNotEmpty(output.stdout)
}

private fun checkRemoteFS(remoteHost: HostMachine): Boolean {
  val tmp = remoteHost.createTempDirectory("host_info", null)
  try {
    val file = Files.createFile(tmp.resolve("file.txt"))
    val testString = "Hello, CLion!"
    file.write(testString, createParentDirs = false)

    val filePath = remoteHost.getPath(file.toAbsolutePath().toString())
    return testString == filePath.readText()
  }
  finally {
    tmp.delete()
  }
}

private fun checkRemoteTar(remoteHost: HostMachine): Boolean = RemoteUtil.isTarInstalled { cmd ->
  remoteHost.runProcess(cmd, ProgressManager.getInstance().progressIndicator, 5000)
}

private fun checkRemoteRsync(remoteHost: HostMachine) = RemoteUtil.isRsyncInstalled { cmd ->
  remoteHost.runProcess(cmd, ProgressManager.getInstance().progressIndicator, 5000)
}

private fun checkRemotePigz(remoteHost: HostMachine) = RemoteUtil.isPigzInstalled { cmd ->
  remoteHost.runProcess(cmd, ProgressManager.getInstance().progressIndicator, 5000)
}

@Throws(IOException::class)
private fun checkRsyncDownload(remoteHost: HostMachine): Boolean {
  val credentials: RemoteCredentials = getCredentials(remoteHost)
  val remote = remoteHost.createTempDirectory("remote", null)
  val local = FileUtil.createTempDirectory("local", null)
  val logFile = FileUtil.createTempFile("log", null)
  try {
    val file = Files.createFile(remote.resolve("file.txt"))
    val testString = "Hello, CLion!"
    file.write(testString, createParentDirs = false)

    RSyncUtil.downloadFolderWithRSync(local.path, remote.toString(), credentials,
                                      ProgressManager.getInstance().progressIndicator, RSyncOptions(logfilePath = logFile.path))

    val text = FileUtil.loadFile(File(local, "file.txt"))
    return testString == text
  }
  finally {
    remote.delete()
    local.delete()
    logFile.delete()
  }
}

private fun test(log: CdIndenter,
                 host: HostMachine,
                 @NonNls testName: String,
                 check: (host: HostMachine) -> Boolean,
                 failStatusMsg: String = "[FAILED]"): Boolean {
  ProgressManager.checkCanceled()
  var status = false
  try {
    status = check(host)
    val statusStr = if (status) "[OK]" else failStatusMsg
    log.put("${statusStr} ${testName}")
  }
  catch (t: Throwable) {
    log.put("[ERROR] ${testName}  ==>  ", stackTraceToString(t))
  }
  return status
}

private fun getCredentials(remoteHost: HostMachine): RemoteCredentials =
  RemoteUtil.getCredentials(remoteHost) ?: throw IllegalStateException("Cannot find credentials by hostId")

fun stackTraceToString(t: Throwable): String {
  val writer = StringWriter()
  t.printStackTrace(PrintWriter(writer))
  return writer.toString()
}

private fun getAllEnvironments(project: Project?): List<CPPEnvironment> {
  // get project environments first - they should be initialized with correct path mapping
  val projectEnvironments = if (project == null) {
    emptyList()
  } else {
    getAllProjectEnvironments(project)
  }

  // look for other environments, that aren't already present
  val toolchainsSet = projectEnvironments.map { it.toolchain.name }.toHashSet()
  val otherEnvironments = CPPToolchains.getInstance().toolchains
    .map { it.name }
    .filter { !toolchainsSet.contains(it) }
    .mapNotNull {
      CPPToolchains.createCPPEnvironment(project,
                                         null,
                                         it,
                                         EnvironmentProblems(),
                                         false,
                                         null)
    }
    .distinctBy { it.toolchain.name }
  return projectEnvironments + otherEnvironments
}

private fun getAllProjectEnvironments(project: Project): List<CPPEnvironment> {
  return CidrWorkspace.getInitializedWorkspaces(project)
    .mapNotNull { it as? WorkspaceWithEnvironment }
    .flatMap { it.getEnvironment() }
    .mapNotNull { it as? CPPEnvironment }
    .distinctBy { it.toolchain.name }
}