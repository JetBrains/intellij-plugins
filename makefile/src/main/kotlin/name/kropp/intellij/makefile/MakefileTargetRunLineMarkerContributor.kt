package name.kropp.intellij.makefile

import com.intellij.execution.lineMarker.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element.node.elementType == MakefileTypes.CHARS) {
      val target = element.parent
      if (target is MakefileTarget) {
        val targets = target.parent as? MakefileTargets ?: return null
        val targetList = targets.targetList
        if (targetList.firstOrNull() == target &&
            targetList.any { !it.isSpecialTarget }) {
          return Info(MakefileTargetIcon, { "" }, targetList.map(::MakefileRunTargetAction).toTypedArray())
        }
      }
    }
    return null
  }
}