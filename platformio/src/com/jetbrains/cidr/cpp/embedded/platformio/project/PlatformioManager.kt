package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionTargetListener
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Function
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspaceInitializationUtil.runAfterPlatformioInitialized
import com.jetbrains.cidr.cpp.embedded.platformio.refreshProject
import java.io.File
import java.nio.file.Path


class PlatformioManager :
  ExternalSystemManager<PlatformioProjectSettings, PlatformioSettingsListener, PlatformioSettings, PlatformioLocalSettings, PlatformioExecutionSettings>,
  ExternalSystemAutoImportAware, StartupActivity {

  override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {}

  override fun getSystemId(): ProjectSystemId = ID

  override fun getSettingsProvider(): Function<Project, PlatformioSettings> =
    Function { project -> project.service<PlatformioSettings>() }

  override fun getLocalSettingsProvider(): Function<Project, PlatformioLocalSettings> =
    Function { project -> project.service<PlatformioLocalSettings>() }


  override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, PlatformioExecutionSettings> =
    Function { executionSettingsFor() }

  override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<PlatformioExecutionSettings>> =
    PlatformioProjectResolver::class.java

  override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<PlatformioExecutionSettings>> =
    PlatformioTaskManager::class.java

  override fun getExternalProjectDescriptor(): FileChooserDescriptor =
    FILE_CHOOSER_DESCRIPTOR

  companion object {
    private val FILE_CHOOSER_DESCRIPTOR = object : FileChooserDescriptor(true,
                                                                         false,
                                                                         false,
                                                                         false,
                                                                         false,
                                                                         false) {}

    private fun executionSettingsFor(): PlatformioExecutionSettings {
      return PlatformioExecutionSettings()
    }
  }

  override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
    return if (project.service<PlatformioService>().iniFiles.contains(changedFileOrDirPath)) project.projectFilePath else null
  }

  override fun getAffectedExternalProjectFiles(projectPath: String?, project: Project): List<File> {
    if (projectPath == null) return emptyList()
    val projectNioPath = Path.of(projectPath)
    return project.service<PlatformioService>().iniFiles.map { projectNioPath.resolve(it).toFile() }
  }

  override fun runActivity(project: Project) {
    project.runAfterPlatformioInitialized {
      val workspace = project.service<PlatformioWorkspace>()
      // Check if initial project reload is needed
      if (PlatformioWorkspace.isPlatformioProject(project)) {
        runInEdt {
          refreshProject(workspace.project, false)
          project.messageBus.connect()
            .subscribe(ExecutionTargetManager.TOPIC, ExecutionTargetListener { runInEdt { refreshProject(project, false) } })
        }
      }
    }
  }
}

class PlatformioFileTypeDetector : FileTypeOverrider {
  override fun getOverriddenFileType(file: VirtualFile): FileType? {
    return if (
      ProjectManager.getInstanceIfCreated()?.openProjects?.any { project ->
        project.service<PlatformioService>().iniFiles.contains(file.path)
      } == true)
      PlatformioFileType.INSTANCE
    else null
  }

}