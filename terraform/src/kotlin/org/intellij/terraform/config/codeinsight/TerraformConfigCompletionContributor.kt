// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementWeigher
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns.not
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Plow.Companion.toPlow
import com.intellij.util.ProcessingContext
import com.intellij.util.Processor
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.inspection.TFNoInterpolationsAllowedInspection.Companion.DependsOnProperty
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.config.patterns.TerraformPatterns.TerraformConfigFile
import org.intellij.terraform.config.patterns.TerraformPatterns.TerraformVariablesFile
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
import org.intellij.terraform.hil.HILFileType
import org.intellij.terraform.hil.codeinsight.ReferenceCompletionHelper.findByFQNRef
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.nullize

class TerraformConfigCompletionContributor : HCLCompletionContributor() {
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
      .withParent(IdentifierOrStringLiteral)
      .withSuperParent(2, Block)
      .withSuperParent(3, Object)
      .withSuperParent(4, Block), BlockPropertiesCompletionProvider)

    // Leftmost identifier of block could be start of new property in case of eol betwen it ant next identifier
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
      .withSuperParent(3, Object)
      .withSuperParent(4, Block), PropertyObjectKeyCompletionProvider)
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

  companion object {
    @JvmField
    val ROOT_BLOCK_KEYWORDS: Set<String> = TypeModel.RootBlocks.map(BlockType::literal).toHashSet()
    val ROOT_BLOCKS_SORTED: List<BlockType> = TypeModel.RootBlocks.sortedBy { it.literal }

    private val LOG = Logger.getInstance(TerraformConfigCompletionContributor::class.java)
    fun DumpPsiFileModel(element: PsiElement): () -> String {
      return { DebugUtil.psiToString(element.containingFile, true) }
    }

    fun create(value: String, quote: Boolean = true): LookupElementBuilder {
      var builder = LookupElementBuilder.create(value)
      if (quote) {
        builder = builder.withInsertHandler(QuoteInsertHandler)
      }
      return builder
    }

    fun create(value: PropertyOrBlockType, lookupString: String? = null): LookupElementBuilder {
      var builder = LookupElementBuilder.create(value, lookupString ?: value.name)
      builder = builder.withRenderer(TerraformLookupElementRenderer())
      if (value is BlockType) {
        builder = builder.withInsertHandler(ResourceBlockNameInsertHandler(value))
      }
      else if (value is PropertyType) {
        builder = builder.withInsertHandler(ResourcePropertyInsertHandler)
      }
      return builder
    }

    fun failIfInUnitTestsMode(position: PsiElement, addition: String? = null) {
      LOG.assertTrue(!ApplicationManager.getApplication().isUnitTestMode, {
        var ret = ""
        if (addition != null) {
          ret = "$addition\n"
        }
        ret += " Position: $position\nFile: " + DebugUtil.psiToString(position.containingFile, true)
        ret
      })
    }

    fun getOriginalObject(parameters: CompletionParameters, obj: HCLObject): HCLObject {
      val originalObject = parameters.originalFile.findElementAt(obj.textRange.startOffset)?.parent
      return originalObject as? HCLObject ?: obj
    }

    fun getClearTextValue(element: PsiElement?): String? {
      return when {
        element == null -> null
        element is HCLIdentifier -> element.id
        element is HCLStringLiteral -> element.value
        element.node?.elementType == HCLElementTypes.ID -> element.text
        HCLTokenTypes.STRING_LITERALS.contains(element.node?.elementType) -> HCLPsiUtil.stripQuotes(element.text)
        else -> return null
      }
    }

    fun getIncomplete(parameters: CompletionParameters): String? {
      val position = parameters.position
      val text = getClearTextValue(position) ?: position.text
      if (text == CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) return null
      return text.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "").nullize(true)
    }
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

  abstract class OurCompletionProvider : CompletionProvider<CompletionParameters>() {
    protected fun getTypeModel(project: Project): TypeModel {
      return TypeModelProvider.getModel(project)
    }

    @Suppress("UNUSED_PARAMETER")
    protected fun addResultsWithCustomSorter(result: CompletionResultSet,
                                             parameters: CompletionParameters,
                                             toAdd: Collection<LookupElementBuilder>) {
      if (toAdd.isEmpty()) return
      result
        .withRelevanceSorter(
          // CompletionSorter.defaultSorter(parameters, result.prefixMatcher)
          CompletionSorter.emptySorter()
            .weigh(PreferRequiredProperty))
        .addAllElements(toAdd)
    }
  }

  private object BlockKeywordCompletionProvider : OurCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent
      val leftNWS = position.getPrevSiblingNonWhiteSpace()
      LOG.debug { "TF.BlockKeywordCompletionProvider{position=$position, parent=$parent, left=${position.prevSibling}, lnws=$leftNWS}" }
      assert(getClearTextValue(leftNWS) == null, DumpPsiFileModel(position))
      result.addAllElements(ROOT_BLOCKS_SORTED.map { create(it) })
    }
  }

  object BlockTypeOrNameCompletionProvider : OurCompletionProvider() {

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      doCompletion(position, Processor { result.addElement(it); !result.isStopped }, parameters.invocationCount)
    }

    fun doCompletion(position: PsiElement, consumer: Processor<LookupElement>, invocationCount: Int = 1): Boolean {
      val parent = position.parent
      LOG.debug { "TF.BlockTypeOrNameCompletionProvider{position=$position, parent=$parent}" }
      val obj = when {
        parent is HCLIdentifier -> parent
        parent is HCLStringLiteral -> parent
        // Next line for the case of two IDs (not Identifiers) nearby (start of block in empty file)
        HCLTokenTypes.IDENTIFYING_LITERALS.contains(position.node.elementType) -> position
        else -> {
          failIfInUnitTestsMode(position); return true
        }
      }
      val leftNWS = obj.getPrevSiblingNonWhiteSpace()
      LOG.debug { "TF.BlockTypeOrNameCompletionProvider{position=$position, parent=$parent, obj=$obj, lnws=$leftNWS}" }
      val type = getClearTextValue(leftNWS) ?: run { failIfInUnitTestsMode(position); return true }
      val cache = HashMap<String, Boolean>()
      val project = position.project
      return when (type) {
        "resource" -> {
          getTypeModel(project).resources.toPlow()
            .filter { invocationCount >= 3 || isProviderUsed(parent, it.provider.type, cache) }
            .map { create(it.type).withInsertHandler(ResourceBlockSubNameInsertHandler(it)) }
            .processWith(consumer)
        }

        "data" ->
          getTypeModel(project).dataSources.toPlow()
            .filter { invocationCount >= 3 || isProviderUsed(parent, it.provider.type, cache) }
            .map { create(it.type).withInsertHandler(ResourceBlockSubNameInsertHandler(it)) }
            .processWith(consumer)

        "provider" ->
          getTypeModel(project).providers.toPlow()
            .map { create(it.type).withInsertHandler(ResourceBlockSubNameInsertHandler(it)) }
            .processWith(consumer)

        "provisioner" ->
          getTypeModel(project).provisioners.toPlow()
            .map { create(it.type).withInsertHandler(ResourceBlockSubNameInsertHandler(it)) }
            .processWith(consumer)

        "backend" ->
          getTypeModel(project).backends.toPlow()
            .map { create(it.type).withInsertHandler(ResourceBlockSubNameInsertHandler(it)) }
            .processWith(consumer)
        
        else -> true
      }
    }

    fun isProviderUsed(element: PsiElement, providerName: String, cache: MutableMap<String, Boolean>): Boolean {
      val hclElement = PsiTreeUtil.getParentOfType(element, HCLElement::class.java, false)
      if (hclElement == null) {
        failIfInUnitTestsMode(element, "Completion called on element without any HCLElement as parent")
        return true
      }
      return isProviderUsed(hclElement.getTerraformModule(), providerName, cache)

    }

    fun isProviderUsed(module: Module, providerName: String, cache: MutableMap<String, Boolean>): Boolean {
      if (!cache.containsKey(providerName)) {
        val providers = module.getDefinedProviders()
        cache[providerName] = providers.isEmpty() || providers.any { it.first.name == providerName }
                              || module.model.getProviderType(providerName)?.properties == TypeModel.AbstractProvider.properties
      }
      return cache[providerName]!!
    }
  }

  private object BlockPropertiesCompletionProvider : OurCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      var _parent: PsiElement? = position.parent
      var right: Type? = null
      var isProperty = false
      var isBlock = false
      val original = parameters.originalPosition ?: return
      val original_parent = original.parent
      if (HCLElementTypes.L_CURLY === original.node.elementType && original_parent is HCLObject) {
        LOG.debug { "Origin is '{' inside Object, O.P.P = ${original_parent.parent}" }
        if (original_parent.parent is HCLBlock) return
      }
      if (_parent is HCLIdentifier || _parent is HCLStringLiteral) {
        val pob = _parent.parent // Property or Block
        if (pob is HCLProperty) {
          // TODO: Support expressions
          val value = pob.value as? HCLValue
          if (value === _parent) return
          right = value.getType()
          if (right == Types.String && value is PsiLanguageInjectionHost) {
            // Check for Injection
            InjectedLanguageManager.getInstance(pob.project).enumerate(value, object : PsiLanguageInjectionHost.InjectedPsiVisitor {
              override fun visit(injectedPsi: PsiFile, places: List<PsiLanguageInjectionHost.Shred>) {
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
            })
          }
          isProperty = true
        }
        else if (pob is HCLBlock) {
          isBlock = true
          if (pob.nameElements.firstOrNull() == _parent) {
            if (_parent.nextSibling is PsiWhiteSpace && _parent.nextSibling.text.contains("\n")) {
              isBlock = false
              _parent = _parent.parent.parent
            }
          }
        }
        if (isBlock || isProperty) {
          _parent = pob?.parent // Object
        }
        LOG.debug { "TF.BlockPropertiesCompletionProvider{position=$position, parent=$_parent, original=$original, right=$right, isBlock=$isBlock, isProperty=$isProperty}" }
      }
      else {
        LOG.debug { "TF.BlockPropertiesCompletionProvider{position=$position, parent=$_parent, original=$original, no right part}" }
      }
      val parent: HCLObject = _parent as? HCLObject ?: return failIfInUnitTestsMode(position, "Parent should be HCLObject")
      val use = getOriginalObject(parameters, parent)
      val block = use.parent
      if (block is HCLBlock) {
        val props = ModelHelper.getBlockProperties(block)
        doAddCompletion(isBlock, isProperty, use, result, right, parameters, props)
      }
    }

    private fun doAddCompletion(isBlock: Boolean,
                                isProperty: Boolean,
                                parent: HCLObject,
                                result: CompletionResultSet,
                                right: Type?,
                                parameters: CompletionParameters,
                                properties: Map<String, PropertyOrBlockType>) {
      if (properties.isEmpty()) return
      val incomplete = getIncomplete(parameters)
      if (incomplete != null) {
        LOG.debug { "Including properties which contains incomplete result: $incomplete" }
      }
      addResultsWithCustomSorter(result, parameters, properties.values
        .filter { it.name != Constants.HAS_DYNAMIC_ATTRIBUTES }
        .filter { isRightOfPropertyWithCompatibleType(isProperty, it, right) || (isBlock && it is BlockType) || (!isProperty && !isBlock) }
        // TODO: Filter should be based on 'max-count' model property (?)
        .filter {
          (it is PropertyType && (parent.findProperty(it.name) == null || (incomplete != null && it.name.contains(
            incomplete)))) || (it is BlockType)
        }
        .filter { it.configurable }
        .map { create(it) })
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

  private object PropertyValueCompletionProvider : OurCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent
      val inArray = (parent.parent is HCLArray)
      LOG.debug { "TF.PropertyValueCompletionProvider{position=$position, parent=$parent}" }
      val property = PsiTreeUtil.getParentOfType(position, HCLProperty::class.java) ?: return
      val block = PsiTreeUtil.getParentOfType(property, HCLBlock::class.java) ?: return

      val type = block.getNameElementUnquoted(0)
      // TODO: Replace with 'ReferenceHint'
      if (property.name == "provider" && (type == "resource" || type == "data")) {
        val providers = property.getTerraformModule().getDefinedProviders()
        result.addAllElements(providers.map { create(it.second) })
        return
      }
      if (DependsOnProperty.accepts(property) && inArray) {
        val resources = property.getTerraformModule().getDeclaredResources()
          .map { "${it.getNameElementUnquoted(1)}.${it.name}" }
        val datas = property.getTerraformModule().getDeclaredDataSources()
          .map { "data.${it.getNameElementUnquoted(1)}.${it.name}" }
        val modules = property.getTerraformModule().getDefinedModules()
          .map { "module.${it.name}" }
        val variables = property.getTerraformModule().getAllVariables()
          .map { "var.${it.name}" }

        val current = when (type) {
          "data" -> "data.${block.getNameElementUnquoted(1)}.${block.name}"
          "resource" -> "${block.getNameElementUnquoted(1)}.${block.name}"
          "module" -> "module.${block.name}"
          "variable" -> "var.${block.name}"
          else -> "unsupported"
        }

        result.addAllElements(resources.asSequence().plus(datas).plus(modules).plus(variables).minus(current).map { create(it) }.toList())
        return
      }
      val prop = ModelHelper.getBlockProperties(block)[property.name] as? PropertyType
      val hint = prop?.hint ?: return
      if (hint is SimpleValueHint) {
        result.addAllElements(hint.hint.map { create(it) })
        return
      }
      if (hint is ReferenceHint) {
        val module = property.getTerraformModule()
        hint.hint
          .mapNotNull { findByFQNRef(it, module) }
          .flatten()
          .mapNotNull {
            return@mapNotNull when (it) {
              // TODO: Enable or remove next two lines
              //                is HCLBlock -> HCLQualifiedNameProvider.getQualifiedModelName(it)
              //                is HCLProperty -> HCLQualifiedNameProvider.getQualifiedModelName(it)
              // TODO: For TF 0.12 interpolation is not needed
              is String -> "" + '$' + "{$it}"
              else -> null
            }
          }
          .forEach { result.addElement(create(it)) }
        return
      }
      // TODO: Support other hint types
    }

  }

  private object VariableNameTFVARSCompletionProvider : OurCompletionProvider() {
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
      result.addAllElements(variables.map { create(it.name, false).withInsertHandler(ResourcePropertyInsertHandler) })
    }
  }

  private object MappedVariableTFVARSCompletionProvider : OurCompletionProvider() {
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
}

