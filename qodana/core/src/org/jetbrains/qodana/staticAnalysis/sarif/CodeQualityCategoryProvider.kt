package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeInspection.ex.CodeQualityCategories
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.extensions.ExtensionPointName

interface CodeQualityCategoryProvider {
  companion object {
    val EP_NAME: ExtensionPointName<CodeQualityCategoryProvider> =
      ExtensionPointName.create("org.intellij.qodana.codeQualityCategoryProvider")

    fun getCodeQualityCategory(wrapper: InspectionToolWrapper<*, *>): CodeQualityCategories? {
      return EP_NAME.extensionList.firstNotNullOfOrNull { it.getCodeQualityCategory(wrapper) }
    }
  }

  fun getCodeQualityCategory(wrapper: InspectionToolWrapper<*, *>): CodeQualityCategories?
}
