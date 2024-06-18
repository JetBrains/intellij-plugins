// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import org.intellij.prisma.lang.psi.PrismaNamedElement
import org.intellij.prisma.lang.psi.stubs.impl.PrismaNamedStubImpl

abstract class PrismaNamedStubElementType<P : PrismaNamedElement>(debugName: String) : PrismaStubElementType<PrismaNamedStub<P>, P>(debugName) {
  override fun serialize(stub: PrismaNamedStub<P>, dataStream: StubOutputStream) {
    super.serialize(stub, dataStream)
    dataStream.writeName(stub.name)
  }

  override fun createStub(psi: P, parentStub: StubElement<out PsiElement>?): PrismaNamedStub<P> {
    return PrismaNamedStubImpl(parentStub, this, psi.name)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): PrismaNamedStub<P> {
    val name = dataStream.readNameString()
    return PrismaNamedStubImpl(parentStub, this, name)
  }
}