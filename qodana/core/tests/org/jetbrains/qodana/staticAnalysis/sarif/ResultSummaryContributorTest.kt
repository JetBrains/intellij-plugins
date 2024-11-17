package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.testFramework.TestDataPath
import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Files
import kotlin.io.path.deleteIfExists

@TestDataPath("\$CONTENT_ROOT/testData/ResultSummaryContributorTest")
class ResultSummaryContributorTest: QodanaRunnerTestCase() {

  private fun getSerializedValue(): Map<String, Int>? {
    val path = Files.createTempFile(null, ".sarif")
    return try {
      SarifUtil.writeReport(path, qodanaRunner().sarif)
      SarifUtil.readReport(path)
        .runs
        .single()
        .resultSummary
    } finally {
      path.deleteIfExists()
    }
  }
  @Test
  fun `no baseline - count all`() {
    runAnalysis()

    assertThat(getSerializedValue()).isEqualTo(mapOf("total" to 3, "high" to 3))
  }

  @Test
  fun `baseline - count new`() {
    runAnalysis()

    assertThat(getSerializedValue()).isEqualTo(mapOf("total" to 1, "high" to 1))
  }
}
