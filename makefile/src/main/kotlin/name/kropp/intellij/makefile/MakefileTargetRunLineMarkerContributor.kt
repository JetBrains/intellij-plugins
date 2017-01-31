package name.kropp.intellij.makefile

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement) = (element as? MakefileTarget)?.let { Info(MakefileRunTargetAction(it)) }
}