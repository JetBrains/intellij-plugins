/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.project

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import com.intellij.openapi.vfs.VirtualFile
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

  /**
   * For example:
   * @projectPath = "/learnProjects/SimpleProject"
   * @projectName = "SimpleProject"
   *
   */
  fun importOrOpenProject(projectPath: String, projectName: String): Project? {
    val dest = File(ideProjectsBasePath, projectName)

    if (!isSameVersion(dest)) {
      if (dest.exists()) {
        dest.deleteRecursively()
      }
      val inputUrl: URL = javaClass.getResource(projectPath)
      FileUtils.copyResourcesRecursively(inputUrl, dest)
      PrintWriter(versionFile(dest), "UTF-8").use {
        it.println(featureTrainerVersion)
      }
    }
    val toSelect = findFileByIoFile(dest, false) ?: throw Exception("Copied Learn project folder is null")
    return doImportOrOpenProject(toSelect)
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

  private fun doImportOrOpenProject(projectDir: VirtualFile): Project? {
    val projectRef = Ref<Project>()
    TransactionGuard.getInstance().submitTransactionAndWait({
      projectRef.set(ProjectUtil.openOrImport(projectDir.path, null, false))
    })
    return projectRef.get()
  }


  fun closeAllEditorsInProject(project: Project) {
    FileEditorManagerEx.getInstanceEx(project).windows.forEach {
      it.files.forEach { file -> it.closeFile(file) }
    }
  }
}