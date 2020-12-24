package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.jetbrains.lang.makefile.psi.impl.*

class MakefilePrerequisiteManipulator : AbstractElementManipulator<MakefilePrerequisiteImpl>() {
  override fun handleContentChange(element: MakefilePrerequisiteImpl, textRange: TextRange, newContent: String) = element.updateText(newContent)
}