package org.jetbrains.qodana.dart

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.PlatformTestUtil
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.script.scoped.COVERAGE_SKIP_COMPUTATION_PROPERTY
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.junit.Test
import kotlin.io.path.writeText

internal class DartAnalyzeGlobalInspectionIncrementalTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "" to readDartAnalyzeTestData("wanted-and-unwanted.json"),
  )

  @Test
  fun `reports only the changed file when another file has an error`() {
    val wanted = qodanaConfig.projectPath.resolve("lib/wanted.dart")
    wanted.writeText("void main() { missing; }\n")
    requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(wanted)).refresh(false, false)

    val scope = qodanaConfig.projectPath.resolve("scope.json")
    scope.writeText("""{"files":[{"path":"lib/wanted.dart","added":[{"firstLine":1,"count":1}],"deleted":[]}]}""")
    updateQodanaConfig {
      it.copy(script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to scope.toString())))
    }
    PlatformTestUtil.withSystemProperty<RuntimeException>(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true") {
      runAnalysis()
    }

    assertEquals(listOf("lib/wanted.dart"), sarifResultPaths())
    assertEquals("undefined_identifier", singleSarifResult().ruleId)
    assertRecordedAnalysisDirectories("")
  }
}
