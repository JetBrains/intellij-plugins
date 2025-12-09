// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createPropertyOrBlockType
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createProviderLookupElement
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.stack.component.TfComponentFile
import org.intellij.terraform.stack.component.TfComponentRequiredProviders

internal abstract class HclRequiredProvidersCompletion : CompletionProvider<CompletionParameters>() {
  abstract val filePattern: PsiFilePattern.Capture<HCLFile>
  abstract val requiredProvidersBlock: PsiElementPattern.Capture<HCLBlock>
  abstract val rootBlockPattern: PsiElementPattern.Capture<HCLBlock>

  // Psi patterns
  protected val requiredProvidersProperty: PsiElementPattern.Capture<HCLProperty> =
    PlatformPatterns.psiElement(HCLProperty::class.java)
      .withSuperParent(2, requiredProvidersBlock)

  protected val requiredProviderIdentifier: PsiElementPattern.Capture<PsiElement> =
    PlatformPatterns.psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(HCLPatterns.Object)
      .withSuperParent(2, requiredProvidersBlock)

  protected val identifierOfRequiredProviderProperty: PsiElementPattern.Capture<PsiElement> =
    PlatformPatterns.psiElement().withElementType(HCLElementTypes.ID)
      .inFile(filePattern)
      .inside(HCLPatterns.Object.withParent(requiredProvidersProperty))

  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val element = parameters.originalPosition ?: return
    val superParent = element.parent?.parent

    // Completion of all types of providers
    if (requiredProvidersBlock.accepts(superParent)) {
      val prefix = result.prefixMatcher.prefix
      val providers = TypeModelProvider.getModel(element).allProviders()
        .filter { it.type.startsWith(prefix) }
        .map { buildLookupForRequiredProvider(it, element) }
        .toList()

      val sorter = CompletionSorter.emptySorter().weigh(object : LookupElementWeigher("tf.providers.weigher") {
        override fun weigh(element: LookupElement): Int {
          val providerType = element.`object` as? ProviderType
          return if (providerType?.tier in ProviderTier.PreferedProviders) 0 else 1
        }
      })
      result.withRelevanceSorter(sorter).addAllElements(providers)
    }
    // Completion properties in the required provider type
    else {
      val parent = element.parentOfType<HCLObject>() ?: return
      if (!requiredProvidersProperty.accepts(parent.parent))
        return

      val properties = listOf("source", "version").map { PropertyType(it, Types.String) }.filter { parent.findProperty(it.name) == null }
      result.addAllElements(properties.map { createPropertyOrBlockType(it) })
    }
  }

  fun registerTo(contributor: CompletionContributor) {
    contributor.extend(CompletionType.BASIC, requiredProviderIdentifier, this)
    contributor.extend(CompletionType.BASIC, identifierOfRequiredProviderProperty, this)
  }

  private fun buildLookupForRequiredProvider(provider: ProviderType, element: PsiElement): LookupElement =
    createProviderLookupElement(provider, element).withInsertHandler { context, _ ->
      val project = context.project
      val providerProperty = TfElementGenerator(project).createRequiredProviderProperty(provider)
      val document = context.document
      document.replaceString(context.startOffset, context.tailOffset, providerProperty.text)
      PsiDocumentManager.getInstance(project).commitDocument(document)

      // It's safe to assume the current file contains a Terraform block with 'required_providers' for .tf file
      val rootBlock = context.file.childrenOfType<HCLBlock>().firstOrNull { rootBlockPattern.accepts(it) } ?: return@withInsertHandler
      CodeStyleManager.getInstance(project).reformatText(rootBlock.containingFile, listOf(rootBlock.textRange))
    }
}

internal object TfRequiredProvidersCompletion : HclRequiredProvidersCompletion() {
  override val filePattern: PsiFilePattern.Capture<HCLFile>
    get() = TfPsiPatterns.TerraformConfigFile

  override val requiredProvidersBlock: PsiElementPattern.Capture<HCLBlock>
    get() = TfPsiPatterns.TfRequiredProvidersBlock

  override val rootBlockPattern: PsiElementPattern.Capture<HCLBlock>
    get() = TfPsiPatterns.TerraformRootBlock
}

internal object TfComponentRequiredProvidersCompletion : HclRequiredProvidersCompletion() {
  override val filePattern: PsiFilePattern.Capture<HCLFile>
    get() = TfComponentFile

  override val requiredProvidersBlock: PsiElementPattern.Capture<HCLBlock>
    get() = TfComponentRequiredProviders

  override val rootBlockPattern: PsiElementPattern.Capture<HCLBlock>
    get() = TfComponentRequiredProviders
}