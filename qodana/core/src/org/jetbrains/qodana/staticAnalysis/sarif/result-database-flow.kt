package org.jetbrains.qodana.staticAnalysis.sarif

import com.google.gson.JsonSyntaxException
import com.intellij.openapi.diagnostic.logger
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ProblemType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.VULNERABLE_API_INSPECTION_ID

private val LOG = logger<QodanaToolResultDatabase>()
private val GSON = SarifUtil.createGson()

/**
 * Given a database filled with SARIF [Result]s provide the flow, to iterate over
 * the captured results for the given inspection group.
 *
 * [org.jetbrains.qodana.staticAnalysis.inspections.runner.AsyncInspectionToolResultWriter] is responsible for filling the table with the SARIF retrieved by this class.
 *
 * Allow for memory-efficient streaming, i.e. don't load all results into memory at the same time.
 *
 * When using this flow in other places, it may be necessary to convert it to a proper list first.
 */
fun QodanaToolResultDatabase.resultsFlowByGroup(inspectionGroup: String, messageReporter: QodanaMessageReporter): Flow<Result> =
  uniqueResultsFlow(inspectionGroup, messageReporter).transform { result ->
    updateRelatedLocations(result)
    emit(result)
  }

private fun QodanaToolResultDatabase.uniqueResultsFlow(inspectionGroup: String, messageReporter: QodanaMessageReporter): Flow<Result> = flow {
  select(inspectionGroup).use { query ->
    val sameHashResults = mutableListOf<String>()
    var previousHash = ""
    for (resultSet in query.executeQuery()) {
      if (previousHash != resultSet.hash && sameHashResults.any()) {
        processSameHash(sameHashResults, messageReporter)?.let { emit(it) }
        sameHashResults.clear()
      }
      previousHash = resultSet.hash
      sameHashResults.add(resultSet.json)
    }
    processSameHash(sameHashResults, messageReporter)?.let { emit(it) }
  }
}.flowOn(StaticAnalysisDispatchers.IO)

private fun QodanaToolResultDatabase.updateRelatedLocations(result: Result) {
  val rootHash = result.getOrAssignProperties()[RELATED_PROBLEMS_ROOT_HASH_PROP] as? String ?: return

  val locations = selectRelatedProblems(rootHash).use { query ->
    val relatedResults = query.executeQuery().toList().parseResults()
    relatedResults.flatMap { it.locations }
  }.toSet()
  result.relatedLocations = result.relatedLocations?.plus(locations) ?: locations
  if (result.ruleId == VULNERABLE_API_INSPECTION_ID && result.relatedLocations?.isNotEmpty() == true) {
    result.getOrAssignProperties()[PROBLEM_TYPE] = ProblemType.VULNERABLE_API_WITH_RELATED_LOCATIONS
  }
}

private fun processSameHash(resultJsons: List<String>, messageReporter: QodanaMessageReporter): Result? {
  val results = resultJsons.parseResults()
  if (results.isEmpty() || results.size == 1) return results.firstOrNull()

  val uniqueResults = results.toSet()
  if (uniqueResults.size != results.size) {
    val first = results.first()
    messageReporter.reportError("Duplicates of problems was found. " +
                                "inspectionId: ${first.ruleId}, " +
                                "file:${first.locations.firstOrNull()?.physicalLocation?.artifactLocation?.uri}, " +
                                "line: ${first.locations.firstOrNull()?.physicalLocation?.region?.startLine}, " +
                                "column: ${first.locations.firstOrNull()?.physicalLocation?.region?.startColumn}, " +
                                "length: ${first.locations.firstOrNull()?.physicalLocation?.region?.charLength}")
  }

  val uniqueTags = uniqueResults.flatMap { it.getOrAssignProperties().tags }.distinct()
  val resultToSubmit = uniqueResults.minByOrNull { it.hashCode() }!!.apply {
    // We do not merge properties, but tags. If properties vary, the first one with the smallest hash code will be used.
    val properties = this.getOrAssignProperties()
    uniqueTags.filter { !properties.tags.contains(it) }.forEach { properties.tags.add(it) }
  }
  return resultToSubmit
}

fun List<String>.parseResults(): List<Result> {
    return this.mapNotNull { json ->
        try {
            GSON.fromJson(json, Result::class.java)
        } catch (e: JsonSyntaxException) {
          LOG.error("Error of reading results in database '$json'", e)
          null
        }
    }
}