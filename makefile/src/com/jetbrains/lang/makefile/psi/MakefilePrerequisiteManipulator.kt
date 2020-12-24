package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.impl.*

class MakefilePrerequisiteManipulator : AbstractElementManipulator<MakefilePrerequisiteImpl>() {
  override fun handleContentChange(element: MakefilePrerequisiteImpl, textRange: TextRange, newContent: String) = element.updateText(newContent)
}