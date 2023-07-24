// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web.codeCompletion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiElement
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolsCompletionProviderBase
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.utils.toCodeCompletionItems
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2Framework
import org.angular2.entities.Angular2EntityUtils
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

class Angular2HostDirectivePropertyMappingCompletionProvider : WebSymbolsCompletionProviderBase<JSLiteralExpression>() {

  override fun getContext(position: PsiElement): JSLiteralExpression? =
    position.parent as? JSLiteralExpression

  override fun addCompletions(parameters: CompletionParameters,
                              result: CompletionResultSet,
                              position: Int,
                              name: String,
                              queryExecutor: WebSymbolsQueryExecutor,
                              context: JSLiteralExpression) {
    val (kind, directive) = Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective(context, false)
                              ?.takeIf { it.hostDirective }
                            ?: return
    val symbols = if (kind == INPUTS_PROP) directive.bindings.inputs else directive.bindings.outputs
    val adjustedName = if (name.isNotEmpty()) name.substring(1) else name
    processWebSymbolCodeCompletionItems(
      symbols.flatMap { it.toCodeCompletionItems(adjustedName, WebSymbolsCodeCompletionQueryParams(queryExecutor, position -1), Stack()) },
      result, NAMESPACE_JS,
      if (kind == INPUTS_PROP) KIND_NG_DIRECTIVE_INPUTS else KIND_NG_DIRECTIVE_OUTPUTS,
      adjustedName, Angular2Framework.ID, context,
      consumer = { item ->
        item
          .addToResult(parameters, result)
      },
    )
    result.stopHere()
  }

}