package org.jetbrains.qodana.staticAnalysis.inspections.config

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

/** The baseline that a run compares its results with. */
sealed interface QodanaBaseline {
  /** The baseline source that the run writes to the report: `LOCAL`, `CLOUD` or `NONE`. */
  val source: String

  /** The baseline file before the analysis. Only a [Local] baseline has one, because a [Cloud] baseline downloads after the analysis. */
  val path: Path?

  /** Calls [compare] with the baseline file after the analysis and gives its value, or null when the run has no baseline file. */
  suspend fun <T> withFile(compare: suspend (Path) -> T): T?

  /** A SARIF report on the disk. A relative [path] is relative to the project. */
  data class Local(override val path: Path) : QodanaBaseline {
    override val source: String get() = "LOCAL"

    override suspend fun <T> withFile(compare: suspend (Path) -> T): T = compare(path)
  }

  /**
   * The baseline of the Qodana Cloud project.
   *
   * [download] writes the baseline to a temporary file, or gives null when Qodana Cloud gives no baseline.
   */
  class Cloud internal constructor(internal val download: suspend () -> Path?) : QodanaBaseline {
    override val source: String get() = "CLOUD"

    override val path: Path? get() = null

    /** Downloads the baseline and deletes the file after [compare]. */
    override suspend fun <T> withFile(compare: suspend (Path) -> T): T? {
      val file = download() ?: return null
      try {
        return compare(file)
      }
      finally {
        withContext(NonCancellable + StaticAnalysisDispatchers.IO) { file.deleteIfExists() }
      }
    }

    override fun toString(): String = "Cloud"
  }

  /** A run without a baseline. */
  data object None : QodanaBaseline {
    override val source: String get() = "NONE"

    override val path: Path? get() = null

    override suspend fun <T> withFile(compare: suspend (Path) -> T): T? = null
  }
}
