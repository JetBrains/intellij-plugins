package org.jetbrains.qodana.inspectionKts.bta

import com.intellij.codeInspection.ex.DynamicInspectionDescriptor
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.yield
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.buildtools.api.KotlinLogger
import org.jetbrains.kotlin.buildtools.api.BaseCompilationOperation
import org.jetbrains.kotlin.buildtools.api.CompilerMessageRenderer
import org.jetbrains.kotlin.buildtools.api.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.buildtools.api.arguments.CompilerPlugin
import org.jetbrains.kotlin.buildtools.api.arguments.CompilerPluginOption
import org.jetbrains.kotlin.buildtools.api.arguments.ExperimentalCompilerArgument
import org.jetbrains.kotlin.buildtools.api.arguments.JvmCompilerArguments
import org.jetbrains.kotlin.buildtools.api.jvm.JvmPlatformToolchain.Companion.jvm
import org.jetbrains.kotlin.buildtools.api.jvm.jvmCompilationOperation
import org.jetbrains.kotlin.buildtools.api.jvm.operations.JvmCompilationOperation
import org.jetbrains.qodana.inspectionKts.CompiledInspectionKtsInspections
import org.jetbrains.qodana.inspectionKts.CompiledInspectionKtsPostProcessorFactory
import org.jetbrains.qodana.inspectionKts.CompiledInspectionsKtsData
import org.jetbrains.qodana.inspectionKts.FLEX_INSPECT_PROVIDER_NAME
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_EXTENSION
import org.jetbrains.qodana.inspectionKts.__asTool__
import org.jetbrains.qodana.inspectionKts.InspectionKtsBundle
import org.jetbrains.qodana.inspectionKts.InspectionKtsClassLoader
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider
import org.jetbrains.qodana.inspectionKts.InspectionKtsErrorLogManager
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.api.InspectionKts
import org.jetbrains.qodana.inspectionKts.api.bta.InspectionKtsBtaCompiler
import org.jetbrains.qodana.inspectionKts.getDocumentByNioPath
import org.jetbrains.qodana.inspectionKts.scriptTemplate.InspectionKtsScriptTemplate
import org.jetbrains.qodana.inspectionKts.waitWhenProjectTrusted
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.walk
import kotlin.io.path.writeText
import kotlin.script.experimental.jvm.util.KotlinJars
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrStdlib

private val LOG = logger<QodanaBtaInspectionKtsCompiler>()

private const val USE_BTA_REGISTRY_KEY: String = "qd.inspection.kts.use.bta"

private const val BTA_TEMP_DIR_PREFIX: String = "qd-inspectionKts-bta-"
private const val SRC_DIR_NAME: String = "src"
private const val OUT_DIR_NAME: String = "out"
private const val CLASS_FILE_EXTENSION: String = "class"
private const val COMPILATION_SUCCESS_RESULT: String = "COMPILATION_SUCCESS"

private const val KOTLIN_SCRIPTING_PLUGIN_ID: String = "kotlin.scripting"
private const val SCRIPT_TEMPLATES_OPTION_KEY: String = "script-templates"

private val KOTLIN_SCRIPT_RUNTIME_ARTIFACTS: Array<String> = arrayOf("kotlin-stdlib", "kotlin-reflect", "kotlin-script-runtime")

/**
 * BTA-backed implementation of [InspectionKtsBtaCompiler]. Compiles a `.inspection.kts` file via the
 * Kotlin Build Tools API instead of the legacy `IdeScriptEngine` (JSR223 REPL) path.
 *
 * Mirrors the legacy compile path's post-compile pipeline (post-processor lookup + casts +
 * descriptor wrapping) so both paths produce equivalent [InspectionKtsFileStatus] values.
 * Selection between this path and the legacy path happens in `KtsInspectionsManager` (core module),
 * gated by the [USE_BTA_REGISTRY_KEY] registry flag.
 */
class QodanaBtaInspectionKtsCompiler : InspectionKtsBtaCompiler {
  override fun isEnabled(): Boolean = Registry.`is`(USE_BTA_REGISTRY_KEY, true)

