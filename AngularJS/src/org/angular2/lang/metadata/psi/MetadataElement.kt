// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata.psi

import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.stubs.StubElement
import org.angular2.lang.metadata.stubs.MetadataElementStub

abstract class MetadataElement<Stub : MetadataElementStub<*>>(private val myStub: Stub) : FakePsiElement(), StubBasedPsiElement<Stub> {
  private val children = NotNullLazyValue.lazy {
    stub.childrenStubs.map { it.psi }.toTypedArray<PsiElement>()
  }

  override fun getContainingFile(): PsiFile {
    var stub: StubElement<*>? = myStub
    while (stub != null && stub !is PsiFileStubImpl<*>) {
      stub = stub.parentStub
    }
    if (stub == null) {
      throw PsiInvalidElementAccessException(this)
    }
    return (stub as PsiFileStubImpl<*>).psi
           ?: throw PsiInvalidElementAccessException(
             this, "Metadata file psi has been cleared: " + stub.invalidationReason!!)
  }

  override fun getParent(): PsiElement? {
    return myStub.parentStub?.psi
  }

  override fun getTextRangeInParent(): TextRange {
    return TextRange.EMPTY_RANGE
  }

  override fun getElementType(): IStubElementType<*, *> {
    return myStub.stubType
  }

  override fun getStub(): Stub {
    return myStub
  }

  open fun findMember(name: String): MetadataElement<*>? {
    return stub.findMember(name)?.psi as? MetadataElement<*>
  }

  override fun getChildren(): Array<PsiElement> {
    return children.value
  }

  override fun getFirstChild(): PsiElement? {
    return getChildren().firstOrNull()
  }

  override fun getLastChild(): PsiElement? {
    return getChildren().lastOrNull()
  }

  override fun getNextSibling(): PsiElement? {
    val parent = parent ?: return null
    val children = parent.children
    val index = children.indexOf(this)
    return if (index >= 0 && index + 1 < children.size) children[index + 1] else null
  }

  override fun getPrevSibling(): PsiElement? {
    val parent = parent ?: return null
    val children = parent.children
    val index = children.indexOf(this) - 1
    return if (index >= 0) children[index] else null
  }
}
