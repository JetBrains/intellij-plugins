// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.CompositePsiElement
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElement
import com.intellij.xml.util.XmlPsiUtil
import org.jetbrains.annotations.NonNls

private const val IMPL_SUFFIX: @NonNls String = "Impl"

abstract class Angular2HtmlCompositePsiElement(type: IElementType) : CompositePsiElement(type), XmlElement {
  override fun toString(): String {
    return javaClass.simpleName.removeSuffix(IMPL_SUFFIX)
  }

  override fun processElements(processor: PsiElementProcessor<*>, place: PsiElement): Boolean {
    @Suppress("UNCHECKED_CAST")
    return XmlPsiUtil.processXmlElements(this, processor as PsiElementProcessor<in PsiElement>, false)
  }
}