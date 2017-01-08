package name.kropp.intellij.makefile

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileTargetLine

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element is MakefileTargetLine) {
      val targets = element.targets.targetList.filter { it.name?.isEmpty() == false}
      if (targets.any()) {
        return Info(MakefileTargetIcon, { it -> "" }, targets.map(::MakefileRunTargetAction).toTypedArray())
      }
    }
    return null
  }
}