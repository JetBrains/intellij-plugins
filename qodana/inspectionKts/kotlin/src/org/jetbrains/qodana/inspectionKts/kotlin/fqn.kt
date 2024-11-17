package org.jetbrains.qodana.inspectionKts.kotlin

import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider

private class KotlinInspectionKtsDefaultImportsProvider : InspectionKtsDefaultImportProvider {
  override fun imports(): List<String> {
    val thisPackage = "org.jetbrains.qodana.inspectionKts.kotlin"
    return listOf(
      "org.jetbrains.kotlin.analysis.api.analyze",
      "$thisPackage.getFQN"
    )
  }

}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun KaClassLikeSymbol.getFQN(): String? {
  return classId?.asFqNameString()
}