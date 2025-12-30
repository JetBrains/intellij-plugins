// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata.stubs

import com.intellij.lang.Language
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IStubFileElementType
import org.angular2.lang.metadata.psi.MetadataFileImpl

class MetadataFileStubImpl(file: MetadataFileImpl?, private val myType: IStubFileElementType<*>) : PsiFileStubImpl<MetadataFileImpl>(
  file), PsiFileStub<MetadataFileImpl> {

  val language: Language
    get() = type.language

  override fun setPsi(psi: MetadataFileImpl) {
    super.setPsi(psi)
  }

  override fun getType(): IStubFileElementType<*> {
    return myType
  }
}