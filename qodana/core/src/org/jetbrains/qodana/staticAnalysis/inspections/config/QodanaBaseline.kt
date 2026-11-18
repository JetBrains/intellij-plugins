package org.jetbrains.qodana.staticAnalysis.inspections.config

import java.nio.file.Path

/** The baseline that a run compares its results with. */
sealed interface QodanaBaseline {
  /** A SARIF report on the disk. A relative [path] is relative to the project. */
  class Local(val path: Path) : QodanaBaseline {
    override fun toString(): String = path.toString()
  }

  /**
   * The baseline of the Qodana Cloud project.
   *
   * [download] runs after a successful analysis. It writes the baseline to a temporary file,
   * or gives null when Qodana Cloud gives no baseline. The caller deletes the file.
   */
  class Cloud internal constructor(internal val download: suspend () -> Path?) : QodanaBaseline {
    override fun toString(): String = "Qodana Cloud"
  }
}
