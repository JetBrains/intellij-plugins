package org.jetbrains.qodana.staticAnalysis.sarif

import com.google.gson.JsonObject
import com.jetbrains.qodana.sarif.SarifUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.inspections.config.FailureConditions
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Files
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText

class FailureConditionsContributorTest : QodanaRunnerTestCase() {
  private fun configure(failureConditions: FailureConditions) = updateQodanaConfig {
    it.copy(failureConditions = failureConditions, profile = QodanaProfileConfig())
  }

  private val gson = SarifUtil.createGson()

  private fun getSerializedValue(): JsonObject? {
    val path = Files.createTempFile(null, ".sarif")
    return try {
      SarifUtil.writeReport(path, qodanaRunner().sarif)
      gson.fromJson(path.readText(), JsonObject::class.java)
        .getAsJsonArray("runs")
        ?.first()
        ?.asJsonObject
        ?.get("properties")
        ?.asJsonObject
        ?.get(QODANA_FAILURE_CONDITIONS)
        ?.asJsonObject
    } finally {
      path.deleteIfExists()
    }
  }

  private fun json(@Language("json") value: String) =
    gson.fromJson(value, JsonObject::class.java)

  private val fullConditions = FailureConditions(
    FailureConditions.SeverityThresholds(
      any = 1,
      critical = 2,
      high = 3,
      moderate = 4,
      low = 5,
      info = 6
    ),
    FailureConditions.TestCoverageThresholds(total = 7, fresh = 8)
  )

  @Test
  fun `should not contribute default failure conditions`() {
    configure(FailureConditions.DEFAULT)
    runAnalysis()

    assertThat(getSerializedValue()).isNull()
  }

  @Test
  fun `should contribute failure conditions in expected format`() {
    val expected = json("""
      {
        "severityThresholds": {
          "any": 1,
          "critical": 2,
          "high": 3,
          "moderate": 4,
          "low": 5,
          "info": 6
        },
        "testCoverageThresholds": {
          "totalCoverage": 7,
          "freshCoverage": 8
        }
      }
    """.trimIndent())
    configure(fullConditions)
    runAnalysis()

    assertThat(getSerializedValue()).isEqualTo(expected)
  }

  @Test
  fun `should only store non-null conditions`() {
    val expected = json("""
      {
        "severityThresholds": {
          "any": 1337
        }
      }
    """.trimIndent())
    configure(FailureConditions(FailureConditions.SeverityThresholds(any = 1337)))
    runAnalysis()
    assertThat(getSerializedValue()).isEqualTo(expected)
  }

}
