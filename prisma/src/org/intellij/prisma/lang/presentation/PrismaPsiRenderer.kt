package org.intellij.prisma.lang.presentation

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import org.intellij.prisma.lang.psi.PrismaBlockAttribute
import org.intellij.prisma.lang.psi.PrismaExpression
import org.intellij.prisma.lang.psi.PrismaFieldAttribute
import org.intellij.prisma.lang.psi.PrismaFieldDeclaration
import org.intellij.prisma.lang.psi.PrismaFieldType

class PrismaPsiRenderer {
  @NlsSafe
  fun build(element: PsiElement?): String {
    // TODO: implement
    return when (element) {
      is PrismaFieldDeclaration -> element.text
      is PrismaFieldType -> element.text
      is PrismaFieldAttribute -> element.text
      is PrismaBlockAttribute -> element.text
      is PrismaExpression -> element.text
      else -> element?.text ?: ""
    }
  }
}