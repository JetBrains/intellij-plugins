// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.psi.PsiElement
import com.intellij.util.resettableLazy
import org.intellij.terraform.config.model.loader.TerraformMetadataLoader
import org.intellij.terraform.config.model.local.LocalSchemaService
import java.util.concurrent.Callable

@Service
internal class TypeModelProvider {
  private val _model_lazy = resettableLazy {
    // Run non-cancellable since it may take time, so it makes sense to finish loading even if the caller was canceled
    @Suppress("RedundantSamConstructor")
    ApplicationUtil.runWithCheckCanceled(Callable {
      TerraformMetadataLoader().loadDefaults() ?: TypeModel()
    }, EmptyProgressIndicator())
  }
  private val _model: TypeModel by _model_lazy

  val ignored_references: Set<String> by lazy { loadIgnoredReferences() }

  companion object {

    @JvmStatic
    val globalModel: TypeModel
      get() = service<TypeModelProvider>()._model

    fun getModel(psiElement: PsiElement): TypeModel {
      val virtualFile = getContainingFile(psiElement)?.virtualFile ?: return globalModel
      return psiElement.containingFile.project.service<LocalSchemaService>().getModel(virtualFile) ?: globalModel
    }
  }

  private fun loadIgnoredReferences(): Set<String> {
    try {
      val stream = TerraformMetadataLoader.loadExternalResource("ignored-references.list")
      if (stream == null) {
        TerraformMetadataLoader.LOG.warn(
          "Cannot read 'ignored-references.list': resource '/terraform/model-external/ignored-references.list' not found")
        return emptySet()
      }
      val lines = stream.use { s -> s.bufferedReader(Charsets.UTF_8).readLines().map(String::trim).filter { !it.isEmpty() } }
      return LinkedHashSet<String>(lines)
    }
    catch (e: Exception) {
      TerraformMetadataLoader.LOG.warn("Cannot read 'ignored-references.list': ${e.message}")
      return emptySet()
    }
  }

}