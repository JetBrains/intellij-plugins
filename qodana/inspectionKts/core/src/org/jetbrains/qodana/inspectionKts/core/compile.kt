package org.jetbrains.qodana.inspectionKts.core


import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.jetbrains.qodana.inspectionKts.CompiledInspectionKtsPostProcessorFactory
import org.jetbrains.qodana.inspectionKts.InspectionKtsBundle
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider
import org.jetbrains.qodana.inspectionKts.InspectionKtsErrorLogManager
import org.jetbrains.qodana.inspectionKts.InspectionKtsCompilationResult
import org.jetbrains.qodana.inspectionKts.api.InspectionKts
import org.jetbrains.qodana.inspectionKts.getDocumentByNioPath
import org.jetbrains.qodana.inspectionKts.waitWhenProjectTrusted
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
): InspectionKtsCompilationResult {
  waitWhenProjectTrusted(project)

  LOG.info("Compiling $file")

  val engine = getKotlinScriptingEngine(classLoader)
  if (engine == null) {
    LOG.warn("Failed to compile $file: kotlin scripting engine not found")
    return InspectionKtsCompilationResult.Compiled(emptyList(), emptySet(), scriptContentHash = 0, keepAlive = null)
  }

  val scriptText = getDocumentByNioPath(file)?.text ?: runInterruptible(IO) {
    file.readText()
  }
  val scriptContentHash = scriptText.hashCode()
  val scriptTextWithDefaultImports = InspectionKtsDefaultImportProvider.imports().joinToString("") { "import $it\n" } + scriptText

  val compiled: InspectionKtsCompilationResult.Compiled = try {
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
          InspectionKtsCompilationResult.Cancelled
        }
      }
    }
    if (result is InspectionKtsCompilationResult.Cancelled) {
      return result
    }
    yield()

    KeepAliveKotlinCompileService.getInstance().keepCompilerAliveJob.start()

    val processor = CompiledInspectionKtsPostProcessorFactory.getProcessor(result)
    if (processor == null) {
      val resultList = result as? Collection<*>
                       ?: error("Got $result from inspection script, expected ${Collection::class.java.canonicalName} of ${InspectionKts::class.java.canonicalName}")
      val inspectionsKts: List<InspectionKts> = resultList.mapIndexed { idx, item ->
        item as? InspectionKts
        ?: error("Got $item on position $idx from inspection script, expected ${InspectionKts::class.java.canonicalName}")
      }
      InspectionKtsCompilationResult.Compiled(inspectionsKts, emptySet(), scriptContentHash, keepAlive = engine)
    }
    else {
      val inspectionsKtsData = processor.process(project, file)
                               ?: error("Failed to process inspection data with ${processor.javaClass.name} processor")
      InspectionKtsCompilationResult.Compiled(emptyList(), setOf(inspectionsKtsData), scriptContentHash, keepAlive = engine)
    }
  }
  catch (ce : CancellationException) {
    throw ce
  }
  catch (e : Exception) {
    LOG.warn("Failed to compile $file")
    LOG.warn(e)
    errorLogger.logException(e)
    return InspectionKtsCompilationResult.Error(e, scriptContentHash)
  }
  LOG.info("Compiled ${file}, number of inspections: ${compiled.inspections.size}")
  return compiled
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
