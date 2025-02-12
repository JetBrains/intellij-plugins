// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.psi.PsiElement
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.intellij.terraform.TfUsageTriggerCollector
import org.intellij.terraform.config.model.loader.TfMetadataLoader
import org.intellij.terraform.config.model.local.LocalSchemaService

@Service
internal class TypeModelProvider(private val coroutineScope: CoroutineScope) {

  private val _model = this.coroutineScope.async(context = Dispatchers.IO, start = CoroutineStart.LAZY) {
    TfMetadataLoader().loadDefaults() ?: TypeModel()
  }

  val ignoredReferences: Set<String> by lazy { loadIgnoredReferences() }

  companion object {

    @JvmStatic
    @OptIn(ExperimentalCoroutinesApi::class)
    val globalModel: TypeModel
      get() {
        val asyncModelLoadTask = service<TypeModelProvider>()._model
        return if (asyncModelLoadTask.isCompleted) {
          asyncModelLoadTask.getCompleted()
        }
        else {
          runBlockingMaybeCancellable { asyncModelLoadTask.await() }
        }
      }

    @RequiresBackgroundThread(generateAssertion = true)
    fun getModel(psiElement: PsiElement): TypeModel {
      val virtualFile = getContainingFile(psiElement)?.virtualFile ?: return globalModel
      return psiElement.containingFile.project.service<LocalSchemaService>().getModel(virtualFile) ?: globalModel
    }
  }

  private fun loadIgnoredReferences(): Set<String> {
    try {
      val stream = TfMetadataLoader.loadExternalResource("ignored-references.list")
      if (stream == null) {
        TfMetadataLoader.LOG.warn(
          "Cannot read 'ignored-references.list': resource '/terraform/model-external/ignored-references.list' not found")
        return emptySet()
      }
      TfUsageTriggerCollector.ODD_FEATURE_USED.log("ignored-references")
      val lines = stream.use { s -> s.bufferedReader(Charsets.UTF_8).readLines().map(String::trim).filter { !it.isEmpty() } }
      return LinkedHashSet<String>(lines)
    }
    catch (e: Exception) {
      TfMetadataLoader.LOG.warn("Cannot read 'ignored-references.list': ${e.message}")
      return emptySet()
    }
  }

}