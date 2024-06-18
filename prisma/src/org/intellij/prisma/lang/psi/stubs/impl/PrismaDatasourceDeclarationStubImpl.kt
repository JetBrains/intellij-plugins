// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.intellij.prisma.lang.psi.PrismaDatasourceDeclaration
import org.intellij.prisma.lang.psi.stubs.PrismaDatasourceDeclarationStub

class PrismaDatasourceDeclarationStubImpl(parent: StubElement<*>?, elementType: IStubElementType<out StubElement<*>, *>, name: String?) :
  PrismaNamedStubImpl<PrismaDatasourceDeclaration>(parent, elementType, name), PrismaDatasourceDeclarationStub