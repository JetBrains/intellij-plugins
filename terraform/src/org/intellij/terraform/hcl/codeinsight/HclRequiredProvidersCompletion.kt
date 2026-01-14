// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_SOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VERSION_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createPropertyOrBlockType
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createProviderLookupElement
import org.intellij.terraform.config.model.PropertyType
import org.intellij.terraform.config.model.ProviderTier
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.isTerraformFile
import org.intellij.terraform.stack.component.TfComponentPsiPatterns
import org.intellij.terraform.stack.component.isTfComponentPsiFile

internal object HclRequiredProvidersCompletion : CompletionProvider<CompletionParameters>() {

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val element = parameters.originalPosition ?: return
    val superParent = element.parent?.parent

    // Completion of all types of providers
    val requiredProviderBlock = getRequiredProviderBlock(element.containingFile) ?: return
    if (requiredProviderBlock.accepts(superParent)) {
      val prefix = result.prefixMatcher.prefix
      val providers = TypeModelProvider.getModel(element).allProviders()
        .filter { it.type.startsWith(prefix) }
        .map { buildLookupForRequiredProvider(it, element) }
        .toList()

      result.withRelevanceSorter(ProviderSorter).addAllElements(providers)
    }
    // Completion properties in the required provider type
    else {
      val parent = element.parentOfType<HCLObject>() ?: return
      if (!providerPropertyPattern(requiredProviderBlock).accepts(parent.parent))
        return

      val properties = RequiredProviderProperties.filter { parent.findProperty(it.name) == null }
      result.addAllElements(properties.map { createPropertyOrBlockType(it) })
    }
  }

  fun registerTo(contributor: CompletionContributor, requiredProviderBlock: PsiElementPattern.Capture<HCLBlock>) {
    contributor.extend(CompletionType.BASIC, providerIdentifierPattern(requiredProviderBlock), this)
    contributor.extend(CompletionType.BASIC, identifierOfProviderPropertyPattern(requiredProviderBlock), this)
  }

  // Psi patterns
  private fun providerIdentifierPattern(requiredProviderBlock: PsiElementPattern.Capture<HCLBlock>): PsiElementPattern.Capture<PsiElement> =
    PlatformPatterns.psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .withParent(HCLPatterns.Object)
      .withSuperParent(2, requiredProviderBlock)

  private fun identifierOfProviderPropertyPattern(requiredProviderBlock: PsiElementPattern.Capture<HCLBlock>): PsiElementPattern.Capture<PsiElement> =
    PlatformPatterns.psiElement().withElementType(HCLElementTypes.ID)
      .inside(HCLPatterns.Object.withParent(providerPropertyPattern(requiredProviderBlock)))

  private fun providerPropertyPattern(requiredProviderBlock: PsiElementPattern.Capture<HCLBlock>): PsiElementPattern.Capture<HCLProperty> =
    PlatformPatterns.psiElement(HCLProperty::class.java)
      .withSuperParent(2, requiredProviderBlock)

  private fun buildLookupForRequiredProvider(provider: ProviderType, element: PsiElement): LookupElement =
    createProviderLookupElement(provider, element).withInsertHandler { context, _ ->
      val project = context.project
      val providerProperty = TfElementGenerator(project).createRequiredProviderProperty(provider)
      val document = context.document
      document.replaceString(context.startOffset, context.tailOffset, providerProperty.text)
      PsiDocumentManager.getInstance(project).commitDocument(document)

      // It's safe to assume the current file contains a Terraform block with 'required_providers' for .tf file
      val file = context.file
      val rootBlockPattern = getRootBlock(file) ?: return@withInsertHandler
      val rootBlock = file.childrenOfType<HCLBlock>().firstOrNull { rootBlockPattern.accepts(it) } ?: return@withInsertHandler
      CodeStyleManager.getInstance(project).reformatText(rootBlock.containingFile, listOf(rootBlock.textRange))
    }

  private fun getRequiredProviderBlock(psiFile: PsiFile): PsiElementPattern.Capture<HCLBlock>? = when {
    isTerraformFile(psiFile) -> TfPsiPatterns.TfRequiredProvidersBlock
    isTfComponentPsiFile(psiFile) -> TfComponentPsiPatterns.TfComponentRequiredProviders
    else -> null
  }

  private fun getRootBlock(psiFile: PsiFile): PsiElementPattern.Capture<HCLBlock>? = when {
    isTerraformFile(psiFile) -> TfPsiPatterns.TerraformRootBlock
    isTfComponentPsiFile(psiFile) -> TfComponentPsiPatterns.TfComponentRequiredProviders
    else -> null
  }
}

private val ProviderSorter: CompletionSorter = CompletionSorter.emptySorter().weigh(
  object : LookupElementWeigher("tf.providers.weigher") {
    override fun weigh(element: LookupElement): Int {
      val providerType = element.`object` as? ProviderType
      return if (providerType?.tier in ProviderTier.PreferedProviders) 0 else 1
    }
  }
)

private val RequiredProviderProperties = listOf(
  PropertyType(HCL_SOURCE_IDENTIFIER, Types.String),
  PropertyType(HCL_VERSION_IDENTIFIER, Types.String)
)