// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.intellij.prisma.PrismaBundle.message
import org.intellij.prisma.lang.psi.*

class PrismaFindUsagesProvider : FindUsagesProvider {

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean = psiElement is PrismaNamedElement

  override fun getType(element: PsiElement): String = when (element) {
    is PrismaModelDeclaration -> message("prisma.model.declaration")
    is PrismaTypeDeclaration -> message("prisma.type.declaration")
    is PrismaViewDeclaration -> message("prisma.view.declaration")
    is PrismaDatasourceDeclaration -> message("prisma.datasource.declaration")
    is PrismaGeneratorDeclaration -> message("prisma.generator.declaration")
    is PrismaTypeAlias -> message("prisma.type.alias")
    is PrismaEnumDeclaration -> message("prisma.enum.declaration")
    is PrismaFieldDeclaration -> message("prisma.field.declaration")
    is PrismaEnumValueDeclaration -> message("prisma.enum.value.declaration")
    is PrismaKeyValue -> message("prisma.key.value")
    else -> ""
  }

  override fun getDescriptiveName(element: PsiElement): String =
    element.asSafely<PrismaNamedElement>()?.name ?: message("prisma.unknown.element")

  override fun getNodeText(element: PsiElement, useFullName: Boolean): String = element.text

  override fun getHelpId(psiElement: PsiElement): String? = null
}