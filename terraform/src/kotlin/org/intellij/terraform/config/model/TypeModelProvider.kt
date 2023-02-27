// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.resettableLazy
import org.intellij.terraform.config.model.loader.TerraformMetadataLoader
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

class TypeModelProvider {
  private val _model_lazy = resettableLazy {
    // Run non-cancellable since it may take time, so it makes sense to finish loading even if caller was cancelled
    @Suppress("RedundantSamConstructor")
    ApplicationUtil.runWithCheckCanceled(Callable {
      TerraformMetadataLoader(external).load() ?: TypeModel()
    }, EmptyProgressIndicator())
  }
  private val _model: TypeModel by _model_lazy

  val external: Map<String, Additional> by lazy { loadExternalInformation() }
  val ignored_references: Set<String> by lazy { loadIgnoredReferences() }

  companion object {
    /**
     * Parameter 'project' is ignored for now since we have only one model.
     * When multiple different models would be supported,
     *   TypeModelProvider should become either per-project service
     *   OR properly handle project in getModel method.
     */
    @JvmStatic fun getModel(project: Project): TypeModel {
      val model = ourModels[project]
      if (model != null) return model
      return ApplicationManager.getApplication().getService(TypeModelProvider::class.java)._model
    }

    /**
     * To be used by tests
     */
    internal fun setModel(project: Project, model: TypeModel?) {
      if (model == null) {
        ourModels.remove(project)
        return
      }
      ourModels[project] = model
      val disposable = Disposable {
        ourModels.remove(project)
        ourDisposers.remove(project)
      }
      if (ourDisposers.putIfAbsent(project, disposable) == null) {
        Disposer.register(project, disposable)
      }
    }

    @JvmStatic
    fun reloadModel(project: Project) {
      // Unload, global way
      ourModels.clear()
      val service = ApplicationManager.getApplication().getService(TypeModelProvider::class.java)
      service._model_lazy.reset()

      // Load, global way
      ApplicationManager.getApplication().getService(TypeModelProvider::class.java)._model
    }

    private val ourModels: MutableMap<Project, TypeModel> = ConcurrentHashMap()
    private val ourDisposers: MutableMap<Project, Disposable> = ConcurrentHashMap()
  }

  private fun loadExternalInformation(): Map<String, Additional> {
    val map = HashMap<String, Additional>()

    val stream = TerraformMetadataLoader.loadExternalResource("external-data.json") ?: return map
    val json = stream.use {
      ObjectMapper().readTree(it) as ObjectNode?
    }

    if (json is ObjectNode) {
      for ((fqn, obj) in json.fields()) {
        if (obj !is ObjectNode) {
          TerraformMetadataLoader.LOG.warn("In external-data.json value for '$fqn' root key is not an object")
          continue
        }
        val hintV = obj["hint"]
        val hint: Hint? = when {
          hintV == null -> null
          hintV.isTextual -> ReferenceHint(hintV.textValue())
          hintV.isArray -> SimpleValueHint(*hintV.mapNotNull { it.textValue() }.toTypedArray())
          else -> null
        }
        val additional = Additional(fqn, obj.string("description"), hint, obj.boolean("optional"), obj.boolean("required"))
        map[fqn] = additional
      }
    }
    return map
  }

  private fun loadIgnoredReferences(): Set<String> {
    try {
      val stream = TerraformMetadataLoader.loadExternalResource("ignored-references.list")
      if (stream == null) {
        TerraformMetadataLoader.LOG.warn("Cannot read 'ignored-references.list': resource '/terraform/model-external/ignored-references.list' not found")
        return emptySet()
      }
      val lines = stream.use { s -> s.bufferedReader(Charsets.UTF_8).readLines().map(String::trim).filter { !it.isEmpty() } }
      return LinkedHashSet<String>(lines)
    } catch(e: Exception) {
      TerraformMetadataLoader.LOG.warn("Cannot read 'ignored-references.list': ${e.message}")
      return emptySet()
    }
  }


  data class Additional(val name: String, val description: String? = null, val hint: Hint? = null, val optional: Boolean? = null, val required: Boolean? = null)
}