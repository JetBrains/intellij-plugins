package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.diagnostic.logger
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
import com.intellij.util.io.write
import com.jetbrains.cidr.cpp.diagnostics.model.CppEnvironmentInfo
import com.jetbrains.cidr.cpp.diagnostics.model.ExecutableToolInfo
import com.jetbrains.cidr.cpp.diagnostics.model.PathMappingItem
import com.jetbrains.cidr.cpp.diagnostics.model.ToolchainsSection
import com.jetbrains.cidr.cpp.diagnostics.toolchain.ToolchainDescriptionProvider
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.WorkspaceWithEnvironment
import com.jetbrains.cidr.system.CidrRSyncUtil
import com.jetbrains.cidr.system.HostMachine
import com.jetbrains.cidr.system.MappedHost
import com.jetbrains.cidr.system.PathMapperWrapper
import com.jetbrains.cidr.system.RemoteDeployment
import com.jetbrains.cidr.system.RemoteDeploymentHelper
import com.jetbrains.cidr.system.RemoteHost
import com.jetbrains.cidr.system.RemoteUtil
import com.jetbrains.cidr.toolchains.CidrExecutableTool
import com.jetbrains.cidr.toolchains.EnvironmentProblems
import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.file.Files
import java.util.concurrent.CancellationException
import kotlin.io.path.readText
import com.jetbrains.cidr.cpp.diagnostics.model.DevOptions as ModelDevOptions
import com.jetbrains.cidr.cpp.diagnostics.model.SystemInfo as ModelSystemInfo

private val LOGGER = logger<CppDiagnosticsAction>()

fun collectToolchains(project: Project?): ToolchainsSection {
  val system = processSystemInfo()
  val envs = mutableListOf<CppEnvironmentInfo>()
  getAllEnvironments(project).forEach {
    try {
      envs.add(processCPPEnvironment(it))
    }
    catch (e: CancellationException) {
      throw e
    }
    catch (e: Exception) {
      LOGGER.warn("Failed to get CPPEnvironment to process ${it.toolchain.name}", e)
      val tc = it.toolchain
      envs.add(
        CppEnvironmentInfo(
          toolchainName = tc.name,
          osType = tc.os.toString(),
          kind = tc.toolSetKind.toString(),
          toolSetPath = tc.toolSetPath,
          options = tc.toolSetOptions.map { opt -> opt.uniqueID },
          customCCompilerPath = tc.customCCompilerPath,
          customCXXCompilerPath = tc.customCXXCompilerPath,
          descriptionExtras = listOf(
            "Failed to get CPPEnvironment to process ${it.toolchain.name}: ${e.localizedMessage}",
            "Please, see logs for details."
          ),
          tools = emptyList(),
          pathMappings = emptyList(),
          rootPath = null,
          headerRootsCache = null,
          environmentFile = tc.environment,
        )
      )
    }
  }
  return ToolchainsSection(system, envs)
}

fun processSystemInfo(): ModelSystemInfo {
  val appInfo = ApplicationInfo.getInstance()
  val namesInfo = ApplicationNamesInfo.getInstance()
  val cppToolchains = CPPToolchains.getInstance()
  return ModelSystemInfo(
    ideFullProductName = namesInfo.fullProductName,
    ideBuild = appInfo.build.asString(),
    osName = SystemInfo.OS_NAME,
    osVersion = SystemInfo.OS_VERSION,
    osArch = SystemInfo.OS_ARCH,
    defaultToolchainName = cppToolchains.defaultToolchain?.name,
    devOptions = processDevOptions()
  )
}

private fun processDevOptions(): ModelDevOptions {
  // Note: keep default values in sync with actual values
  val compressTar = Registry.`is`("clion.remote.compress.tar", true)
  val tarTimeout = Registry.intValue("clion.remote.tar.timeout", 240000)
  val resyncSystemCache = Registry.`is`("clion.remote.resync.system.cache", false)
  val uploadExternalChanges = Registry.`is`("clion.remote.upload.external.changes", true)
  return ModelDevOptions(
    compressTar = compressTar,
    tarTimeoutMs = tarTimeout,
    resyncSystemCache = resyncSystemCache,
    uploadExternalChanges = uploadExternalChanges
  )
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
      val envInfo = processCPPEnvironment(environment)
      envInfo.appendTo(log)
    }
  }
}

private fun processCPPEnvironment(environment: CPPEnvironment): CppEnvironmentInfo {
  ProgressManager.checkCanceled()
  val toolchain = environment.toolchain
  val hostMachine = environment.hostMachine

  val descriptionExtras = mutableListOf<String>()
  ToolchainDescriptionProvider.describe(toolchain, hostMachine)?.let { section ->
    descriptionExtras += section.toText().lines()
  }

  fun toolInfo(tool: CidrExecutableTool?, name: String): ExecutableToolInfo? {
    if (tool == null) return null
    val version = if (ApplicationManager.getApplication()?.isUnitTestMode == true) {
      null
    }
    else {
      try {
        tool.readVersion()
      }
      catch (_: ExecutionException) {
        null
      }
    }
    return ExecutableToolInfo(name, version, tool.executablePath)
  }

  val tools = buildList {
    toolInfo(environment.cMake, "cmake")?.let { add(it) }
    toolInfo(environment.make, "make")?.let { add(it) }
    toolInfo(environment.gdb, "cmake")?.let { add(it) } // TODO questionable. Why cmake?
  }

  var pathMappings: List<PathMappingItem>? = null
  try {
    if (hostMachine is MappedHost) {
      var pathMapper: PathMapper = hostMachine.pathMapper
      if (pathMapper is PathMapperWrapper) pathMapper = pathMapper.original
      if (pathMapper is PathMappingSettings) {
        pathMappings = pathMapper.pathMappings.map { PathMappingItem(it.localRoot, it.remoteRoot) }
      }
    }
  }
  catch (e: CancellationException) {
    throw e
  }
  catch (e: Exception) {
    descriptionExtras += listOf("Failed to get path mappings: ${e.localizedMessage}", "Please, see logs for details.")
    LOGGER.warn("Failed to get path mappings", e)
  }

  var rootPath: String? = null
  var headerRootsCache: String? = null
  if (serviceOrNull<RemoteDeployment>() != null) {
    var root = RemoteDeploymentHelper.getRootPath(hostMachine.hostId)
    if (root.isEmpty()) {
      root = "not specified"
    }
    rootPath = root

    if (hostMachine is RemoteHost) {
      headerRootsCache = hostMachine.cacheDirectory.absolutePath
    }
  }

  return CppEnvironmentInfo(
    toolchainName = toolchain.name,
    osType = toolchain.os.toString(),
    kind = toolchain.toolSetKind.toString(),
    toolSetPath = toolchain.toolSetPath,
    options = toolchain.toolSetOptions.map { it.uniqueID },
    customCCompilerPath = toolchain.customCCompilerPath,
    customCXXCompilerPath = toolchain.customCXXCompilerPath,
    descriptionExtras = descriptionExtras,
    tools = tools,
    pathMappings = pathMappings,
    rootPath = rootPath,
    headerRootsCache = headerRootsCache,
    environmentFile = toolchain.environment,
  )
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

private fun test(
  log: CdIndenter,
  host: HostMachine,
  @NonNls testName: String,
  check: (host: HostMachine) -> Boolean,
  failStatusMsg: String = "[FAILED]",
): Boolean {
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
  }
  else {
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
