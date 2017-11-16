package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression

/**
 * @author Irina.Chernushina on 7/31/2017.
 */
class VueJSCompletionContributor : CompletionContributor() {
  override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
    if (fillVueCompletionVariants(parameters, result)) return
    super.fillCompletionVariants(parameters, result)
  }

  private fun fillVueCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) : Boolean {
    val scriptWithExport = findScriptWithExport(parameters.position.originalElement) ?: return false
    val defaultExport = scriptWithExport.second
    val obj = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: return false
    VueComponentDetailsProvider.INSTANCE.getAttributes(obj, false, false)
      // do not suggest directives in injected javascript fragments
      .filter { !it.isDirective()}
      .forEach {
        val builder = if (it.declaration == null) LookupElementBuilder.create(it.name)
          else JSLookupUtilImpl.createLookupElement(it.declaration!!, it.name)
        result.addElement(JSCompletionUtil.withJSLookupPriority(builder, JSLookupPriority.MAX_PRIORITY))
      }
    return true
  }
}