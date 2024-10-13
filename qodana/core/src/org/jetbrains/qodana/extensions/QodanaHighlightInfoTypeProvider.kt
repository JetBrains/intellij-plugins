package org.jetbrains.qodana.extensions

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.openapi.extensions.ExtensionPointName

interface QodanaHighlightInfoTypeProvider {
  companion object {
    private val EP_NAME: ExtensionPointName<QodanaHighlightInfoTypeProvider> =
      ExtensionPointName.create("org.intellij.qodana.qodanaHighlightInfoTypeProvider")

    fun getHighlightTypeInfo() : HighlightInfoType {
      for (e in EP_NAME.extensionList) {
        return e.createHighlightInfoType()
      }
      return HighlightInfoType.GENERIC_WARNINGS_OR_ERRORS_FROM_SERVER
    }
  }

  fun createHighlightInfoType() : HighlightInfoType
}