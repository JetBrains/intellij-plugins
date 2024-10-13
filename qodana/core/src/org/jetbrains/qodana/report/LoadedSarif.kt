package org.jetbrains.qodana.report

import com.jetbrains.qodana.sarif.model.*
import org.jetbrains.qodana.run.RUN_TIMESTAMP
import java.time.Instant

sealed class LoadedSarif(val sarif: SarifReport)

class NotValidatedSarif(sarif: SarifReport) : LoadedSarif(sarif)

class ValidatedSarif(sarif: SarifReport) : LoadedSarif(sarif) {
  val runs: List<Run> = sarif.runs

  val revisionsToResults: Map<String?, List<Result>> = createRevisionsToResultsMap()

  val tools: List<Tool> = runs.mapNotNull { it.tool }

  val createdAt: Instant? = runs.firstNotNullOfOrNull { run -> run.invocations?.filterNotNull()?.firstNotNullOfOrNull { it.startTimeUtc } }

  val runTimestamp: Long? = sarif.properties?.get(RUN_TIMESTAMP).toString().toLongOrNull()

  val revision: String?
    get() = firstVcsData()?.revisionId

  val branch: String?
    get() = firstVcsData()?.branch

  val jobUrl: String?
    get() = (runs.firstOrNull()?.automationDetails?.properties?.get("jobUrl") as? String)?.takeIf { it.isNotEmpty() }

  private fun createRevisionsToResultsMap(): Map<String?, List<Result>> {
    return runs.groupBy(
      { it.versionControlProvenance?.firstOrNull()?.revisionId }, { it.results.filterNotNull() }
    ).mapValues { it.value.flatten() }
  }

  private fun firstVcsData(): VersionControlDetails? {
    return runs.firstOrNull()?.versionControlProvenance?.firstOrNull()
  }
}
