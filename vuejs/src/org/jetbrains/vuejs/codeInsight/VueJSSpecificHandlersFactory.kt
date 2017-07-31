package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.xml.XmlFile

/**
 * @author Irina.Chernushina on 7/28/2017.
 */
class VueJSSpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return VueJSReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }
}

class VueJSReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl?,
                                       ignorePerformanceLimits: Boolean) :
    JSReferenceExpressionResolver(referenceExpression!!, ignorePerformanceLimits) {
  override fun resolve(ref: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    val xmlFile = if (ref.containingFile is XmlFile) ref.containingFile else
      InjectedLanguageManager.getInstance(myContainingFile.project).getInjectionHost(ref)?.containingFile
    if (xmlFile != null) {
      val resolvedInProps = tryResolveInProps(xmlFile as XmlFile, ref)
      if (resolvedInProps != null) return resolvedInProps
    }
    return super.resolve(ref, incompleteCode)
  }

  private fun tryResolveInProps(xmlFile: XmlFile, ref: JSReferenceExpressionImpl): Array<ResolveResult>? {
    val embeddedScriptContents = org.jetbrains.vuejs.codeInsight.findModule(xmlFile) ?: return null
    val defaultExport = com.intellij.lang.ecmascript6.resolve.ES6PsiUtil.findDefaultExport(embeddedScriptContents)
    if (defaultExport is ES6ExportDefaultAssignment && defaultExport.stubSafeExpression is JSObjectLiteralExpression) {
      val refName = ref.canonicalText
      val tagDescriptor = tagDescriptorFromObjectLiteral(defaultExport.stubSafeExpression as JSObjectLiteralExpression)
      val attributeDescriptor = tagDescriptor?.getAttributeDescriptor(refName, null)
      if (attributeDescriptor != null) return arrayOf(PsiElementResolveResult(attributeDescriptor.declaration))
    }
    return null
  }
}
