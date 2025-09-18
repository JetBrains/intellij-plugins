package com.jetbrains.cidr.cpp.diagnostics.model

import com.jetbrains.cidr.cpp.diagnostics.CdIndenter
import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticsFile(val filename: String, val contents: String) : Reportable {
  override fun toText(): String = contents
}

@Serializable
data class DiagnosticsBundle(
  val toolchains: ToolchainsSection,
  val workspaces: CidrWorkspacesSection,
  val ocWorkspace: OCWorkspaceSection,
  val ocWorkspaceEvents: OCWorkspaceEventsSection
) {
  fun files(): List<DiagnosticsFile> = listOf(
    DiagnosticsFile("Toolchains.txt", toolchains.toText()),
    DiagnosticsFile("CidrWorkspaces.txt", workspaces.toText()),
    DiagnosticsFile("OCWorkspaceEvents.txt", ocWorkspaceEvents.toText()),
    DiagnosticsFile("OCWorkspace.txt", ocWorkspace.toText()),
  )

  fun summaryText(): String {
    return files().fold(StringBuilder("=====CLION SUMMARY=====").appendLine()) { acc, f ->
      acc.append(f.filename).appendLine().append(f.contents).appendLine()
    }.toString()
  }
}

@Serializable
data class LinesSection(val lines: List<String>) : Reportable {
  override fun toText(): String = lines.joinToString("\n")
}

// ---------------- Toolchains ----------------

@Serializable
data class ToolchainsSection(
  val systemInfo: SystemInfo,
  val environments: List<CppEnvironmentInfo>
) : Reportable {
  override fun toText(): String {
    val log = CdIndenter(indentSize = 4)
    systemInfo.appendTo(log)
    log.put()
    environments.forEach { env ->
      env.appendTo(log)
      log.put()
    }
    return log.result
  }
}

@Serializable
data class SystemInfo(
  val ideFullProductName: String,
  val ideBuild: String,
  val osName: String,
  val osVersion: String,
  val osArch: String,
  val defaultToolchainName: String?,
  val devOptions: DevOptions
) : Reportable {
  fun appendTo(log: CdIndenter) {
    log.put("IDE: ${ideFullProductName} (build #${ideBuild})")
    log.put("OS: ${osName} (${osVersion}, ${osArch})")
    log.put("Default toolchain: ${defaultToolchainName ?: "UNKNOWN"}")
    devOptions.appendTo(log)
  }

  override fun toText(): String {
    val log = CdIndenter(indentSize = 4)
    appendTo(log)
    return log.result
  }
}

@Serializable
data class DevOptions(
  val compressTar: Boolean,
  val tarTimeoutMs: Int,
  val resyncSystemCache: Boolean,
  val uploadExternalChanges: Boolean
) : Reportable {
  fun appendTo(log: CdIndenter) {
    log.put("Options:")
    log.scope {
      log.put("clion.remote.compress.tar = ${compressTar}")
      log.put("clion.remote.tar.timeout = ${tarTimeoutMs}")
      log.put("clion.remote.resync.system.cache = ${resyncSystemCache}")
      log.put("clion.remote.upload.external.changes = ${uploadExternalChanges}")
    }
  }

  override fun toText(): String {
    val log = CdIndenter(indentSize = 4)
    appendTo(log)
    return log.result
  }
}

@Serializable
data class ExecutableToolInfo(
  val name: String,
  val version: String?,
  val executablePath: String
) {
  fun appendTo(log: CdIndenter) {
    log.put("${name} (${version ?: "unknown"}): ${executablePath}")
  }
}

@Serializable
data class PathMappingItem(val localRoot: String, val remoteRoot: String)

@Serializable
data class CppEnvironmentInfo(
  val toolchainName: String,
  val osType: String,
  val kind: String,
  val toolSetPath: String?,
  val options: List<String>,
  val customCCompilerPath: String?,
  val customCXXCompilerPath: String?,
  val descriptionExtras: List<String>,
  val tools: List<ExecutableToolInfo>,
  val pathMappings: List<PathMappingItem>?,
  val rootPath: String?,
  val headerRootsCache: String?
) : Reportable {
  fun appendTo(log: CdIndenter) {
    log.put("Toolchain: ${toolchainName}")
    log.scope {
      log.put("OS: ${osType}")
      log.put("Kind: ${kind}")
      log.put("Path: ${toolSetPath}")
      if (options.isNotEmpty()) {
        log.put("Options:")
        log.scope { options.forEach { log.put(it) } }
      }
      customCCompilerPath?.let { log.put("c: ${it}") }
      customCXXCompilerPath?.let { log.put("cxx: ${it}") }

      // Provider-specific extras
      descriptionExtras.forEach { log.put(it) }

      tools.forEach { it.appendTo(log) }

      pathMappings?.let { mappings ->
        log.put("Path Mappings:")
        log.scope {
          mappings.forEach { log.put("${it.localRoot} -> ${it.remoteRoot}") }
        }
      }

      rootPath?.let { log.put("Root Path: ${it}") }
      headerRootsCache?.let { log.put("Header-roots cache: ${it}") }
    }
  }

  override fun toText(): String {
    val log = CdIndenter(indentSize = 4)
    appendTo(log)
    return log.result
  }
}

