// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.childrenOfType
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.Constants.HCL_COUNT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_EPHEMERAL_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LOCAL_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PATH_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_SELF_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VAR_IDENTIFIER
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.documentation.psi.HclFakeElementPsiFactory
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.Icons
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hil.codeinsight.ScopeSelectInsertHandler
import org.intellij.terraform.opentofu.OpenTofuConstants.OpenTofuScopes
import org.intellij.terraform.opentofu.OpenTofuFileType
import java.util.*
import javax.swing.Icon

internal object TfCompletionUtil {
  val Scopes: Set<String> = setOf(
    HCL_DATASOURCE_IDENTIFIER,
    HCL_VAR_IDENTIFIER,
    HCL_SELF_IDENTIFIER,
    HCL_PATH_IDENTIFIER,
    HCL_COUNT_IDENTIFIER,
    HCL_TERRAFORM_IDENTIFIER,
    HCL_LOCAL_IDENTIFIER,
    HCL_MODULE_IDENTIFIER,
    HCL_EPHEMERAL_IDENTIFIER
  ) + OpenTofuScopes

  val GlobalScopes: SortedSet<String> = (setOf(
    HCL_VAR_IDENTIFIER,
    HCL_PATH_IDENTIFIER,
    HCL_DATASOURCE_IDENTIFIER,
    HCL_MODULE_IDENTIFIER,
    HCL_LOCAL_IDENTIFIER,
    HCL_EPHEMERAL_IDENTIFIER
  ) + OpenTofuScopes).toSortedSet()

  val RootBlockKeywords: Set<String> = TfTypeModel.RootBlocksMap.keys
  val RootBlockSorted: List<BlockType> = TfTypeModel.RootBlocks.sortedBy { it.literal }

  fun createPropertyOrBlockType(value: PropertyOrBlockType, lookupString: String? = null, psiElement: PsiElement? = null): LookupElementBuilder {
    val elementBuilder = when {
      psiElement == null -> create(value, lookupString ?: value.name)
      else -> create(value, lookupString ?: value.name).withPsiElement(psiElement)
    }
    return elementBuilder
      .withRenderer(TfLookupElementRenderer())
      .withInsertHandler(
        when (value) {
          is BlockType -> ResourceBlockNameInsertHandler(value)
          is PropertyType -> ResourcePropertyInsertHandler
          else -> null
        }
      )
  }

  fun createScopeLookup(value: String): LookupElementBuilder = LookupElementBuilder.create(value)
    .withInsertHandler(ScopeSelectInsertHandler)
    .withRenderer(object : LookupElementRenderer<LookupElement?>() {
      override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
        presentation.icon = AllIcons.Nodes.Tag
        presentation.itemText = element.lookupString
      }
    })

  fun createFunction(function: TfFunction, isTerragrunt: Boolean = false): LookupElementBuilder = create(function, function.presentableName)
    .withInsertHandler(if (function.arguments.isEmpty()) ParenthesesInsertHandler.NO_PARAMETERS else ParenthesesInsertHandler.WITH_PARAMETERS)
    .withTailText(function.getArgumentsAsText())
    .withTypeText(function.returnType.presentableText)
    .withIcon(if (isTerragrunt) TerraformIcons.Terragrunt else AllIcons.Nodes.Function)

  fun buildLookupForProviderBlock(provider: ProviderType, element: PsiElement): LookupElement =
    createProviderLookupElement(provider, element)
      .withInsertHandler(BlockSubNameInsertHandler(provider))
      .withPsiElement(element.project.service<HclFakeElementPsiFactory>().createFakeHclBlock(provider, element.containingFile.originalFile))

  fun buildLookupForRequiredProvider(provider: ProviderType, element: PsiElement): LookupElement =
    createProviderLookupElement(provider, element)
      .withInsertHandler { context, _ ->
        val project = context.project
        val providerProperty = TfElementGenerator(project).createRequiredProviderProperty(provider)
        val document = context.document
        document.replaceString(context.startOffset, context.tailOffset, providerProperty.text)
        PsiDocumentManager.getInstance(project).commitDocument(document)

        // It's safe to assume the current file contains a Terraform block with 'required_providers'
        val terraformBlock = context.file.childrenOfType<HCLBlock>().firstOrNull { TfPsiPatterns.TerraformRootBlock.accepts(it) }
                             ?: return@withInsertHandler
        CodeStyleManager.getInstance(project).reformatText(terraformBlock.containingFile, listOf(terraformBlock.textRange))
      }

  private fun createProviderLookupElement(provider: ProviderType, element: PsiElement): LookupElementBuilder =
    create(provider, provider.type)
      .withTailText(" ${provider.fullName}")
      .withTypeText(provider.version)
      .withIcon(getLookupIcon(element))

  fun getOriginalObject(parameters: CompletionParameters, obj: HCLObject): HCLObject {
    val originalObject = parameters.originalFile.findElementAt(obj.textRange.startOffset)?.parent
    return originalObject as? HCLObject ?: obj
  }

  fun getClearTextValue(element: PsiElement?): String? = when {
    element == null -> null
    element is HCLIdentifier -> element.id
    element is HCLStringLiteral -> element.value
    element.node?.elementType == HCLElementTypes.ID -> element.text
    HCLTokenTypes.STRING_LITERALS.contains(element.node?.elementType) -> HCLPsiUtil.stripQuotes(element.text)
    else -> null
  }

  fun getIncomplete(parameters: CompletionParameters): String? {
    val position = parameters.position
    val text = getClearTextValue(position) ?: position.text
    if (text == CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) return null
    return StringUtil.nullize(text.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, ""), true)
  }

  @NlsSafe
  internal fun buildProviderTypeText(provider: ProviderType): String =
    """${provider.fullName}${if (provider.version.isNotBlank()) " ${provider.version}" else ""}"""


  @NlsSafe
  internal fun buildResourceDisplayString(block: BlockType, providerLocalNames: Map<String, String>): String {
    return when (block) {
      is ResourceOrDataSourceType -> {
        val providerLocalName = providerLocalNames[block.provider.fullName] ?: return block.type
        return "${providerLocalName}_${TfTypeModel.getResourceName(block.type)}"
      }
      is ProviderType -> {
        val providerLocalName = providerLocalNames[block.fullName] ?: return block.type
        providerLocalName
      }
      else -> {
        block.literal
      }
    }
  }

  @NlsSafe
  internal fun buildResourceFullString(block: BlockType): String {
    return when (block) {
      is ResourceOrDataSourceType -> {
        return "${block.type} (${buildProviderTypeText(block.provider)})"
      }
      is ProviderType -> {
        return "${block.fullName} ${block.version}"
      }
      else -> {
        block.presentableText
      }
    }
  }

  fun getLookupIcon(element: PsiElement): Icon = when (element.containingFile.fileType) {
    is TerraformFileType -> TerraformIcons.Terraform
    is OpenTofuFileType -> TerraformIcons.Opentofu
    else -> Icons.FileTypes.HCL
  }
}