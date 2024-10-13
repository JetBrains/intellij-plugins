package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.command.impl.DummyProject
import com.jetbrains.qodana.sarif.model.*
import com.jetbrains.qodana.sarif.model.Result.BaselineState
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.jetbrains.qodana.staticAnalysis.inspections.config.FailureConditions
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.coverageStats
import org.jetbrains.qodana.staticAnalysis.sarif.ResultSummaryContributor
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.ToolErrorInspectListener
import org.jetbrains.qodana.staticAnalysis.sarif.resultSummary
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import org.junit.Test
import kotlin.io.path.Path

class QodanaExitStatusKtTest {
  private fun withRun(f: (Run) -> Unit): ExitStatus {
    val run = Run().withInvocations(mutableListOf(Invocation()))
    run.resultSummary = emptyMap()
    f(run)
    return run.firstExitStatus
  }

  private val emptyConfig = QodanaConfig.fromYaml(
    Path("/ignored"),
    Path("/ignored"),
    outputFormat = DEFAULT_OUTPUT_FORMAT,
    resultsStorage = Path("/ignored")
  )

  private fun ObjectAssert<ExitStatus>.isExitWithDescription(desc: String, code: Int = 255, executionSuccessful: Boolean = true) {
    isEqualTo(ExitStatus(code, desc, executionSuccessful))
  }

  private fun ObjectAssert<ExitStatus>.isSuccessful() {
    isEqualTo(ExitStatus(0, null, true))
  }

  private fun resultOf(severity: HighlightSeverity, baselineState: BaselineState? = null) =
    Result().withBaselineState(baselineState)
      .withProperties(PropertyBag().apply { put("ideaSeverity", severity.name) })

  private fun Run.setup(cfg: QodanaConfig = emptyConfig, vararg results: Result) {
    ResultSummaryContributor().contribute(withResults(results.toList()), DummyProject.getInstance(), cfg)
  }

  @Test
  fun `should not do anything when there is not exactly 1 invocation`() {
    val run = Run()
    run.resultSummary = emptyMap()

    setInvocationExitStatus(run, emptyConfig)

    run.withInvocations(emptyList())
    setInvocationExitStatus(run, emptyConfig)

    run.withInvocations(listOf(Invocation(), Invocation()))
    setInvocationExitStatus(run, emptyConfig)

    assertThat(run.invocations)
      .allSatisfy {
        assertThat<Int>(it.exitCode).isNull()
        assertThat<Boolean>(it.executionSuccessful).isNull()
        assertThat(it.exitCodeDescription).isNull()
      }
  }

  @Test
  fun `should not do anything if summary not available`() {
    setInvocationExitStatus(Run().withInvocations(listOf(Invocation())), emptyConfig)

    assertThat(Run().withInvocations(listOf(Invocation())).invocations)
      .allSatisfy {
        assertThat<Int>(it.exitCode).isNull()
        assertThat<Boolean>(it.executionSuccessful).isNull()
        assertThat(it.exitCodeDescription).isNull()
      }
  }

  @Test
  fun `should not override existing status`() {
    val expect = ExitStatus(123, "my description", false)
    val actual = withRun {
      val inv = it.invocations.single()
      inv.exitCode = expect.code
      inv.executionSuccessful = expect.success
      inv.exitCodeDescription = expect.description

      setInvocationExitStatus(it, emptyConfig)
    }

    assertThat(actual).isEqualTo(expect)
  }

  @Test
  fun `should set successful execution if all conditions ok`() {
    val cfg = emptyConfig.copy(
      failOnErrorNotification = true,
      failureConditions = FailureConditions(
        FailureConditions.SeverityThresholds(
          any = 0,
          critical = 0,
          high = 0,
          moderate = 0),
        FailureConditions.TestCoverageThresholds(
          total = 0,
          fresh = 0
        )
      )
    )
    val actual = withRun { run ->
      run.coverageStats = CoverageData.entries.associate { it.prop to 1 }
      setInvocationExitStatus(run, cfg)
    }

    assertThat(actual).isSuccessful()
  }

  @Test
  fun `should set failure when any tool error notifications present`() {
    val cfg = emptyConfig.copy(failOnErrorNotification = true)
    val actual = withRun {
      it.invocations.single()
        .withToolExecutionNotifications(
          listOf(Notification()
                   .withKind(ToolErrorInspectListener.TOOL_ERROR_NOTIFICATION)
                   .withLevel(Notification.Level.ERROR))
        )

      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual).isExitWithDescription("Qodana was configured to fail on any error notification", code = 70, false)
  }

  @Test
  fun `should set success when only other error notifications present`() {
    val cfg = emptyConfig.copy(failOnErrorNotification = true)
    val actual = withRun {
      it.invocations.single()
        .withToolExecutionNotifications(
          listOf(Notification()
                   .withKind("definitely not a tool error")
                   .withLevel(Notification.Level.ERROR))
        )

      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual).isSuccessful()
  }

