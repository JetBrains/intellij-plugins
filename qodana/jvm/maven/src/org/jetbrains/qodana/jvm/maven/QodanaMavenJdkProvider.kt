package org.jetbrains.qodana.jvm.maven

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.platform.backend.observation.trackActivityBlocking
import com.intellij.util.PlatformUtils
import org.jetbrains.idea.maven.MavenCommandLineInspectionProjectConfigurator
import org.jetbrains.idea.maven.project.MavenImportListener
import org.jetbrains.idea.maven.project.MavenProject
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaActivityKey

class QodanaMavenJdkProvider(val project: Project) : MavenImportListener {

  override fun importFinished(importedProjects: MutableCollection<MavenProject>, newModules: MutableList<Module>): Unit = project.trackActivityBlocking(QodanaActivityKey) {
    if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
      return@trackActivityBlocking
    }
    if (!PlatformUtils.isQodana()) {
      return@trackActivityBlocking
    }
    if (ProjectRootManager.getInstance(project).projectSdk != null) {
      return@trackActivityBlocking
    }
    val future = MavenCommandLineInspectionProjectConfigurator().setupJdkWithSuitableVersion(importedProjects.toList(), EmptyProgressIndicator())
    ApplicationManager.getApplication().executeOnPooledThread {
      val sdk = future.join()
      if (sdk == null) {
        return@executeOnPooledThread
      }
      ApplicationManager.getApplication().invokeLater {
        runWriteAction {
          ProjectRootManager.getInstance(project).projectSdk = sdk
        }
      }
    }
  }
}