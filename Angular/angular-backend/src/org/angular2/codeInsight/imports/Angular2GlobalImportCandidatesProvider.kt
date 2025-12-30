// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.imports

import com.intellij.lang.javascript.modules.JSImportPlaceInfo
import com.intellij.lang.javascript.modules.imports.JSImportCandidatesBase
import com.intellij.lang.javascript.modules.imports.providers.JSCandidatesProcessor
import com.intellij.lang.javascript.modules.imports.providers.JSImportCandidatesProvider
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.html.Angular2HtmlFile
import java.util.function.Consumer

class Angular2GlobalImportCandidatesProvider(private val placeInfo: JSImportPlaceInfo) : JSImportCandidatesBase(placeInfo) {

  override fun collectNames(consumer: Consumer<String>) {
    // Provided by Angular2CompletionContributor
  }

  override fun processCandidates(name: String, processor: JSCandidatesProcessor) {
    TypeScriptClassResolver.getInstance().findGlobalElementsByQName(name, placeInfo.place)
      .firstOrNull()
      ?.let { processor.processCandidate(Angular2GlobalImportCandidate(name, name, placeInfo.place)) }
  }

  class Factory : JSImportCandidatesProvider.CandidatesFactory {

    override fun createProvider(placeInfo: JSImportPlaceInfo): JSImportCandidatesProvider? =
      if (placeInfo.place.containingFile.let { it is Angular2HtmlFile || it.language is Angular2ExprDialect }
          && Angular2LangUtil.isAngular2Context(placeInfo.place))
        Angular2GlobalImportCandidatesProvider(placeInfo)
      else
        null

  }
}