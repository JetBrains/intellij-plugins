// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.GlobalScopes
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createFunction
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createScope
import org.intellij.terraform.config.codeinsight.TfConfigCompletionContributor
import org.intellij.terraform.config.codeinsight.TfLookupElementRenderer
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.HCLPsiUtil.getPrevSiblingNonWhiteSpace
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.HILLanguage
import org.intellij.terraform.hil.HilContainingBlockType
import org.intellij.terraform.hil.getResourceTypeAndName
import org.intellij.terraform.hil.guessContainingBlockType
import org.intellij.terraform.hil.patterns.HILPatterns.ForEachIteratorPosition
import org.intellij.terraform.hil.patterns.HILPatterns.IlseDataSource
import org.intellij.terraform.hil.patterns.HILPatterns.IlseFromKnownScope
import org.intellij.terraform.hil.patterns.HILPatterns.IlseNotFromKnownScope
import org.intellij.terraform.hil.patterns.HILPatterns.InsideForExpressionBody
import org.intellij.terraform.hil.patterns.HILPatterns.MethodPosition
import org.intellij.terraform.hil.patterns.HILPatterns.VariableTypePosition
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.hil.psi.impl.getHCLHost
import org.intellij.terraform.opentofu.codeinsight.EncryptionMethodsCompletionProvider
import org.intellij.terraform.opentofu.codeinsight.KeyProvidersCompletionProvider
import org.intellij.terraform.opentofu.codeinsight.findEncryptionBlocksIdsByType
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.EncryptionMethodBlock
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.IlseOpenTofuEncryptionMethod
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.IlseOpenTofuKeyProvider
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.KeyProviderBlock

open class HILCompletionContributor : CompletionContributor(), DumbAware {
  private val scopeProviders = listOf(
    CountCompletionProvider,
    DataSourceCompletionProvider,
    LocalsCompletionProvider,
    ModuleCompletionProvider,
    PathCompletionProvider,
    SelfCompletionProvider,
    TfCompletionProvider,
    VariableCompletionProvider,
    KeyProvidersCompletionProvider,
    EncryptionMethodsCompletionProvider
  ).associateBy { it.scope }

