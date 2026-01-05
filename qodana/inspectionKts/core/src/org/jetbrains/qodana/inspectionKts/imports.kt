package org.jetbrains.qodana.inspectionKts

import com.intellij.openapi.extensions.ExtensionPointName

interface InspectionKtsDefaultImportProvider {
  companion object {
    @Suppress("UnresolvedPluginConfigReference")
    val EP_NAME: ExtensionPointName<InspectionKtsDefaultImportProvider> = ExtensionPointName("org.intellij.qodana.inspectionKtsDefaultImportProvider")

    fun imports(): List<String> = EP_NAME.extensionList.flatMap { it.imports() }
  }

  fun imports(): List<String>
}