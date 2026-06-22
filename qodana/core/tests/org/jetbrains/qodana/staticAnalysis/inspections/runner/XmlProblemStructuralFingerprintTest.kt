package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.DefaultInspectionToolResultExporter
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi.PsiFingerprintData

class XmlProblemStructuralFingerprintTest : BasePlatformTestCase() {
  fun testXmlProblemStoresPsiStructuralSignalsBeforeFingerprinting(): Unit = runBlocking {
    val fingerprintData = PsiFingerprintData(
      structuralSignals = StructuralFingerprintSignals(
        astShape = "CALL_EXPRESSION(REFERENCE_EXPRESSION,VALUE_ARGUMENT_LIST)",
        enclosingScopeName = "handleInput",
        enclosingScopeType = "FUN",
      ),
    )
    val database = QodanaToolResultDatabase.create(FileUtil.createTempDirectory(getTestName(false), null, true).toPath())

    try {
      val result = XmlProblem(problemElement(), fingerprintData = fingerprintData)
        .getSarif(PathMacroManager.getInstance(project), database)

      assertThat(result.partialFingerprints.get(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, 1)).isNotNull()
      assertThat(result.partialFingerprints.get(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, 1)).isNotNull()
      assertThat(result.partialFingerprints.get(BaselineCalculation.ENCLOSING_SCOPE_INDICATOR, 1)).isEqualTo("FUN#handleInput")
    }
    finally {
      database.close()
    }
  }

  private fun problemElement(): Element {
    val problemClass = Element(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)
      .setAttribute(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_ID_ATTRIBUTE, "SplitModeApiUsage")
      .setAttribute("severity", "WARNING")

    return Element("problem")
      .addContent(problemClass)
      .addContent(Element("file").setText("src/Foo.kt"))
      .addContent(Element("line").setText("10"))
      .addContent(Element("offset").setText("12"))
      .addContent(Element("length").setText("3"))
      .addContent(Element("description").setText("Structural problem"))
      .addContent(Element("language").setText("kotlin"))
  }
}
