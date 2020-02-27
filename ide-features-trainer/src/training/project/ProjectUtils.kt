// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.project

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import training.lang.LangSupport
import training.learn.LearnBundle
import training.util.featureTrainerVersion
import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.nio.file.Files

object ProjectUtils {

  private val ideProjectsBasePath by lazy {
    val ideaProjectsPath = WizardContext(null, null).projectFileDirectory
    val ideaProjects = File(ideaProjectsPath)
    FileUtils.ensureDirectoryExists(ideaProjects)
    return@lazy ideaProjectsPath
  }

  @Deprecated("Use method below", ReplaceWith("importOrOpenProject(langSupport, null)"))
  fun importOrOpenProject(projectPath: String,
                          projectName: String,
                          classLoader: ClassLoader): Project? {
    throw IllegalStateException("This method should not be called at all")
  }

  /**
   * For example:
   * @projectPath = "/learnProjects/SimpleProject"
   * @projectName = "SimpleProject"
   *
   */
  fun importOrOpenProject(langSupport: LangSupport, projectToClose: Project?, postInitCallback: (learnProject: Project) -> Unit) {
    runBackgroundableTask(LearnBundle.message("learn.project.initializing.process"), project = projectToClose) {
      val dest = File(ideProjectsBasePath, langSupport.defaultProjectName)

      if (!isSameVersion(dest)) {
        if (dest.exists()) {
          dest.deleteRecursively()
        }

        val inputUrl: URL = langSupport.javaClass.classLoader.getResource(langSupport.projectResourcePath) ?: throw IllegalArgumentException(
          "No project ${langSupport.projectResourcePath} in resources for ${langSupport.primaryLanguage} IDE learning course"
        )

        FileUtils.copyResourcesRecursively(inputUrl, dest)
        PrintWriter(versionFile(dest), "UTF-8").use {
          it.println(featureTrainerVersion)
        }
      }
      val toSelect = findFileByIoFile(dest, true) ?: throw Exception("Copied Learn project folder is null")
      invokeLater {
        val openOrImport = ProjectUtil.openOrImport(toSelect.path, projectToClose, false)
                           ?: error("Could not create project for " + langSupport.primaryLanguage)
        postInitCallback(openOrImport)
      }
    }
  }

  private fun isSameVersion(dest: File): Boolean {
    if (!dest.exists()) {
      return false
    }
    val versionFile = versionFile(dest)
    if (!versionFile.exists()) {
      return false
    }
    val res = Files.lines(versionFile.toPath()).findFirst()
    if (res.isPresent) {
      return featureTrainerVersion == res.get()
    }
    return false
  }

  private fun versionFile(dest: File) = File(dest, "feature-trainer-version.txt")


  fun closeAllEditorsInProject(project: Project) {
    FileEditorManagerEx.getInstanceEx(project).windows.forEach {
      it.files.forEach { file -> it.closeFile(file) }
    }
  }
}