// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver
import org.angular2.lang.Angular2LangUtil
import java.util.function.Predicate

class Angular2GlobalImportCandidatesProvider(private val placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  override fun getNames(keyFilter: Predicate<in String>): Set<String> =
    // Provided by Angular2CompletionContributor
    emptySet()

  override fun processCandidates(ref: String, processor: JSCandidatesProcessor) {
    TypeScriptClassResolver.getInstance().findGlobalElementsByQName(ref, placeInfo.place)
      .firstOrNull()
      ?.let { processor.processCandidate(Angular2GlobalImportCandidate(ref, placeInfo.place)) }
  }

  class Factory : JSImportCandidatesProvider.CandidatesFactory {

    override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? =
      if (Angular2LangUtil.isAngular2Context(placeInfo.place)) Angular2GlobalImportCandidatesProvider(placeInfo) else null

  }
}