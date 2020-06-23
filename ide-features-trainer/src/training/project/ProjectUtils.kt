// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.project

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.ex.FileChooserDialogImpl
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.LearnBundle
import training.util.featureTrainerVersion
import java.io.File
import java.io.PrintWriter
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

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
  fun importOrOpenProject(langSupport: LangSupport, projectToClose: Project?, postInitCallback: (learnProject: Project) -> Unit) {
    runBackgroundableTask(LearnBundle.message("learn.project.initializing.process"), project = projectToClose) {
      val path = LangManager.getInstance().state.languageToProjectMap[langSupport.primaryLanguage]
      val canonicalPlace = File(ideProjectsBasePath, langSupport.defaultProjectName)
      var dest = if (path != null) File(path) else canonicalPlace

      if (!isSameVersion(dest)) {
        if (dest.exists()) {
          dest.deleteRecursively()
        }
        else {
          dest = canonicalPlace
        }

        val installRemoteProject = langSupport.installRemoteProject
        if (installRemoteProject != null) {
          val ok = invokeAndWaitIfNeeded {
            Messages.showOkCancelDialog(projectToClose,
                                        LearnBundle.message("learn.project.initializing.download.message"),
                                        LearnBundle.message("learn.project.initializing.download.title"),
                                        LearnBundle.message("learn.project.initializing.download.accept"),
                                        Messages.getCancelButton(),
                                        null) == Messages.OK
          }
          if (!ok) return@runBackgroundableTask
          installRemoteProject(dest)
        }
        else {
          val inputUrl: URL = langSupport.javaClass.classLoader.getResource(langSupport.projectResourcePath)
          ?: throw IllegalArgumentException("No project ${langSupport.projectResourcePath} in resources for ${langSupport.primaryLanguage} IDE learning course")

          if (!FileUtils.copyResourcesRecursively(inputUrl, dest)) {
            val directories = invokeAndWaitIfNeeded {
              val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
                .withTitle(LearnBundle.message("learn.project.initializing.choose.place"))
              val dialog = FileChooserDialogImpl(descriptor, null)
              val result = CompletableFuture<List<VirtualFile>>()
              dialog.choose(VfsUtil.getUserHomeDir(), Consumer { result.complete(it) })
              result
            }.get()
            if (directories.isEmpty())
              return@runBackgroundableTask
            val chosen = directories.single()
            val canonicalPath = chosen.canonicalPath ?: error("No canonical path for $chosen")
            dest = File(canonicalPath, langSupport.defaultProjectName)
            if (!FileUtils.copyResourcesRecursively(inputUrl, dest)) {
              invokeLater {
                Messages.showInfoMessage(LearnBundle.message("learn.project.initializing.cannot.create.message"),
                                         LearnBundle.message("learn.project.initializing.cannot.create.title"))
              }
              return@runBackgroundableTask
            }
          }
        }

        LangManager.getInstance().state.languageToProjectMap[langSupport.primaryLanguage] = dest.absolutePath
        PrintWriter(versionFile(dest), "UTF-8").use {
          it.println(featureTrainerVersion)
        }
      }
      val toSelect = findFileByIoFile(dest, true) ?: throw Exception("Copied Learn project folder is null")
      invokeLater {
        val openOrImport = ProjectUtil.openOrImport(toSelect.toNioPath(), OpenProjectTask(projectToClose = projectToClose))
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