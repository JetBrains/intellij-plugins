package com.intellij.openRewrite.recipe

import com.intellij.psi.PsiElement

internal class OpenRewriteRecipePsiElement(val descriptor: OpenRewriteRecipeDescriptor) : OpenRewriteFakePsiElement() {
  override fun getParent(): PsiElement? = descriptor.declaration.retrieve()

  override fun getNavigationElement(): PsiElement = descriptor.declaration.retrieve() ?: this

  override fun getPresentableText(): String = descriptor.displayName ?: descriptor.name

  override fun getName(): String = descriptor.name

  override fun getLocationString(): String? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false

    val element = other as OpenRewriteRecipePsiElement
    return descriptor == element.descriptor
  }

  override fun hashCode(): Int = descriptor.hashCode()
}