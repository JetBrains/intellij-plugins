// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.REF_ATTRIBUTE
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeImpl
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeStubImpl

class VueRefAttributeStubFactory : StubElementFactory<VueRefAttributeStubImpl, VueRefAttributeImpl> {
  override fun createStub(psi: VueRefAttributeImpl, parentStub: StubElement<out PsiElement>?): VueRefAttributeStubImpl =
    VueRefAttributeStubImpl(psi, parentStub, REF_ATTRIBUTE)

  override fun createPsi(stub: VueRefAttributeStubImpl): VueRefAttributeImpl =
    VueRefAttributeImpl(stub, REF_ATTRIBUTE)
}