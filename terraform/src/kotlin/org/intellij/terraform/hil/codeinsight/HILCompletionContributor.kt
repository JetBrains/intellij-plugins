// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.patterns.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.codeinsight.ModelHelper
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor.BlockTypeOrNameCompletionProvider.isProviderUsed
import org.intellij.terraform.config.codeinsight.TerraformLookupElementRenderer
import org.intellij.terraform.config.inspection.TFNoInterpolationsAllowedInspection.Companion.DependsOnProperty
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.Function
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.navigation.HCLQualifiedNameProvider
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.*
import org.intellij.terraform.hil.HILLanguage
import org.intellij.terraform.hil.codeinsight.ReferenceCompletionHelper.findByFQNRef
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.hil.psi.impl.getHCLHost
import java.util.*
import kotlin.collections.ArrayList

class HILCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, METHOD_POSITION, MethodsCompletionProvider)
    extend(CompletionType.BASIC, METHOD_POSITION, ResourceTypesCompletionProvider)
    extend(null, METHOD_POSITION, FullReferenceCompletionProvider)
    extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
        .withParent(Identifier::class.java).withSuperParent(2, ILSE_FROM_KNOWN_SCOPE)
        , KnownScopeCompletionProvider)
    extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
        .withParent(Identifier::class.java).withSuperParent(2, ILSE_NOT_FROM_KNOWN_SCOPE)
        , SelectCompletionProvider)

    extend(CompletionType.BASIC, VARIABLE_TYPE_POSITION, VariableTypeCompletionProvider)

    extend(CompletionType.BASIC, FOR_EACH_ITERATOR_POSITION, ForEachIteratorCompletionProvider)
    extend(CompletionType.BASIC, INSIDE_FOR_EXPRESSION_BODY, ForVariableCompletion())
  }

  override fun beforeCompletion(context: CompletionInitializationContext) {
    if (context.dummyIdentifier != CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) {
      context.dummyIdentifier = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
    }
  }

  companion object {
    @JvmField val GLOBAL_SCOPES: SortedSet<String> = sortedSetOf("var", "path", "data", "module", "local")

    private fun getScopeSelectPatternCondition(scopes: Set<String>): PatternCondition<SelectExpression<*>?> {
      return object : PatternCondition<SelectExpression<*>?>("ScopeSelect($scopes)") {
        override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
          val from = t.from
          return from is Identifier && from.name in scopes
        }
      }
    }

    private val SCOPE_PROVIDERS = listOf(
        DataSourceCompletionProvider,
        VariableCompletionProvider,
        SelfCompletionProvider,
        PathCompletionProvider,
        CountCompletionProvider,
        TerraformCompletionProvider,
        LocalsCompletionProvider,
        ModuleCompletionProvider
    ).map { it.scope to it }.toMap()
    val SCOPES = SCOPE_PROVIDERS.keys

    private val METHOD_POSITION = PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
        .withParent(PlatformPatterns.psiElement(Identifier::class.java)
            .with(object : PatternCondition<Identifier?>("Not a Block Identifier") {
              override fun accepts(t: Identifier, context: ProcessingContext?): Boolean {
                return t.parent !is HCLBlock
              }
            })
            .withHCLHost(PlatformPatterns.psiElement(HCLElement::class.java)
                .with(object : PatternCondition<HCLElement?>("Not in Variable block") {
                  override fun accepts(t: HCLElement, context: ProcessingContext?): Boolean {
                    val topmost = t.parentsOfType(HCLBlock::class.java).lastOrNull() ?: return true
                    return !TerraformPatterns.VariableRootBlock.accepts(topmost)
                  }
                })
                .andNot(PlatformPatterns.psiElement().inside(DependsOnProperty)))
        )
        .andNot(PlatformPatterns.psiElement().withSuperParent(2, SelectExpression::class.java))

    private val VARIABLE_TYPE_POSITION = PlatformPatterns.psiElement().withLanguages(HCLLanguage)
        .withParent(PlatformPatterns.psiElement(HCLIdentifier::class.java)
            .with(object : PatternCondition<HCLIdentifier?>("Not a Block Identifier") {
              override fun accepts(t: HCLIdentifier, context: ProcessingContext?): Boolean {
                return t.parent !is HCLBlock
              }
            })
            .with(object : PatternCondition<HCLElement?>("In Variable block") {
              override fun accepts(t: HCLElement, context: ProcessingContext?): Boolean {
                val topmost = t.parentsOfType(HCLBlock::class.java).lastOrNull() ?: return false
                return TerraformPatterns.VariableRootBlock.accepts(topmost)
              }
            }
            ))
        .andNot(PlatformPatterns.psiElement().withSuperParent(2, SelectExpression::class.java))

    private val FOR_EACH_ITERATOR_POSITION = PlatformPatterns.psiElement().withLanguages(HILLanguage, HCLLanguage)
        .withParent(PlatformPatterns.psiElement(Identifier::class.java)
            .withHCLHost(PlatformPatterns.psiElement(HCLElement::class.java)
                .inside(TerraformPatterns.DynamicBlock)
            )
        )


    val ILSE_FROM_KNOWN_SCOPE = PlatformPatterns.psiElement(SelectExpression::class.java)
        .with(getScopeSelectPatternCondition(SCOPES))
    val ILSE_NOT_FROM_KNOWN_SCOPE = PlatformPatterns.psiElement(SelectExpression::class.java)
        .without(getScopeSelectPatternCondition(SCOPES))
    val ILSE_FROM_DATA_SCOPE = PlatformPatterns.psiElement(SelectExpression::class.java)
        .with(getScopeSelectPatternCondition(setOf("data")))
    val ILSE_DATA_SOURCE = PlatformPatterns.psiElement(SelectExpression::class.java)
        .with(object : PatternCondition<SelectExpression<*>?>(" SE_Data_Source()") {
          override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
            val from = t.from as? SelectExpression<*> ?: return false
            return ILSE_FROM_DATA_SCOPE.accepts(from)
          }
        })
    val INSIDE_FOR_EXPRESSION_BODY = PlatformPatterns.psiElement()
      .withParent(PlatformPatterns.psiElement(BaseExpression::class.java)
                    .withHCLHost(PlatformPatterns.psiElement()
                                   .inside(false, PlatformPatterns.psiElement(HCLForExpression::class.java))))
    val IS_SE_FROM_CONDITION = object : PatternCondition<PsiElement?>("IsSelectFrom") {
      override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
        val parent = t.parent as? SelectExpression<*> ?: return false
        return parent.from === t
      }
    }


    private val LOG = Logger.getInstance(HILCompletionContributor::class.java)
    fun create(value: String): LookupElementBuilder {
      return LookupElementBuilder.create(value)
    }

    fun createScope(value: String): LookupElementBuilder {
      var builder = LookupElementBuilder.create(value)
      builder = builder.withInsertHandler(ScopeSelectInsertHandler)
      builder = builder.withRenderer(object : LookupElementRenderer<LookupElement?>() {
        override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
          presentation?.icon = AllIcons.Nodes.Tag
          presentation?.itemText = element?.lookupString
        }
      })
      return builder
    }

    fun create(f: Function): LookupElementBuilder {
      var builder = LookupElementBuilder.create(f.name)
      builder = builder.withInsertHandler(FunctionInsertHandler)
      builder = builder.withRenderer(object : LookupElementRenderer<LookupElement?>() {
        override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
          presentation?.icon = AllIcons.Nodes.Method // or Function
          presentation?.itemText = element?.lookupString
        }
      })
      return builder
    }

    fun create(value: PropertyOrBlockType, lookupString: String? = null): LookupElementBuilder {
      var builder = LookupElementBuilder.create(lookupString ?: value.name)
      builder = builder.withRenderer(TerraformLookupElementRenderer())
      return builder
    }
  }

  private object MethodsCompletionProvider : CompletionProvider<CompletionParameters>() {

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
        val isRootBlock = TerraformPatterns.RootBlock.accepts(block)
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

        // Since 'depends_on', 'provider' does not allows interpolations, don't add anything
        if (DependsOnProperty.accepts(property)) return
        if (property.name == "provider" && (type == "resource" || type == "data") && isRootBlock) return
        // Same for 'providers' binding in 'module'
        if (property.name == "providers" && type == "module" && isRootBlock) return

        val hint = (ModelHelper.getBlockProperties(block)[property.name] as? PropertyType)?.hint
        if (hint is SimpleValueHint || hint is ReferenceHint) {
          return
        }
      }
      result.addAllElements(TypeModelProvider.getModel(position.project).functions.map { create(it) })
      result.addAllElements(GLOBAL_SCOPES.map { createScope(it) })
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

  private abstract class SelectFromScopeCompletionProvider(val scope: String) : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent as? Identifier ?: return
      val pp = parent.parent as? SelectExpression<*> ?: return
      val from = pp.from as? Identifier ?: return
      if (scope != from.name) return
      LOG.debug { "HIL.SelectFromScopeCompletionProvider($scope){position=$position, parent=$parent, pp=$pp}" }
      doAddCompletions(parent, parameters, context, result)
    }

    abstract fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet)
  }

  object KnownScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val position = parameters.position
      val parent = position.parent as? Identifier ?: return
      val pp = parent.parent as? SelectExpression<*> ?: return
      val from = pp.from as? Identifier ?: return
      val provider = SCOPE_PROVIDERS[from.name] ?: return
      LOG.debug { "HIL.SelectFromScopeCompletionProviderAny($from.name){position=$position, parent=$parent, pp=$pp}" }
      provider.doAddCompletions(parent, parameters, context, result)
    }
  }

  private object VariableCompletionProvider : SelectFromScopeCompletionProvider("var") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val variables: List<Variable> = getLocalDefinedVariables(variable)
      for (v in variables) {
        result.addElement(create(v.name))
      }
    }
  }

  private object SelfCompletionProvider : SelectFromScopeCompletionProvider("self") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      // For now 'self' allowed only for provisioners inside resources

      val resource = getProvisionerOrConnectionResource(variable) ?: return
      val properties = ModelHelper.getBlockProperties(resource)
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

    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      result.addAllElements(PATH_REFERENCES.map { create(it) })
    }
  }

  private object CountCompletionProvider : SelectFromScopeCompletionProvider("count") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      getContainingResourceOrDataSourceOrModule(variable.getHCLHost()) ?: return
      result.addElement(create("index"))
    }
  }

  private object TerraformCompletionProvider : SelectFromScopeCompletionProvider("terraform") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      result.addElement(create("workspace"))
      getContainingResourceOrDataSource(variable.getHCLHost()) ?: return
      result.addElement(create("env"))
    }
  }

  private object LocalsCompletionProvider : SelectFromScopeCompletionProvider("local") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val variables: List<String> = getLocalDefinedLocals(variable)
      for (v in variables) {
        result.addElement(create(v))
      }
    }
  }

  private object ModuleCompletionProvider : SelectFromScopeCompletionProvider("module") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val module = getTerraformModule(variable) ?: return
      val modules = module.getDefinedModules()
      for (m in modules) {
        val name = m.getNameElementUnquoted(1)
        if (name != null) result.addElement(create(name))
      }
    }
  }

  private object DataSourceCompletionProvider : SelectFromScopeCompletionProvider("data") {
    override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val module = getTerraformModule(variable) ?: return

      val dataSources = module.getDeclaredDataSources()
      val types = dataSources.mapNotNull { it.getNameElementUnquoted(1) }.toSortedSet()
      result.addAllElements(types.map { create(it) })

      if (parameters.isExtendedCompletion) {
        @Suppress("NAME_SHADOWING")
        var dataSources = ModelHelper.getTypeModel(parameters.position.project).dataSources
        val cache = HashMap<String, Boolean>()
        if (parameters.invocationCount == 2) {
          dataSources = dataSources.filter { isProviderUsed(module, it.provider.type, cache) }
        }
        result.addAllElements(dataSources.map { it.type }.filter { it !in types }.map { create(it) })
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
      val references = HCLPsiUtil.getReferencesSelectAware(expression)
      if (references.isNotEmpty()) {
        val collectedTargets = references.asSequence()
          .flatMap { reference -> resolve(reference, true, false) }
          .flatMap { collectVariants(it, false, 2) }
          .toList()
        if (collectedTargets.isNotEmpty()) {
          result.addAllElements(collectedTargets)
        }
        return
      }

      if (expression is Identifier) {
        val module = host.getTerraformModule()
        val names = TreeSet<String>()
        if (ILSE_DATA_SOURCE.accepts(parent)) {
          val dataSources = module.findDataSource(expression.name, null)
          dataSources.mapNotNull { it.getNameElementUnquoted(2) }.toCollection(names)
        } else {
          val resources = module.findResources(expression.name, null)
          resources.mapNotNull { it.getNameElementUnquoted(2) }.toCollection(names)
        }
        result.addAllElements(names.map { create(it) })
        // TODO: support 'module.MODULE_NAME.OUTPUT_NAME' references (in that or another provider)
      }
    }

    private fun collectVariants(r: PsiElement?, iteratorResolve: Boolean, depth: Int): List<LookupElement> {
      when (r) {
        is HCLBlock -> {
          return ArrayList<LookupElement>().also { getBlockProperties(r, it) }
        }
        is FakeTypeProperty -> return ArrayList<LookupElement>().also { collectTypeVariants(r.type, it) }
        is HCLProperty -> {
          when(val value = r.value) {
            is HCLArray -> {
              return value.elements.reversed().flatMap { collectVariants(it, false, depth) }
            }
            is HCLObject -> {
              if (depth > 0)
                return collectVariants(value, iteratorResolve, depth)
              else
                return emptyList()
            }
            else -> return collectVariantsSelectAware(value, iteratorResolve, depth)
          }
        }
        is HCLObject -> {
          return ArrayList<LookupElement>().also { found ->
            if(!iteratorResolve) {
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
            return collectVariants(rParent.container, true, depth)
          }
          else
            return collectVariantsSelectAware(r, iteratorResolve, depth)
        }
      }
    }

    private fun collectVariantsSelectAware(value: PsiElement?, iteratorResolve: Boolean, depth: Int): List<LookupElement> =
      HCLPsiUtil.getReferencesSelectAware(value).flatMap { ref ->
        resolve(ref, false, false).filter { it != value }.flatMap {
          collectVariants(it, iteratorResolve, depth)
        }
      }

    private fun getBlockProperties(r: HCLBlock, found: ArrayList<LookupElement>) {
      if (TerraformPatterns.VariableRootBlock.accepts(r)) {
        val variable = Variable(r)
        val defaultMap = variable.getDefault()
        if (defaultMap is HCLObject) {
          val names = HashSet<String>()
          defaultMap.propertyList.mapNotNullTo(names) { it.name }
          defaultMap.blockList.mapNotNullTo(names) { it.name }
          names.mapTo(found) { create(it) }
        }
        collectTypeVariants(variable.getType(), found)
        return
      } else if (TerraformPatterns.ModuleRootBlock.accepts(r)) {
        val module = Module.getAsModuleBlock(r)
        if (module != null) {
          // TODO: Add special LookupElementRenderer
          module.getDefinedOutputs().map { create(it.name) }.toCollection(found)
        }
        return
      }
      val properties = ModelHelper.getBlockProperties(r).filterKeys { it != Constants.HAS_DYNAMIC_ATTRIBUTES }
      val done = properties.keys.toSet()
      found.addAll(properties.values.map { create(it) })
      val pl = r.`object`?.propertyList
      if (pl != null) {
        found.addAll(pl.map { it.name }.filter { it !in done }.map { create(it) })
      }
    }

    private fun collectTypeVariants(type: Type?, found: ArrayList<LookupElement>) {
      if (type is ObjectType) {
        type.elements?.keys?.mapTo(found) { create(it) }
      }
      else if (type is TupleType) {
        type.elements.filterIsInstance<ObjectType>().mapNotNull { it.elements }.flatMap { it.keys }.toSet().mapTo(found) { create(it) }
      }
      else if (type is ContainerType<*>) {
        if (type.elements is ObjectType) {
          (type.elements as ObjectType).elements?.keys?.mapTo(found) { create(it) }
        }
      }
    }

  }

  private object ResourceTypesCompletionProvider : TerraformConfigCompletionContributor.OurCompletionProvider() {
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
        val isRootBlock = TerraformPatterns.RootBlock.accepts(block)

        if (property.parent?.parent !== block) {
          if (property.parent is HCLObject && property.parent.parent is HCLProperty) {
            val outerProp = property.parent.parent as HCLProperty
            // Same for 'providers' binding in 'module'
            if (outerProp.name == "providers" && type == "module" && isRootBlock) return
          }
        } else {
          // Since 'depends_on', 'provider' does not allows interpolations, don't add anything
          if (DependsOnProperty.accepts(property)) return
          if (property.name == "provider" && (type == "resource" || type == "data") && isRootBlock) return
          // Same for 'providers' binding in 'module'
          if (property.name == "providers" && type == "module" && isRootBlock) return

          val hint = (ModelHelper.getBlockProperties(block)[property.name] as? PropertyType)?.hint
          if (hint is SimpleValueHint || hint is ReferenceHint) {
            return
          }
        }
      }

      val module = host.getTerraformModule()
      val resources = module.getDeclaredResources()
      val types = resources.mapNotNull { it.getNameElementUnquoted(1) }.toSortedSet()
      result.addAllElements(types.map { create(it) })

      if (parameters.isExtendedCompletion) {
        @Suppress("NAME_SHADOWING")
        var resources = getTypeModel(position.project).resources
        val cache = HashMap<String, Boolean>()
        if (parameters.invocationCount == 2) {
          resources = resources.filter { isProviderUsed(module, it.provider.type, cache) }
        }
        result.addAllElements(resources.map { it.type }.filter { it !in types }.map { create(it) })
      }
    }
  }

  private object FullReferenceCompletionProvider : TerraformConfigCompletionContributor.OurCompletionProvider() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      if (parameters.completionType != CompletionType.SMART && !parameters.isExtendedCompletion) return
      val position = parameters.position
      val parent = position.parent as? BaseExpression ?: return
      val leftNWS = position.getPrevSiblingNonWhiteSpace()
      LOG.debug { "HIL.ResourceTypesCompletionProvider{position=$position, parent=$parent, left=${position.prevSibling}, lnws=$leftNWS}" }

      val host = parent.getHCLHost() ?: return

      val property = PsiTreeUtil.getParentOfType(host, HCLProperty::class.java) ?: return
      val block = PsiTreeUtil.getParentOfType(property, HCLBlock::class.java) ?: return

      val hint = (ModelHelper.getBlockProperties(block)[property.name] as? PropertyType)?.hint
      if (hint is ReferenceHint) {
        val module = property.getTerraformModule()
        hint.hint
            .mapNotNull { findByFQNRef(it, module) }
            .flatten()
            .mapNotNull {
              return@mapNotNull when (it) {
                is HCLBlock -> HCLQualifiedNameProvider.getQualifiedModelName(it)
                is HCLProperty -> HCLQualifiedNameProvider.getQualifiedModelName(it)
                is String -> it
                else -> null
              }
            }
            .forEach { result.addElement(create(it)) }
        return
      }
      // TODO: Support other hint types
    }
  }
}

fun <T : PsiElement, Self : PsiElementPattern<T, Self>> PsiElementPattern<T, Self>.withLanguages(vararg languages: Language): PsiElementPattern<T, Self> {
  return with(object : PatternCondition<T>("withLanguages") {
    override fun accepts(t: T, context: ProcessingContext): Boolean {
      return t.language in languages
    }
  })
}

