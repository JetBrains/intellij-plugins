package name.kropp.intellij.makefile

import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileTargetRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element is MakefileTarget && element.name?.isEmpty() == false) {
      return Info(object : AnAction("make ${element.name}", "make ${element.name}", MakefileTargetIcon) {
        override fun actionPerformed(event: AnActionEvent) {
          val factory = ConfigurationTypeUtil.findConfigurationType(MakefileRunConfigurationType::class.java).configurationFactories.first()
          val configuration = factory.createTemplateConfiguration(event.project!!)
          configuration.filename = element.containingFile.virtualFile.path
          configuration.target = element.name!!


        }
      })
    }
    return null
  }
}