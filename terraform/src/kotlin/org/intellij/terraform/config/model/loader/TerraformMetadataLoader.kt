/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.ensureHavePrefix
import org.intellij.terraform.config.model.string
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TerraformMetadataLoader(external: Map<String, TypeModelProvider.Additional>) {
  private val pool = ReusePool()
  private val model = LoadingModel(external)
  private val context: LoadContext = LoadContext(pool, model)

  private val loaders: List<VersionedMetadataLoader> = listOf(
    TerraformProvidersSchema(),

    ProviderLoaderV2(),
    ProvisionerLoaderV2(),
    BackendLoaderV2(),
    FunctionsLoaderV2(),

    ProviderLoaderV1(),
    ProvisionerLoaderV1(),
    BackendLoaderV1(),
    FunctionsLoaderV1()
  )

  fun load(): TypeModel? {
    val application = ApplicationManager.getApplication()
    try {
      loadExternal(application)
      loadBundled(application)

      return TypeModel(
        model.resources,
        model.dataSources,
        model.providers,
        model.provisioners,
        model.backends,
        model.functions
      )
    } catch(e: Exception) {
      logErrorAndFailInInternalMode(application, "Failed to load Terraform Model", e)
      return null
    }
  }

  private fun loadBundled(application: Application) {
    val resources: Collection<String> = getAllResourcesToLoad(ModelResourcesPrefix)

    for (it in resources) {
      val file = it.ensureHavePrefix("/")
      val stream = getResource(file)
      if (stream == null) {
        LOG.warn("Resource '$file' was not found")
        continue
      }

      loadOne(application, file, stream)
    }
  }

  private fun loadExternal(application: Application) {
    val schemas = getSharedSchemas()
    for (file in schemas) {
      val stream: FileInputStream
      try {
        stream = file.inputStream()
      } catch (e: Exception) {
        logErrorAndFailInInternalMode(application, "Cannot open stream for file '${file.absolutePath}'", e)
        continue
      }
      loadOne(application, file.absolutePath, stream)
    }
  }

  private fun loadOne(application: Application, file: String, stream: InputStream) {
    val json: ObjectNode?
    try {
      json = stream.use {
        ObjectMapper().readTree(it) as ObjectNode?
      }
      if (json == null) {
        logErrorAndFailInInternalMode(application, "In file '$file' no JSON found")
        return
      }
    } catch(e: Exception) {
      logErrorAndFailInInternalMode(application, "Failed to load json data from file '$file'", e)
      return
    }
    try {
      parseFile(json, file)
    } catch(e: Throwable) {
      logErrorAndFailInInternalMode(application, "Failed to parse file '$file'", e)
    }
    return
  }

  private fun logErrorAndFailInInternalMode(application: Application, msg: String, e: Throwable? = null) {
    if (e is ProcessCanceledException) throw e

    val msg2 = if (e == null) msg else "$msg: ${e.message}"
    if (e == null) LOG.error(msg2) else LOG.error(msg2, e)
    if (application.isInternal) {
      throw AssertionError(msg2, e)
    }
  }

  private fun getSharedSchemas(): List<File> {
    val terraform_d: File = getGlobalTerraformDir()
        ?: return emptyList()

    val result = ArrayList<File>()

    val schemas = File(terraform_d, "schemas")
    if (schemas.exists() && schemas.isDirectory) {
      FileUtil.processFilesRecursively(schemas) {
        if (it.isFile && it.name.endsWith(".json", ignoreCase = true)) {
          result.add(it)
        }
        return@processFilesRecursively true
      }
    }

    val metadataRepo = File(terraform_d, "metadata-repo/terraform/model")
    if (metadataRepo.exists() && metadataRepo.isDirectory) {
      FileUtil.processFilesRecursively(metadataRepo) {
        if (it.isFile && it.name.endsWith(".json", ignoreCase = true)) {
          result.add(it)
        }
        return@processFilesRecursively true
      }
    }

    return result
  }

  companion object {
    internal val LOG by lazy { Logger.getInstance(TerraformMetadataLoader::class.java) }
    const val ModelResourcesPrefix = "/terraform/model"

    fun getResource(path: String): InputStream? {
      return TypeModelProvider::class.java.getResourceAsStream(path)
    }

    fun getModelExternalInformation(path: String): Any? {
      return getResourceJson("/terraform/model-external/$path")
    }

    @Throws(RuntimeException::class, NullPointerException::class)
    fun getResourceJson(path: String): Any? {
      val stream = getResource(path)
          ?: return null
      stream.use {
        return ObjectMapper().readTree(it)
      }
    }

    internal fun getAllResourcesToLoad(prefix: String): Collection<String> {
      val resources = ArrayList<String>()
      loadList("$prefix/providers.list").map { "$prefix/providers/$it.json" }.toCollection(resources)
      loadList("$prefix/provisioners.list").map { "$prefix/provisioners/$it.json" }.toCollection(resources)
      loadList("$prefix/backends.list").map { "$prefix/backends/$it.json" }.toCollection(resources)
      resources.add("$prefix/functions.json")
      return resources
    }

    private fun loadList(name: String): Set<String> {
      try {
        val stream = getResource(name)
        if (stream == null) {
          val message = "Cannot read list '$name': resource not found"
          LOG.warn(message)
          val application = ApplicationManager.getApplication()
          if (application.isUnitTestMode || application.isInternal) {
            assert(false) { message }
          }
          return emptySet()
        }
        val lines = stream.bufferedReader(Charsets.UTF_8).readLines().map { it.trim() }.filter { !it.isEmpty() }
        return LinkedHashSet<String>(lines)
      } catch(e: Exception) {
        LOG.warn("Cannot read 'ignored-references.list': ${e.message}")
        return emptySet()
      }
    }

    fun getGlobalTerraformDir(): File? {
      val terraform_d = if (SystemInfo.isWindows) {
        System.getenv("APPDATA")?.let { File(it, "terraform.d") }
      } else {
        val userHome = SystemProperties.getUserHome()
        File(userHome, ".terraform.d")
      }
      if (terraform_d == null || !terraform_d.exists() || !terraform_d.isDirectory) return null
      return terraform_d
    }

    fun loadExternalResource(name: String): InputStream? {
      val stream: InputStream? = getGlobalTerraformDir()?.let { tf ->
        listOf(
          File(tf, "schemas/$name"),
          File(tf, "metadata-repo/terraform/model-external/$name")
        ).firstOrNull { it.exists() && it.isFile }?.let {
          try {
            it.inputStream()
          } catch (e: Exception) {
            LOG.warn("Cannot open stream for file '${it.absolutePath}'", e)
            null
          }
        }
      } ?: getResource("/terraform/model-external/$name")
      return stream
    }
  }

  private fun parseFile(json: ObjectNode, file: String) {
    val type: String
    val version: String
    if (json.has("format_version")) {
      type = "terraform-providers-schema-json"
      version = json.string("format_version")!!
    }
    else {
      type = json.string("type") ?: "unknown"
      version = json.string(".schema_version") ?: "1"
    }
    val loader = loaders.find {
      it.isSupportedType(type) && it.isSupportedVersion(version)
    }
    if (loader == null) {
      val message = "Cannot find loader for model file content '$file', type: '$type', version: '$version'"
      val application = ApplicationManager.getApplication()
      if (application.isUnitTestMode || application.isInternal) {
        LOG.error(message)
        assert(false) { message }
      }
      LOG.warn(message)
      return
    }
    loader.load(context, json, file)
  }
}