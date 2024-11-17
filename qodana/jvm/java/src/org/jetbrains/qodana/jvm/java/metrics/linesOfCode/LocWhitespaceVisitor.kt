package org.jetbrains.qodana.jvm.java.metrics.linesOfCode

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.qodana.jvm.java.metrics.getNumberOfLinesWhereOnlyElementOnALine

class LocWhitespaceVisitor(private val document: Document) : PsiElementVisitor() {
  var whitespaceLines: Int = 0

  override fun visitWhiteSpace(space: PsiWhiteSpace) {
    val text: String = space.text
    val containsNewLineCharacter: Boolean = text.contains("\n") || text.contains("\r")
    if (!containsNewLineCharacter) {
      return
    }
    whitespaceLines += space.getNumberOfLinesWhereOnlyElementOnALine(document, ignoreWhitespace = false)
  }

}