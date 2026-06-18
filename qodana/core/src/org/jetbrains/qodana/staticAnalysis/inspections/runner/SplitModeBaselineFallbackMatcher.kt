// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.LogicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport

internal object SplitModeBaselineFallbackMatcher {
  private const val MATCHED_BY_PROPERTY = "matchedMethod"
  private const val MODULE_LOGICAL_LOCATION_KIND = "module"
  private const val BASELINE_EQUALITY_V2 = 2
  private const val BASELINE_EQUALITY_V1 = 1
  private const val STRUCTURAL_FINGERPRINT_VERSION = 1
  private val NORMALIZED_WHITESPACE = Regex("\\s+")

  private const val SPLIT_MODE_API_USAGE = "SplitModeApiUsage"
  private const val SPLIT_MODE_XML_API_USAGE = "SplitModeXmlApiUsage"
  private const val SPLIT_MODE_MIXED_DEPENDENCIES = "SplitModeMixedDependencies"
  private const val SPLIT_MODE_IMPLICIT_MODULE_KIND = "SplitModeImplicitModuleKind"
  private const val MISSING_RUNTIME_DEPENDENCY = "MissingFrontendOrBackendRuntimeDependency"

  private val MESSAGE_ONLY_RULES = setOf(
    SPLIT_MODE_API_USAGE,
    SPLIT_MODE_XML_API_USAGE,
    MISSING_RUNTIME_DEPENDENCY,
  )

  private val MODULE_AWARE_RULES = setOf(
    SPLIT_MODE_MIXED_DEPENDENCIES,
    SPLIT_MODE_IMPLICIT_MODULE_KIND,
  )

  private val MATCH_TIERS = listOf(
    MatchTier("moveAndRefactorTolerant") { it.partialFingerprint(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR) },
    MatchTier("extractionAndRefactorTolerant") { it.partialFingerprint(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR) },
    MatchTier("snippet") { it.normalizedSnippet() },
  )

  fun apply(report: SarifReport, baseline: SarifReport) {
    val baselineRuns = baseline.runs.orEmpty().toMutableList()
    val unmatchedRuns = mutableListOf<Run>()

    for (run in report.runs.orEmpty()) {
      val baselineRun = baselineRuns.firstOrNull { it.toolName() == run.toolName() }
      if (baselineRun == null) {
        unmatchedRuns += run
      }
      else {
        apply(run, baselineRun)
        baselineRuns.remove(baselineRun)
      }
    }

    unmatchedRuns.forEachIndexed { index, run ->
      baselineRuns.getOrNull(index)?.let { apply(run, it) }
    }
  }

  internal fun apply(run: Run, baseline: Run) {
    val currentBuckets = splitModeBuckets(run.results.orEmpty())
    if (currentBuckets.isEmpty()) return

    val baselineBuckets = splitModeBuckets(baseline.results.orEmpty())
    for ((key, currentBucket) in currentBuckets) {
      val baselineBucket = baselineBuckets[key] ?: continue
      reclassifyRedistributedBucket(currentBucket, baselineBucket)
    }
  }

  private fun reclassifyRedistributedBucket(currentBucket: List<Result>, baselineBucket: List<Result>) {
    if (currentBucket.size != baselineBucket.size) return

    val currentEntries = currentBucket.mapIndexed(::BucketEntry)
    val baselineEntries = baselineBucket.mapIndexed(::BucketEntry)
    val currentNewEntries = currentEntries.filter { it.result.baselineState == Result.BaselineState.NEW }
    if (currentNewEntries.isEmpty()) return

    var unmatchedCurrent = currentNewEntries
    var unmatchedBaseline = subtractExactlyMatchedBaselineEntries(currentEntries, baselineEntries)
    if (unmatchedBaseline.size != unmatchedCurrent.size) return

    val matchedTierByCurrentEntryId = mutableMapOf<Int, String>()
    for (tier in MATCH_TIERS) {
      val matchedGroup = matchByTier(unmatchedCurrent, unmatchedBaseline, tier)
      if (matchedGroup.currentIds.isEmpty()) continue

      matchedGroup.currentIds.forEach { matchedTierByCurrentEntryId[it] = tier.name }
      unmatchedCurrent = unmatchedCurrent.filterNot { it.id in matchedGroup.currentIds }
      unmatchedBaseline = unmatchedBaseline.filterNot { it.id in matchedGroup.baselineIds }
    }

    if (unmatchedCurrent.isNotEmpty() || unmatchedBaseline.isNotEmpty()) return

    for ((id, result) in currentNewEntries) {
      val tierName = matchedTierByCurrentEntryId[id] ?: return
      result.baselineState = Result.BaselineState.UNCHANGED
      result.properties = (result.properties ?: PropertyBag()).apply {
        put(MATCHED_BY_PROPERTY, "splitMode:$tierName")
      }
    }
  }

