package org.jetbrains.qodana.report

import com.intellij.ide.BrowserUtil
import org.jetbrains.qodana.cloud.projectFrontendUrlForQodanaCloud

interface BrowserViewProvider {
  companion object {
    fun qodanaCloudReport(projectId: String, reportId: String): BrowserViewProvider {
      return object : BrowserViewProvider {
        override suspend fun openBrowserView() {
          BrowserUtil.browse(projectFrontendUrlForQodanaCloud(projectId, reportId).toString())
        }
      }
    }
  }

  suspend fun openBrowserView()
}