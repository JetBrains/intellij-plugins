package org.jetbrains.qodana.inspectionKts.examples

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.thisLogger
import org.jetbrains.qodana.QodanaBundle

class JsonYamlInspectionKtsExampleProvider : InspectionKtsExample.Provider {
  override fun example(): InspectionKtsExample? {
    val resourceUrl = this::class.java.classLoader.getResource("examples/json-yaml.inspection.kts")
    @Suppress("UrlHashCode")
    if (resourceUrl == null) {
      thisLogger().error("Can't find yaml-json inspection kts examples")
      return null
    }
    return InspectionKtsExample(
      icon = AllIcons.FileTypes.Json,
      text = QodanaBundle.message("inspectionkts.example.json"),
      weight = 32,
      resourceUrl
    )
  }
}