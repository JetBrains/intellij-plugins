package org.jetbrains.qodana.inspectionKts.kotlin

import com.intellij.openapi.diagnostic.thisLogger
import icons.KotlinBaseResourcesIcons
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample

private class KotlinInspectionKtsExampleProvider : InspectionKtsExample.Provider {
  override fun example(): InspectionKtsExample? {
    val resourceUrl = this::class.java.classLoader.getResource("examples/kotlin-examples.inspection.kts")
    @Suppress("UrlHashCode")
    if (resourceUrl == null) {
      thisLogger().error("Can't find kotlin inspection kts examples")
      return null
    }
    return InspectionKtsExample(
      icon = KotlinBaseResourcesIcons.Kotlin,
      text = QodanaBundle.message("inspectionkts.example.kotlin"),
      weight = 8,
      resourceUrl
    )
  }
}