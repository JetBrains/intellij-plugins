package com.intellij.deno.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.deno.DenoSettings
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptLanguageServiceCompletionContributor
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.response.TypeScriptCompletionResponse.CompletionEntryDetail
import java.util.function.Consumer

class DenoTypeScriptServiceCompletionContributor : TypeScriptLanguageServiceCompletionContributor() {
  override fun fillCompletionVariants(parameters: CompletionParameters, resultSet: CompletionResultSet) {
    if (!DenoSettings.getService(parameters.position.project).isUseDeno()) return

    val position = parameters.position.parent
    if (position !is ES6ImportSpecifier) return
    val fromClause = position.declaration?.fromClause ?: return
    if (!fromClause.resolveReferencedElements().isEmpty()) return
    val file = parameters.originalFile
    val tsService = getServiceIfEnabled(parameters, file)
    if (tsService !is DenoTypeScriptService) return

    val future = getServiceEntriesFuture(tsService, file, parameters)
    val details = JSLanguageServiceUtil.awaitFuture(future, TIMEOUT_MILLS, JSLanguageServiceUtil.QUOTA_MILLS, null)
    if (details == null || details.isEmpty()) return
    details.forEach(Consumer { item: CompletionEntryDetail ->
      var el = createLookupElementImpl(item, null)
      if (el is PrioritizedLookupElement<*>) el = el.delegate
      if (el is LookupElementBuilder) {
        resultSet.consume(el.withInsertHandler(null))
      }
    })
  }
}