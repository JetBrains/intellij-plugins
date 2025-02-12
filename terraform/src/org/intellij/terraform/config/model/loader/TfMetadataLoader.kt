// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.loader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import org.intellij.terraform.config.model.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TfMetadataLoader {
  private val pool = ReusePool()
  private val model = LoadingModel()
  private val context: LoadContext = LoadContext(pool, model)

  private val loaders: List<VersionedMetadataLoader> = listOf(
    TfProvidersSchema(),

    ProviderLoaderV2(),
    ProvisionerLoaderV2(),
    BackendLoaderV2(),
    FunctionsLoaderV2(),

    ProviderLoaderV1(),
    ProvisionerLoaderV1(),
    BackendLoaderV1(),
    FunctionsLoaderV1()
  )

  fun loadDefaults(): TypeModel? {
    try {
      model.external.putAll(loadExternalInformation())
      loadExternal()
      loadBundled()

      return buildModel()
    }
    catch (e: Exception) {
      logErrorAndFailInInternalMode("Failed to load Terraform Model", e)
      return null
    }
  }

  fun loadFrom(another: TypeModel) {
    val tmp = buildModel()
    model.resources.addAll(another.allResources().filter { tmp.getResourceType(it.type) == null })
    model.dataSources.addAll(another.allDatasources().filter { tmp.getDataSourceType(it.type) == null })
    model.providers.addAll(another.allProviders().filter { tmp.getProviderType(it.type) == null })
    model.provisioners.addAll(another.provisioners.filter { tmp.getProvisionerType(it.type) == null })
    model.backends.addAll(another.backends.filter { tmp.getBackendType(it.type) == null })
    model.functions.addAll(another.functions.filter { tmp.getFunction(it.name) == null })
    model.providerDefinedFunctions.addAll(another.providerDefinedFunctions.filter { tmp.getFunction(it.name) == null })
  }

  fun buildModel(): TypeModel {
    return TypeModel(
      model.resources,
      model.dataSources,
      model.providers,
      model.provisioners,
      model.backends,
      model.functions,
      model.providerDefinedFunctions
    )
  }

  private fun loadExternalInformation(): Map<String, LoadingModel.Additional> {
    val map = HashMap<String, LoadingModel.Additional>()

    val stream = loadExternalResource("external-data.json") ?: return map
    val json = stream.use {
      ObjectMapper().readTree(it) as ObjectNode?
    }

    if (json is ObjectNode) {
      for ((fqn, obj) in json.fields()) {
        if (obj !is ObjectNode) {
          LOG.warn("In external-data.json value for '$fqn' root key is not an object")
          continue
        }
        val hintV = obj["hint"]
        val hint: Hint? = when {
          hintV == null -> null
          hintV.isTextual -> ReferenceHint(hintV.textValue())
          hintV.isArray -> SimpleValueHint(*hintV.mapNotNull { it.textValue() }.toTypedArray())
          else -> null
        }
        val additional = LoadingModel.Additional(fqn, obj.string("description"), hint, obj.boolean("optional"),
                                                 obj.boolean("required"))
        map[fqn] = additional
      }
    }
    return map
  }


  private fun loadBundled() {
    val resources: Collection<String> = getAllResourcesToLoad(ModelResourcesPrefix)

    for (it in resources) {
      val file = it.ensureHavePrefix("/")
      val stream = getResource(file)
      if (stream == null) {
        LOG.warn("Resource '$file' was not found")
        continue
      }

      loadOne(file, stream)
    }
  }

  private fun loadExternal() {
    val schemas = getSharedSchemas()
    for (file in schemas) {
      val stream: FileInputStream
      try {
        stream = file.inputStream()
      }
      catch (e: Exception) {
        logErrorAndFailInInternalMode("Cannot open stream for file '${file.absolutePath}'", e)
        continue
      }
      loadOne(file.absolutePath, stream)
    }
  }

  fun loadOne(sourceName: String, stream: InputStream) {
    val json: ObjectNode?
    try {
      json = stream.use {
        ObjectMapper().readTree(it) as ObjectNode?
      }
      if (json == null) {
        logErrorAndFailInInternalMode("In file '$sourceName' no JSON found")
        return
      }
    }
    catch (e: Exception) {
      logErrorAndFailInInternalMode("Failed to load json data from file '$sourceName'", e)
      return
    }
    try {
      parseFile(json, sourceName)
    }
    catch (e: Throwable) {
      logErrorAndFailInInternalMode("Failed to parse file '$sourceName'", e)
    }
    return
  }

  private fun logErrorAndFailInInternalMode(msg: String, e: Throwable? = null) {
    if (e is ProcessCanceledException) throw e

    val msg2 = if (e == null) msg else "$msg: ${e.message}"
    if (e == null) LOG.error(msg2) else LOG.error(msg2, e)
    if (ApplicationManager.getApplication().isInternal) {
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
    internal val LOG: Logger by lazy { Logger.getInstance(TfMetadataLoader::class.java) }
    const val ModelResourcesPrefix: String = "/terraform/model"

    fun getResource(path: String): InputStream? {
      return TfMetadataLoader::class.java.getResourceAsStream(path)
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
      }
      catch (e: Exception) {
        LOG.warn("Cannot read 'ignored-references.list': ${e.message}")
        return emptySet()
      }
    }

    fun getGlobalTerraformDir(): File? {
      if (!AdvancedSettings.getBoolean("org.intellij.terraform.use.global.meta")) return null
      val terraform_d = if (SystemInfo.isWindows) {
        System.getenv("APPDATA")?.let { File(it, "terraform.d") }
      }
      else {
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
          }
          catch (e: Exception) {
            LOG.warn("Cannot open stream for file '${it.absolutePath}'", e)
            null
          }
        }
      } ?: getResource("/terraform/model-external/$name")
      return stream
    }
  }

  private fun parseFile(json: ObjectNode, fileName: String) {
    val type: String
    val version: String
    val schemasNode = json.obj("schemas") ?: json
    if (schemasNode.has("format_version"))  {
      type = "terraform-providers-schema-json"
      version = schemasNode.get("format_version").textValue()
    }
    else {
      type = schemasNode.string("type") ?: "unknown"
      version = schemasNode.string(".schema_version") ?: "1"
    }
    val loader = loaders.find {
      it.isSupportedType(type) && it.isSupportedVersion(version)
    }
    if (loader == null) {
      val message = "Cannot find loader for model file content '$fileName', type: '$type', version: '$version'"
      val application = ApplicationManager.getApplication()
      if (application.isUnitTestMode || application.isInternal) {
        LOG.error(message)
        assert(false) { message }
      }
      LOG.warn(message)
      return
    }
    loader.load(context, json, fileName)
  }

}