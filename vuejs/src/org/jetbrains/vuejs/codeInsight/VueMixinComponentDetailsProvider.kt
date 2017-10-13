package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.MIXINS
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.resolve

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
class VueMixinComponentDetailsProvider : VueAbstractComponentDetailsProvider() {
  override fun getIterable(descriptor: JSObjectLiteralExpression,
                           filter: ((String, PsiElement) -> Boolean)?,
                           onlyPublic: Boolean, onlyFirst: Boolean): Iterable<VueAttributeDescriptor> {
    return getComponentMixins(descriptor)?.mapNotNull {
      val mixinObject = resolveMixinObject(it) ?: return@mapNotNull null
      VueComponentOwnDetailsProvider().getIterable(mixinObject, filter, onlyPublic, onlyFirst)
    }?.flatten() ?: emptyList()
  }

  private fun getComponentMixins(obj: JSObjectLiteralExpression): List<JSImplicitElement>? {
    val mixinsProperty = findProperty(obj, MIXINS) ?: return null
    val elements = resolve("", GlobalSearchScope.fileScope(mixinsProperty.containingFile), VueMixinBindingIndex.KEY) ?: return null
    return elements.filter { PsiTreeUtil.isAncestor(mixinsProperty, it.parent, false) }
  }

  private fun resolveMixinObject(it: JSImplicitElement): JSObjectLiteralExpression? {
    var mixinObj = it.parent as? JSObjectLiteralExpression
    if (it.typeString != null) {
      mixinObj = VueComponents.resolveReferenceToObjectLiteral(it, it.typeString!!)
    }
    return mixinObj
  }
}