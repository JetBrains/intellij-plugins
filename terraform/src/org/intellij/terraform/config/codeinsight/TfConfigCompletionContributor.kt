// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.patterns.PlatformPatterns.not
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.Plow.Companion.toPlow
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.Constants.HCL_BACKEND_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVISIONER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockSorted
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createPropertyOrBlockType
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.dumpPsiFileModel
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getClearTextValue
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getIncomplete
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getLookupIcon
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.getOriginalObject
import org.intellij.terraform.config.documentation.psi.FakeHCLElementPsiFactory
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.DependsOnPattern
import org.intellij.terraform.config.patterns.TfPsiPatterns.TerraformConfigFile
import org.intellij.terraform.config.patterns.TfPsiPatterns.TerraformVariablesFile
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.codeinsight.HCLCompletionContributor
import org.intellij.terraform.hcl.patterns.HCLPatterns.Array
import org.intellij.terraform.hcl.patterns.HCLPatterns.AtLeastOneEOL
import org.intellij.terraform.hcl.patterns.HCLPatterns.Block
import org.intellij.terraform.hcl.patterns.HCLPatterns.File
import org.intellij.terraform.hcl.patterns.HCLPatterns.FileOrBlock
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteral
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteralOrSimple
import org.intellij.terraform.hcl.patterns.HCLPatterns.Nothing
import org.intellij.terraform.hcl.patterns.HCLPatterns.Object
import org.intellij.terraform.hcl.patterns.HCLPatterns.Property
import org.intellij.terraform.hcl.patterns.HCLPatterns.PropertyOrBlock
import org.intellij.terraform.hcl.patterns.HCLPatterns.WhiteSpace
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.HCLPsiUtil.getPrevSiblingNonWhiteSpace
import org.intellij.terraform.hil.HILFileType
import org.intellij.terraform.hil.codeinsight.ReferenceCompletionHelper.findByFQNRef
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEY_PROVIDER
import org.intellij.terraform.opentofu.model.encryptionKeyProviders
import org.intellij.terraform.opentofu.model.encryptionMethods

