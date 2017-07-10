package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import name.kropp.intellij.makefile.psi.impl.MakefilePrerequisiteImpl

class MakefilePrerequisiteManipulator : AbstractElementManipulator<MakefilePrerequisiteImpl>() {
  override fun handleContentChange(element: MakefilePrerequisiteImpl, textRange: TextRange, newContent: String?) = element.updateText(newContent)
}