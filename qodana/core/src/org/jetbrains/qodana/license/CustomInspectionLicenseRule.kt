package org.jetbrains.qodana.license

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.extensions.ExtensionPointName

interface CustomInspectionLicenseRule {
  companion object {
    val EP_NAME: ExtensionPointName<CustomInspectionLicenseRule> =
      ExtensionPointName.create("org.intellij.qodana.customInspectionLicenseRule")

    fun requiresUltimatePlus(wrapper: InspectionToolWrapper<*, *>): Boolean {
      return EP_NAME.extensionList.any { it.requiresUltimatePlus(wrapper) }
    }
  }

  fun requiresUltimatePlus(wrapper: InspectionToolWrapper<*, *>): Boolean
}
