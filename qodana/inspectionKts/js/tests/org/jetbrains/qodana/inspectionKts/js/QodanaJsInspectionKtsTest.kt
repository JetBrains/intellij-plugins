package org.jetbrains.qodana.inspectionKts.js

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.jetbrains.qodana.staticAnalysis.testFramework.withInspectionKtsFile
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/test-data/QodanaJsInspectionKtsTest")
class QodanaJsInspectionKtsTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "inspectionKts", "js", "test-data")

  @Test
  fun `testTemplate provides valid inspection`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"
    val inspectionKtsContent = inspectionKtsTemplate.templateContent.invoke(filename)

    withInspectionKtsFile(qodanaConfig.projectPath, filename, inspectionKtsContent) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("qodana.single:MyJsInspection"),
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
          profile = QodanaProfileConfig.named("qodana.single:MyJsInspection"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)

      runAnalysis()

      assertSarifResults()
    }
  }
}