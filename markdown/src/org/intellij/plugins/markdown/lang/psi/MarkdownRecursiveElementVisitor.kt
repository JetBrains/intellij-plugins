package org.intellij.plugins.markdown.lang.psi

import com.intellij.psi.PsiRecursiveElementVisitor
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl

open class MarkdownRecursiveElementVisitor : PsiRecursiveElementVisitor() {
  open fun visitHeader(header: MarkdownHeaderImpl) = visitElement(header)

  open fun visitMarkdownFile(markdownFile: MarkdownFile) = visitFile(markdownFile)
}
