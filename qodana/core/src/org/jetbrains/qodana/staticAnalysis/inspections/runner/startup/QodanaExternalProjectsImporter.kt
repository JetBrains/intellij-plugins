package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionNotApplicableException
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.observation.Observation
import com.intellij.projectImport.ProjectOpenProcessor
import com.intellij.util.PlatformUtils
import com.intellij.util.SystemProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import kotlin.reflect.KClass

private val LOG = logger<QodanaExternalProjectsImporter>()

abstract class QodanaExternalProjectsImporter(
  val externalSystemUnlinkedProjectAware: ExternalSystemUnlinkedProjectAware,
  val openProcessorClass: KClass<out ProjectOpenProcessor>,
) : QodanaWorkflowExtension {

  init {
    if (!PlatformUtils.isQodana()) {
      throw ExtensionNotApplicableException.create()
    }
  }

  override suspend fun manualProjectsImport(project: Project) {
    importProjects(serviceAsync<QodanaManualModuleImportersService>(), project)
  }

  override suspend fun automaticProjectsImport(project: Project) {
    importProjects(serviceAsync<QodanaAutomaticModuleImportersService>(), project)
  }

  fun shouldRun(): Boolean {
    return !SystemProperties.getBooleanProperty("idea.java.project.setup.disabled", false)
  }

  suspend fun importProjects(moduleImportersService: QodanaModuleImportersService, project: Project) {
    if (shouldRun()) {
      val detectedFiles = moduleImportersService.getFilesForImporter(openProcessorClass)
      if (detectedFiles.isNotEmpty()) {
        withContext(Dispatchers.Default) {
          unlinkRemovedProjectsForImport(detectedFiles, project)
          getNewProjectsToImport(detectedFiles, project)
            .forEach {
              LOG.info("Importing ${externalSystemUnlinkedProjectAware.systemId.id} project file: ${it.path}")
              externalSystemUnlinkedProjectAware.linkAndLoadProjectAsync(project, it.path)
            }
        }
      }
    }
  }

  suspend fun unlinkRemovedProjectsForImport(detectedFiles: List<VirtualFile>, project: Project) {
    getProjectsToUnlink(detectedFiles, project).forEach {
      externalSystemUnlinkedProjectAware.unlinkProject(project, it)
      Observation.awaitConfiguration(project)
    }
  }

  private fun getProjectsToUnlink(detectedFiles: List<VirtualFile>, project: Project): List<String> {
    val detectedFilesPaths = detectedFiles.map { it.parent.path }
    return externalSystemUnlinkedProjectAware.getLinkedProjectsPaths(project)
      .filter { !detectedFilesPaths.contains(it) }
  }

  fun getNewProjectsToImport(detectedFiles: List<VirtualFile>, project: Project): List<VirtualFile> {
    val linkedPaths = externalSystemUnlinkedProjectAware.getLinkedProjectsPaths(project)
    return detectedFiles.filter { !linkedPaths.contains(it.parent.path) }
  }
}