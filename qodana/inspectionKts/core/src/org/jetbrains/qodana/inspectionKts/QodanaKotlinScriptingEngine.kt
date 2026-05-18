package org.jetbrains.qodana.inspectionKts

import com.intellij.ide.script.IdeScriptEngine
import com.intellij.ide.script.IdeScriptException
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.ClassLoaderUtil
import com.intellij.util.ExceptionUtilRt
import java.io.Reader
import java.io.Writer
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

private const val KOTLIN_IDE_JSR223_SCRIPT_ENGINE_NAME = "kotlin-ide-jsr223"

private val LOG = logger<QodanaIdeScriptEngine>()

fun getKotlinScriptingEngine(classLoader: ClassLoader?): IdeScriptEngine? {
  val engineClassLoader = classLoader ?: InspectionKtsClassLoader()
  val engine = runCatching {
    ClassLoaderUtil.computeWithClassLoader<ScriptEngine, RuntimeException>(engineClassLoader) {
      ScriptEngineManager(engineClassLoader).getEngineByName(KOTLIN_IDE_JSR223_SCRIPT_ENGINE_NAME)
    }
  }.getOrElse {
    LOG.warn("Failed to create Kotlin scripting engine", it)
    return null
  }
  if (engine == null) {
    LOG.warn("Failed to create Kotlin scripting engine: $KOTLIN_IDE_JSR223_SCRIPT_ENGINE_NAME is not found")
    return null
  }

  return QodanaIdeScriptEngine(engine, engineClassLoader)
}

private class QodanaIdeScriptEngine(
  private val engine: ScriptEngine,
  private val classLoader: ClassLoader,
) : IdeScriptEngine {
  override fun getBinding(name: String): Any? = engine.get(name)

  override fun setBinding(name: String, value: Any?) {
    engine.put(name, value)
  }

  override fun getStdOut(): Writer = engine.context.writer

  override fun setStdOut(writer: Writer) {
    engine.context.writer = writer
  }

  override fun getStdErr(): Writer = engine.context.errorWriter

  override fun setStdErr(writer: Writer) {
    engine.context.errorWriter = writer
  }

  override fun getStdIn(): Reader = engine.context.reader

  override fun setStdIn(reader: Reader) {
    engine.context.reader = reader
  }

  override fun getLanguage(): String = engine.factory.languageName

  override fun getFileExtensions(): List<String> = engine.factory.extensions

  override fun eval(script: String): Any? {
    return ClassLoaderUtil.computeWithClassLoader<Any?, IdeScriptException>(classLoader) {
      try {
        engine.eval(script)
      }
      catch (ex: Throwable) {
        throw IdeScriptException(ExceptionUtilRt.unwrapException(ex, ScriptException::class.java))
      }
    }
  }
}
