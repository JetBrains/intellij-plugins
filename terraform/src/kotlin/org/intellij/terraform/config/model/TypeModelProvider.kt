// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.util.resettableLazy
import org.intellij.terraform.config.model.loader.TerraformMetadataLoader
import java.util.concurrent.Callable

class TypeModelProvider {
  private val _model_lazy = resettableLazy {
    // Run non-cancellable since it may take time, so it makes sense to finish loading even if caller was cancelled
    @Suppress("RedundantSamConstructor")
    ApplicationUtil.runWithCheckCanceled(Callable {
      TerraformMetadataLoader().load() ?: TypeModel()
    }, EmptyProgressIndicator())
  }
  private val _model: TypeModel by _model_lazy

  val ignored_references: Set<String> by lazy { loadIgnoredReferences() }

  companion object {
    /**
     * Parameter 'project' is ignored for now since we have only one model.
     * When multiple different models would be supported,
     *   TypeModelProvider should become either per-project service
     *   OR properly handle project in getModel method.
     */
    @JvmStatic fun getGlobalModel(): TypeModel {
      return ApplicationManager.getApplication().getService(TypeModelProvider::class.java)._model
    }

    @JvmStatic
    fun reloadGlobalModel() {
      // Unload, global way
      val service = ApplicationManager.getApplication().getService(TypeModelProvider::class.java)
      service._model_lazy.reset()

      // Load, global way
      ApplicationManager.getApplication().getService(TypeModelProvider::class.java)._model
    }

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

}