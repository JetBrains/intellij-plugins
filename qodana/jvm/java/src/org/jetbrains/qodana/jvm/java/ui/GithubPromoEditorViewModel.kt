package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel

interface GithubPromoEditorViewModel {

  interface GithubPromoBannerViewModel {

    fun addQodanaWorkflow()

    fun openLandingPage()

    fun dismissPromo()

  }

  data class CreatedFiles(
    val workflowFile: VirtualFile,
    val qodanaYamlFile: VirtualFile?,
  )

  fun getNotificationBanner(): EditorNotificationPanel?

  suspend fun notifySuccessfulWorkflowAddition(files: CreatedFiles)

  fun notifyFailedWorkflowAddition()
}