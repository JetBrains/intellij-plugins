package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.MIXINS
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.vueMixinDescriptorFinder
import org.jetbrains.vuejs.index.LOCAL
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.resolve

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
class VueMixinLocalComponentDetailsProvider : VueAdvancedComponentDetailsProvider {
  override fun getIndexedData(descriptor : JSObjectLiteralExpression): Collection<JSImplicitElement> {
    val mixinsProperty = findProperty(descriptor, MIXINS) ?: return emptyList()
    val elements = resolve(LOCAL, GlobalSearchScope.fileScope(mixinsProperty.containingFile), VueMixinBindingIndex.KEY) ?: return emptyList()
    return elements.filter { PsiTreeUtil.isAncestor(mixinsProperty, it.parent, false) }
  }

  override fun getDescriptorFinder(): (JSImplicitElement) -> JSObjectLiteralExpression? {
    return { vueMixinDescriptorFinder(it) }
  }
}