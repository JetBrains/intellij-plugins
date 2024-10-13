package org.jetbrains.qodana.inspectionKts.js

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample
import java.net.URL

private class JsInspectionKtsExampleProvider : InspectionKtsExample.Provider {
  override fun example(): InspectionKtsExample? {
    return InspectionKtsExample(
      icon = AllIcons.FileTypes.JavaScript,
      text = QodanaBundle.message("inspectionkts.example.js"),
      weight = 16,
      examplesResourceUrl() ?: return null,
    )
  }
}

private fun examplesResourceUrl(): URL? {
  val resourceUrl = JsInspectionKtsExampleProvider::class.java.classLoader.getResource("examples/javascript-typescript-examples.inspection.kts")
  @Suppress("UrlHashCode")
  if (resourceUrl == null) {
    logger<JsInspectionKtsExampleProvider>().error("Can't find javascript inspection kts examples")
  }
  return resourceUrl
}