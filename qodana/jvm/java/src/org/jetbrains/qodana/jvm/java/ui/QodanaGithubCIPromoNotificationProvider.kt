package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.ui.EditorNotificationProvider
import org.jetbrains.qodana.stats.logGithubPromoNotificationShown
import java.util.function.Function
import javax.swing.JComponent

class QodanaGithubCIPromoNotificationProvider: EditorNotificationProvider, DumbAware {

  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
    return Function { fileEditor ->
      when {
        fileEditor !is UserDataHolderEx -> null
        !isGithubActionsFile(file) -> null
        else -> {
          val newViewModel = lazy { GithubPromoEditorViewModelImpl(project, fileEditor, this) }
          val viewModel = fileEditor.putUserDataIfAbsent(GITHUB_PROMO_EDITOR_VIEW_MODEL_KEY, newViewModel).value
          viewModel.getNotificationBanner()?.also { logGithubPromoNotificationShown(project) }
        }
      }
    }
  }

  // copied from community/plugins/github/github-core/src/org/jetbrains/plugins/github/extensions/githubYamlFileDetection.kt
  private fun isGithubActionsFile(virtualFile: VirtualFile): Boolean {
    return isGithubActionYamlFile(virtualFile) || isGithubWorkflowYamlFile(virtualFile)
  }

  // copied from community/plugins/github/github-core/src/org/jetbrains/plugins/github/extensions/githubYamlFileDetection.kt
  private fun isGithubActionYamlFile(virtualFile: VirtualFile): Boolean {
    val fileName = virtualFile.name
    return virtualFile.isFile
           && (FileUtilRt.extensionEquals(fileName, "yml") || FileUtilRt.extensionEquals(fileName, "yaml"))
           && virtualFile.nameWithoutExtension == "action"
  }

  // copied from community/plugins/github/github-core/src/org/jetbrains/plugins/github/extensions/githubYamlFileDetection.kt
  private fun isGithubWorkflowYamlFile(virtualFile: VirtualFile): Boolean {
    val fileName = virtualFile.name
    val filePath = virtualFile.path
    val workflowDirIndex = filePath.indexOf("/workflows")
    val githubDirIndex = filePath.indexOf(".github/")
    return virtualFile.isFile
           && (FileUtilRt.extensionEquals(fileName, "yml") || FileUtilRt.extensionEquals(fileName, "yaml"))
           && workflowDirIndex != -1
           && githubDirIndex != -1
           && workflowDirIndex > githubDirIndex
  }
}