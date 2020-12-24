package com.jetbrains.lang.makefile.psi

import com.intellij.navigation.ItemPresentation
import com.jetbrains.lang.makefile.MakefileTargetIcon

class MakefileTargetPresentation(private val target: MakefileTarget) : ItemPresentation {
  override fun getIcon(b: Boolean) = MakefileTargetIcon
  override fun getPresentableText() = target.text
  override fun getLocationString() = "in " + target.containingFile?.virtualFile?.presentableName
}