  init {
    extend(CompletionType.BASIC, MethodPosition, MethodsCompletionProvider)
    extend(CompletionType.BASIC, MethodPosition, ResourceTypesCompletionProvider)

    extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
      .withParent(Identifier::class.java).withSuperParent(2, IlseFromKnownScope), KnownScopeCompletionProvider())
    extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
      .withParent(Identifier::class.java).withSuperParent(2, IlseNotFromKnownScope), SelectCompletionProvider)

    extend(CompletionType.BASIC, VariableTypePosition, VariableTypeCompletionProvider)

    extend(CompletionType.BASIC, ForEachIteratorPosition, ForEachIteratorCompletionProvider)
    extend(CompletionType.BASIC, InsideForExpressionBody, ForVariableCompletion)
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    if (context.dummyIdentifier != CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) {
      context.dummyIdentifier = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
    }
  }

  internal abstract class SelectFromScopeCompletionProvider(val scope: String) {
    abstract fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet)
  }

  inner class KnownScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent as? Identifier ?: return
      val pp = parent.parent as? SelectExpression<*> ?: return
      val from = pp.from as? Identifier ?: return
      val provider = this@HILCompletionContributor.scopeProviders[from.name] ?: return
      LOG.debug { "HIL.SelectFromScopeCompletionProvider(${from.name}){position=$position, parent=$parent, pp=$pp}" }
      provider.doAddCompletions(parent, parameters, context, result)
    }
  }

  private object VariableCompletionProvider : SelectFromScopeCompletionProvider("var") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      val variables: List<Variable> = getLocalDefinedVariables(variable)
      for (v in variables) {
        result.addElement(create(v.name))
      }
    }
  }

  private object SelfCompletionProvider : SelectFromScopeCompletionProvider("self") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      // For now 'self' allowed only for provisioners inside resources

      val resource = getProvisionerOrConnectionResource(variable) ?: return
      val properties = TfModelHelper.getBlockProperties(resource)
      // TODO: Filter already defined or computed properties (?)
      // TODO: Add type filtration
      val set = properties.keys.toHashSet()
      set.remove(Constants.HAS_DYNAMIC_ATTRIBUTES)
      resource.`object`?.propertyList?.mapTo(set) { it.name }
      result.addAllElements(set.map { create(it) })
    }
  }

  private object PathCompletionProvider : SelectFromScopeCompletionProvider("path") {
    private val PATH_REFERENCES = sortedSetOf("root", "module", "cwd")

    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      result.addAllElements(PATH_REFERENCES.map { create(it) })
    }
  }

  private object CountCompletionProvider : SelectFromScopeCompletionProvider("count") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      getContainingResourceOrDataSourceOrModule(variable.getHCLHost()) ?: return
      result.addElement(create("index"))
    }
  }

  private object TfCompletionProvider : SelectFromScopeCompletionProvider("terraform") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      result.addElement(create("workspace"))
      getContainingResourceOrDataSource(variable.getHCLHost()) ?: return
      result.addElement(create("env"))
    }
  }

  private object LocalsCompletionProvider : SelectFromScopeCompletionProvider("local") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      val variables: List<String> = getLocalDefinedLocals(variable)
      for (v in variables) {
        result.addElement(create(v))
      }
    }
  }

  private object ModuleCompletionProvider : SelectFromScopeCompletionProvider("module") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      val module = getTerraformModule(variable) ?: return
      val modules = module.getDefinedModules()
      for (m in modules) {
        val name = m.getNameElementUnquoted(1)
        if (name != null) result.addElement(create(name))
      }
    }
  }

  private object DataSourceCompletionProvider : SelectFromScopeCompletionProvider("data") {
    override fun doAddCompletions(variable: Identifier,
                                  parameters: CompletionParameters,
                                  context: ProcessingContext,
                                  result: CompletionResultSet) {
      val module = getTerraformModule(variable) ?: return

      val dataSources = module.getDeclaredDataSources()
      val types = dataSources.mapNotNull { it.getNameElementUnquoted(1) }.toSortedSet()
      result.addAllElements(types.map { create(it) })
    }
  }

  private object MethodsCompletionProvider : TfConfigCompletionContributor.TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent as? BaseExpression ?: return
      val leftNWS = position.getPrevSiblingNonWhiteSpace()
      LOG.debug { "HIL.MethodsCompletionProvider{position=$position, parent=$parent, left=${position.prevSibling}, lnws=$leftNWS}" }
      // parent is usually HCLIdentifier or so
      var property = PsiTreeUtil.getParentOfType(parent, HCLProperty::class.java, false, HCLBlock::class.java)
      val block = PsiTreeUtil.getParentOfType(parent, HCLBlock::class.java)
      if (property != null && property.nameIdentifier == parent) return
      if (block != null && property != null) {
        val isRootBlock = TfPsiPatterns.RootBlock.accepts(block)
        val type = (if (isRootBlock) block else PsiTreeUtil.getTopmostParentOfType(parent, HCLBlock::class.java))?.getNameElementUnquoted(0)

        if (property.parent?.parent !== block) {
          /* Case for:
            (rootBlock,block)module x {
              providers = {
                (property)aws=<caret>
              }
            }
           */
          if (property.parent is HCLObject && property.parent.parent is HCLProperty) {
            property = property.parent.parent as HCLProperty
          }
        }
        if (!isRootBlock) {
          /* Case for:
            (rootBlock)module x {
              (block)providers {
                (property)aws=<caret>
              }
            }
           */
          if (block.fullName == "providers" && type == "module") return
        }

        if (needCompletionForBlock(block, property, type)) {
          return
        }
      }
      val model = TypeModelProvider.getModel(parent)
      result.addAllElements(model.functions.map { createFunction(it) })
      result.addAllElements(model.providerDefinedFunctions.map { createFunction(it) })

      result.addAllElements(GlobalScopes.map { createScope(it) })
      if (getProvisionerOrConnectionResource(parent) != null) result.addElement(createScope("self"))

      val host = parent.getHCLHost() ?: return
      val resourceOrDataSource = getContainingResourceOrDataSourceOrModule(host)
      if (resourceOrDataSource != null) {
        if (resourceOrDataSource.`object`?.findProperty("for_each") != null) {
          result.addElement(createScope("each"))
        }
        if (resourceOrDataSource.`object`?.findProperty("count") != null) {
          result.addElement(createScope("count"))
        }
      }
    }
  }

  private object SelectCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position

      val element = position.parent as? BaseExpression ?: return
      if (element !is Identifier && element !is LiteralExpression) return
      val host = element.getHCLHost() ?: return

      val parent = element.parent as? SelectExpression<*> ?: return

      val expression = getGoodLeftElement(parent, element) ?: return
      val isEachValueProperty = expression.parent.text == "each.value"
      val contextType = guessContainingBlockType(expression)
      val references = HCLPsiUtil.getReferencesSelectAware(expression)
      if (references.isNotEmpty()) {
        val collectedTargets = references.asSequence()
          .flatMap { reference -> resolve(reference, true, false) }
          .flatMap { collectVariants(it, false, 2, isEachValueProperty, contextType) }
          .toList()
        if (collectedTargets.isNotEmpty()) {
          result.addAllElements(collectedTargets)
        }
        return
      }

      if (expression is Identifier) {
        val module = host.getTerraformModule()
        if (IlseDataSource.accepts(parent)) {
          val dataSources = module.findDataSource(expression.name, null)
          result.addAllElements(dataSources.mapNotNull { it.getNameElementUnquoted(2) }.map { create(it) })
        }
        else if (IlseOpenTofuKeyProvider.accepts(parent)) {
          val keyProviderIds = findEncryptionBlocksIdsByType(parent, expression.name, KeyProviderBlock).toList()
          result.addAllElements(keyProviderIds)
        }
        else if (IlseOpenTofuEncryptionMethod.accepts(parent)) {
          val encryptionMethods = findEncryptionBlocksIdsByType(parent, expression.name, EncryptionMethodBlock).toList()
          result.addAllElements(encryptionMethods)
        }
        else {
          val resources = module.findResources(expression.name, null)
          result.addAllElements(resources.mapNotNull { it.getNameElementUnquoted(2) }.map { create(it) })
        }
      }
    }

    private fun collectVariants(r: PsiElement?,
                                iteratorResolve: Boolean,
                                depth: Int,
                                isEachValueProperty: Boolean = false,
                                contextType: HilContainingBlockType = HilContainingBlockType.UNSPECIFIED): List<LookupElement> {
      when (r) {
        is HCLBlock -> {
          return ArrayList<LookupElement>().also { getBlockProperties(r, contextType, isEachValueProperty, it) }
        }
        is FakeTypeProperty -> return ArrayList<LookupElement>().also { collectTypeVariants(r.type, it) }
        is HCLProperty -> {
          when (val value = r.value) {
            is HCLArray -> {
              return value.elements.reversed().flatMap { collectVariants(it, false, depth, isEachValueProperty) }
            }
            is HCLObject -> {
              if (depth > 0)
                return collectVariants(value, iteratorResolve, depth, isEachValueProperty)
              else
                return emptyList()
            }
            else -> return collectVariantsSelectAware(value, iteratorResolve, depth, isEachValueProperty)
          }
        }
        is HCLObject -> {
          return ArrayList<LookupElement>().also { found ->
            if (isEachValueProperty) {
              // IDEA-297901 Need to complete the value of map with each.value context
              val properties = r.propertyList
              properties.forEach { property ->
                (property.value as? HCLObject)?.propertyList?.mapNotNull { create(it.name) }?.let { found.addAll(it) }
              }
            }
            else if (!iteratorResolve) {
              found.addAll(r.propertyList.map { create(it.name) })
            }
            else if (depth > 0) {
              found.addAll(r.propertyList.flatMap { hclProperty ->
                collectVariants(hclProperty, false, depth - 1)
              })
            }
            found.addAll(r.blockList.map { create(it.getNameElementUnquoted(0)!!) })
          }
        }
        null -> {
          return emptyList()
        }
        else -> {
          val rParent = r.parent
          if (rParent is HCLForIntro && rParent.container != r) {
            return collectVariants(rParent.container, true, depth, isEachValueProperty)
          }
          else
            return collectVariantsSelectAware(r, iteratorResolve, depth, isEachValueProperty)
        }
      }
    }

    private fun collectVariantsSelectAware(value: PsiElement?, iteratorResolve: Boolean, depth: Int, isEachValueProperty: Boolean): List<LookupElement> =
      HCLPsiUtil.getReferencesSelectAware(value).flatMap { ref ->
        resolve(ref, false, false).filter { it != value }.flatMap {
          collectVariants(it, iteratorResolve, depth, isEachValueProperty)
        }
      }

    private fun getBlockProperties(r: HCLBlock, contextType: HilContainingBlockType, isEachValueProp: Boolean, found: ArrayList<LookupElement>) {
      if (TfPsiPatterns.VariableRootBlock.accepts(r)) {
        val variable = Variable(r)
        val defaultMap = variable.getDefault()
        if (defaultMap is HCLObject && !isEachValueProp) {
          handleHCLObject(defaultMap, found)
        }
        collectTypeVariants(variable.getType(), found)
        return
      }
      else if (TfPsiPatterns.ModuleRootBlock.accepts(r)) {
        val module = Module.getAsModuleBlock(r)
        if (module != null) {
          // TODO: Add special LookupElementRenderer
          val suitableBlocks = when (contextType) {
            HilContainingBlockType.IMPORT_OR_MOVED_BLOCK -> {
              module.getDeclaredResources().map { resourceDeclaration -> getResourceTypeAndName(resourceDeclaration) }
            }
            HilContainingBlockType.UNSPECIFIED -> {
              module.getDefinedOutputs().map { it.name }
            }
          }
          suitableBlocks.map { create(it) }.toCollection(found)
        }
        return
      }
      else if (TfPsiPatterns.OutputRootBlock.accepts(r)) {
        val outputValue = r.`object`?.findProperty(TypeModel.ValueProperty.name)?.value
        if (outputValue is HCLObject) {
          handleHCLObject(outputValue, found)
        }
        collectTypeVariants(outputValue.getType(), found)
        return
      }
      val properties = TfModelHelper.getBlockProperties(r).filterKeys { it != Constants.HAS_DYNAMIC_ATTRIBUTES }
      val done = properties.keys.toSet()
      found.addAll(properties.values.map { create(it.name).withRenderer(TfLookupElementRenderer()) })
      val pl = r.`object`?.propertyList
      if (pl != null) {
        found.addAll(pl.map { it.name }.filter { it !in done }.map { create(it) })
      }
    }

    private fun handleHCLObject(objectProperty: HCLObject, found: ArrayList<LookupElement>) {
      val names = HashSet<String>()
      objectProperty.propertyList.mapNotNullTo(names) { it.name }
      objectProperty.blockList.mapNotNullTo(names) { it.name }
      names.mapTo(found) { create(it) }
    }

    private fun collectTypeVariants(type: Type?, found: ArrayList<LookupElement>) {
      if (type is ObjectType) {
        type.elements?.keys?.mapTo(found) { create(it) }
      }
      else if (type is TupleType) {
        type.elements.asSequence().filterIsInstance<ObjectType>().mapNotNull { it.elements }.flatMap { it.keys }.toSet().mapTo(found) {
          create(it)
        }
      }
      else if (type is ContainerType<*>) {
        if (type.elements is ObjectType) {
          type.elements.elements?.keys?.mapTo(found) { create(it) }
        }
      }
    }

  }

  private object ResourceTypesCompletionProvider : TfConfigCompletionContributor.TfCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent as? BaseExpression ?: return
      val leftNWS = position.getPrevSiblingNonWhiteSpace()
      LOG.debug { "HIL.ResourceTypesCompletionProvider{position=$position, parent=$parent, left=${position.prevSibling}, lnws=$leftNWS}" }

      val host = parent.getHCLHost() ?: return

      val property = PsiTreeUtil.getParentOfType(parent, HCLProperty::class.java, false, HCLBlock::class.java)
      val block = PsiTreeUtil.getParentOfType(parent, HCLBlock::class.java)
      if (property != null && property.nameIdentifier == parent) return
      if (block != null && property != null) {
        val type = block.getNameElementUnquoted(0)
        val isRootBlock = TfPsiPatterns.RootBlock.accepts(block)

        if (property.parent?.parent !== block) {
          if (property.parent is HCLObject && property.parent.parent is HCLProperty) {
            val outerProp = property.parent.parent as HCLProperty
            // Same for 'providers' binding in 'module'
            if (outerProp.name == "providers" && type == "module" && isRootBlock) return
          }
        }
        else {
          if (needCompletionForBlock(block, property, type)) {
            return
          }
        }
      }

      val module = host.getTerraformModule()
      val resources = module.getDeclaredResources()
      val types = resources.mapNotNull { it.getNameElementUnquoted(1) }.toSortedSet()
      result.addAllElements(types.map { create(it) })
    }
  }

  companion object {
    private val LOG = Logger.getInstance(HILCompletionContributor::class.java)
  }
}

fun <T : PsiElement, Self : PsiElementPattern<T, Self>> PsiElementPattern<T, Self>.withLanguages(vararg languages: Language): PsiElementPattern<T, Self> {
  return with(object : PatternCondition<T>("withLanguages") {
    override fun accepts(t: T, context: ProcessingContext): Boolean {
      return t.language in languages
    }
  })
}
