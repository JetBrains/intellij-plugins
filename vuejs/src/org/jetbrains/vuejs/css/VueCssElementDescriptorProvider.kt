// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.css

import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssElementDescriptorProvider
import com.intellij.psi.css.CssSimpleSelector
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub
import org.jetbrains.vuejs.context.isVueContext

class VueCssElementDescriptorProvider : CssElementDescriptorProvider() {

  private val ALL_SELECTORS = listOf(

    CssPseudoSelectorDescriptorStub("v-deep", true, false), // Deprecated

    CssPseudoSelectorDescriptorStub("v-deep", true, true),
    CssPseudoSelectorDescriptorStub("deep", false, true),

    CssPseudoSelectorDescriptorStub("v-slotted", true, true),
    CssPseudoSelectorDescriptorStub("slotted", false, true),

    CssPseudoSelectorDescriptorStub("v-global", true, true),
    CssPseudoSelectorDescriptorStub("global", false, true),

  )

  override fun isMyContext(context: PsiElement?): Boolean = context?.let { isVueContext(it) } == true

  override fun findPseudoSelectorDescriptors(name: String, context: PsiElement?): Collection<CssPseudoSelectorDescriptor> =
    if (context != null) ALL_SELECTORS.filter { it.id == name } else emptyList()

  override fun getAllPseudoSelectorDescriptors(context: PsiElement?): Collection<CssPseudoSelectorDescriptor> =
    ALL_SELECTORS

  override fun getDeclarationsForSimpleSelector(selector: CssSimpleSelector): Array<PsiElement> = PsiElement.EMPTY_ARRAY

}
