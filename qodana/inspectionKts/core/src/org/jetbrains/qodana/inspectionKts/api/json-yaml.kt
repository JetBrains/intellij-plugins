@file:Suppress("unused")

package org.jetbrains.qodana.inspectionKts.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.IOException

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS INTERFACE IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
sealed interface JsonParseResult<out T> {
  class Success<T>(val value: T) : JsonParseResult<T>

  class Failed(val exception: IOException) : JsonParseResult<Nothing>
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun <T> parseJson(text: String, clazz: Class<T>, failOnUnknownProperties: Boolean): JsonParseResult<T> {
  return try {
    JsonParseResult.Success(parseImpl(text, clazz, failOnUnknownProperties, jacksonObjectMapper()))
  } catch (jacksonException: IOException) {
    JsonParseResult.Failed(jacksonException)
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS INTERFACE IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
sealed interface YamlParseResult<out T> {
  class Success<T>(val value: T) : YamlParseResult<T>

  class Failed(val exception: IOException) : YamlParseResult<Nothing>
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun <T> parseYaml(text: String, clazz: Class<T>, failOnUnknownProperties: Boolean): YamlParseResult<T> {
  return try {
    YamlParseResult.Success(parseImpl(text, clazz, failOnUnknownProperties, yamlObjectMapper()))
  } catch (jacksonException: IOException) {
    YamlParseResult.Failed(jacksonException)
  }
}

private fun <T> parseImpl(
  text: String,
  clazz: Class<T>,
  failOnUnknownProperties: Boolean,
  mapper: ObjectMapper,
): T {
  val configuredMapper = if (failOnUnknownProperties) {
    mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  } else {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
  }
  return configuredMapper.readValue(text, clazz)
}

private fun yamlObjectMapper(): YAMLMapper {
  val builder = YAMLMapper.builder()
  builder.addModule(kotlinModule())
  return builder.build()
}