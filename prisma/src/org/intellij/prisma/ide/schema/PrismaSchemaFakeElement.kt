package org.intellij.prisma.ide.schema

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.parentOfTypes
import com.intellij.psi.util.prevLeafs
import com.intellij.psi.util.skipTokens
import org.intellij.prisma.lang.presentation.icon
import org.intellij.prisma.lang.psi.PrismaArgumentsOwner
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaMemberDeclaration
import org.intellij.prisma.lang.psi.PrismaNamedElement
import javax.swing.Icon

class PrismaSchemaFakeElement(
  private val parent: PsiElement,
  val schemaElement: PrismaSchemaElement,
) : FakePsiElement(), PrismaNamedElement {
  override fun getParent(): PsiElement = parent

  override fun getProject(): Project = parent.project

  override fun getContainingFile(): PsiFile = parent.containingFile

  override fun getName(): String = schemaElement.label

  override fun getText(): String = name

  override fun getIcon(open: Boolean): Icon? = this.schemaElement.icon

  companion object {
    fun createForCompletion(
      parameters: CompletionParameters,
      schemaElement: PrismaSchemaElement,
    ): PrismaSchemaFakeElement {
      val parent = findSuitableParent(parameters) ?: parameters.originalFile
      return createForCompletion(parent, schemaElement)
    }

    fun createForCompletion(
      parent: PsiElement,
      schemaElement: PrismaSchemaElement,
    ): PrismaSchemaFakeElement {
      return PrismaSchemaFakeElement(parent, schemaElement)
    }

    fun findSuitableParent(parameters: CompletionParameters): PsiElement? {
      var context = parameters.originalPosition

      if (context is PsiWhiteSpace) {
        val prevLeaf = context.prevLeafs.skipTokens(IGNORE_TOKENS).firstOrNull()
        if (prevLeaf != null) {
          context = prevLeaf
        }
      }

      context = context?.parentOfTypes(
        PrismaDeclaration::class,
        PrismaMemberDeclaration::class,
        PrismaArgumentsOwner::class,
        withSelf = true
      )

      return context
    }

    private val IGNORE_TOKENS = TokenSet.create(TokenType.WHITE_SPACE, TokenType.ERROR_ELEMENT)
  }
}