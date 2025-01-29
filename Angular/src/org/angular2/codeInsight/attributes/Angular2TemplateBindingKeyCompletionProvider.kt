package org.angular2.codeInsight.attributes

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import com.intellij.webSymbols.completion.WebSymbolsCompletionProviderBase
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.web.NG_TEMPLATE_BINDINGS

class Angular2TemplateBindingKeyCompletionProvider : WebSymbolsCompletionProviderBase<Angular2TemplateBindingKey>() {

  override fun getContext(position: PsiElement): Angular2TemplateBindingKey? =
    position as? Angular2TemplateBindingKey
    ?: position.parent as? Angular2TemplateBindingKey

  override fun addCompletions(
    parameters: CompletionParameters,
    result: CompletionResultSet,
    position: Int,
    name: String,
    queryExecutor: WebSymbolsQueryExecutor,
    context: Angular2TemplateBindingKey,
  ) {
    val patchedResultSet = result.withPrefixMatcher(result.prefixMatcher.cloneWithPrefix(name))
    val binding = (context.parent as? Angular2TemplateBinding)?.takeIf { !it.keyIsVar() } ?: return
    val existingBindings = binding.siblings(false, false)
      .filterIsInstance<Angular2TemplateBinding>()
      .filter { !it.keyIsVar() }
      .mapNotNullTo(mutableSetOf()) { it.name }
    processCompletionQueryResults(
      queryExecutor,
      result,
      NG_TEMPLATE_BINDINGS,
      name,
      position,
      context,
      providedNames = existingBindings,
    ) { item ->
      item.addToResult(parameters, patchedResultSet)
    }
    result.stopHere()
  }
}