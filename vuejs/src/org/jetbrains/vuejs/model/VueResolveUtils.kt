// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.impl.source.xml.stub.XmlAttributeStub
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.HtmlUtil.SRC_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.refs.VueReferenceContributor.Companion.BASIC_REF_PROVIDER

@StubSafe
fun resolveTagSrcReference(tag: XmlTag): PsiElement? {
  val tagStub = (tag as? StubBasedPsiElementBase<*>)?.stub
  var attrReferences: Array<PsiReference>? = null
  if (tagStub != null) {
    tagStub.childrenStubs
      .asSequence()
      .mapNotNull { (it as? XmlAttributeStub<*>)?.psi }
      .filter { it.name == SRC_ATTRIBUTE_NAME && it.value != null }
      .firstOrNull()
      ?.let {
        attrReferences = BASIC_REF_PROVIDER.getReferencesByElement(FakeAttributeValue(it, it.value!!), ProcessingContext())
      }
  }
  else {
    tag.getAttribute(SRC_ATTRIBUTE_NAME)
      ?.valueElement
      ?.let { attrReferences = BASIC_REF_PROVIDER.getReferencesByElement(it, ProcessingContext()) }
  }
  if (attrReferences != null) {
    return attrReferences
      ?.asSequence()
      ?.mapNotNull { it.resolve()?.let { it as? PsiFile } }
      ?.firstOrNull()
  }
  return tag
}

private class FakeAttributeValue internal constructor(private val myParent: PsiElement, value: String) : FakePsiElement() {

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
