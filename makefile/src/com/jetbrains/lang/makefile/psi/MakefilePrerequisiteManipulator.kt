package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class MakefilePrerequisiteManipulator : AbstractElementManipulator<MakefilePrerequisite>() {
  override fun handleContentChange(element: MakefilePrerequisite, textRange: TextRange, newContent: String): MakefilePrerequisite =
    element.updateText(newContent)
}
