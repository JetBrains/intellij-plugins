package org.intellij.prisma.ide.documentation

import com.intellij.openapi.util.NlsSafe
import org.intellij.prisma.lang.psi.PrismaVirtualDocumentationComment

class PrismaDocumentationRenderer(private val element: PrismaVirtualDocumentationComment?) {

  @NlsSafe
  fun render(): String? {
    if (element == null) {
      return null
    }

    return documentationMarkdownToHtml(element.content)
  }
}