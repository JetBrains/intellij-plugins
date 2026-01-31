// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiFileSystemItemProcessor
import com.intellij.psi.util.parentOfType
import com.intellij.terraform.template.psi.TftplFile
import com.intellij.util.ProcessingContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.hcl.psi.HCLMethodCallExpression
import org.intellij.terraform.hcl.psi.HCLParameterList
import org.intellij.terraform.hcl.psi.HCLStringLiteral

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
      !module.moduleRoot.isDirectory -> {
        TemplateCompletionData("", templateFile.name)
      }
      templateFile.parent == module.moduleRoot -> {
        TemplateCompletionData(PATH_MODULE_LOCATOR, templateFile.name)
      }
      else -> {
        val modulePath = module.moduleRoot.virtualFile.path
        val templatePath = templateFile.virtualFile.path
        val templateRelativePath = templatePath.removePrefix(modulePath).removePrefix("/")
        return TemplateCompletionData(PATH_MODULE_LOCATOR, templateRelativePath)
      }
    }
  }

  private fun Module.getAllTemplates(): List<TftplFile> {
    val visitor = TemplateFilesVisitor()
    moduleRoot.processChildren(visitor)
    return visitor.getResults()
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

private class TemplateFilesVisitor : PsiFileSystemItemProcessor {
  private val myTemplates = mutableListOf<TftplFile>()

  fun getResults(): List<TftplFile> {
    return myTemplates.toList()
  }

  override fun execute(element: PsiFileSystemItem): Boolean {
    when {
      element.isDirectory -> element.processChildren(this)
      element is TftplFile -> myTemplates.add(element)
    }
    return true
  }

  override fun acceptItem(name: String, isDirectory: Boolean): Boolean {
    return when {
      isDirectory -> true
      FileUtilRt.getExtension(name) == "tftpl" -> true
      else -> false
    }
  }
}

private const val HCL_TEMPLATEFILE_FUNCTION = "templatefile"
private const val PATH_MODULE_LOCATOR = "\${path.module}"