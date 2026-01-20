package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.ui.ExternalSystemIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PlatformIcons
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.ui.PlatformioActionBase.Companion.pioIcon
import com.jetbrains.cidr.cpp.external.system.linkExternalProject
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.cpp.toolchains.TrivialNativeToolchain
import com.jetbrains.cidr.external.system.workspace.ExternalWorkspace
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment
import com.jetbrains.cidr.project.workspace.CidrWorkspaceProvider
import com.jetbrains.cidr.project.workspace.WorkspaceWithEnvironment
import com.jetbrains.cidr.toolchains.EnvironmentProblems
import icons.ClionEmbeddedPlatformioIcons
import java.io.File
import javax.swing.Icon
import kotlin.io.path.isDirectory

@State(name = "PlatformIOWorkspace")
@Service(Service.Level.PROJECT)
internal class PlatformioWorkspace(project: Project) : ExternalWorkspace(project, ID), WorkspaceWithEnvironment {

  class PlatformioStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
      if (!project.isDisposed) {
        project.service<PlatformioWorkspace>().projectOpened()
      }
    }
  }

  override fun createEnvironment(project: Project?,
                                 toolchainNameOrNullForDefault: String?,
                                 problems: EnvironmentProblems,
                                 checkIfFunctional: Boolean,
                                 onMissingToolchain: Runnable?): CPPEnvironment? {
    if (toolchainNameOrNullForDefault != null) throw IllegalStateException("Internal error: Named toolchains are not supported under PlatformIO")
    val toolchain = TrivialNativeToolchain.createInstance()
    return CPPToolchains.createCPPEnvironment(project, toolchain, problems, checkIfFunctional)

  }

  val environment: CPPEnvironment
    get() {
      val environmentProblems = EnvironmentProblems()
      val environment = createEnvironment(myProject,
                                          null,
                                          environmentProblems,
                                          false,
                                          null)
      environmentProblems.throwAsExecutionException()
      if (environment == null) {
        throw ExecutionException(ClionEmbeddedPlatformioBundle.message("message.no.environment"))
      }
      return environment
    }

  override fun getEnvironment(): List<CidrToolEnvironment> {
    return try {
      listOf(environment)
    }
    catch (_: ExecutionException) {
      emptyList()
    }
  }

  override fun collectExcludeRoots(contentRoot: File?): List<File> {
    if (isInitialized) {
      val buildDirectory = project.service<PlatformioService>().buildDirectory
      if (buildDirectory?.isDirectory() == true) return listOf(buildDirectory.toFile())
    }
    return emptyList<File>()

  }

  companion object {

    fun isPlatformioProject(project: Project): Boolean =
      project.service<PlatformioSettings>().linkedProjectsSettings.any { it is PlatformioProjectSettings }

    fun linkProject(project: Project, projectPath: VirtualFile) {
      val settings = PlatformioProjectSettings.default()
      settings.externalProjectPath = projectPath.path
      val workspace = project.service<PlatformioWorkspace>()
      linkExternalProject(project, ID, settings, workspace)
    }
  }
}

val ID: ProjectSystemId = ProjectSystemId("PlatformIO", ClionEmbeddedPlatformioBundle.message("platformio.id"))

private class PlatformioWorkspaceProvider : CidrWorkspaceProvider {
  override fun getWorkspace(project: Project): PlatformioWorkspace =
    project.service<PlatformioWorkspace>()

  override fun loadWorkspace(project: Project) {
    runInEdt {
      if (!project.isDisposed) {
        project.service<PlatformioWorkspace>().projectOpened()
      }
    }
  }
}

private class PlatformioIconProvider : ExternalSystemIconProvider {
  override val reloadIcon: Icon
    get() = pioIcon(PlatformIcons.SYNCHRONIZE_ICON)
  override val projectIcon: Icon
    get() = ClionEmbeddedPlatformioIcons.LogoPlatformIO
}

internal val LOG: Logger = Logger.getInstance(PlatformioWorkspace::class.java)