object ModelHelper {
  private val LOG = Logger.getInstance(ModelHelper::class.java)

  fun getBlockProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(0) ?: return emptyMap()
    // Special case for 'backend' blocks, since it's located not in root
    if (TerraformPatterns.Backend.accepts(block)) {
      return getBackendProperties(block)
    }
    if (TerraformPatterns.DynamicBlockContent.accepts(block)) {
      val dynamic = block.getParent(HCLBlock::class.java, true) ?: return emptyMap()
      assert(TerraformPatterns.DynamicBlock.accepts(dynamic))
      val origin = dynamic.getParent(HCLBlock::class.java, true) ?: return emptyMap()
      // origin is either ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock or ProvisionerBlock
      val blockType = getBlockProperties(origin)[dynamic.name] as? BlockType ?: return emptyMap()
      return blockType.properties
    }
    if (TerraformPatterns.DynamicBlock.accepts(block)) {
      return TypeModel.ResourceDynamic.properties
    }
    if (TerraformPatterns.ProvisionerBlock.accepts(block)) {
      return getProvisionerProperties(block)
    }
    if (TerraformPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TypeModel.ResourceLifecycle.properties
    }
    if (TerraformPatterns.ResourceConnectionBlock.accepts(block)) {
      return getConnectionProperties(block)
    }

