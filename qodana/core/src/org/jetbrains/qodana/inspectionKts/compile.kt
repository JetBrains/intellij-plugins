package org.jetbrains.qodana.inspectionKts

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.ide.impl.isTrusted
import com.intellij.ide.script.IdeScriptEngine
import com.intellij.ide.script.IdeScriptEngineManager
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
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.inspectionKts.api.InspectionKts
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
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
      CompiledInspectionKtsInspections(emptySet(), null),
      exceptionDuringAnalysisFlow,
      errorLogger,
      0,
      false,
      file
    )
    return emptyCompiled
  }

  val scriptText = getDocumentByNioPath(file)?.text ?: runInterruptible(StaticAnalysisDispatchers.IO) {
    file.readText()
  }
  val scriptContentHash = scriptText.hashCode()
  val scriptTextWithDefaultImports = InspectionKtsDefaultImportProvider.imports().joinToString("") { "import $it\n" } + scriptText

  val dynamicInspectionDescriptors: Set<DynamicInspectionDescriptor>  = try {
    val result = coroutineScope {
      val resultDeferred = async(StaticAnalysisDispatchers.IO) {
        withBackgroundProgress(project, QodanaBundle.message("compiling.kts.file", file.name), true) {
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

    val resultList = result as? Collection<*>
                     ?: error("Got $result from inspection script, expected ${Collection::class.java.canonicalName} of ${InspectionKts::class.java.canonicalName}")
    val inspectionsKts: Sequence<InspectionKts> = resultList.asSequence().mapIndexed { idx, item ->
      item as? InspectionKts ?: error("Got $item on position $idx from inspection script, expected ${InspectionKts::class.java.canonicalName}")
    }
    val dynamicInspectionsDescriptor = inspectionsKts.map {
      val inspectionTool = it.asTool(exceptionReporter = { exception ->
        project.qodanaProjectScope.launch(StaticAnalysisDispatchers.Default) {
          errorLogger.logException(exception)
          exceptionDuringAnalysisFlow.value = exception
        }
      })
      DynamicInspectionDescriptor.fromTool(inspectionTool)
    }
    dynamicInspectionsDescriptor.toSet()
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

  val compiled = CompiledInspectionKtsInspections(dynamicInspectionDescriptors, engine)
  LOG.info("Compiled ${file}, number of inspections: ${dynamicInspectionDescriptors.size}")
  return InspectionKtsFileStatus.Compiled(compiled, exceptionDuringAnalysisFlow, errorLogger, scriptContentHash, isOutdated = false, file)
}

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
  if (project.isTrusted()) return

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

  val keepCompilerAliveJob = scope.launch(StaticAnalysisDispatchers.Default, start = CoroutineStart.LAZY) {
    while (true) {
      delay(1.hours)
      withContext(StaticAnalysisDispatchers.IO) {
        getKotlinScriptingEngine(classLoader = null)?.eval("true")
      }
    }
  }
}

private fun getKotlinScriptingEngine(classLoader: ClassLoader?): IdeScriptEngine? {
  return IdeScriptEngineManager.getInstance().getEngineByName("Kotlin - Beta", classLoader)
}