class TfConfigCompletionContributor : HCLCompletionContributor() {
  init {
    // Block first word
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(File)
      .andNot(psiElement().afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple)),
           BlockKeywordCompletionProvider)
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Block)
      .withSuperParent(3, File)
      .withParent(not(psiElement().and(IdentifierOrStringLiteral).afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple))),
           BlockKeywordCompletionProvider)

    // Block type or name
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(FileOrBlock)
      .afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple), BlockTypeOrNameCompletionProvider)
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(psiElement().and(IdentifierOrStringLiteral).afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple))
      .withSuperParent(2, FileOrBlock), BlockTypeOrNameCompletionProvider)

    //region InBlock Property key
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(Object)
      .withSuperParent(2, Block), BlockPropertiesCompletionProvider)
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Property)
      .withSuperParent(3, Object)
      .withSuperParent(4, Block), BlockPropertiesCompletionProvider)
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .and(psiElement().insideStarting(Block))
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Block)
      .withSuperParent(3, Object)
      .withSuperParent(4, Block), BlockPropertiesCompletionProvider)
    // Leftmost identifier of block could be start of new property in case of eol between it and the next identifier
    //```
    //resource "X" "Y" {
    //  count<caret>
    //  provider {}
    //}
    //```
    extend(CompletionType.BASIC, psiElement(HCLElementTypes.ID)
      .inFile(TerraformConfigFile)
      .withParent(psiElement(HCLIdentifier::class.java).beforeLeafSkipping(Nothing, AtLeastOneEOL))
      .withSuperParent(2, Block)
      .withSuperParent(3, Object)
      .withSuperParent(4, Block), BlockPropertiesCompletionProvider)
    //endregion

    //region InBlock Property value
    extend(null, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Property)
      .withSuperParent(3, Object)
      .withSuperParent(4, Block), PropertyValueCompletionProvider)
    // depends_on completion
    extend(null, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Array)
      .withSuperParent(3, Property)
      .withSuperParent(4, Object)
      .withSuperParent(5, Block), PropertyValueCompletionProvider)
    //endregion

    //region InBlock PropertyWithObjectValue Key
    // property = { <caret> }
    // property = { "<caret>" }
    // property { <caret> }
    // property { "<caret>" }
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(Object)
      .withSuperParent(2, PropertyOrBlock)
      .withSuperParent(3, Object), PropertyObjectKeyCompletionProvider)
    // property = { <caret>a="" }
    // property = { "<caret>a"="" }
    // property { <caret>="" }
    // property { "<caret>"="" }
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Property)
      .withSuperParent(3, Object)
      .withSuperParent(4, PropertyOrBlock)
      .withSuperParent(5, Object)
      .withSuperParent(6, Block), PropertyObjectKeyCompletionProvider)
    // property = { a=""  <caret> }
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformConfigFile)
      .withParent(psiElement(PsiErrorElement::class.java))
      .withSuperParent(2, Object)
      .withSuperParent(3, PropertyOrBlock)
      .withSuperParent(4, Object)
      .withSuperParent(5, Block), PropertyObjectKeyCompletionProvider)
    //endregion

    //region .tfvars
    // Variables in .tvars files
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformVariablesFile)
      .andOr(
        psiElement()
          .withParent(File),
        psiElement()
          .withParent(IdentifierOrStringLiteral)
          .withSuperParent(2, Property)
          .withSuperParent(3, File)
      ), VariableNameTFVARSCompletionProvider)
    extend(CompletionType.BASIC, psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(TerraformVariablesFile)
      .andOr(
        psiElement()
          .withSuperParent(1, IdentifierOrStringLiteral)
          .withSuperParent(2, Property)
          .withSuperParent(3, Object)
          .withSuperParent(4, Property)
          .withSuperParent(5, File),
        psiElement()
          .withSuperParent(1, Object)
          .withSuperParent(2, Property)
          .withSuperParent(3, File)
      ), MappedVariableTFVARSCompletionProvider)
    //endregion
  }

  private object PreferRequiredProperty : LookupElementWeigher("hcl.required.property") {
    override fun weigh(element: LookupElement): Comparable<Nothing> {
      val obj = element.`object`
      if (obj is PropertyOrBlockType) {
        if (obj.required) return 0
        else return 1
      }
      return 10
    }
  }

  abstract class TfCompletionProvider : CompletionProvider<CompletionParameters>() {

    protected fun addResultsWithCustomSorter(result: CompletionResultSet,
                                             toAdd: Collection<LookupElementBuilder>) {
      if (toAdd.isEmpty()) return
      result.withRelevanceSorter(
        CompletionSorter.emptySorter().weigh(PreferRequiredProperty)
      ).addAllElements(toAdd)
    }

    protected fun needCompletionForBlock(block: HCLBlock, property: HCLProperty, type: String?): Boolean {
      val isRootBlock = TfPsiPatterns.RootBlock.accepts(block)

      // Since 'depends_on', 'provider' does not allow interpolations, don't add anything
      if (DependsOnPattern.accepts(property)) return true
      if (property.name == "provider" && (type == "resource" || type == "data") && isRootBlock) return true
      // Same for 'providers' binding in 'module'
      if (property.name == "providers" && type == "module" && isRootBlock) return true

      val hint = (TfModelHelper.getBlockProperties(block)[property.name] as? PropertyType)?.hint
      return hint is SimpleValueHint || hint is ReferenceHint
    }
  }

  private object BlockKeywordCompletionProvider : TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent
      val leftNWS = position.getPrevSiblingNonWhiteSpace()
      LOG.debug { "TF.BlockKeywordCompletionProvider{position=$position, parent=$parent, left=${position.prevSibling}, lnws=$leftNWS}" }
      assert(getClearTextValue(leftNWS) == null, dumpPsiFileModel(position))
      result.addAllElements(RootBlockSorted.map { createPropertyOrBlockType(it) })
    }
  }

  object BlockTypeOrNameCompletionProvider : TfCompletionProvider() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      doCompletion(position, parameters, result, Processor {
        result.addElement(it)
        !result.isStopped
      })
    }


    private fun doCompletion(position: PsiElement, parameters: CompletionParameters, result: CompletionResultSet, consumer: Processor<LookupElement>): Boolean {
      val parent = position.parent
      LOG.debug { "TF.BlockTypeOrNameCompletionProvider{position=$position, parent=$parent}" }
      val obj = when {
        parent is HCLIdentifier -> parent
        parent is HCLStringLiteral -> parent
        // Next line for the case of two IDs (not Identifiers) nearby (start of block in empty file)
        HCLTokenTypes.IDENTIFYING_LITERALS.contains(position.node.elementType) -> position
        else -> return true
      }
      val leftNWS = obj.getPrevSiblingNonWhiteSpace()
      LOG.debug { "TF.BlockTypeOrNameCompletionProvider{position=$position, parent=$parent, obj=$obj, lnws=$leftNWS}" }
      val type = getClearTextValue(leftNWS) ?: return true
      val typeModel = TypeModelProvider.getModel(position)
      val localProviders = TypeModel.collectProviderLocalNames(position)
      val tiers = setOf(ProviderTier.TIER_BUILTIN, ProviderTier.TIER_OFFICIAL, ProviderTier.TIER_LOCAL)
      if (parameters.invocationCount == 1) {
        val message = HCLBundle.message("popup.advertisement.press.to.show.partner.community.providers", KeymapUtil.getFirstKeyboardShortcutText(IdeActions.ACTION_CODE_COMPLETION))
        result.addLookupAdvertisement(message)
      }
      return when (type) {
        HCL_RESOURCE_IDENTIFIER -> {
          typeModel.allResources().toPlow()
            .filter { parameters.invocationCount > 1 || it.provider.tier in tiers || localProviders.containsValue(it.provider.fullName) }
            .map { buildResourceOrDataLookupElement(it, position) }
            .processWith(consumer)
        }
        HCL_DATASOURCE_IDENTIFIER -> {
          typeModel.allDatasources().toPlow()
            .filter { parameters.invocationCount > 1 || it.provider.tier in tiers || localProviders.containsValue(it.provider.fullName) }
            .map { buildResourceOrDataLookupElement(it, position) }
            .processWith(consumer)
        }
        HCL_PROVIDER_IDENTIFIER -> {
          typeModel.allProviders().toPlow()
            .filter { parameters.invocationCount > 1 || it.tier in tiers || localProviders.containsValue(it.fullName)}
            .map { buildProviderLookupElement(it, position) }
            .processWith(consumer)
        }
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

    private fun buildResourceOrDataLookupElement(it: ResourceOrDataSourceType, position: PsiElement): LookupElementBuilder {
      val providerLocalNamesReversed = TypeModel.collectProviderLocalNames(position).entries.associateBy({ it.value }) { it.key }
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
        .withPsiElement(position.project.service<FakeHCLElementPsiFactory>().createFakeHCLBlock(it, position.containingFile.originalFile))
    }

    private fun buildProviderLookupElement(it: ProviderType, position: PsiElement): LookupElementBuilder {
      return create(it, it.type)
        .withRenderer(object : LookupElementRenderer<LookupElement>() {
          override fun renderElement(element: LookupElement, presentation: LookupElementPresentation) {
            presentation.setItemText(it.type)
            presentation.tailText = " (${it.namespace})"
            presentation.typeText = it.version
            presentation.isTypeGrayed = true
            presentation.icon = getLookupIcon(position)
          }
        })
        .withInsertHandler(BlockSubNameInsertHandler(it))
        .withPsiElement(position.project.service<FakeHCLElementPsiFactory>().createFakeHCLBlock(it, position.containingFile.originalFile))
    }

    private fun buildLookupElement(it: BlockType, typeName: String, typeText: String?, position: PsiElement): LookupElementBuilder = create(typeName)
      .withTypeText(typeText, true)
      .withIcon(getLookupIcon(position))
      .withInsertHandler(BlockSubNameInsertHandler(it))
      .withPsiElement(position.project.service<FakeHCLElementPsiFactory>().createFakeHCLBlock(it.literal, typeName, position.containingFile.originalFile))
  }

  private object BlockPropertiesCompletionProvider : TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      var parent: PsiElement? = position.parent
      var right: Type? = null
      var isProperty = false
      var isBlock = false
      val original = parameters.originalPosition ?: return
      val original_parent = original.parent
      if (HCLElementTypes.L_CURLY === original.node.elementType && original_parent is HCLObject) {
        LOG.debug { "Origin is '{' inside Object, O.P.P = ${original_parent.parent}" }
        if (original_parent.parent is HCLBlock) return
      }
      if (parent is HCLIdentifier || parent is HCLStringLiteral) {
        val pob = parent.parent // Property or Block
        if (pob is HCLProperty) {
          val value = pob.value as? HCLValue
          if (value === parent) {
            val valueBlock = PsiTreeUtil.getParentOfType(pob, HCLBlock::class.java) ?: return
            val property = TfModelHelper.getBlockProperties(valueBlock)[pob.name] as? PropertyType
            val defaultsOfProperty = property?.type?.suggestedValues
            defaultsOfProperty?.map { create(it) }?.let { result.addAllElements(it) }
            return
          }
          right = value.getType()
          if (right == Types.String && value is PsiLanguageInjectionHost) {
            // Check for Injection
            InjectedLanguageManager.getInstance(pob.project).enumerate(value) { injectedPsi, _ ->
              if (injectedPsi.fileType == HILFileType) {
                right = Types.StringWithInjection
                val root = injectedPsi.firstChild
                if (root == injectedPsi.lastChild && root is ILExpression) {
                  val type = root.getType()
                  if (type != null && type != Types.Any) {
                    right = type
                  }
                }
              }
            }
          }
          isProperty = true
        }
        else if (pob is HCLBlock) {
          isBlock = true
          if (pob.nameElements.firstOrNull() == parent) {
            if (parent.nextSibling is PsiWhiteSpace && parent.nextSibling.text.contains("\n")) {
              isBlock = false
              parent = parent.parent.parent
            }
          }
        }
        if (isBlock || isProperty) {
          parent = pob?.parent // Object
        }
        LOG.debug { "TF.BlockPropertiesCompletionProvider{position=$position, parent=$parent, original=$original, right=$right, isBlock=$isBlock, isProperty=$isProperty}" }
      }
      else {
        LOG.debug { "TF.BlockPropertiesCompletionProvider{position=$position, parent=$parent, original=$original, no right part}" }
      }
      val hclObject: HCLObject = parent as? HCLObject ?: return
      val use = getOriginalObject(parameters, hclObject)
      val block = use.parent
      if (block is HCLBlock) {
        val props = TfModelHelper.getBlockProperties(block)
        doAddCompletion(isBlock, isProperty, use, result, right, parameters, props)
      }
    }

    private fun doAddCompletion(
      isBlock: Boolean,
      isProperty: Boolean,
      parent: HCLObject,
      result: CompletionResultSet,
      right: Type?,
      parameters: CompletionParameters,
      properties: Map<String, PropertyOrBlockType>,
    ) {
      if (properties.isEmpty()) return
      val incomplete = getIncomplete(parameters)
      if (incomplete != null) {
        LOG.debug { "Including properties which contains incomplete result: $incomplete" }
      }
      val fakeHCLPsiFactory = parent.project.service<FakeHCLElementPsiFactory>()
      addResultsWithCustomSorter(result, properties.values
        .asSequence()
        .filter { it.name != Constants.HAS_DYNAMIC_ATTRIBUTES }
        .filter { isRightOfPropertyWithCompatibleType(isProperty, it, right) || (isBlock && it is BlockType) || (!isProperty && !isBlock) }
        // TODO: Filter should be based on 'max-count' model property (?)
        .filter {
          (it is PropertyType &&
           (parent.findProperty(it.name) == null || (incomplete != null && it.name.contains(incomplete)))
          ) || (it is BlockType)
        }
        .filter { it.configurable }
        .map { property ->
          when {
            (property is BaseModelType && property.description_kind != null) -> {
              val hclBlock = parent.parentOfType<HCLBlock>()
              val hclProperty = hclBlock?.let { fakeHCLPsiFactory.createFakeHCLProperty(hclBlock, property) }
              createPropertyOrBlockType(property, property.name, hclProperty)
            }
            else -> {
              createPropertyOrBlockType(property, property.name, fakeHCLPsiFactory.emptyHCLBlock)
            }
          }
        }
        .toList())
    }

    private fun isRightOfPropertyWithCompatibleType(isProperty: Boolean, it: PropertyOrBlockType, right: Type?): Boolean {
      if (!isProperty) return false
      if (it !is PropertyType) return false
      if (right == Types.StringWithInjection) {
        // StringWithInjection means TypeCachedValueProvider was unable to understand type of interpolation
        return true
      }
      return it.type == right
    }
  }

  private object PropertyValueCompletionProvider : TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent
      val inArray = (parent.parent is HCLArray)
      LOG.debug { "TF.PropertyValueCompletionProvider{position=$position, parent=$parent}" }
      val property = PsiTreeUtil.getParentOfType(position, HCLProperty::class.java) ?: return
      val block = PsiTreeUtil.getParentOfType(property, HCLBlock::class.java) ?: return

      val type = block.getNameElementUnquoted(0)
      // TODO: Replace with 'ReferenceHint'
      if (property.name == HCL_PROVIDER_IDENTIFIER && (type == HCL_RESOURCE_IDENTIFIER || type == HCL_DATASOURCE_IDENTIFIER)) {
        val providers = property.getTerraformModule().getDefinedProviders()
        result.addAllElements(providers.map { create(it.second).withInsertHandler(QuoteInsertHandler) })
        return
      }
      if (DependsOnPattern.accepts(property) && inArray) {
        val resources = property.getTerraformModule().getDeclaredResources()
          .map { "${it.getNameElementUnquoted(1)}.${it.name}" }
        val datas = property.getTerraformModule().getDeclaredDataSources()
          .map { "data.${it.getNameElementUnquoted(1)}.${it.name}" }
        val modules = property.getTerraformModule().getDefinedModules()
          .map { "module.${it.name}" }
        val variables = property.getTerraformModule().getAllVariables()
          .map { "var.${it.name}" }

        val current = when (type) {
          HCL_DATASOURCE_IDENTIFIER -> "data.${block.getNameElementUnquoted(1)}.${block.name}"
          HCL_RESOURCE_IDENTIFIER -> "${block.getNameElementUnquoted(1)}.${block.name}"
          HCL_MODULE_IDENTIFIER -> "module.${block.name}"
          HCL_VARIABLE_IDENTIFIER -> "var.${block.name}"
          else -> "unsupported"
        }

        result.addAllElements(resources.asSequence().plus(datas).plus(modules).plus(variables).minus(current).map {
          create(it).withInsertHandler(QuoteInsertHandler)
        }.toList())
        return
      }
      val prop = TfModelHelper.getBlockProperties(block)[property.name] as? PropertyType
      val hint = prop?.hint ?: return
      if (hint is SimpleValueHint) {
        result.addAllElements(hint.hint.map { create(it).withInsertHandler(QuoteInsertHandler) })
        return
      }
      if (hint is ReferenceHint) {
        val module = property.getTerraformModule()
        hint.hint
          .mapNotNull { findByFQNRef(it, module) }
          .flatten()
          .mapNotNull {
            return@mapNotNull when (it) {
              is String -> "" + '$' + "{$it}"
              else -> null
            }
          }
          .forEach { result.addElement(create(it).withInsertHandler(QuoteInsertHandler)) }
        return
      }
      // TODO: Support other hint types
    }
  }

  private object VariableNameTFVARSCompletionProvider : TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent
      LOG.debug { "TF.VariableNameTFVARSCompletionProvider{position=$position, parent=$parent}" }
      val module: Module
      when (parent) {
        is HCLFile -> {
          module = parent.getTerraformModule()
        }
        is HCLElement -> {
          val pp = parent.parent as? HCLProperty ?: return
          if (parent !== pp.nameIdentifier) return
          module = parent.getTerraformModule()
        }
        else -> return
      }
      val variables = module.getAllVariables()
      result.addAllElements(variables.map { create(it.name).withInsertHandler(ResourcePropertyInsertHandler) })
    }
  }

  private object MappedVariableTFVARSCompletionProvider : TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent
      LOG.debug { "TF.MappedVariableTFVARSCompletionProvider{position=$position, parent=$parent}" }
      val varProperty: HCLProperty
      if (parent is HCLObject) {
        val pp = parent.parent
        if (pp is HCLProperty) {
          varProperty = pp
        }
        else return
      }
      else if (parent is HCLElement) {
        if (!HCLPsiUtil.isPropertyKey(parent)) return
        val ppp = parent.parent.parent as? HCLObject ?: return
        val pppp = ppp.parent as? HCLProperty ?: return
        varProperty = pppp
      }
      else return

      if (varProperty.parent !is HCLFile) return

      val variables = varProperty.getTerraformModule().findVariables(varProperty.name)
      val defaults = variables.mapNotNull { it.getDefault() as? HCLObject }

      result.addAllElements(
        defaults.flatMap { default -> default.propertyList.map { create(it.name).withInsertHandler(ResourcePropertyInsertHandler) } })
    }
  }

  companion object {
    val LOG: Logger = Logger.getInstance(TfConfigCompletionContributor::class.java)
  }
}
