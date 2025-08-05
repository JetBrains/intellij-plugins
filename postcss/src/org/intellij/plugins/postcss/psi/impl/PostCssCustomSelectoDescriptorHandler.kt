package org.intellij.plugins.postcss.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssDescriptorHandler
import com.intellij.psi.css.descriptor.CssElementDescriptor
import org.intellij.plugins.postcss.descriptors.PostCssCustomSelectorDescriptor

class PostCssCustomSelectoDescriptorHandler : CssDescriptorHandler<PostCssCustomSelectorImpl, CssElementDescriptor> {
  override fun getDescriptors(descriptorOwner: PostCssCustomSelectorImpl, context: PsiElement): MutableCollection<out CssElementDescriptor> =
    mutableListOf(PostCssCustomSelectorDescriptor(descriptorOwner))
}