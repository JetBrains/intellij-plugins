// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.*
import java.util.function.Predicate

class VueComponentImportCandidatesProvider(private val placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  override fun getNames(keyFilter: Predicate<in String>): Set<String> =
    VueModelManager.getGlobal(placeInfo.place).unregistered.components
      .keys.asSequence().map { toAsset(it, true) }
      .filter { keyFilter.test(it) }.toSet()

  override fun processCandidates(ref: String, processor: JSCandidatesProcessor) {
    val component = VueModelManager.getGlobal(placeInfo.place).unregistered.components[fromAsset(ref)]
    if (component != null && component is VueSourceComponent) {
      val source = component.descriptor.source
      if (source is PsiFile) {
        processor.processCandidate(ES6ImportCandidate(ref, source, placeInfo.place))
      }
    }
  }

  companion object : JSImportCandidatesProvider.CandidatesFactory {

    override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? =
      if (isVueContext(placeInfo.place)) VueComponentImportCandidatesProvider(placeInfo) else null

  }
}