  override suspend fun compile(
    project: Project,
    file: Path,
    errorLogger: InspectionKtsErrorLogManager.Logger,
    classLoader: ClassLoader,
    scope: CoroutineScope,
  ): InspectionKtsFileStatus = compileInspectionKtsFileViaBta(project, file, errorLogger, classLoader, scope)
}

private data class BtaInspectionKtsResultData(
  val inspections: Set<DynamicInspectionDescriptor>,
  val userData: Set<CompiledInspectionsKtsData>,
  val keepAlive: ClassLoader?,
)

@OptIn(ExperimentalBuildToolsApi::class, ExperimentalCompilerArgument::class)
private suspend fun compileInspectionKtsFileViaBta(
  project: Project,
  file: Path,
  errorLogger: InspectionKtsErrorLogManager.Logger,
  classLoader: ClassLoader,
  scope: CoroutineScope,
): InspectionKtsFileStatus {
  waitWhenProjectTrusted(project)

  val exceptionDuringAnalysisFlow = MutableStateFlow<Exception?>(null)

  val scriptText = getDocumentByNioPath(file)?.text ?: runInterruptible(IO) { file.readText() }
  val scriptContentHash = scriptText.hashCode()
  val defaultImports = InspectionKtsDefaultImportProvider.imports()
  val scriptTextWithDefaultImports = defaultImports.joinToString("") { "import $it\n" } + scriptText
  val scriptCompileClasspath = buildInspectionScriptCompileClasspath(classLoader)

  val dynamicInspectionData: BtaInspectionKtsResultData = try {
    val rawResult = coroutineScope {
      val resultDeferred = async(IO) {
        withBackgroundProgress(project, InspectionKtsBundle.message("compiling.kts.file", file.name), true) {
          val compileOutput = coroutineToIndicator {
            runBtaCompile(file, scriptTextWithDefaultImports, defaultImports.size, scriptCompileClasspath)
          }
          BtaCompiledScriptExecutor.execute(file, compileOutput, scriptCompileClasspath, classLoader)
        }
      }
      try {
        resultDeferred.await()
      } catch (ce: CancellationException) {
        if (this.coroutineContext.job.isCancelled) throw ce
        return@coroutineScope InspectionKtsFileStatus.Cancelled(file)
      }
    }
    if (rawResult is InspectionKtsFileStatus.Cancelled) return rawResult
    yield()
    val outcome = rawResult as BtaCompiled

    val processor = CompiledInspectionKtsPostProcessorFactory.getProcessor(outcome.result)
    if (processor == null) {
      val inspectionsKts: Sequence<InspectionKts> = extractInspectionKtsResults(outcome.result).asSequence()
      val dynamicInspectionsDescriptor = inspectionsKts.map {
        val inspectionTool = it.__asTool__(exceptionReporter = { exception ->
          scope.launch {
            errorLogger.logException(exception)
            exceptionDuringAnalysisFlow.value = exception
          }
        })
        DynamicInspectionDescriptor.fromTool(inspectionTool, FLEX_INSPECT_PROVIDER_NAME)
      }
      val set = dynamicInspectionsDescriptor.toSet()
      BtaInspectionKtsResultData(set, emptySet(), outcome.keepAlive)
    }
    else {
      val inspectionsKtsData = processor.process(project, file)
                               ?: error("Failed to process inspection data with ${processor.javaClass.name} processor")
      BtaInspectionKtsResultData(emptySet(), setOf(inspectionsKtsData), outcome.keepAlive)
    }
  }
  catch (ce: CancellationException) {
    throw ce
  }
  catch (e: Exception) {
    LOG.warn("Failed to compile $file via BTA", e)
    errorLogger.logException(e)
    return InspectionKtsFileStatus.Error(e, errorLogger, scriptContentHash, isOutdated = false, file)
  }

  // BTA path doesn't hold an IdeScriptEngine reference, so the compiling URLClassLoader (and its
  // on-disk output) would otherwise become collectible the moment this call returns — breaking
  // already-compiled inspections from other files that load classes lazily. Retain it via
  // `keepAlive` for as long as these inspections stay registered; the temp output dir is freed
  // only once that classloader is unreachable (see the Cleaner in `BtaCompiledScriptExecutor.execute`).
  val compiled = CompiledInspectionKtsInspections(dynamicInspectionData.inspections, dynamicInspectionData.userData, dynamicInspectionData.keepAlive)
  return InspectionKtsFileStatus.Compiled(compiled, exceptionDuringAnalysisFlow, errorLogger, scriptContentHash, isOutdated = false, file)
}

