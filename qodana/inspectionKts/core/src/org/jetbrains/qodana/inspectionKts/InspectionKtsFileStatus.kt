package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.ide.script.IdeScriptEngine
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

sealed interface InspectionKtsFileStatus {
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

class CompiledInspectionKtsInspections(
  val inspections: Set<DynamicInspectionDescriptor>,
  val customData: Set<CompiledInspectionsKtsData>,
  @Suppress("unused") private val engine: IdeScriptEngine?, // to keep classes loaded by the engine
)

interface CompiledInspectionKtsPostProcessorFactory {
  companion object {
    private val EP_NAME: ExtensionPointName<CompiledInspectionKtsPostProcessorFactory> = ExtensionPointName.create("com.intellij.compiledInspectionKtsPostProcessorFactory")
    
    fun getProcessor(result: Any): CompiledInspectionKtsPostProcessor? =
      EP_NAME.extensionList.firstNotNullOfOrNull { it.createProcessorIfApplicable(result) }
  }

  fun createProcessorIfApplicable(result: Any): CompiledInspectionKtsPostProcessor?
}

fun interface CompiledInspectionKtsPostProcessor {
  fun process(project: Project, filePath: Path): CompiledInspectionsKtsData?
}