  private fun subtractExactlyMatchedBaselineEntries(
    currentEntries: List<BucketEntry>,
    baselineEntries: List<BucketEntry>,
  ): List<BucketEntry> {
    val unmatchedByExactFingerprint = baselineEntries
      .mapNotNull { entry -> entry.result.exactFingerprint()?.let { it to entry } }
      .groupBy({ it.first }, { it.second })
      .mapValuesTo(HashMap()) { (_, entries) -> entries.toMutableList() }

    val consumedBaselineIds = HashSet<Int>()
    currentEntries.asSequence()
      .filter {
        it.result.baselineState == Result.BaselineState.UNCHANGED ||
        it.result.baselineState == Result.BaselineState.UPDATED
      }
      .mapNotNull { entry -> entry.result.exactFingerprint()?.let { it to entry } }
      .forEach { (exactFingerprint, _) ->
        val availableBaselineEntries = unmatchedByExactFingerprint[exactFingerprint] ?: return@forEach
        if (availableBaselineEntries.isEmpty()) return@forEach

        val matchedBaselineEntry = availableBaselineEntries.removeAt(availableBaselineEntries.lastIndex)
        consumedBaselineIds += matchedBaselineEntry.id
      }

    return baselineEntries.filterNot { it.id in consumedBaselineIds }
  }

  private fun matchByTier(
    currentEntries: List<BucketEntry>,
    baselineEntries: List<BucketEntry>,
    tier: MatchTier,
  ): MatchedGroup {
    val currentByKey = currentEntries.groupByKey(tier)
    val baselineByKey = baselineEntries.groupByKey(tier)
    if (currentByKey.isEmpty() || baselineByKey.isEmpty()) return MatchedGroup.EMPTY

    val matchedCurrentIds = mutableSetOf<Int>()
    val matchedBaselineIds = mutableSetOf<Int>()
    for ((key, sameKeyCurrentEntries) in currentByKey) {
      val sameKeyBaselineEntries = baselineByKey[key] ?: continue
      if (sameKeyCurrentEntries.size != sameKeyBaselineEntries.size) continue

      matchedCurrentIds += sameKeyCurrentEntries.map { it.id }
      matchedBaselineIds += sameKeyBaselineEntries.map { it.id }
    }
    return MatchedGroup(matchedCurrentIds, matchedBaselineIds)
  }

  private fun List<BucketEntry>.groupByKey(tier: MatchTier): Map<String, List<BucketEntry>> {
    val grouped = LinkedHashMap<String, MutableList<BucketEntry>>()
    for (entry in this) {
      val key = tier.key(entry.result)?.trim()?.takeIf { it.isNotEmpty() } ?: continue
      grouped.getOrPut(key) { mutableListOf() } += entry
    }
    return grouped
  }

  private fun splitModeBuckets(results: List<Result>): Map<PrimaryBucketKey, List<Result>> {
    val buckets = LinkedHashMap<PrimaryBucketKey, MutableList<Result>>()
    for (result in results) {
      val key = result.primaryBucketKey() ?: continue
      buckets.getOrPut(key) { mutableListOf() } += result
    }
    return buckets
  }

  private fun Result.primaryBucketKey(): PrimaryBucketKey? {
    val ruleId = ruleId ?: return null
    val shortMessage = shortMessage() ?: return null
    return when (ruleId) {
      in MESSAGE_ONLY_RULES -> PrimaryBucketKey(ruleId, shortMessage, null)
      in MODULE_AWARE_RULES -> {
        val moduleLogicalLocation = moduleLogicalLocation() ?: return null
        PrimaryBucketKey(ruleId, shortMessage, moduleLogicalLocation)
      }
      else -> null
    }
  }

  private fun Result.shortMessage(): String? {
    val messageText = message?.text ?: return null
    return messageText.replace("\r\n", "\n").substringBefore("\n\n").trim().takeIf { it.isNotEmpty() }
  }

  private fun Result.moduleLogicalLocation(): String? {
    return locations.orEmpty().asSequence()
      .flatMap { it.logicalLocations.orEmpty().asSequence() }
      .firstNotNullOfOrNull { it.asModuleLogicalLocation() }
  }

  private fun LogicalLocation.asModuleLogicalLocation(): String? {
    return fullyQualifiedName?.takeIf { kind == MODULE_LOGICAL_LOCATION_KIND && it.isNotBlank() }
  }

  private fun Result.exactFingerprint(): String? {
    val partialFingerprints = partialFingerprints ?: return null
    return partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, BASELINE_EQUALITY_V2)
           ?: partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, BASELINE_EQUALITY_V1)
  }

  private fun Result.partialFingerprint(name: String): String? {
    return partialFingerprints?.get(name, STRUCTURAL_FINGERPRINT_VERSION)
  }

  private fun Result.normalizedSnippet(): String? {
    val snippet = locations.orEmpty().firstOrNull()?.physicalLocation?.region?.snippet?.text ?: return null
    return snippet.replace(NORMALIZED_WHITESPACE, " ").trim().takeIf { it.isNotEmpty() }
  }

  private fun Run.toolName(): String? = tool?.driver?.name

  private data class PrimaryBucketKey(
    val ruleId: String,
    val shortMessage: String,
    val moduleLogicalLocation: String?,
  )

  private data class BucketEntry(
    val id: Int,
    val result: Result,
  )

  private data class MatchTier(
    val name: String,
    val key: (Result) -> String?,
  )

  private data class MatchedGroup(
    val currentIds: Set<Int>,
    val baselineIds: Set<Int>,
  ) {
    companion object {
      val EMPTY = MatchedGroup(emptySet(), emptySet())
    }
  }
}
