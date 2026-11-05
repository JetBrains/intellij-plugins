package org.jetbrains.qodana.inspectionKts

import org.jetbrains.qodana.inspectionKts.api.InspectionKts

/**
 * The outcome of compiling and running an `.inspection.kts` file. The caller turns the returned [InspectionKts] into inspection tools,
 * deciding how they are registered.
 */
sealed interface InspectionKtsCompilationResult {
  /**
   * [inspections] are what the script returned, [customData] what a [CompiledInspectionKtsPostProcessor] made of it instead.
   * [keepAlive] keeps the classes they were compiled into reachable.
   */
  class Compiled(
    val inspections: List<InspectionKts>,
    val customData: Set<CompiledInspectionsKtsData>,
    val scriptContentHash: Int,
    val keepAlive: Any?,
  ) : InspectionKtsCompilationResult

  class Error(val exception: Exception, val scriptContentHash: Int) : InspectionKtsCompilationResult

  data object Cancelled : InspectionKtsCompilationResult
}
