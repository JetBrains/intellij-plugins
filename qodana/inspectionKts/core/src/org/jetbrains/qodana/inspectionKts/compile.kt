package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.ide.script.IdeScriptEngine
import com.intellij.ide.script.IdeScriptEngineManager
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.ide.trustedProjects.TrustedProjectsListener
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.qodana.inspectionKts.api.InspectionKts
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.hours

private val LOG = logger<KtsInspectionsManager>()

internal suspend fun compileInspectionKtsFile(
  project: Project,
  file: Path,
  errorLogger: InspectionKtsErrorLogManager.Logger,
  classLoader: ClassLoader
): InspectionKtsFileStatus {
  waitWhenProjectTrusted(project)

  LOG.info("Compiling $file")

  val exceptionDuringAnalysisFlow = MutableStateFlow<Exception?>(null)
  val engine = getKotlinScriptingEngine(classLoader)
  if (engine == null) {
    LOG.warn("Failed to compile $file: kotlin scripting engine not found")
    val emptyCompiled = InspectionKtsFileStatus.Compiled(
      CompiledInspectionKtsInspections(emptySet(), emptySet(), null),
      exceptionDuringAnalysisFlow,
      errorLogger,
      0,
      false,
      file
    )
    return emptyCompiled
  }

  val scriptText = getDocumentByNioPath(file)?.text ?: runInterruptible(IO) {
    file.readText()
  }
  val scriptContentHash = scriptText.hashCode()
  val scriptTextWithDefaultImports = InspectionKtsDefaultImportProvider.imports().joinToString("") { "import $it\n" } + scriptText

  val dynamicInspectionData: InspectionKtsResultData = try {
    val result = coroutineScope {
      val resultDeferred = async(IO) {
        withBackgroundProgress(project, InspectionKtsBundle.message("compiling.kts.file", file.name), true) {
          val res = engine.eval(scriptTextWithDefaultImports)
          yield()
          res
        }
      }
      return@coroutineScope try {
        resultDeferred.await()
      }
      catch (ce : CancellationException) {
        if (this.coroutineContext.job.isCancelled) {
          throw ce
        }
        else {
          InspectionKtsFileStatus.Cancelled(file)
        }
      }
    }
    if (result is InspectionKtsFileStatus.Cancelled) {
      return result
    }
    yield()

    KeepAliveKotlinCompileService.getInstance().keepCompilerAliveJob.start()

    val processor = CompiledInspectionKtsPostProcessorFactory.getProcessor(result)
    if (processor == null) {
      val resultList = result as? Collection<*>
                       ?: error("Got $result from inspection script, expected ${Collection::class.java.canonicalName} of ${InspectionKts::class.java.canonicalName}")
      val inspectionsKts: Sequence<InspectionKts> = resultList.asSequence().mapIndexed { idx, item ->
        item as? InspectionKts
        ?: error("Got $item on position $idx from inspection script, expected ${InspectionKts::class.java.canonicalName}")
      }
      val dynamicInspectionsDescriptor = inspectionsKts.map {
        val inspectionTool = it.__asTool__(exceptionReporter = { exception ->
          project.projectScope.launch {
            errorLogger.logException(exception)
            exceptionDuringAnalysisFlow.value = exception
          }
        })
        DynamicInspectionDescriptor.fromTool(inspectionTool)
      }
      val descriptors = dynamicInspectionsDescriptor.toSet()
      InspectionKtsResultData(descriptors, emptySet())
    }
    else {
      val inspectionsKtsData = processor.process(project, file)
                               ?: error("Failed to process inspection data with ${processor::javaClass.name} processor")
      InspectionKtsResultData(emptySet(), setOf(inspectionsKtsData))
    }
  }
  catch (ce : CancellationException) {
    throw ce
  }
  catch (e : Exception) {
    LOG.warn("Failed to compile $file")
    LOG.warn(e)
    errorLogger.logException(e)
    return InspectionKtsFileStatus.Error(e, errorLogger, scriptContentHash, isOutdated = false, file)
  }
  val compiled = CompiledInspectionKtsInspections(dynamicInspectionData.inspections, dynamicInspectionData.userData, engine)
  LOG.info("Compiled ${file}, number of inspections: ${dynamicInspectionData.inspections.size}")
  return InspectionKtsFileStatus.Compiled(compiled, exceptionDuringAnalysisFlow, errorLogger, scriptContentHash, isOutdated = false, file)
}

internal data class InspectionKtsResultData(
  val inspections: Set<DynamicInspectionDescriptor>,
  val userData: Set<CompiledInspectionsKtsData>
)

internal suspend fun getDocumentByNioPath(file: Path): Document? {
  val virtualFile = LocalFileSystem.getInstance().findFileByNioFile(file)
  val document = if (virtualFile != null) {
    readAction {
      FileDocumentManager.getInstance().getDocument(virtualFile)
    }
  } else {
    null
  }
  return document
}

private suspend fun waitWhenProjectTrusted(project: Project) {
  if (TrustedProjects.isProjectTrusted(project)) return

  suspendCancellableCoroutine { cont ->
    val disposable = Disposer.newDisposable()
    cont.invokeOnCancellation {
      Disposer.dispose(disposable)
    }
    TrustedProjectsListener.onceWhenProjectTrusted(disposable) {
      cont.resumeWith(Result.success(Unit))
    }
  }
}

/**
 * Kotlin compiled disconnects after some idle time (need to investigate and tune daemon options), for now we have this temp fix
 */
@Service(Service.Level.APP)
private class KeepAliveKotlinCompileService(scope: CoroutineScope) {
  companion object {
    fun getInstance(): KeepAliveKotlinCompileService = service()
  }

  val keepCompilerAliveJob = scope.launch(start = CoroutineStart.LAZY) {
    while (true) {
      delay(1.hours)
      withContext(IO) {
        getKotlinScriptingEngine(classLoader = null)?.eval("true")
      }
    }
  }
}

fun getKotlinScriptingEngine(classLoader: ClassLoader?): IdeScriptEngine? {
  return IdeScriptEngineManager.getInstance().getEngineByName("Kotlin - Beta", classLoader)
}