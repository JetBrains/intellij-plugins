package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.completion.JSSmartCompletionContributor
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression

/**
 * @author Irina.Chernushina on 7/31/2017.
 */
class VueJSCompletionContributor : JSSmartCompletionContributor() {
  override fun getSmartCompletionVariants(location: JSReferenceExpression): List<LookupElement>? {
    val baseVariants = super.getSmartCompletionVariants(location)

    val scriptWithExport = findScriptWithExport(location.originalElement) ?: return baseVariants
    val defaultExport = scriptWithExport.second
    val obj = defaultExport.stubSafeElement as? JSObjectLiteralExpression ?: return baseVariants
    val vueVariants = ArrayList<LookupElement>()
    VueComponentDetailsProvider.INSTANCE.getAttributes(obj, false, false)
      // do not suggest directives in injected javascript fragments
      .filter { !it.isDirective()}
      .forEach {
        val builder = if (it.declaration == null) LookupElementBuilder.create(it.name)
          else JSLookupUtilImpl.createLookupElement(it.declaration!!, it.name)
        vueVariants.add(JSCompletionUtil.withJSLookupPriority(builder, JSLookupPriority.MAX_PRIORITY))
      }
    return mergeVariants(baseVariants, vueVariants)
  }
}