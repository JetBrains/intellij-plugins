// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.intellij.prisma.lang.psi.PrismaGeneratorDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaGeneratorDeclarationStub

class PrismaGeneratorDeclarationStubImpl(parent: StubElement<*>?, elementType: IStubElementType<out StubElement<*>, *>, name: String?) :
  PrismaNamedStubImpl<PrismaGeneratorDeclaration>(parent, elementType, name), PrismaGeneratorDeclarationStub