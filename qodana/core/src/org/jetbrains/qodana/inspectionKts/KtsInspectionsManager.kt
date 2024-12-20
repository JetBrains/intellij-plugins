@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)

package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.codeInspection.ex.DynamicInspectionsProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.PlatformUtils
import com.intellij.util.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runInterruptible
import org.jetbrains.annotations.ApiStatus.Internal
import org.jetbrains.qodana.coroutines.appearedFilePath
import org.jetbrains.qodana.coroutines.disappearedFilePath
import org.jetbrains.qodana.coroutines.documentChangesFlow
import org.jetbrains.qodana.coroutines.vfsChangesMapFlow
import org.jetbrains.qodana.license.QodanaLicenseChecker
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.Path

internal const val FORCE_DISABLE_INSPECTION_KTS = "inspection.kts.disabled"

private val QODANA_FLEXINSPECT_SUPPORTED_PLANS = setOf(
  QodanaLicenseType.ULTIMATE,
  QodanaLicenseType.ULTIMATE_PLUS,
  QodanaLicenseType.PREMIUM,
)

fun isInspectionKtsEnabled(): Boolean {
  val forceDisabled = java.lang.Boolean.getBoolean(FORCE_DISABLE_INSPECTION_KTS)
  val isUnitTests = application.isUnitTestMode
  val isHeadless = application.isHeadlessEnvironment
  return when {
    forceDisabled -> {
      false
    }
    isUnitTests -> {
      true
    }
    isHeadless ->  {
      try {
        val qodanaLicenseType = QodanaLicenseChecker.getLicenseType().type
        qodanaLicenseType in QODANA_FLEXINSPECT_SUPPORTED_PLANS
      }
      catch (_ : QodanaException) {
        false
      }
    }
    else -> {
      !PlatformUtils.isCommunityEdition()
    }
  }
}

const val INSPECTIONS_KTS_DIRECTORY = "inspections"
internal const val INSPECTIONS_KTS_EXTENSION = "inspection.kts"

private class KtsDynamicInspectionsProvider : DynamicInspectionsProvider {
  override fun inspections(project: Project): Flow<Set<DynamicInspectionDescriptor>> {
    return KtsInspectionsManager.getInstance(project).ktsInspectionsFlow
      .filterNotNull()
      .mapNotNull {  statuses ->
        val isCompiling = statuses.any { it is InspectionKtsFileStatus.Compiling }
        if (isCompiling && application.isHeadlessEnvironment) {
          return@mapNotNull null
        }
        statuses
          .filterIsInstance<InspectionKtsFileStatus.Compiled>()
          .flatMap { compiled -> compiled.inspections.inspections }
          .toSet()
      }
  }
}