// ---------------- Cidr Workspaces ----------------

@Serializable
data class CidrWorkspacesSection(
  val workspaces: List<WorkspaceInfo>
) : Reportable {
  override fun toText(): String {
    val log = CdIndenter()
    log.put("Workspaces: ", workspaces.size)
    log.scope {
      workspaces.forEach { ws ->
        ws.appendTo(log)
      }
    }
    return log.result
  }
}

@Serializable
data class WorkspaceInfo(
  val className: String,
  val projectPath: String?,
  val contentRoot: String?,
  // sections are ordered exactly as printed by EP providers
  val sections: List<WorkspaceSection>
) {
  fun appendTo(log: CdIndenter) {
    log.put(className)
    log.scope {
      log.put("Project path: ${projectPath}")
      log.put("Content root: ${contentRoot}")
      sections.forEach { it.appendTo(log) }
    }
  }
}

@Serializable
sealed class WorkspaceSection {
  abstract fun appendTo(log: CdIndenter)
}

@Serializable
data class ToolchainsNamesSection(val toolchainNames: List<String>) : WorkspaceSection() {
  override fun appendTo(log: CdIndenter) {
    log.put("Toolchains:")
    log.scope { toolchainNames.forEach { log.put(it) } }
  }
}

@Serializable
data class CMakeSection(
  val autoReloadEnabled: Boolean,
  val profiles: List<CMakeProfileInfo>
) : WorkspaceSection() {
  override fun appendTo(log: CdIndenter) {
    log.put("Auto reload enabled: ${autoReloadEnabled}")
    profiles.forEach { it.appendTo(log) }
  }
}

@Serializable
data class CMakeProfileInfo(
  val name: String,
  val buildType: String?,
  val toolchainName: String?,
  val effectiveToolchain: String?,
  val generationOptions: String?,
  val generationDir: String?,
  val effectiveGenerationDir: String?,
  val buildOptions: String?
) {
  fun appendTo(log: CdIndenter) {
    log.scope {
      log.put("Profile: ${name}")
      log.scope {
        log.put("buildType: ${buildType}")
        log.put("toolchainName: ${toolchainName}")
        log.put("effective toolchain: ${effectiveToolchain ?: "UNKNOWN"}")
        log.put("generationOptions: ${generationOptions}")
        log.put("generationDir: ${generationDir}")
        log.put("effective generation dir: ${effectiveGenerationDir}")
        log.put("buildOptions: ${buildOptions}")
      }
    }
  }
}

// ---------------- OC Workspace ----------------

@Serializable
data class OCWorkspaceSection(
  val configurations: List<ResolveConfigurationInfo>
) : Reportable {
  override fun toText(): String {
    val log = CdIndenter()
    log.put("Resolve configurations: ", configurations.size)
    log.scope {
      configurations.forEach { it.appendTo(log) }
    }
    return log.result
  }
}

@Serializable
data class ResolveConfigurationInfo(
  val displayName: String,
  val name: String,
  val variant: String?,
  val sources: List<SourceInfo>
) {
  fun appendTo(log: CdIndenter) {
    log.put("Configuration: ${displayName} (${name}: ${variant}), ${sources.size} source file(s)")
    sources.forEach { it.appendTo(log) }
  }
}

@Serializable
data class SourceInfo(
  val path: String,
  val kind: String?,
  val compilerExecutablePath: String?,
  val compilerSwitches: String?
) {
  fun appendTo(log: CdIndenter) {
    log.scope {
      log.put(path, " ", "[${kind ?: "UNKNOWN"}]")
      if (kind != null) {
        log.scope {
          val compilerPath = compilerExecutablePath ?: "UNKNOWN"
          val switches = compilerSwitches ?: ""
          log.put(compilerPath, " ", switches)
        }
      }
    }
  }
}

// ---------------- OC Workspace Events ----------------

@Serializable
data class OCWorkspaceEventsSection(
  val enabled: Boolean,
  val rawText: String
) : Reportable {
  override fun toText(): String = rawText
}
