package org.intellij.prisma.ide.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import org.intellij.prisma.ide.schema.PrismaSchemaContext
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.resolve.PrismaResolver
import java.util.function.Consumer

class PrismaDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
    return PrismaDocumentationBuilder(element).buildDocumentation()
  }

  override fun generateRenderedDoc(comment: PsiDocCommentBase): String? {
    val docComment = comment as? PrismaVirtualDocumentationComment ?: return null
    return PrismaDocumentationRenderer(docComment).render()
  }

  override fun getCustomDocumentationElement(
    editor: Editor,
    file: PsiFile,
    contextElement: PsiElement?,
    targetOffset: Int
  ): PsiElement? {
    val context = contextElement?.let { PrismaSchemaContext.adjustContextElement(it) }
    if (acceptCustomElement(context)) {
      return context
    }

    return super.getCustomDocumentationElement(editor, file, contextElement, targetOffset)
  }

  private fun acceptCustomElement(context: PsiElement?): Boolean {
    if (context == null) {
      return false
    }
    if ((context as? PrismaReferenceElement)?.resolve() != null) {
      return false
    }
    if (context is PrismaArrayExpression || context is PrismaValueArgument) {
      return false
    }
    if (context is PrismaFunctionCall) {
      return !PrismaResolver.isFieldExpression(context)
    }
    return true
  }

  override fun findDocComment(file: PsiFile, range: TextRange): PsiDocCommentBase? {
    val element = file.findElementAt(range.startOffset)
    val comments = generateSequence(element) { el -> el.nextSibling }
      .takeWhile { it.startOffset < range.endOffset }
      .filter { it.isDocComment && !it.isTrailingComment }
      .mapNotNull { it as? PsiComment }
      .toList()

    return groupComments(comments).firstOrNull()
  }

  override fun collectDocComments(file: PsiFile, sink: Consumer<in PsiDocCommentBase>) {
    val comments = PsiTreeUtil
      .findChildrenOfType(file, PsiComment::class.java)
      .filter { it.isDocComment && !it.isTrailingComment }

    groupComments(comments).forEach {
      sink.accept(it)
    }
  }

  private fun groupComments(comments: List<PsiComment>): List<PrismaVirtualDocumentationComment> {
    if (comments.isEmpty()) {
      return emptyList()
    }

    val docComments = mutableListOf<PrismaVirtualDocumentationComment>()
    var i = comments.lastIndex
    while (i >= 0) {
      val comment = comments[i]
      val block = comment.collectPrecedingDocComments(false)

      if (block.isNotEmpty()) {
        docComments.add(PrismaVirtualDocumentationComment(block))
        i = comments.indexOf(block.first()) - 1
      }
      else {
        i--
      }
    }
    return docComments
  }
}