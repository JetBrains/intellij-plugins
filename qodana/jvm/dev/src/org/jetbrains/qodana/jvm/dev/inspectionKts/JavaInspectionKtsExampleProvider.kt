package org.jetbrains.qodana.jvm.dev.inspectionKts

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.thisLogger
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample

private class JavaInspectionKtsExampleProvider : InspectionKtsExample.Provider {
  override fun example(): InspectionKtsExample? {
    val resourceUrl = this::class.java.classLoader.getResource("examples/java-examples.inspection.kts")
    @Suppress("UrlHashCode")
    if (resourceUrl == null) {
      thisLogger().error("Can't find java inspection kts examples")
      return null
    }
    return InspectionKtsExample(
      icon = AllIcons.FileTypes.Java,
      text = QodanaBundle.message("inspectionkts.example.java"),
      weight = 2,
      resourceUrl
    )
  }
}