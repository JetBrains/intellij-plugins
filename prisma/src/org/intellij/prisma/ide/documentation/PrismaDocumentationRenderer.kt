package org.intellij.prisma.ide.documentation

import org.intellij.prisma.lang.psi.PrismaVirtualDocumentationComment

class PrismaDocumentationRenderer(private val element: PrismaVirtualDocumentationComment?) {
  fun render(): String? {
    if (element == null) {
      return null
    }

    return documentationMarkdownToHtml(element.content)
  }
}