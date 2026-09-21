package org.jetbrains.qodana.inspectionKts.bta

import com.intellij.openapi.diagnostic.logger
import org.jetbrains.qodana.inspectionKts.scriptTemplate.INSPECTION_KTS_RESULT_FIELD_NAME
import org.jetbrains.qodana.inspectionKts.scriptTemplate.InspectionKtsScriptTemplate
import java.lang.ref.Cleaner
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

private val LOG = logger<BtaCompiledScriptExecutor>()

private val BTA_OUTPUT_CLEANER: Cleaner = Cleaner.create()

internal class BtaCompileOutput(
  val workDir: Path,
  val outDir: Path,
  val classFiles: List<Path>,
)

internal class BtaCompiled(val result: Any, val keepAlive: ClassLoader)

/**
 * Turns a successful BTA [compile output][BtaCompileOutput] into a usable script result: builds a
 * [URLClassLoader] over the output, locates the generated script class, instantiates it, and recovers
 * its last-expression value.
 */
internal object BtaCompiledScriptExecutor {
  /**
   * @param scriptFile original `.inspection.kts` path, used only for diagnostics.
   * @param scriptClasspath the same classpath the script was compiled against, exposed to it at runtime.
   * @param parentClassLoader parent of the [URLClassLoader] that defines the generated classes.
   */
  @OptIn(ExperimentalPathApi::class)
  fun execute(
    scriptFile: Path,
    compileOutput: BtaCompileOutput,
    scriptClasspath: List<Path>,
    parentClassLoader: ClassLoader,
  ): BtaCompiled {
    try {
      val classLoader = createScriptClassLoader(compileOutput.outDir, scriptClasspath, parentClassLoader)
      val scriptClass = locateScriptClass(scriptFile, compileOutput.outDir, compileOutput.classFiles, classLoader)
      val instance = instantiateScript(scriptClass)
      val result = recoverLastExpressionValue(scriptClass, instance) ?: instance

      // The script's classes live in `outDir` and load lazily through `classLoader` (e.g. helper
      // classes a checker only touches while running), so the output must outlive this call. Tie the
      // dir's lifetime to `classLoader`: delete it only once `classLoader` is unreachable. The cleanup
      // must NOT capture `classLoader` (it only references `workDir`), or the classloader would never
      // be collected.
      val workDir = compileOutput.workDir
      BTA_OUTPUT_CLEANER.register(classLoader) { runCatching { workDir.deleteRecursively() } }
      return BtaCompiled(result, classLoader)
    }
    catch (t: Throwable) {
      LOG.warn("Preserving BTA temp directory after failure: ${compileOutput.workDir}")
      throw t
    }
  }

  private fun createScriptClassLoader(
    outDir: Path,
    scriptClasspath: List<Path>,
    parentClassLoader: ClassLoader,
  ): URLClassLoader {
    val outDirUrl = outDir.toUri().toURL()
    val classpathUrls = scriptClasspath.map { it.toUri().toURL() }
    return URLClassLoader((listOf(outDirUrl) + classpathUrls).toTypedArray(), parentClassLoader)
  }

  private fun locateScriptClass(
    scriptFile: Path,
    outDir: Path,
    classFiles: List<Path>,
    classLoader: ClassLoader,
  ): Class<*> {
    val generatedClassNames = classFiles
      .map { classFile -> outDir.relativize(classFile).joinToString(".") { it.toString() }.removeSuffix(".class") }
    val loadedClasses = generatedClassNames.mapNotNull { className ->
      runCatching { classLoader.loadClass(className) }.getOrNull()
    }
    // The compiled script is the single non-abstract subclass of the template; helper / lambda
    // classes generated alongside it don't extend `InspectionKtsScriptTemplate`.
    return loadedClasses.firstOrNull {
      InspectionKtsScriptTemplate::class.java.isAssignableFrom(it) && !Modifier.isAbstract(it.modifiers)
    } ?: error("Could not locate generated script class for $scriptFile in $outDir. Generated classes: ${generatedClassNames.joinToString()}")
  }

  private fun instantiateScript(scriptClass: Class<*>): Any {
    val ctor = scriptClass.declaredConstructors.minByOrNull { it.parameterCount }
               ?: error("Script class ${scriptClass.name} has no constructors")
    ctor.isAccessible = true
    return ctor.newInstance(*buildScriptConstructorArguments(ctor))
  }

  /**
   * Recovers the script's last-expression value from the result field the compiler generates for the
   * template's `resultField` ([INSPECTION_KTS_RESULT_FIELD_NAME]). Returns `null` when the field is
   * absent — i.e. the script didn't end in an expression — and the caller falls back to the instance
   * itself, which then fails downstream with a clear "expected … of InspectionKts" error.
   */
  private fun recoverLastExpressionValue(scriptClass: Class<*>, instance: Any): Any? {
    val resultField = runCatching { scriptClass.getDeclaredField(INSPECTION_KTS_RESULT_FIELD_NAME) }.getOrNull()
                      ?: return null
    resultField.isAccessible = true
    return runCatching { resultField.get(instance) }.getOrNull()
  }

  private fun buildScriptConstructorArguments(ctor: Constructor<*>): Array<Any?> {
    // The generated `.inspection.kts` script class extends `InspectionKtsScriptTemplate`, whose
    // constructor takes at most the script `args: Array<String>`. Supply an empty array for that
    // parameter; any other (unexpected) parameter gets null.
    return Array(ctor.parameterCount) { index ->
      val type = ctor.parameterTypes[index]
      if (!type.isPrimitive && type.isArray) java.lang.reflect.Array.newInstance(type.componentType, 0) else null
    }
  }
}
