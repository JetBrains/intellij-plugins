package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class MakefileVariableUsageManipulator : AbstractElementManipulator<MakefileVariableUsage>() {
  override fun handleContentChange(element: MakefileVariableUsage, range: TextRange, newContent: String): MakefileVariableUsage {
    val newNameNode = MakefileElementFactory.createChars(element.project, newContent ?: "")

    val second = element.firstChild.nextSibling.node
    val nameNode = if (second.elementType == MakefileTypes.OPEN_CURLY || second.elementType == MakefileTypes.OPEN_PAREN) {
      element.firstChild.nextSibling.nextSibling.node
    }
    else {
      second
    }

    element.node.replaceChild(nameNode, newNameNode)
    return element
  }
}
