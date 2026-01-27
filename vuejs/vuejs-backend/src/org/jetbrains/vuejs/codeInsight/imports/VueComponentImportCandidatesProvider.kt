// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.model.VueModelManager
import java.util.function.Consumer

class VueComponentImportCandidatesProvider(private val placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  override fun collectNames(consumer: Consumer<String>) {
    VueModelManager.getGlobal(placeInfo.place).unregistered.components
      .keys.forEach { consumer.accept(toAsset(it, true)) }
  }

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    VueModelManager.getGlobal(placeInfo.place).unregistered.components[fromAsset(name)]
      ?.elementToImport
      ?.asSafely<PsiFile>()
      ?.let { processor.processCandidate(ES6ImportCandidate(name, it, placeInfo.place)) }
  }
}

class VueComponentImportCandidatesProviderFactory : JSImportCandidatesProvider.CandidatesFactory {
  override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? =
    if (isVueContext(placeInfo.place)) VueComponentImportCandidatesProvider(placeInfo) else null

}