    if (block.parent !is PsiFile) {
      return getModelBlockProperties(block, type)
    }
    val props: Map<String, PropertyOrBlockType>
    props = when (type) {
      "provider" -> getProviderProperties(block)
      "resource" -> getResourceProperties(block)
      "data" -> getDataSourceProperties(block)
      "module" -> getModuleProperties(block)
      "terraform" -> getTerraformProperties(block)
      else -> TypeModel.RootBlocksMap[type]?.properties ?: emptyMap()
    }
    return props
  }

  fun getAbstractBlockType(block: HCLBlock): BlockType? {
    val type = block.getNameElementUnquoted(0) ?: return null
    if (block.parent is PsiFile) {
      return TypeModel.RootBlocksMap[type]
    }

    // non-root blocks, match using patterns
    if (TerraformPatterns.Backend.accepts(block)) {
      return TypeModel.AbstractBackend
    }
    if (TerraformPatterns.DynamicBlock.accepts(block)) {
      return TypeModel.ResourceDynamic
    }
    if (TerraformPatterns.DynamicBlockContent.accepts(block)) {
      return TypeModel.AbstractResourceDynamicContent
    }
    if (TerraformPatterns.ProvisionerBlock.accepts(block)) {
      return TypeModel.AbstractResourceProvisioner
    }
    if (TerraformPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TypeModel.ResourceLifecycle
    }
    if (TerraformPatterns.ResourceConnectionBlock.accepts(block)) {
      return TypeModel.Connection
    }
    return null
  }

  fun getBlockType(block: HCLBlock): Type? {
    val type = block.getNameElementUnquoted(0) ?: return null

    // non-root blocks, match using patterns
    if (TerraformPatterns.Backend.accepts(block)) {
      val fallback = TypeModel.AbstractBackend
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return getTypeModel(block.project).getBackendType(name) ?: return fallback
    }
    if (TerraformPatterns.DynamicBlockContent.accepts(block)) {
      val fallback = TypeModel.AbstractResourceDynamicContent
      val dynamic = block.getParent(HCLBlock::class.java, true) ?: return fallback
      assert(TerraformPatterns.DynamicBlock.accepts(dynamic))
      val origin = dynamic.getParent(HCLBlock::class.java, true) ?: return fallback
      // origin is either ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock or ProvisionerBlock
      return getBlockProperties(origin)[dynamic.name] as? BlockType ?: return fallback
    }
    if (TerraformPatterns.DynamicBlock.accepts(block)) {
      // TODO: consider more specific content instead of AbstractResourceDynamicContent
      return TypeModel.ResourceDynamic
    }
    if (TerraformPatterns.ProvisionerBlock.accepts(block)) {
      val fallback = TypeModel.AbstractResourceProvisioner
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return getTypeModel(block.project).getProvisionerType(name)
    }
    if (TerraformPatterns.ResourceLifecycleBlock.accepts(block)) {
      return TypeModel.ResourceLifecycle
    }
    if (TerraformPatterns.ResourceConnectionBlock.accepts(block)) {
      return TypeModel.Connection
    }

    if (type !in TypeModel.RootBlocksMap.keys || block.parent !is PsiFile) {
      return null
    }

    if (type == "provider") {
      val fallback = TypeModel.AbstractProvider
      val name = block.getNameElementUnquoted(1) ?: return fallback
      return getTypeModel(block.project).getProviderType(name)
    }
    if (type == "resource") {
      val fallback = TypeModel.AbstractResource
      val name = block.getNameElementUnquoted(1) ?: return wrapIfCountForEach(fallback, block)
      return wrapIfCountForEach(getTypeModel(block.project).getResourceType(name) ?: fallback, block)
    }
    if (type == "data") {
      val fallback = TypeModel.AbstractDataSource
      val name = block.getNameElementUnquoted(1) ?: return wrapIfCountForEach(fallback, block)
      return wrapIfCountForEach(getTypeModel(block.project).getDataSourceType(name) ?: fallback, block)
    }
    if (type == "module") {
      val fallback = TypeModel.Module
      val name = block.getNameElementUnquoted(1) ?: return fallback
      val module = Module.getAsModuleBlock(block) ?: return fallback
      val result = HashMap<String, Type?>()

      val outputs = module.getDefinedOutputs()
      for (output in outputs) {
        val value = output.`object`?.findProperty("value")?.value
        result[output.name] = value.getType() ?: Types.Any
      }

      // TODO: Should variables be in type?
      val variables = module.getAllVariables()
      for (variable in variables) {
        result[variable.name] = variable.getCombinedType()
      }

      return ModuleType(name, result.map { PropertyType(it.key, type = it.value ?: Types.Any) })
    }
    if (type == "terraform") {
      return TypeModel.Terraform
    }
    if (type == "variable") {
      val variable = Variable(block)
      return variable.getCombinedType()
    }
    if (type == "output") {
      val value = block.`object`?.findProperty("value")?.value ?: return Types.Any
      return value.getType()
    }

    return TypeModel.RootBlocksMap[type]
  }

  private fun wrapIfCountForEach(type: BlockType, block: HCLBlock): Type {
    val obj = block.`object` ?: return type
    if (obj.findProperty("count") != null) {
      return ListType(type)
    }
    else if (obj.findProperty("for_each") != null) {
      return MapType(type)
    }
    return type
  }

  private fun getModelBlockProperties(block: HCLBlock, type: String): Map<String, PropertyOrBlockType> {
    // TODO: Speedup, remove recursive up-traverse
    val bp = block.parent as? HCLObject ?: return emptyMap()
    val bpp = bp.parent as? HCLBlock ?: return emptyMap()
    val properties = getBlockProperties(bpp)
    val candidate: BlockType? = properties[type] as? BlockType
    return candidate?.properties ?: emptyMap()
  }

  private fun getProviderProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val providerType = if (type != null) getTypeModel(block.project).getProviderType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractProvider, providerType)
  }

  private fun getProvisionerProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val provisionerType = if (type != null) getTypeModel(block.project).getProvisionerType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractResourceProvisioner, provisionerType)
  }

  private fun getBackendProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val backendType = if (type != null) getTypeModel(block.project).getBackendType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractBackend, backendType)
  }

  @Suppress("UNUSED_PARAMETER")
  private fun getTerraformProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    return TypeModel.Terraform.properties
  }

  private fun getConnectionProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.`object`?.findProperty("type")?.value
    val properties = HashMap<String, PropertyOrBlockType>()
    properties.putAll(TypeModel.Connection.properties)
    if (type is HCLStringLiteral) {
      when (type.value.toLowerCase().trim()) {
        "ssh" -> properties.putAll(TypeModel.ConnectionPropertiesSSH)
        "winrm" -> properties.putAll(TypeModel.ConnectionPropertiesWinRM)
        // TODO: Support interpolation resolving
        else -> LOG.warn("Unsupported 'connection' block type '${type.value}'")
      }
    }
    if (type == null) {
      // ssh by default
      properties.putAll(TypeModel.ConnectionPropertiesSSH)
    }
    return properties
  }

  fun getResourceProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val resourceType = if (type != null) getTypeModel(block.project).getResourceType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractResource, resourceType)
  }

  private fun getDataSourceProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val type = block.getNameElementUnquoted(1)
    val dataSourceType = if (type != null) getTypeModel(block.project).getDataSourceType(type) else null
    return getPropertiesWithDefaults(TypeModel.AbstractDataSource, dataSourceType)
  }

  private fun getModuleProperties(block: HCLBlock): Map<String, PropertyOrBlockType> {
    val defaults = TypeModel.Module.properties
    val module = Module.getAsModuleBlock(block) ?: return defaults
    val variables = module.getAllVariables()
    if (variables.isEmpty()) {
      return defaults
    }

    val properties = HashMap<String, PropertyOrBlockType>()
    properties.putAll(defaults)
    for (v in variables) {
      val hasDefault = v.getDefault() != null
      properties[v.name] = PropertyType(v.name, v.getType() ?: Types.Any, required = !hasDefault)
    }
    return properties
  }

  private fun getPropertiesWithDefaults(defaults: BlockType, origin: BlockType?): Map<String, PropertyOrBlockType> {
    if (origin == null) return defaults.properties
    val result = HashMap<String, PropertyOrBlockType>(defaults.properties.size + origin.properties.size)
    result.putAll(defaults.properties)
    result.putAll(origin.properties)
    return result
  }


  fun getTypeModel(project: Project): TypeModel {
    return TypeModelProvider.getModel(project)
  }
}