@OptIn(ExperimentalBuildToolsApi::class, ExperimentalCompilerArgument::class, ExperimentalPathApi::class)
private fun runBtaCompile(
  scriptFile: Path,
  scriptTextWithDefaultImports: String,
  prependedLineCount: Int,
  scriptCompileClasspath: List<Path>,
): BtaCompileOutput {
  val workDir = Files.createTempDirectory(BTA_TEMP_DIR_PREFIX)
  val srcDir = Files.createDirectories(workDir.resolve(SRC_DIR_NAME))
  val outDir = Files.createDirectories(workDir.resolve(OUT_DIR_NAME))
  // Preserve the user's filename so the generated class name reflects it.
  val srcCopy = srcDir.resolve(scriptFile.name).also { it.writeText(scriptTextWithDefaultImports) }

  try {
    val session = QodanaBtaSessionService.getInstance()
    val toolchains = session.toolchains()

    val operation = toolchains.jvm.jvmCompilationOperation(
      sources = listOf(srcCopy),
      destinationDirectory = outDir,
    ) {
      compilerArguments[CommonCompilerArguments.X_ALLOW_ANY_SCRIPTS_IN_SOURCE_ROOTS] = true
      compilerArguments[JvmCompilerArguments.CLASSPATH] = scriptCompileClasspath
      compilerArguments[JvmCompilerArguments.X_DEFAULT_SCRIPT_EXTENSION] = INSPECTIONS_KTS_EXTENSION
      compilerArguments[JvmCompilerArguments.X_DISABLE_STANDARD_SCRIPT] = true
      set(JvmCompilationOperation.KOTLINSCRIPT_EXTENSIONS, arrayOf(INSPECTIONS_KTS_EXTENSION))
      compilerArguments[CommonCompilerArguments.COMPILER_PLUGINS] = listOf(scriptingPluginWithTemplate())
      set(
        BaseCompilationOperation.COMPILER_MESSAGE_RENDERER,
        InspectionKtsCompilerMessageRenderer(srcCopy, scriptFile, prependedLineCount),
      )
    }

    val errors = mutableListOf<String>()
    val logger = diagnosticsLogger(errors)
    // Reuse the shared build session (retains jar caches across compilations)
    val compileResult = session.execute(operation, logger)
    if (compileResult.toString() != COMPILATION_SUCCESS_RESULT) {
      error("BTA compilation failed: $compileResult\n${errors.joinToString("\n")}")
    }

    val classFiles = outDir.walk().filter { it.extension == CLASS_FILE_EXTENSION }.toList()
    if (classFiles.isEmpty()) error("BTA compile produced no class files")

    return BtaCompileOutput(workDir, outDir, classFiles)
  }
  catch (t: Throwable) {
    LOG.warn("Preserving BTA temp directory after failure: $workDir")
    throw t
  }
}

// We only need to hand the `script-templates` option to kotlin.scripting (already default-loaded by
// kotlinc); the option is delivered through `pluginOptions` regardless of classpath. But
// COMPILER_PLUGINS rejects an empty classpath, so we anchor it at our own inspectionKts jar — the one
// that actually holds the referenced template class.
@OptIn(ExperimentalBuildToolsApi::class)
private fun scriptingPluginWithTemplate(): CompilerPlugin =
  CompilerPlugin(
    pluginId = KOTLIN_SCRIPTING_PLUGIN_ID,
    classpath = listOf(locateJarOf(InspectionKtsScriptTemplate::class.java)),
    rawArguments = listOf(CompilerPluginOption(SCRIPT_TEMPLATES_OPTION_KEY, InspectionKtsScriptTemplate::class.qualifiedName!!)),
    orderingRequirements = emptySet(),
  )


