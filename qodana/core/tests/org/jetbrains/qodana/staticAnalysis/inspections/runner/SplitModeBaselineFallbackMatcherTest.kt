// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.ArtifactContent
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.LogicalLocation
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.VersionedMap
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SplitModeBaselineFallbackMatcherTest {
  @Test
  fun `should reclassify redistributed split mode api usage bucket`() {
    val unchangedCurrent = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nCurrent reasoning",
      baselineState = Result.BaselineState.UNCHANGED,
      exactFingerprintV2 = "exact-stable",
      extractionTolerant = "shape-stable",
    )
    val redistributedCurrent = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nReasoning after refactoring",
      baselineState = Result.BaselineState.NEW,
      exactFingerprintV2 = "exact-current",
      extractionTolerant = "shape-redistributed",
    )

    val unchangedBaseline = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nBaseline reasoning",
      exactFingerprintV2 = "exact-stable",
      extractionTolerant = "shape-stable",
    )
    val redistributedBaseline = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nReasoning before refactoring",
      exactFingerprintV2 = "exact-baseline",
      extractionTolerant = "shape-redistributed",
    )

    SplitModeBaselineFallbackMatcher.apply(runOf(unchangedCurrent, redistributedCurrent), runOf(unchangedBaseline, redistributedBaseline))

    assertThat(redistributedCurrent.baselineState).isEqualTo(Result.BaselineState.UNCHANGED)
    assertThat(redistributedCurrent.properties?.get("matchedMethod")).isEqualTo("splitMode:extractionAndRefactorTolerant")
  }

  @Test
  fun `should not reclassify when split mode bucket size changed`() {
    val current = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nCurrent reasoning",
      baselineState = Result.BaselineState.NEW,
      exactFingerprintV2 = "exact-current",
      extractionTolerant = "shape-redistributed",
    )

    val baselineFirst = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nBaseline reasoning 1",
      exactFingerprintV2 = "baseline-first",
      extractionTolerant = "shape-redistributed",
    )
    val baselineSecond = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nBaseline reasoning 2",
      exactFingerprintV2 = "baseline-second",
      extractionTolerant = "shape-redistributed",
    )

    SplitModeBaselineFallbackMatcher.apply(runOf(current), runOf(baselineFirst, baselineSecond))

    assertThat(current.baselineState).isEqualTo(Result.BaselineState.NEW)
    assertThat(current.properties?.get("matchedMethod") as? String).isNull()
  }

  @Test
  fun `should keep entire split mode bucket new when only part of it matches`() {
    val currentFirst = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nCurrent reasoning 1",
      baselineState = Result.BaselineState.NEW,
      exactFingerprintV2 = "current-first",
      extractionTolerant = "shared-shape",
    )
    val currentSecond = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nCurrent reasoning 2",
      baselineState = Result.BaselineState.NEW,
      exactFingerprintV2 = "current-second",
      extractionTolerant = "current-only-shape",
    )

    val baselineFirst = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nBaseline reasoning 1",
      exactFingerprintV2 = "baseline-first",
      extractionTolerant = "shared-shape",
    )
    val baselineSecond = splitModeResult(
      ruleId = "SplitModeApiUsage",
      message = "Restricted API usage.\n\nBaseline reasoning 2",
      exactFingerprintV2 = "baseline-second",
      extractionTolerant = "baseline-only-shape",
    )

    SplitModeBaselineFallbackMatcher.apply(runOf(currentFirst, currentSecond), runOf(baselineFirst, baselineSecond))

    assertThat(currentFirst.baselineState).isEqualTo(Result.BaselineState.NEW)
    assertThat(currentSecond.baselineState).isEqualTo(Result.BaselineState.NEW)
    assertThat(currentFirst.properties?.get("matchedMethod") as? String).isNull()
    assertThat(currentSecond.properties?.get("matchedMethod") as? String).isNull()
  }

  @Test
  fun `should reclassify module aware split mode bucket when module logical location is present`() {
    val current = splitModeResult(
      ruleId = "SplitModeMixedDependencies",
      message = "Module has both frontend-only and backend-only dependencies.\n\nCurrent reasoning",
      baselineState = Result.BaselineState.NEW,
      exactFingerprintV2 = "current-mixed",
      extractionTolerant = "mixed-signature",
      moduleLogicalLocation = "intellij.test.module",
    )
    val baseline = splitModeResult(
      ruleId = "SplitModeMixedDependencies",
      message = "Module has both frontend-only and backend-only dependencies.\n\nBaseline reasoning",
      exactFingerprintV2 = "baseline-mixed",
      extractionTolerant = "mixed-signature",
      moduleLogicalLocation = "intellij.test.module",
    )

    SplitModeBaselineFallbackMatcher.apply(runOf(current), runOf(baseline))

    assertThat(current.baselineState).isEqualTo(Result.BaselineState.UNCHANGED)
    assertThat(current.properties?.get("matchedMethod")).isEqualTo("splitMode:extractionAndRefactorTolerant")
  }

  @Test
  fun `should skip module aware split mode bucket without module logical location`() {
    val current = splitModeResult(
      ruleId = "SplitModeMixedDependencies",
      message = "Module has both frontend-only and backend-only dependencies.\n\nCurrent reasoning",
      baselineState = Result.BaselineState.NEW,
      exactFingerprintV2 = "current-mixed",
      extractionTolerant = "mixed-signature",
    )
    val baseline = splitModeResult(
      ruleId = "SplitModeMixedDependencies",
      message = "Module has both frontend-only and backend-only dependencies.\n\nBaseline reasoning",
      exactFingerprintV2 = "baseline-mixed",
      extractionTolerant = "mixed-signature",
    )

    SplitModeBaselineFallbackMatcher.apply(runOf(current), runOf(baseline))

    assertThat(current.baselineState).isEqualTo(Result.BaselineState.NEW)
    assertThat(current.properties?.get("matchedMethod") as? String).isNull()
  }

  private fun runOf(vararg results: Result): Run {
    return Run().apply {
      this.results = results.toList()
    }
  }

  private fun splitModeResult(
    ruleId: String,
    message: String,
    baselineState: Result.BaselineState? = null,
    exactFingerprintV2: String? = null,
    moveTolerant: String? = null,
    extractionTolerant: String? = null,
    snippet: String? = null,
    moduleLogicalLocation: String? = null,
  ): Result {
    val partialFingerprints = VersionedMap<String>().apply {
      exactFingerprintV2?.let { put(BaselineCalculation.EQUAL_INDICATOR, 2, it) }
      moveTolerant?.let { put(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, 1, it) }
      extractionTolerant?.let { put(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, 1, it) }
    }

    return Result().apply {
      this.ruleId = ruleId
      this.message = Message().withText(message)
      this.baselineState = baselineState
      this.partialFingerprints = partialFingerprints
      this.locations = listOf(location(moduleLogicalLocation, snippet))
    }
  }

  private fun location(moduleLogicalLocation: String?, snippet: String?): Location {
    val region = Region().apply {
      if (snippet != null) {
        this.snippet = ArtifactContent().withText(snippet)
      }
    }

    return Location().apply {
      this.physicalLocation = PhysicalLocation().apply {
        this.artifactLocation = ArtifactLocation().withUri("src/Test.kt")
        this.region = region
      }
      if (moduleLogicalLocation != null) {
        this.logicalLocations = linkedSetOf(
          LogicalLocation()
            .withKind("module")
            .withFullyQualifiedName(moduleLogicalLocation)
        )
      }
    }
  }
}
