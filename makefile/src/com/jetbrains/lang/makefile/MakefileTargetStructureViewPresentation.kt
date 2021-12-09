package com.jetbrains.lang.makefile

import com.intellij.navigation.ItemPresentation
import com.jetbrains.lang.makefile.psi.MakefileTarget

class MakefileTargetStructureViewPresentation(private val target: MakefileTarget) : ItemPresentation {
  override fun getIcon(b: Boolean) = MakefileTargetIcon
  override fun getPresentableText() = target.text
  override fun getLocationString() = ""
}