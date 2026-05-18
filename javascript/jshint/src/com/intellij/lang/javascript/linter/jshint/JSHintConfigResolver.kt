package com.intellij.lang.javascript.linter.jshint

import com.google.gson.Gson
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation
import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Utility for resolving JSHint configuration files and options.
 */
internal class JSHintConfigResolver(
  private val state: JSHintState,
  private val project: Project,
  private val fileToLint: VirtualFile,
) {

  /**
   * Resolves JSHint configuration based on the state and context.
   *
   * @return [Result.Success] with resolved configuration or [Result.Error] with error details
   */
  fun resolve(): Result {
    return if (state.isConfigFileUsed) {
      resolveFromConfigFile()
    }
    else {
      resolveFromTempConfig()
    }
  }

  private fun resolveFromConfigFile(): Result {
    val configLookupResult = if (state.isCustomConfigFileUsed) {
      JSHintConfigFileUtil.loadConfigByPath(state.customConfigFilePath)
    }
    else {
      JSHintConfigFileUtil.lookupConfig(project, fileToLint)
    }

    if (configLookupResult == null) {
      return Result.Error(
        JSLinterFileLevelAnnotation(JSHintBundle.message("jshint.inspection.message.config.not.found"))
      )
    }

    val optionsState = configLookupResult.optionsState
    if (optionsState == null) {
      val message = configLookupResult.errorMessage ?: JSHintBundle.message("jshint.inspection.message.malformed.config")
      return Result.Error(
        JSLinterFileLevelAnnotation(message),
        configLookupResult.configFile
      )
    }

    return Result.Success(JSHintConfigResolution(
      optionsState,
      configLookupResult.configFile.toNioPath(),
      configLookupResult.configFile
    ))
  }

  private fun resolveFromTempConfig(): Result {
    val tempConfigFile = try {
      getOrCreateTempConfigFile(state.optionsState)
    }
    catch (e: IOException) {
      logger<JSHintConfigResolver>().warn("Failed to create temp config file", e)
      return Result.Error(
        JSLinterFileLevelAnnotation(JSHintBundle.message("jshint.inspection.message.cannot.create.temp.config"))
      )
    }
    return Result.Success(JSHintConfigResolution(state.optionsState, tempConfigFile, null))
  }

  sealed class Result {
    data class Success(val resolution: JSHintConfigResolution) : Result()

    data class Error(
      val annotation: JSLinterFileLevelAnnotation,
      val configFile: VirtualFile? = null,
    ) : Result()
  }

  private data class GeneratedConfigFileInfo(
    val path: Path,
    val optionsMap: Map<String, Any>,
  )

  companion object {

    private val lastOptionsConfigLock = Any()

    @Volatile
    private var lastGeneratedConfigInfo: GeneratedConfigFileInfo? = null

    /**
     * Gets or creates a temporary config file for the given options state.
     * Uses caching to avoid recreating files when options haven't changed.
     */
    fun getOrCreateTempConfigFile(optionsState: JSHintOptionsState): Path {
      val optionsMap = toSerializableOptions(optionsState.valueByOptionMap)

      // Check if we can reuse cached config (without lock for fast path)
      // Single atomic read ensures path and optionsMap are consistent
      val cache = lastGeneratedConfigInfo
      if (cache != null && cache.path.exists() && cache.optionsMap == optionsMap) {
        return cache.path
      }

      // Need to create new config - acquire lock
      synchronized(lastOptionsConfigLock) {
        // Double-check after acquiring lock
        val cached = lastGeneratedConfigInfo
        if (cached != null && cached.path.exists() && cached.optionsMap == optionsMap) {
          return cached.path
        }
        return createTempConfigFile(optionsMap).also {
          lastGeneratedConfigInfo = GeneratedConfigFileInfo(it, optionsMap)
        }
      }
    }

    private fun createTempConfigFile(optionsMap: Map<String, Any>): Path {
      return Files.createTempFile("intellij-jshint-config-", ".json").apply {
        @Suppress("SSBasedInspection")
        toFile().deleteOnExit()

        val gson = Gson()
        val json = gson.toJson(optionsMap)
        writeText(json, StandardCharsets.UTF_8)
      }
    }

    private fun toSerializableOptions(optionsMap: Map<String, Any>): Map<String, Any> {
      // Convert PREDEF option from string format to JSHint globals format
      // Input: "foo:true, bar" or "foo, bar"
      // Output: {"foo": true, "bar": false} (true = writable, false = read-only)
      val predefValue = optionsMap[JSHintOption.PREDEF.key]
      if (predefValue is String && predefValue.isNotBlank()) {
        val globals = parsePredefString(predefValue)
        if (globals.isNotEmpty()) {
          val copy = optionsMap.toMutableMap()
          copy[JSHintOption.PREDEF.key] = globals
          return copy
        }
      }
      return optionsMap
    }

    private fun parsePredefString(predef: String): Map<String, Boolean> {
      // Parse format like "foo:true, bar, baz:false"
      // Returns map like {"foo": true, "bar": false, "baz": false}
      val result = mutableMapOf<String, Boolean>()

      predef.split(',').forEach { item ->
        val trimmed = item.trim()
        if (trimmed.isNotEmpty()) {
          val parts = trimmed.split(':')
          val name = parts[0].trim()
          val writable = if (parts.size > 1) {
            parts[1].trim().equals("true", ignoreCase = true)
          }
          else {
            false // Default to read-only
          }
          if (name.isNotEmpty()) {
            result[name] = writable
          }
        }
      }

      return result
    }
  }
}

data class JSHintConfigResolution(
  val optionsState: JSHintOptionsState,
  val configFile: Path,
  val configVirtualFile: VirtualFile?,
)
