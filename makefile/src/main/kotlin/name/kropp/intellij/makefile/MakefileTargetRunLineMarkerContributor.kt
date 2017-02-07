package name.kropp.intellij.makefile

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileRule

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element is MakefileRule) {
      return Info(MakefileTargetIcon, { "" }, element.targets.filterNot { it.isSpecialTarget }.map(::MakefileRunTargetAction).toTypedArray())
    }
    return null
  }
}