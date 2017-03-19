package name.kropp.intellij.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class MakefilePrerequisiteManipulator : AbstractElementManipulator<MakefilePrerequisite>() {
  override fun handleContentChange(element: MakefilePrerequisite, textRange: TextRange, newContent: String?) = element.updateText(newContent)
}