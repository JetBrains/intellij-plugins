package org.jetbrains.qodana.inspectionKts.java

import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider

private class JavaInspectionKtsDefaultImportsProvider : InspectionKtsDefaultImportProvider {
  override fun imports(): List<String> {
    val thisPackage = "org.jetbrains.qodana.jvm.dev.inspectionKts"
    return listOf(
      "$thisPackage.getAllSuperTypes",
      "$thisPackage.asPsiClassType",
    )
  }
}
