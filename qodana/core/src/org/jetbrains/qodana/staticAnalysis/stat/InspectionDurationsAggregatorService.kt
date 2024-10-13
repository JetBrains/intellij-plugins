package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow

/**
 * Aggregator of InspectionFinished events. Each event belongs to single bucket.
 * Each bucket is a list of events with same inspectionId and duration in interval [durationBase^bucket - 1, durationBase^(bucket + 1) - 1)
 * InspectionKind.LOCAL_PRIORITY is skipped because it should always have 0 duration for qodana statistics
 * and LOCAL_PRIORITY should not increase filesCount counter.
 */
@Service(Service.Level.PROJECT)
class InspectionDurationsAggregatorService(val project: Project) {
  companion object {
    suspend fun getInstance(project: Project): InspectionDurationsAggregatorService = project.serviceAsync()
  }

  private val base = System.getProperty("qodana.inspections.duration.bucket.base", "1.3").toDouble()
  private val maxExactRecordsInBucket = System.getProperty("qodana.inspections.duration.bucket.exact.records", "1").toInt()
  private val baseLog = ln(base)

  private data class Key(val inspectionId: String, val kind: InspectListener.InspectionKind, val lowerBound: Long, val upperBound: Long)
  private class Value(
    var problemsCount: Int,
    var filesCount: Int,
    val tool: InspectionToolWrapper<*, *>,
    val exactValues: MutableList<Pair<Long, Int>>)

  private val durations = ConcurrentHashMap<Key, Value>()

  fun addInspectionFinishedEvent(duration: Long, problemsCount: Int, tool: InspectionToolWrapper<*, *>,
                                 kind: InspectListener.InspectionKind) {
    if (kind == InspectListener.InspectionKind.LOCAL_PRIORITY) return

    val (lowerBound, upperBound) = toBucket(duration)
    val key = Key(tool.shortName, kind, lowerBound, upperBound)
    durations.compute(key) { _, v ->
      if (v == null) {
        Value(problemsCount, 1, tool, mutableListOf(duration to problemsCount))
      }
      else {
        v.problemsCount += problemsCount
        v.filesCount += 1
        if (v.filesCount <= maxExactRecordsInBucket) {
          v.exactValues.add(duration to problemsCount)
        }
        v
      }
    }
  }

  fun logDurations() {
    durations.entries.forEach { (key, value) ->
      if (value.filesCount > maxExactRecordsInBucket) {
        InspectionEventsCollector.logInspectionDuration(key.lowerBound,
                                                        key.upperBound,
                                                        value.problemsCount,
                                                        value.filesCount,
                                                        value.tool,
                                                        key.kind,
                                                        project)
      }
      else { // for low count of events for bucket, exact duration will be logged
        value.exactValues.forEach { (duration, problemsCount) ->
          InspectionEventsCollector.logInspectionDuration(duration,
                                                          duration,
                                                          problemsCount,
                                                          1,
                                                          value.tool,
                                                          key.kind,
                                                          project)
        }
      }
    }
  }

  @TestOnly
  fun getSummary(inspectionId: String): String {
    var files = 0
    var problems = 0
    durations.forEach { (k, v) ->
      if (k.inspectionId == inspectionId) {
        files += v.filesCount
        problems += v.problemsCount
      }
    }
    return "files: $files, problems: $problems"
  }

  private fun toBucket(duration: Long): Pair<Long, Long> {
    if (duration < 0) throw IllegalArgumentException()
    if (duration == 0L) return (0L to 1)
    val fairBucket = floor(ln((duration + 1).toDouble()) / baseLog).toInt()
    val fairLowerBound = fromBucket(fairBucket).first
    val bucket = if (fairLowerBound > duration) fairBucket - 1 else fairBucket

    return fromBucket(bucket)
  }

  private fun fromBucket(bucket: Int): Pair<Long, Long> {
    val lowerBound = ceil(base.pow(bucket) - 1).toLong()
    val upperBound = ceil(base.pow(bucket + 1) - 1).toLong()
    return lowerBound to upperBound
  }
}
