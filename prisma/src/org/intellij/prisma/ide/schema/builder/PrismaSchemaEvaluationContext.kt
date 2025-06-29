// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.schema.builder

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.intellij.prisma.lang.psi.PrismaFile
import org.intellij.prisma.lang.resolve.PrismaSchemaMetadata

class PrismaSchemaEvaluationContext(val position: PsiElement?, val file: PsiFile?) {
  val metadata: PrismaSchemaMetadata?
    get() = if (file is PrismaFile) file.metadata else null

  companion object {
    fun forElement(element: PsiElement?): PrismaSchemaEvaluationContext {
      return PrismaSchemaEvaluationContext(element, element?.containingFile)
    }
  }
}