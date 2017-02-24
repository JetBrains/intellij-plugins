package name.kropp.intellij.makefile

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileRule

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element is MakefileRule) {
      val targets = element.targets.filterNot { it.isSpecialTarget }
      if (targets.any()) {
        return Info(MakefileTargetIcon, { "" }, targets.map(::MakefileRunTargetAction).toTypedArray())
      }
    }
    return null
  }
}