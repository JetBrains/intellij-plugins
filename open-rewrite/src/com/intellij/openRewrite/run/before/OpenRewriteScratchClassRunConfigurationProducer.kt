package com.intellij.openRewrite.run.before

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openRewrite.run.OpenRewriteWorkingDirectoryConfigurator
import com.intellij.openRewrite.run.isRecipe
import com.intellij.openRewrite.run.openRewriteRunConfigurationType
import com.intellij.openRewrite.run.splitConfigurationValue
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier

internal class OpenRewriteScratchClassRunConfigurationProducer : LazyRunConfigurationProducer<OpenRewriteRunConfiguration>() {
  override fun getConfigurationFactory(): ConfigurationFactory = openRewriteRunConfigurationType().configurationFactories[0]

  override fun setupConfigurationFromContext(
    configuration: OpenRewriteRunConfiguration,
    context: ConfigurationContext,
    sourceElement: Ref<PsiElement>,
  ): Boolean {
    val psiElement = context.psiLocation ?: return false
    if (psiElement !is PsiIdentifier) return false
    val parent = psiElement.parent ?: return false
    if (parent !is PsiClass) return false

    val psiFile = psiElement.containingFile ?: return false
    val virtualFile = psiFile.originalFile.virtualFile ?: return false
    if (!ScratchUtil.isScratch(virtualFile)) return false

    if (!isRecipe(parent)) return false

    configuration.activeRecipes = parent.qualifiedName ?: return false
    configuration.setGeneratedName()

    val module = ModuleUtilCore.findModuleForPsiElement(psiElement)
    configuration.workingDirectory = OpenRewriteWorkingDirectoryConfigurator(context.project, module).getWorkingDirectory()
    val task = OpenRewriteInstallBeforeRunTask()
    task.scratchFileUrl = FileUtil.toSystemDependentName(virtualFile.path)
    configuration.beforeRunTasks = listOf(task)

    return true
  }

  override fun isConfigurationFromContext(configuration: OpenRewriteRunConfiguration, context: ConfigurationContext): Boolean {
    val psiClass = context.psiLocation?.parent as? PsiClass ?: return false
    val virtualFile = psiClass.containingFile?.originalFile?.virtualFile ?: return false
    val fileUrl = FileUtil.toSystemDependentName(virtualFile.path)

    val activeRecipes = splitConfigurationValue(configuration.activeRecipes)
    if (!activeRecipes.contains(psiClass.qualifiedName)) return false

    return configuration.beforeRunTasks.any { it is OpenRewriteInstallBeforeRunTask && it.scratchFileUrl == fileUrl }
  }
}