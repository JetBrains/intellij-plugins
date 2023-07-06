// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.util.stubSafeGetAttribute
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
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
        BASIC_REF_PROVIDER.getReferencesByElement(VueFakeSrcAttributeValue(attribute, it), ProcessingContext())
      }
    else
      attribute.valueElement?.let {
        BASIC_REF_PROVIDER.getReferencesByElement(it, ProcessingContext())
      }
  return attrReferences
    ?.asSequence()
    ?.flatMap { if (it is PsiReferencesWrapper) it.references else listOf(it) }
    ?.filterIsInstance<FileReference>()
    ?.mapNotNull { it.resolve()?.asSafely<PsiFile>() }
    ?.firstOrNull()
}

class VueFakeScrAttributeValueManipulator : AbstractElementManipulator<VueFakeSrcAttributeValue>() {
  override fun handleContentChange(element: VueFakeSrcAttributeValue, range: TextRange, newContent: String): VueFakeSrcAttributeValue =
    VueFakeSrcAttributeValue(element.parent, range.replace(element.text, newContent))

}

class VueFakeSrcAttributeValue(private val myParent: PsiElement, value: String) : FakePsiElement() {

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
