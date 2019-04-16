// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.codeInsight.VueComponents.Companion.vueMixinDescriptorFinder
import org.jetbrains.vuejs.index.LOCAL
import org.jetbrains.vuejs.index.MIXINS
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.resolve

/**
 * @author Irina.Chernushina on 10/12/2017.
 */
class VueMixinLocalComponentDetailsProvider : VueAdvancedComponentDetailsProvider {
  override fun getIndexedData(descriptor: JSObjectLiteralExpression?, project: Project): Collection<JSImplicitElement> {
    if (descriptor == null) return emptyList()
    val mixinsProperty = findProperty(descriptor, MIXINS) ?: return emptyList()
    val elements = resolve(LOCAL, GlobalSearchScope.fileScope(mixinsProperty.containingFile.originalFile), VueMixinBindingIndex.KEY)
                   ?: return emptyList()
    val original = CompletionUtil.getOriginalOrSelf<PsiElement>(mixinsProperty)
    return elements.filter { PsiTreeUtil.isAncestor(original, it.parent, false) }
  }

  override fun getDescriptorFinder(): (JSImplicitElement) -> JSObjectLiteralExpression? {
    return { vueMixinDescriptorFinder(it) }
  }
}