@Internal
@Service(Service.Level.PROJECT)
class KtsInspectionsManager(val project: Project, val scope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): KtsInspectionsManager = project.service()
  }

  private val inspectionKtsClassLoader: ClassLoader by lazy {
    InspectionKtsClassLoader()
  }

  private val inspectionKtsErrorLogManager by lazy {
    InspectionKtsErrorLogManager(scope)
  }

  private val recompileFileFlow = MutableSharedFlow<Path>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  internal val ktsInspectionsFlow: StateFlow<Set<InspectionKtsFileStatus>?> by lazy {
    inspectionKtsFlow()
      .onEmpty { emit(emptySet()) }
      .flowOn(StaticAnalysisDispatchers.Default)
      .stateIn(scope, SharingStarted.Lazily, null)
  }

  fun recompileFile(file: Path) {
    recompileFileFlow.tryEmit(file)
  }

  private fun inspectionKtsFlow(): Flow<Set<InspectionKtsFileStatus>> {
    if (!isInspectionKtsEnabled()) {
      return emptyFlow()
    }

    val projectDirectory = project.basePath?.let { Path(it) } ?: project.guessProjectDir()?.toNioPath() ?: return flowOf(emptySet())

    val inspectionFiles = inspectionFilesFlow(scriptDirectory = projectDirectory.resolve(INSPECTIONS_KTS_DIRECTORY))
    return compiledInspectionsFilesFlow(inspectionFiles)
  }

  private fun inspectionFilesFlow(scriptDirectory: Path): Flow<Set<Path>> {
    val currentProvidersFiles = mutableSetOf<Path>()

    return flow {
      val inspectionFilesInDirectory = scriptDirectory.collectAllInspectionKtsFiles()
      emit(inspectionFilesInDirectory.map { FileEventType.Appeared(it) })
      emitAll(
        vfsChangesMapFlow { vfsEvents: List<VFileEvent> ->
          val appearedFiles = vfsEvents
            .asSequence()
            .mapNotNull { it.appearedFilePath }
            .filter { it.isInspectionKtsFile() && it.startsWith(scriptDirectory) }
            .map { FileEventType.Appeared(it) }
          val disappearedFiles = vfsEvents
            .asSequence()
            .mapNotNull { it.disappearedFilePath }
            .filter { it.isInspectionKtsFile() && it.startsWith(scriptDirectory) }
            .map { FileEventType.Disappeared(it) }

          if (appearedFiles.none() && disappearedFiles.none()) {
            return@vfsChangesMapFlow null
          }

          val allEvents = (appearedFiles + disappearedFiles).toList()
          allEvents
        }
      )
    }.map { fileEvents ->
      for (event in fileEvents) {
        when(event) {
          is FileEventType.Appeared -> currentProvidersFiles.add(event.path)
          is FileEventType.Disappeared -> currentProvidersFiles.remove(event.path)
        }
      }
      currentProvidersFiles.toSet()
    }.distinctUntilChanged()
  }

  private fun compiledInspectionsFilesFlow(inspectionFilesFlow: Flow<Set<Path>>): Flow<Set<InspectionKtsFileStatus>> {
    val alreadyPresentFiles = mutableMapOf<Path, Flow<InspectionKtsFileStatus>>()
    return inspectionFilesFlow.flatMapLatest { currentFiles ->
      alreadyPresentFiles.keys.removeIf { it !in currentFiles }
      for (file in currentFiles) {
        alreadyPresentFiles.computeIfAbsent(file) {
          compiledInspectionFileFlow(it)
        }
      }

      combine(alreadyPresentFiles.values.toList()) {
        it.toSet()
      }.onEmpty { emit(emptySet()) }
    }
  }

  private fun compiledInspectionFileFlow(file: Path): Flow<InspectionKtsFileStatus> {
    var status: InspectionKtsFileStatus? = null
    return flow {
      val currentStatus = status
      if (currentStatus != null) {
        emit(currentStatus)
      } else {
        emit(InspectionKtsFileStatus.Compiling(file))
        val newStatus = doCompileInspectionKtsFile(file)
        status = newStatus
        emit(newStatus)
      }

      emitAll(
        signalsAfterInitialCompilation(file).transform { signal ->
          when(signal) {
            CompiledInspectionSignal.Recompile -> {
              status = null
              emit(InspectionKtsFileStatus.Compiling(file))
              val newStatus = doCompileInspectionKtsFile(file)
              status = newStatus
              emit(newStatus)
            }
            is CompiledInspectionSignal.UpdateOutdatedState -> {
              @Suppress("NAME_SHADOWING") val currentStatus = status
              val newStatus = when(currentStatus) {
                is InspectionKtsFileStatus.Compiled -> {
                  val currentHash = currentStatus.scriptContentHash
                  val isNewHash = currentHash != signal.newContentHash
                  currentStatus.copy(isOutdated = isNewHash)
                }
                is InspectionKtsFileStatus.Error -> {
                  val currentHash = currentStatus.scriptContentHash
                  val isNewHash = currentHash != signal.newContentHash
                  currentStatus.copy(isOutdated = isNewHash)
                }
                else -> return@transform
              }
              status = newStatus
              emit(newStatus)
            }
          }
        }
      )
    }.distinctUntilChanged()
  }

  private suspend fun doCompileInspectionKtsFile(file: Path): InspectionKtsFileStatus {
    return compileInspectionKtsFile(project, file, inspectionKtsErrorLogManager.Logger(file), inspectionKtsClassLoader)
  }

  private suspend fun signalsAfterInitialCompilation(file: Path): Flow<CompiledInspectionSignal> {
    val document = getDocumentByNioPath(file)

    val recompileFlow = recompileFileFlow
      .filter { it == file }
      .map { CompiledInspectionSignal.Recompile }

    val updateOutdatedStatusFlow = document?.let {
      documentChangesFlow(document)
        .map { CompiledInspectionSignal.UpdateOutdatedState(it.text.hashCode()) }
        .distinctUntilChanged()
    } ?: emptyFlow()

    return merge(recompileFlow, updateOutdatedStatusFlow)
  }
}

private sealed interface CompiledInspectionSignal {
  data object Recompile : CompiledInspectionSignal

  data class UpdateOutdatedState(val newContentHash: Int) : CompiledInspectionSignal
}

private sealed class FileEventType(val path: Path) {
  class Appeared(path: Path) : FileEventType(path)

  class Disappeared(path: Path) : FileEventType(path)
}

private fun Path.isInspectionKtsFile(): Boolean {
  return fileName?.toString()?.endsWith(INSPECTIONS_KTS_EXTENSION) ?: false
}

private suspend fun Path.collectAllInspectionKtsFiles(): List<Path> {
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    try {
      Files.walk(this@collectAllInspectionKtsFiles)
        .filter { it.isInspectionKtsFile() }
        .toList()
    }
    catch (_ : NoSuchFileException) {
      emptyList()
    }
  }
}