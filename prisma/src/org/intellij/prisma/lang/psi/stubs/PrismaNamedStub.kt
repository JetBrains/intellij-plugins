// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi.stubs

import com.intellij.psi.stubs.NamedStub
import org.intellij.prisma.lang.psi.PrismaNamedElement

interface PrismaNamedStub<T : PrismaNamedElement> : NamedStub<T>