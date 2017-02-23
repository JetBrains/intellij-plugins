package name.kropp.intellij.makefile

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import name.kropp.intellij.makefile.psi.MakefileTarget
import java.io.File

class MakefileRunConfigurationProducer : RunConfigurationProducer<MakefileRunConfiguration>(MakefileRunConfigurationType()) {
  public override fun setupConfigurationFromContext(configuration : MakefileRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>?): Boolean {
    if (context.psiLocation?.containingFile !is MakefileFile) {
      return false
    }
    configuration.filename = context.location?.virtualFile?.path ?: ""
    configuration.target = findTarget(context)?.name ?: ""

    if (!configuration.target.isNullOrEmpty()) {
      configuration.name = configuration.target
    } else {
      configuration.name = File(configuration.filename).name
    }

    return true
  }

  override fun isConfigurationFromContext(configuration: MakefileRunConfiguration, context: ConfigurationContext): Boolean {
    return configuration.filename == context.location?.virtualFile?.path &&
           configuration.target == findTarget(context)?.name
  }

  private fun findTarget(context: ConfigurationContext): MakefileTarget? {
    var element = context.psiLocation
    while (element != null && element !is MakefileTarget) {
      element = element.parent
    }
    val target = element as? MakefileTarget
    if (target?.isSpecialTarget == false) {
      return target
    }
    return null
  }
}