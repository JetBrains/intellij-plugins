package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.vfs.VirtualFile

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

  fun getNotificationBannerViewModel(): GithubPromoBannerViewModelImpl?

  fun notifySuccessfulWorkflowAddition(files: CreatedFiles)

  fun notifyFailedWorkflowAddition(message: String)
}