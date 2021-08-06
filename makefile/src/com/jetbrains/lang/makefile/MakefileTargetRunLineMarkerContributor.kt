package com.jetbrains.lang.makefile

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.jetbrains.lang.makefile.psi.MakefileTarget
import com.jetbrains.lang.makefile.psi.MakefileTargets
import com.jetbrains.lang.makefile.psi.MakefileTypes

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element.node.elementType == MakefileTypes.CHARS) {
      val target = element.parent
      if (target.firstChild != element) {
        return null
      }
      if (target is MakefileTarget) {
        val targets = target.parent as? MakefileTargets ?: return null
        val targetList = targets.targetList
        if (targetList.firstOrNull() == target &&
            targetList.any { !it.isSpecialTarget }) {
          return Info(MakefileTargetIcon, { "" }, *targetList.map(::MakefileRunTargetAction).toTypedArray())
        }
      }
    }
    return null
  }
}