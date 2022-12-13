package org.intellij.prisma.ide.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import org.intellij.prisma.ide.schema.PrismaSchemaContext
import org.intellij.prisma.lang.psi.*

fun findAssociatedField(position: PsiElement?): PrismaFieldDeclaration? {
  if (position is PsiWhiteSpace) {
    val prev = position.skipWhitespacesBackwardWithoutNewLines()
    return prev?.parentOfType(true)
  }

  if (position.elementType == PrismaElementTypes.IDENTIFIER) {
    return position?.parentOfType()
  }

  return null
}

fun collectExistingAttributeNamesForDeclaration(declaration: PrismaDeclaration): Set<String> {
  val attributes = PsiTreeUtil.findChildrenOfAnyType(
    declaration,
    PrismaBlockAttribute::class.java,
    PrismaFieldAttribute::class.java
  )

  return attributes.mapNotNullTo(mutableSetOf()) { PrismaSchemaContext.getSchemaLabel(it) }
}

fun collectExistingAttributeNamesForField(field: PrismaFieldDeclaration): Set<String> {
  return field.fieldAttributeList.mapNotNullTo(mutableSetOf()) { PrismaSchemaContext.getSchemaLabel(it) }
}

fun collectExistingMemberNames(parameters: CompletionParameters) =
  parameters.originalPosition?.parentOfType<PrismaDeclaration>()?.let {
    collectExistingMemberNames(it)
  } ?: emptySet()

fun collectExistingMemberNames(declaration: PrismaDeclaration): Set<String> {
  return declaration.getMembers().mapNotNull { PrismaSchemaContext.getSchemaLabel(it) }.toSet()
}