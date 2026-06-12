package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

class CompilationException(message: String, cause: Throwable? = null) : Exception(message, cause)

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


  fun getToolOrThrow(): Pair<LocalInspectionTool, StateFlow<Exception?>> {
    return when (this) {
      is Cancelled -> {
        throw CompilationException("Inspection compilation was cancelled")
      }
      is Compiling -> {
        throw CompilationException("Inspection compilation is still in progress")
      }
      is Error -> {
        throw CompilationException("Failed to compile inspection", exception)
      }
      is Compiled -> {
        val inspection = inspections.inspections.firstOrNull()
        if (inspection == null) {
          throw CompilationException("No inspection created after compilation")
        }

        val localTool = (inspection as? DynamicInspectionDescriptor.Local)?.tool
        if (localTool == null) throw CompilationException("Compiled inspection is not a local inspection tool")
        localTool to exceptionDuringAnalysis
      }
    }
  }
}

interface CompiledInspectionsKtsData

class CompiledInspectionKtsInspections(
  val inspections: Set<DynamicInspectionDescriptor>,
  val customData: Set<CompiledInspectionsKtsData>,
  @Suppress("unused") private val keepAlive: Any?, // Keep this field to prevent the class from being optimized away
)

interface CompiledInspectionKtsPostProcessorFactory {
  companion object {
    private val EP_NAME: ExtensionPointName<CompiledInspectionKtsPostProcessorFactory> =
      ExtensionPointName.create("org.jetbrains.qodana.inspectionKts.postProcessorFactory")
    
    fun getProcessor(result: Any): CompiledInspectionKtsPostProcessor? =
      EP_NAME.extensionList.firstNotNullOfOrNull { it.createProcessorIfApplicable(result) }
  }

  fun createProcessorIfApplicable(result: Any): CompiledInspectionKtsPostProcessor?
}

fun interface CompiledInspectionKtsPostProcessor {
  fun process(project: Project, filePath: Path): CompiledInspectionsKtsData?
}