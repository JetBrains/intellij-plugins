// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template.model

import com.intellij.icons.AllIcons
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.cache.CacheManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.UsageSearchContext.IN_CODE
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.terraform.template.HclFileReference
import com.intellij.terraform.template.TftplBundle
import com.intellij.terraform.template.psi.TftplFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLMethodCallExpression
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLSelectExpression
import org.intellij.terraform.hil.psi.ForCondition
import org.intellij.terraform.hil.psi.ILSelectFromScopeReferenceProvider
import org.intellij.terraform.hil.psi.ILTemplateForBlockExpression
import org.intellij.terraform.hil.psi.ILVariable
import org.intellij.terraform.opentofu.OpenTofuFileType
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

internal data class TftplVariable(
  @NonNls val name: String,
  val type: TftplVariableType,
  val navigationElement: PsiElement
)

internal enum class TftplVariableType(@Nls val presentableName: String, val icon: Icon) {
  LOOP_VARIABLE(TftplBundle.message("tftpl.loop.variable.title"), AllIcons.General.InlineRefreshHover),
  EXTERNAL_VARIABLE(TftplBundle.message("tftpl.external.variable.title"), AllIcons.Ide.ConfigFile)
}

@RequiresReadLock
internal fun collectAvailableVariables(currentNode: PsiElement): Sequence<TftplVariable> {
  return collectLocalVariables(currentNode) + collectCallSiteVariables(currentNode)
}

private fun collectLocalVariables(currentNode: PsiElement): Sequence<TftplVariable> {
  if (currentNode.containingFile !is TftplFile) return emptySequence()

  val amILoopVariable = isLoopCollection(currentNode)
  return currentNode
    .parentsOfType<ILTemplateForBlockExpression>(false)
    .filterIndexed { index, _ -> !(amILoopVariable && index == 0) }
    .flatMap { forExpression ->
      forExpression.getLoopVariables()
        .map { variable -> TftplVariable(variable.text.orEmpty(), TftplVariableType.LOOP_VARIABLE, variable) }
    }
}

private fun isLoopCollection(currentNode: PsiElement): Boolean {
  return currentNode.parent is ILVariable && currentNode.parent.parent is ForCondition
}

private fun collectCallSiteVariables(currentNode: PsiElement): Sequence<TftplVariable> {
  val templateFile = InjectedLanguageManager.getInstance(currentNode.project)
    .getTopLevelFile(currentNode)
    .originalFile

  return findTemplateUsage(templateFile)
    .flatMap { templateFunctionCall -> collectVariablesFromTemplateFunctionParameters(templateFunctionCall) }
    .map { variable -> TftplVariable(variable.name, TftplVariableType.EXTERNAL_VARIABLE, variable) }
}

internal fun findTemplateUsage(templateFile: PsiFile): Sequence<HCLMethodCallExpression> {
  val searchCandidates = getOrComputeSearchScope(templateFile.project).takeIf { it.isNotEmpty() } ?: return emptySequence()
  val narrowedSearchScope = GlobalSearchScope.filesScope(templateFile.project, searchCandidates)
  return ReferencesSearch.search(templateFile.originalFile, narrowedSearchScope, false)
    .filtering { reference -> reference is HclFileReference }
    .findAll()
    .asSequence()
    .mapNotNull { reference -> reference.element.parentOfType<HCLMethodCallExpression>() }
}

private fun getOrComputeSearchScope(project: Project): List<VirtualFile> {
  return CachedValuesManager.getManager(project).getCachedValue(project) {
    CachedValueProvider.Result.create(computeSearchScope(project), PsiModificationTracker.getInstance(project).forLanguage(TerraformLanguage))
  }
}

private fun computeSearchScope(project: Project): List<VirtualFile> {
  return CacheManager.getInstance(project)
    .getVirtualFilesWithWord(
      TEMPLATEFILE_FUNCTION_NAME,
      IN_CODE,
      GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), TerraformFileType, OpenTofuFileType),
      true)
    .toList()
}

private fun collectVariablesFromTemplateFunctionParameters(templateFunctionCall: HCLMethodCallExpression): Sequence<HCLProperty> {
  val providedParameters = templateFunctionCall.parameterList.elements
  if (providedParameters.size <= EXPECTED_VARIABLES_PARAMETER_INDEX) return emptySequence()

  return when (val templateVariables = providedParameters[EXPECTED_VARIABLES_PARAMETER_INDEX]) {
    is HCLSelectExpression -> collectVariablesFromReference(templateVariables)
    is HCLObject -> collectVariablesFromObject(templateVariables)
    else -> emptySequence()
  }
}

private fun collectVariablesFromReference(templateVariables: HCLSelectExpression): Sequence<HCLProperty> {
  val maybeVariableReference = templateVariables.field
                                 ?.reference
                                 as? ILSelectFromScopeReferenceProvider.VariableReference
                               ?: return emptySequence()
  val maybeVariableDeclaration = maybeVariableReference.resolve()
                                   ?.parentOfType<HCLBlock>(true)
                                 ?: return emptySequence()

  if (maybeVariableDeclaration.nameElements.firstOrNull()?.text != VARIABLE_ID) return emptySequence()
  val defaultPropertyValue = maybeVariableDeclaration.`object`
                               ?.findProperty(DEFAULT_VARIABLE_VALUE_FIELD)
                             ?: return emptySequence()
  val propertyValueAsObject = defaultPropertyValue.value
                                as? HCLObject
                              ?: return emptySequence()
  return collectVariablesFromObject(propertyValueAsObject)
}

private fun collectVariablesFromObject(templateVariables: HCLObject): Sequence<HCLProperty> {
  return templateVariables.propertyList.asSequence()
}

private const val EXPECTED_VARIABLES_PARAMETER_INDEX = 1
private const val VARIABLE_ID = "variable"
private const val DEFAULT_VARIABLE_VALUE_FIELD = "default"
private const val TEMPLATEFILE_FUNCTION_NAME = "templatefile"