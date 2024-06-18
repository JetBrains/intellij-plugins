// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubOutputStream
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.psi.PrismaElement

abstract class PrismaStubElementType<S : StubElement<P>, P : PrismaElement>(debugName: String) :
  IStubElementType<S, P>(debugName, PrismaLanguage) {

  override fun getExternalId(): String = EXTERNAL_PREFIX_ID + super.toString()

  override fun serialize(stub: S, dataStream: StubOutputStream) {
  }

  override fun indexStub(stub: S, sink: IndexSink) {
  }
}