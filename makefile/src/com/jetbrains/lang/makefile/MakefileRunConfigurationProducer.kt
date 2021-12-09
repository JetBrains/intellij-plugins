package com.jetbrains.lang.makefile

import com.intellij.execution.actions.*
import com.intellij.execution.configurations.*
import com.intellij.openapi.components.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.jetbrains.lang.makefile.psi.*
import java.io.*

class MakefileRunConfigurationProducer : LazyRunConfigurationProducer<MakefileRunConfiguration>() {
  override fun setupConfigurationFromContext(configuration: MakefileRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
    if (context.psiLocation?.containingFile !is MakefileFile) {
      return false
    }
    val macroManager = PathMacroManager.getInstance(context.project)
    val path = context.location?.virtualFile?.path
    configuration.filename = macroManager.collapsePath(path) ?: ""
    configuration.target = findTarget(context)?.name ?: ""

    if (configuration.target.isNotEmpty()) {
      configuration.name = configuration.target
    } else {
      configuration.name = File(path).name
    }

    return true
  }

  override fun isConfigurationFromContext(configuration: MakefileRunConfiguration, context: ConfigurationContext): Boolean {
    val macroManager = PathMacroManager.getInstance(context.project)
    return macroManager.expandPath(configuration.filename) == context.location?.virtualFile?.path &&
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

  override fun getConfigurationFactory(): ConfigurationFactory = MakefileRunConfigurationFactory(MakefileRunConfigurationType.instance)
}
