package org.jetbrains.qodana.inspectionKts.js

import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.jetbrains.qodana.staticAnalysis.testFramework.withInspectionKtsFile
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/../../core/test-data/QodanaJsInspectionKtsTest")
class QodanaJsInspectionKtsTest : QodanaRunnerTestCase() {
  @Test
  fun `testTemplate provides valid inspection`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"
    val inspectionKtsContent = inspectionKtsTemplate.templateContent.invoke(filename)

    withInspectionKtsFile(qodanaConfig.projectPath, filename, inspectionKtsContent) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig(name = "qodana.single:MyJsInspection"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)

      runAnalysis()

      assertSarifResults()
    }
  }

  @Test
  fun `testInspection works in flexinspect subdir`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"
    val inspectionKtsContent = inspectionKtsTemplate.templateContent.invoke(filename)

    withInspectionKtsFile(qodanaConfig.projectPath, "js/$filename", inspectionKtsContent) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig(name = "qodana.single:MyJsInspection"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)

      runAnalysis()

      assertSarifResults()
    }
  }
}