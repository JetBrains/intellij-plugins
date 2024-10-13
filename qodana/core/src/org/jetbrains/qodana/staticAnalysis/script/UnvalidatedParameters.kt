package org.jetbrains.qodana.staticAnalysis.script

import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import kotlin.reflect.KClass
import kotlin.reflect.cast

/** The parameters of a [QodanaScript], for validating required and unknown parameters. */
class UnvalidatedParameters(private val scriptName: String, parameters: Map<String, Any>) {
  private fun fail(message: String): Nothing = throw QodanaException("Script '$scriptName' $message")

  private val remaining = parameters.toMutableMap()

  fun <T : Any> require(name: String, clazz: KClass<T>): T {
    val value = remaining.remove(name)
    return when {
      value == null -> fail("requires parameter '$name'")
      !clazz.isInstance(value) -> fail("expects ${clazz.java.canonicalName} but found ${value.javaClass.canonicalName}")
      else -> clazz.cast(value)
    }
  }

  fun <T : Any> optional(name: String, clazz: KClass<T>): T? {
    val value = remaining.remove(name)
    return when {
      value == null -> null
      !clazz.isInstance(value) -> fail("expects ${clazz.java.canonicalName} but found ${value.javaClass.canonicalName}")
      else -> clazz.cast(value)
    }
  }

  fun done() {
    if (remaining.isEmpty()) return

    val parameters = if (remaining.size == 1) "parameter" else "parameters"
    val unknown = remaining.keys.joinToString()
    throw QodanaException("Script '$scriptName' cannot handle $parameters '$unknown'")
  }
}

inline fun <reified T : Any> UnvalidatedParameters.require(name: String) = require(name, T::class)
inline fun <reified T : Any> UnvalidatedParameters.optional(name: String) = optional(name, T::class)