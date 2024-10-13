package org.jetbrains.qodana.highlight

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface QodanaHighlightingListener {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaHighlightingListener> = ExtensionPointName("org.intellij.qodana.highlightingListener")
    fun callOnStart(project: Project) = EP_NAME.extensionList.forEach { it.onStart(project) }
  }
  fun onStart(project: Project)
}
