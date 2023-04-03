// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.util.stubSafeGetAttribute
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SRC_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.refs.VueReferenceContributor.Companion.BASIC_REF_PROVIDER

fun XmlTag.hasSrcReference(): Boolean =
  !stubSafeGetAttribute(SRC_ATTRIBUTE_NAME)?.value.isNullOrBlank()

@StubSafe
fun XmlTag.tryResolveSrcReference(): PsiElement? {
  val attribute = stubSafeGetAttribute(SRC_ATTRIBUTE_NAME)?.takeIf { !it.value.isNullOrBlank() }
                  ?: return null
  val attrReferences: Array<PsiReference>? =
    if ((attribute as? StubBasedPsiElement<*>)?.stub != null)
      attribute.value?.let {
        BASIC_REF_PROVIDER.getReferencesByElement(FakeAttributeValue(attribute, it), ProcessingContext())
      }
    else
      attribute.valueElement?.let {
        BASIC_REF_PROVIDER.getReferencesByElement(it, ProcessingContext())
      }
  return attrReferences
    ?.asSequence()
    ?.mapNotNull { it.resolve()?.asSafely<PsiFile>() }
    ?.firstOrNull()
}

private class FakeAttributeValue constructor(private val myParent: PsiElement, value: String) : FakePsiElement() {

  private val myValue: String = value

  override fun getParent(): PsiElement {
    return myParent
  }

  override fun getText(): String {
    return myValue
  }

  override fun getTextLength(): Int {
    return myValue.length
  }

  override fun getStartOffsetInParent(): Int {
    throw IllegalStateException()
  }
}
