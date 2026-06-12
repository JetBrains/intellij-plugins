package com.intellij.openRewrite.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openRewrite.RECIPE_TYPE_REGEX
import com.intellij.openRewrite.YAML_KEY_NAME
import com.intellij.openRewrite.YAML_KEY_TYPE
import com.intellij.openRewrite.isRecipe
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

internal class OpenRewriteRunConfigurationProducer : LazyRunConfigurationProducer<OpenRewriteRunConfiguration>() {
  override fun isDumbAware(): Boolean = true

  override fun getConfigurationFactory(): ConfigurationFactory = openRewriteRunConfigurationType().configurationFactories[0]

  override fun setupConfigurationFromContext(configuration: OpenRewriteRunConfiguration,
                                             context: ConfigurationContext,
                                             sourceElement: Ref<PsiElement>): Boolean {
    val psiElement = context.psiLocation ?: return false
    val psiFile = psiElement.containingFile ?: return false
    val virtualFile = psiFile.originalFile.virtualFile ?: return false
    if (!isRecipe(psiFile)) return false
    val recipeName = getRecipeName(psiElement) ?: return false

    configuration.activeRecipes = recipeName
    configuration.setGeneratedName()

    val module = ModuleUtilCore.findModuleForPsiElement(psiElement)
    configuration.workingDirectory = OpenRewriteWorkingDirectoryConfigurator(context.project, module).getWorkingDirectory()
    if (psiFile.name != RECIPE_FILE_NAME || configuration.workingDirectory != virtualFile.parent.path) {
      configuration.configLocation = virtualFile.path
    }
    return true
  }

  override fun isConfigurationFromContext(configuration: OpenRewriteRunConfiguration, context: ConfigurationContext): Boolean {
    val psiElement = context.psiLocation ?: return false
    val psiFile = psiElement.containingFile ?: return false
    val virtualFile = psiFile.originalFile.virtualFile ?: return false
    if (!isRecipe(psiFile)) return false
    val recipeName = getRecipeName(psiElement) ?: return false

    if (configuration.activeRecipes != recipeName) return false
    if (configuration.getExpandedConfigLocation() == virtualFile.path) return true
    if (psiFile.name == RECIPE_FILE_NAME) {
      return configuration.configLocation.isNullOrEmpty() &&
             configuration.getExpandedWorkingDirectory() == virtualFile.parent.path
    }
    return false
  }

  private fun getRecipeName(psiElement: PsiElement): String? {
    val yamlKeyValue = PsiTreeUtil.getParentOfType(psiElement, YAMLKeyValue::class.java) ?: return null
    if (yamlKeyValue.key != psiElement) return null

    if (yamlKeyValue.keyText != YAML_KEY_NAME) return null
    val parent = yamlKeyValue.parent as? YAMLMapping ?: return null
    if (parent.parent !is YAMLDocument) return null

    val type = (parent.getKeyValueByKey(YAML_KEY_TYPE)?.value as? YAMLScalar)?.textValue ?: return null
    if (!RECIPE_TYPE_REGEX.matches(type)) return null

    return (yamlKeyValue.value as? YAMLScalar)?.textValue?.takeIf { it.isNotBlank() }
  }
}