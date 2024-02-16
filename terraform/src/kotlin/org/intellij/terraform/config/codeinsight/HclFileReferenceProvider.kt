// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.hcl.psi.HCLMethodCallExpression
import org.intellij.terraform.hcl.psi.HCLParameterList
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.template.psi.TftplFile

internal class HclFileReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      templateFileArgumentPattern,
      HclFileReferenceProvider()
    )
  }
}

private class HclFileReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    return arrayOf(HclFileReference(element))
  }
}

internal class HclFileReference(psiElement: PsiElement) : PsiPolyVariantReferenceBase<PsiElement>(psiElement), HighlightedReference {
  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val templateFileName = ElementManipulators.getValueText(element)
                             .substringAfterLast("/")
                             .takeIf(String::isNotBlank)
                           ?: return emptyArray()
    val searchScope = ModuleUtilCore.findModuleForFile(element.containingFile)?.moduleContentScope
                      ?: GlobalSearchScope.projectScope(element.project)
    return PsiElementResolveResult.createResults(
      FilenameIndex.getVirtualFilesByName(templateFileName, searchScope).mapNotNull(element.manager::findFile)
    )
  }

  override fun getVariants(): Array<Any> {
    val terraformModule = Module.getModule(element.containingFile.originalFile)
    return terraformModule
      .getAllTemplates()
      .map { templateFile -> createTemplateFileLookup(templateFile, terraformModule) }
      .toTypedArray()
  }

  private fun createTemplateFileLookup(templateFile: TftplFile, terraformModule: Module): LookupElement {
    return LookupElementBuilder.create(templateFile)
      .withBaseLookupString(templateFile.name)
      .withInsertHandler { context, _ ->
        val (locator, relativePath) = computeTemplatePathWithRespectToModuleRoot(templateFile, terraformModule)
        context.run { document.replaceString(startOffset, tailOffset, "$locator/$relativePath") }
      }
      .withIcon(TerraformIcons.Terraform)
  }

  private fun computeTemplatePathWithRespectToModuleRoot(templateFile: TftplFile, module: Module): TemplateCompletionData {
    return when {
      !module.item.isDirectory -> {
        TemplateCompletionData("", templateFile.name)
      }
      templateFile.parent == module.item -> {
        TemplateCompletionData(PATH_MODULE_LOCATOR, templateFile.name)
      }
      else -> {
        val modulePath = module.item.virtualFile.path
        val templatePath = templateFile.virtualFile.path
        val templateRelativePath = templatePath.removePrefix(modulePath).removePrefix("/")
        return TemplateCompletionData(PATH_MODULE_LOCATOR, templateRelativePath)
      }
    }
  }

  private data class TemplateCompletionData(val locator: String, val relativePath: String)
}

private val templateFileArgumentPattern: PsiElementPattern.Capture<HCLStringLiteral> =
  PlatformPatterns.psiElement(HCLStringLiteral::class.java)
    .withSuperParent(1, HCLParameterList::class.java)
    .withSuperParent(2, HCLMethodCallExpression::class.java)
    .with(object : PatternCondition<HCLStringLiteral>("isFirstTemplatefileFunctionParameter") {
      override fun accepts(literal: HCLStringLiteral, context: ProcessingContext?): Boolean {
        val parameterList = literal.parentOfType<HCLParameterList>() ?: return false
        val functionCall = literal.parentOfType<HCLMethodCallExpression>() ?: return false
        return parameterList.elements.firstOrNull() == literal
               && functionCall.method.textMatches(HCL_TEMPLATEFILE_FUNCTION)
      }
    })

private const val HCL_TEMPLATEFILE_FUNCTION = "templatefile"
private const val PATH_MODULE_LOCATOR = "\${path.module}"