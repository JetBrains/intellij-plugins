package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Aggregator of InspectionFinished events. Each event belongs to a single bucket.
 * Each bucket is a list of events with the same inspectionId and contains details about analyzed files and problems found counts.
 * InspectionKind.LOCAL_PRIORITY is skipped because it should always have 0 duration for qodana statistics
 * and LOCAL_PRIORITY should not increase filesCount counter.
 */
@Service(Service.Level.PROJECT)
class InspectionProblemsFoundAggregatorService(val project: Project) {
  companion object {
    suspend fun getInstance(project: Project): InspectionProblemsFoundAggregatorService = project.serviceAsync()
  }

  private data class Key(val inspectionId: String)
  private class Value(
    var problemsCount: Int,
    var filesCount: Int,
    val tool: InspectionToolWrapper<*, *>)

  private val problemsFound = ConcurrentHashMap<Key, Value>()

  fun addInspectionFinishedEvent(problemsCount: Int, tool: InspectionToolWrapper<*, *>,
                                 kind: InspectListener.InspectionKind) {
    if (kind == InspectListener.InspectionKind.LOCAL_PRIORITY) return

    problemsFound.compute(Key(tool.shortName)) { _, v ->
      if (v == null) {
        Value(problemsCount, 1, tool)
      }
      else {
        v.problemsCount += problemsCount
        v.filesCount += 1
        v
      }
    }
  }

  fun logProblemsFound() {
    problemsFound.values.forEach { value ->
      InspectionEventsCollector.logInspectionProblemsFound(value.problemsCount,
                                                           value.filesCount,
                                                           value.tool,
                                                           project)
    }
  }

  @TestOnly
  fun getSummaryFor(inspectionId: String): Pair<Int, Int> {
    var files = 0
    var problems = 0
    problemsFound.forEach { (k, v) ->
      if (k.inspectionId == inspectionId) {
        files += v.filesCount
        problems += v.problemsCount
      }
    }
    return files to problems
  }
}
