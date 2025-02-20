package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.ide.script.IdeScriptEngine
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
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

interface CompiledInspectionsKtsData

internal class CompiledInspectionKtsInspections(
  val inspections: Set<DynamicInspectionDescriptor>,
  val userData: Set<CompiledInspectionsKtsData>,
  @Suppress("unused") private val engine: IdeScriptEngine?, // to keep classes loaded by the engine
)

abstract class CompiledInspectionKtsPostProcessor {
  companion object {
    val EP_NAME: ExtensionPointName<CompiledInspectionKtsPostProcessor> = ExtensionPointName.create("org.intellij.qodana.compiledInspectionKtsPostProcessor")

    fun getProcessor(result: Any): CompiledInspectionKtsPostProcessor? =
      EP_NAME.extensionList.firstOrNull { it.isApplicable(result) }
  }

  abstract fun isApplicable(result: Any): Boolean

  abstract fun process(project: Project, filePath: Path, result: Any): CompiledInspectionsKtsData?
}