  @Test
  fun `should set success when non-error notifications present`() {
    val cfg = emptyConfig.copy(failOnErrorNotification = true)
    val actual = withRun {
      it.invocations.single()
        .withToolExecutionNotifications(
          listOf(Notification()
                   .withKind(ToolErrorInspectListener.TOOL_ERROR_NOTIFICATION)
                   .withLevel(Notification.Level.WARNING))
        )

      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual).isSuccessful()
  }

  @Test
  fun `should set failure when total exceeds the threshold`() {
    val cfg = emptyConfig.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 0)))
    val actual = withRun {
      it.setup(cfg, resultOf(HighlightSeverity.INFORMATION))
      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual).isExitWithDescription("""
      Failure condition triggered:
      - Detected 1 problem across all severities, fail threshold: 0
    """.trimIndent())
  }

  @Test
  fun `should set success when total equals the threshold`() {
    val cfg = emptyConfig.copy(failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 1)))
    val actual = withRun {
      it.setup(cfg, resultOf(HighlightSeverity.INFORMATION))
      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual).isSuccessful()
  }


  @Test
  fun `should set failure when results by severity exceed their threshold`() {
    val cfg = emptyConfig.copy(
      failureConditions = FailureConditions(
        FailureConditions.SeverityThresholds(
          critical = 0,
          high = 1,
          moderate = 2
        )
      )
    )
    val actual = withRun {
      it.setup(
        cfg,
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WARNING),
        resultOf(HighlightSeverity.WARNING),
        resultOf(HighlightSeverity.ERROR),
      )
      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual)
      .isExitWithDescription("""
        Failure conditions triggered:
        - Detected 1 problem for severity CRITICAL, fail threshold: 0
        - Detected 2 problems for severity HIGH, fail threshold: 1
        - Detected 3 problems for severity MODERATE, fail threshold: 2
      """.trimIndent())
  }

  @Test
  fun `should set success when results by severity equal their threshold`() {
    val cfg = emptyConfig.copy(
      failureConditions = FailureConditions(
        FailureConditions.SeverityThresholds(
          critical = 1,
          high = 2,
          moderate = 3
        )
      )
    )
    val actual = withRun {
      it.setup(
        cfg,
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WARNING),
        resultOf(HighlightSeverity.WARNING),
        resultOf(HighlightSeverity.ERROR),
      )
      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual).isSuccessful()
  }

  @Test
  fun `should set failure when coverage below the threshold`() {
    val cfg = emptyConfig.copy(failureConditions = FailureConditions(
      testCoverageThresholds = FailureConditions.TestCoverageThresholds(
        total = 2,
        fresh = 3
      )
    ))
    val actual = withRun {
      it.coverageStats = mapOf(CoverageData.TOTAL_COV.prop to 1, CoverageData.FRESH_COV.prop to 2)
      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual)
      .isExitWithDescription("""
        Failure conditions triggered:
        - Total coverage minimum not met. Got 1%, fail threshold 2%
        - Fresh coverage minimum not met. Got 2%, fail threshold 3%
      """.trimIndent())
  }

  @Test
  fun `should set success with unchanged baseline results`() {
    val cfg = emptyConfig.copy(
      baseline = "anything that's not null",
      failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 0))
    )

    val actual = withRun {
      it.setup(cfg, resultOf(HighlightSeverity.ERROR, BaselineState.UNCHANGED))

      setInvocationExitStatus(it, cfg)
    }
    assertThat(actual).isSuccessful()
  }

  @Test
  fun `should set failure with new baseline results`() {
    val cfg = emptyConfig.copy(
      baseline = "anything that's not null",
      failureConditions = FailureConditions(FailureConditions.SeverityThresholds(any = 0)),
    )

    val actual = withRun {
      it.setup(cfg, resultOf(HighlightSeverity.ERROR, BaselineState.NEW))

      setInvocationExitStatus(it, cfg)
    }
    assertThat(actual).isExitWithDescription("""
      Failure condition triggered:
      - Detected 1 problem across all severities, fail threshold: 0
    """.trimIndent())
  }

  @Test
  fun `should set failure with complete failure message`() {
    val cfg = emptyConfig.copy(
      failureConditions = FailureConditions(
        FailureConditions.SeverityThresholds(
          critical = 0,
          high = 1,
          moderate = 2
        ),
        FailureConditions.TestCoverageThresholds(
          total = 2,
          fresh = 3
        )
      )
    )
    val actual = withRun {
      it.setup(
        cfg,
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WEAK_WARNING),
        resultOf(HighlightSeverity.WARNING),
        resultOf(HighlightSeverity.WARNING),
        resultOf(HighlightSeverity.ERROR),
      )
      it.coverageStats = mapOf(CoverageData.TOTAL_COV.prop to 1, CoverageData.FRESH_COV.prop to 2)
      setInvocationExitStatus(it, cfg)
    }

    assertThat(actual)
      .isExitWithDescription("""
        Failure conditions triggered:
        - Detected 1 problem for severity CRITICAL, fail threshold: 0
        - Detected 2 problems for severity HIGH, fail threshold: 1
        - Detected 3 problems for severity MODERATE, fail threshold: 2
        - Total coverage minimum not met. Got 1%, fail threshold 2%
        - Fresh coverage minimum not met. Got 2%, fail threshold 3%
      """.trimIndent())
  }
}
