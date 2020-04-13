package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.impl.*

class MakefileVariableUsageManipulator : AbstractElementManipulator<MakefileVariableUsageImpl>() {
  override fun handleContentChange(element: MakefileVariableUsageImpl, range: TextRange, newContent: String?): MakefileVariableUsageImpl? {
    val newNameNode = MakefileElementFactory.createChars(element.project, newContent ?: "")

    val second = element.firstChild.nextSibling.node
    val nameNode = if (second.elementType == MakefileTypes.OPEN_CURLY || second.elementType == MakefileTypes.OPEN_PAREN) {
      element.firstChild.nextSibling.nextSibling.node
    } else {
      second
    }

    element.node.replaceChild(nameNode, newNameNode)
    return element
  }
}