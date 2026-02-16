// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.components.service
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiElement
import com.intellij.util.Plow.Companion.toPlow
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.intellij.terraform.config.Constants.HCL_BACKEND_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_EPHEMERAL_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVISIONER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.codeinsight.BlockSubNameInsertHandler
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createProviderLookup
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getClearTextValue
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getLookupIcon
import org.intellij.terraform.config.documentation.psi.HclFakeElementPsiFactory
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.ProviderTier
import org.intellij.terraform.config.model.ResourceOrDataSourceType
import org.intellij.terraform.config.model.TfTypeModel
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns.FileOrBlock
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteral
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteralOrSimple
import org.intellij.terraform.hcl.patterns.HCLPatterns.WhiteSpace
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLPsiUtil.getPrevSiblingNonWhiteSpace
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.afterSiblingSkipping2
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEY_PROVIDER
import org.intellij.terraform.opentofu.model.encryptionKeyProviders
import org.intellij.terraform.opentofu.model.encryptionMethods
import org.intellij.terraform.stack.component.TfComponentFileType

internal object HclBlockTypeNameCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position
    doCompletion(position, parameters, result, Processor {
      result.addElement(it)
      !result.isStopped
    })
  }

  private fun doCompletion(
    position: PsiElement,
    parameters: CompletionParameters,
    result: CompletionResultSet,
    consumer: Processor<LookupElement>,
  ): Boolean {
    val parent = position.parent
    val obj = when {
      parent is HCLIdentifier -> parent
      parent is HCLStringLiteral -> parent
      // The next line for the case of two IDs (not Identifiers) nearby (start of block in an empty file)
      HCLTokenTypes.IDENTIFYING_LITERALS.contains(position.node.elementType) -> position
      else -> return true
    }
    val previousNonSpace = obj.getPrevSiblingNonWhiteSpace()
    val type = getClearTextValue(previousNonSpace) ?: return true

    val isTfComponent = position.containingFile.fileType == TfComponentFileType
    if (isTfComponent && type != HCL_PROVIDER_IDENTIFIER) return true

    val typeModel = TypeModelProvider.getModel(position)
    val localProviders = TfTypeModel.collectProviderLocalNames(position)
    val tiers = ProviderTier.PreferedProviders

    if (parameters.invocationCount == 1) {
      val message = HCLBundle.message("popup.advertisement.press.to.show.partner.community.providers",
                                      KeymapUtil.getFirstKeyboardShortcutText(IdeActions.ACTION_CODE_COMPLETION))
      result.addLookupAdvertisement(message)
    }
    return when (type) {
      HCL_RESOURCE_IDENTIFIER ->
        typeModel.allResources().toPlow()
          .filter { parameters.invocationCount > 1 || it.provider.tier in tiers || localProviders.containsValue(it.provider.fullName) }
          .map { buildResourceOrDataLookupElement(it, position) }
          .processWith(consumer)
      HCL_DATASOURCE_IDENTIFIER ->
        typeModel.allDataSources().toPlow()
          .filter { parameters.invocationCount > 1 || it.provider.tier in tiers || localProviders.containsValue(it.provider.fullName) }
          .map { buildResourceOrDataLookupElement(it, position) }
          .processWith(consumer)
      HCL_EPHEMERAL_IDENTIFIER ->
        typeModel.allEphemeralResources().toPlow()
          .filter { parameters.invocationCount > 1 || it.provider.tier in tiers || localProviders.containsValue(it.provider.fullName) }
          .map { buildResourceOrDataLookupElement(it, position) }
          .processWith(consumer)
      HCL_PROVIDER_IDENTIFIER ->
        typeModel.allProviders().toPlow()
          .filter { parameters.invocationCount > 1 || it.tier in tiers || localProviders.containsValue(it.fullName) }
          .map { createProviderLookup(it, position, !isTfComponent) }
          .processWith(consumer)
      HCL_PROVISIONER_IDENTIFIER ->
        typeModel.provisioners.toPlow()
          .map { buildLookupElement(it, it.type, it.description, position) }
          .processWith(consumer)
      HCL_BACKEND_IDENTIFIER ->
        typeModel.backends.toPlow()
          .map { buildLookupElement(it, it.type, it.description, position) }
          .processWith(consumer)
      TOFU_KEY_PROVIDER ->
        encryptionKeyProviders.values.toPlow()
          .map { buildLookupElement(it, it.type, it.description, position) }
          .processWith(consumer)
      TOFU_ENCRYPTION_METHOD_BLOCK ->
        encryptionMethods.values.toPlow()
          .map { buildLookupElement(it, it.type, it.description, position) }
          .processWith(consumer)
      else -> true
    }
  }

  // Lookup element builders
  private fun buildResourceOrDataLookupElement(it: ResourceOrDataSourceType, position: PsiElement): LookupElementBuilder {
    val providerLocalNamesReversed = TfTypeModel.collectProviderLocalNames(position).entries.associateBy({ it.value }) { it.key }
    return create(it, it.type)
      .withRenderer(object : LookupElementRenderer<LookupElement>() {
        override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
          presentation.setItemText(TfCompletionUtil.buildResourceDisplayString(it as BlockType, providerLocalNamesReversed))
          presentation.typeText = TfCompletionUtil.buildProviderTypeText(it.provider)
          presentation.isTypeGrayed = true
          presentation.icon = getLookupIcon(position)
        }
      })
      .withInsertHandler(BlockSubNameInsertHandler(it as BlockType))
      .withPsiElement(fakeFactory(position).createFakeHclBlock(it, position.containingFile.originalFile))
  }

  private fun buildLookupElement(it: BlockType, typeName: String, typeText: String?, position: PsiElement): LookupElementBuilder =
    create(typeName)
      .withTypeText(typeText, true)
      .withIcon(getLookupIcon(position))
      .withInsertHandler(BlockSubNameInsertHandler(it))
      .withPsiElement(fakeFactory(position).createFakeHclBlock(it.literal, typeName, position.containingFile.originalFile))

  private fun fakeFactory(position: PsiElement): HclFakeElementPsiFactory = position.project.service<HclFakeElementPsiFactory>()

  // Psi Patterns
  private fun topLevelBlockNamePattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> {
    return psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(FileOrBlock)
      .afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple)
  }

  private fun nestedBlockNamePattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> {
    return psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(psiElement().and(IdentifierOrStringLiteral).afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple))
      .withSuperParent(2, FileOrBlock)
  }

  fun registerTo(contributor: CompletionContributor, filePattern: PsiFilePattern.Capture<HCLFile>) {
    contributor.extend(CompletionType.BASIC, topLevelBlockNamePattern(filePattern), this)
    contributor.extend(CompletionType.BASIC, nestedBlockNamePattern(filePattern), this)
  }
}
