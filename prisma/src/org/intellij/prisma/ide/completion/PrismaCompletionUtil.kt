package org.intellij.prisma.ide.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.prisma.ide.schema.PrismaSchemaPath
import org.intellij.prisma.lang.psi.*

fun findAttributeOwner(position: PsiElement?): PrismaFieldAttributeOwner? {
  if (position is PsiWhiteSpace) {
    val prev = position.skipWhitespacesBackwardWithoutNewLines()
    return prev?.parentOfType(true)
  }

  if (position.elementType == PrismaElementTypes.IDENTIFIER) {
    return position?.parentOfType()
  }

  return null
}

fun collectExistingAttributeNames(declaration: PrismaDeclaration): Set<String> {
  val attributes = PsiTreeUtil.findChildrenOfAnyType(
    declaration,
    PrismaBlockAttribute::class.java,
    PrismaFieldAttribute::class.java
  )

  return attributes.mapNotNullTo(mutableSetOf()) { PrismaSchemaPath.getSchemaLabel(it) }
}

fun collectExistingAttributeNames(attributeOwner: PrismaFieldAttributeOwner): Set<String> {
  return attributeOwner.fieldAttributeList.mapNotNullTo(mutableSetOf()) { PrismaSchemaPath.getSchemaLabel(it) }
}

fun collectExistingMemberNames(parameters: CompletionParameters) =
  parameters.originalPosition?.parentOfType<PrismaDeclaration>()?.let {
    collectExistingMemberNames(it)
  } ?: emptySet()

fun collectExistingMemberNames(declaration: PrismaDeclaration): Set<String> {
  return declaration.getMembers().mapNotNull { PrismaSchemaPath.getSchemaLabel(it) }.toSet()
}

val PRISMA_ENTITY_DECLARATION = Key.create<PrismaEntityDeclaration>("prisma.containing.declaration")

internal fun ProcessingContext?.populateContext(entityDeclaration: PrismaEntityDeclaration?) {
  if (this == null) return

  entityDeclaration?.let { put(PRISMA_ENTITY_DECLARATION, it) }
}