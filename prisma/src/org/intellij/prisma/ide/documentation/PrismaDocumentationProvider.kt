package org.intellij.prisma.ide.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import org.intellij.prisma.ide.schema.PrismaSchemaPath
import org.intellij.prisma.lang.psi.*
import java.util.function.Consumer

class PrismaDocumentationProvider : AbstractDocumentationProvider() {
  override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
    return PrismaDocumentationBuilder(element).buildDocumentation()
  }

  override fun generateRenderedDoc(comment: PsiDocCommentBase): String? {
    val docComment = comment as? PrismaDocComment ?: return null
    return PrismaDocumentationRenderer(docComment).render()
  }

  override fun getCustomDocumentationElement(
    editor: Editor,
    file: PsiFile,
    contextElement: PsiElement?,
    targetOffset: Int,
  ): PsiElement? {
    val path = contextElement?.let { PrismaSchemaPath.adjustPathElement(it) }
    if (acceptCustomElement(path)) {
      return path
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
      return !isFieldExpression(context)
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

    return groupDocComments(comments).firstOrNull()
  }

  override fun collectDocComments(file: PsiFile, sink: Consumer<in PsiDocCommentBase>) {
    buildDocComments(file).forEach { sink.accept(it) }
  }
}