private fun diagnosticsLogger(errors: MutableList<String>): KotlinLogger = object : KotlinLogger {
  override val isDebugEnabled: Boolean get() = LOG.isDebugEnabled
  override fun error(msg: String, throwable: Throwable?) { errors += msg; LOG.warn(msg, throwable) }
  override fun warn(msg: String, throwable: Throwable?) { LOG.warn(msg, throwable) }
  override fun info(msg: String) { LOG.debug(msg) }
  override fun debug(msg: String) { LOG.debug(msg) }
  override fun lifecycle(msg: String) { LOG.debug(msg) }
}

/**
 * Renders BTA compiler diagnostics against the author's real `.inspection.kts`: for locations in the
 * compiled temp copy ([compiledSource]) it substitutes [originalFile] and subtracts the
 * [prependedLineCount] default-import lines we inject, so line/column match what the editor shows.
 * Other locations (e.g. classpath sources) are rendered as-is.
 */
private class InspectionKtsCompilerMessageRenderer(
  private val compiledSource: Path,
  private val originalFile: Path,
  private val prependedLineCount: Int,
) : CompilerMessageRenderer {
  override fun render(
    severity: CompilerMessageRenderer.Severity,
    message: String,
    location: CompilerMessageRenderer.SourceLocation?,
  ): String {
    val severityLabel = when (severity) {
      CompilerMessageRenderer.Severity.ERROR -> "error"
      CompilerMessageRenderer.Severity.WARNING -> "warning"
      CompilerMessageRenderer.Severity.INFO -> "info"
      CompilerMessageRenderer.Severity.DEBUG -> "debug"
    }
    if (location == null) return "$severityLabel: $message"
    val isCompiledSource = runCatching {
      Path.of(location.path).toAbsolutePath().normalize() == compiledSource.toAbsolutePath().normalize()
    }.getOrDefault(false)
    val path = if (isCompiledSource) originalFile else Path.of(location.path)
    val line = if (isCompiledSource) (location.line - prependedLineCount).coerceAtLeast(1) else location.line
    return "$path:$line:${location.column}: $severityLabel: $message"
  }
}

private fun locateJarOf(cls: Class<*>): Path =
  PathManager.getJarForClass(cls) ?: error("Cannot locate JAR for ${cls.name}")

private fun buildInspectionScriptCompileClasspath(classLoader: ClassLoader): List<Path> {
  val pluginClasspath = (classLoader as? InspectionKtsClassLoader)
    ?.getUrls()
    ?.mapNotNull { url -> runCatching { Path.of(url.toURI()) }.getOrNull() }
    .orEmpty()
  val anchoredClasspath = listOfNotNull(
    runCatching { locateJarOf(InspectionKtsScriptTemplate::class.java) }.getOrNull(),
  )
  // Provide kotlin-stdlib / kotlin-reflect / kotlin-script-runtime explicitly so the BTA
  // compiler doesn't have to find them through `-kotlin-home`. Mirrors the IDE's JSR223
  // script engine factory.
  val kotlinRuntimeClasspath = (
    scriptCompilationClasspathFromContextOrStdlib(
      *KOTLIN_SCRIPT_RUNTIME_ARTIFACTS,
      classLoader = classLoader,
    ) + KotlinJars.kotlinScriptStandardJars
    ).map { it.toPath() }
  return (pluginClasspath + anchoredClasspath + kotlinRuntimeClasspath)
    .distinct()
    .filter { Files.exists(it) }
}

private fun extractInspectionKtsResults(result: Any): List<InspectionKts> {
  return when (result) {
    is InspectionKts -> listOf(result)
    is Collection<*> -> result.mapIndexed { idx, item ->
      item as? InspectionKts
      ?: error("Got $item on position $idx from inspection script, expected ${InspectionKts::class.java.canonicalName}")
    }
    else -> error("Got $result from inspection script, expected ${Collection::class.java.canonicalName} of ${InspectionKts::class.java.canonicalName}")
  }
}
