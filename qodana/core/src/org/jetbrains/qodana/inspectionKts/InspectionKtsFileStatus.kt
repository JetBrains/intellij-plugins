package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.ide.script.IdeScriptEngine
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

internal sealed interface InspectionKtsFileStatus {
  val file: Path

  data class Compiled(
    val inspections: CompiledInspectionKtsInspections,
    val exceptionDuringAnalysis: StateFlow<Exception?>,
    val errorInLogProvider: InspectionKtsErrorLogManager.ErrorInLogProvider,
    val scriptContentHash: Int,
    val isOutdated: Boolean,
    override val file: Path
  ) : InspectionKtsFileStatus

  data class Error(
    val exception: Exception,
    val errorInLogProvider: InspectionKtsErrorLogManager.ErrorInLogProvider,
    val scriptContentHash: Int,
    val isOutdated: Boolean,
    override val file: Path
  ) : InspectionKtsFileStatus

  data class Compiling(override val file: Path) : InspectionKtsFileStatus

  data class Cancelled(override val file: Path) : InspectionKtsFileStatus
}

internal class CompiledInspectionKtsInspections(
  val inspections: Set<DynamicInspectionDescriptor>,
  @Suppress("unused") private val engine: IdeScriptEngine? // to keep classes loaded by the engine
)
