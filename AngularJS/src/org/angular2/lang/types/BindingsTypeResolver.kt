// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.evaluation.JSExpressionTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.resolve.JSGenericMappings
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluatorBase
import com.intellij.lang.javascript.psi.resolve.generic.JSTypeSubstitutorImpl
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory.createIntersectionType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory.createUnionType
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor.JSTypeGenericId
import com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType.OptimizedKind
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.lang.typescript.resolve.TypeScriptGenericTypesEvaluator
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2LibrariesHacks.hackNgModelChangeType
import org.angular2.codeInsight.Angular2TypeScriptConfigCustomizer
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder.Companion.NG_TEMPLATE_CONTEXT_GUARD
import org.angular2.entities.Angular2ComponentLocator.findComponentClass
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import java.util.function.BiFunction
import java.util.function.Predicate

internal class BindingsTypeResolver private constructor(element: PsiElement,
                                                        provider: Angular2ApplicableDirectivesProvider,
                                                        inputExpressionsProvider: () -> Sequence<Pair<String, JSExpression>>) {
  private val myElement: PsiElement
  private val myMatched: List<Angular2Directive>
  private val myScope: Angular2DeclarationsScope
  private val myRawTemplateContextType: JSType?
  private val myTypeSubstitutor: JSTypeSubstitutor?

  init {
    myElement = element
    myMatched = provider.matched
    myScope = Angular2DeclarationsScope(element)
    val directives = myMatched.filter { myScope.contains(it) }
    if (directives.isEmpty()) {
      myRawTemplateContextType = null
      myTypeSubstitutor = null
    }
    else {
      val analyzed = analyze(directives, element, inputExpressionsProvider)
      myRawTemplateContextType = analyzed.first
      myTypeSubstitutor = analyzed.second
    }
  }

  fun resolveDirectiveEventType(name: String): JSType? {
    val types: MutableList<JSType?> = SmartList()
    for (directive in myMatched) {
      if (myScope.contains(directive)) {
        directive.outputs.find { it.name == name }?.let { property ->
          types.add(hackNgModelChangeType(property.type, name))
        }
      }
    }
    return processAndMerge(types)
  }

  fun resolveDirectiveInputType(key: String): JSType? {
    val types: MutableList<JSType?> = SmartList()
    for (directive in myMatched) {
      if (myScope.contains(directive)) {
        directive.inputs.find { it.name == key }?.let { property ->
          types.add(property.type)
        }
      }
    }
    return processAndMerge(types)
  }

  fun resolveDirectiveExportAsType(exportName: String?): JSType? {
    val hasExport = !exportName.isNullOrEmpty()
    return myMatched
      .asSequence()
      .filter { myScope.contains(it) }
      .filter { directive ->
        if (hasExport)
          directive.exportAsList.contains(exportName)
        else
          directive.isComponent
      }
      .mapNotNull { directive ->
        val cls = directive.typeScriptClass ?: return@mapNotNull null
        val genericParameters = cls.typeParameters.mapTo(HashSet()) { it.genericId }
        val clsType = if (genericParameters.isNotEmpty())
          JSTypeUtils.createNotSubstitutedGenericType(cls, cls.jsType)
        else
          cls.jsType
        processAndMerge(listOf(clsType))
          // In case of references, it may happen that some generic params are not substituted.
          // Let's be permissive here and replace each generic param from directive definition
          // with `any` type to avoid type checking errors in such situation.
          ?.transformTypeHierarchy {
            if (it is JSGenericParameterType && genericParameters.contains(it.genericId))
              JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, false)
            else it
          }
      }
      .firstOrNull()

  }

  fun resolveTemplateContextType(): JSType? {
    return JSTypeUtils.applyGenericArguments(myRawTemplateContextType, myTypeSubstitutor)
  }

  private fun processAndMerge(types: List<JSType?>): JSType? {
    var notNullTypes = types.filterNotNull()
    val source = getTypeSource(myElement, notNullTypes)
    if (source == null || notNullTypes.isEmpty()) {
      return null
    }
    if (myTypeSubstitutor != null) {
      notNullTypes = notNullTypes.mapNotNull { JSTypeUtils.applyGenericArguments(it, myTypeSubstitutor) }
    }
    return merge(source, notNullTypes, false)
  }

  companion object {

    fun resolve(attribute: XmlAttribute,
                infoValidation: Predicate<Angular2AttributeNameParser.AttributeInfo>,
                resolveMethod: BiFunction<BindingsTypeResolver, String, JSType?>): JSType? {
      val descriptor = attribute.descriptor as? Angular2AttributeDescriptor
      val tag = attribute.parent
      val info = Angular2AttributeNameParser.parse(attribute.name, attribute.parent)
      return if (descriptor == null || tag == null || !infoValidation.test(info)) {
        null
      }
      else resolveMethod.apply(get(tag), info.name)
    }

    fun get(tag: XmlTag): BindingsTypeResolver =
      CachedValuesManager.getCachedValue(tag) {
        CachedValueProvider.Result.create(create(tag), PsiModificationTracker.MODIFICATION_COUNT)
      }

    fun get(bindings: Angular2TemplateBindings): BindingsTypeResolver =
      CachedValuesManager.getCachedValue(bindings) {
        CachedValueProvider.Result.create(create(bindings), PsiModificationTracker.MODIFICATION_COUNT)
      }

    private fun create(bindings: Angular2TemplateBindings) =
      BindingsTypeResolver(bindings, Angular2ApplicableDirectivesProvider(bindings)) {
        bindings.bindings
          .asSequence()
          .filter { binding: Angular2TemplateBinding -> !binding.keyIsVar() }
          .mapNotNull { Pair(it.key, it.expression ?: return@mapNotNull null) }
      }

    private fun create(tag: XmlTag) =
      BindingsTypeResolver(tag, Angular2ApplicableDirectivesProvider(tag)) {
        tag.attributes
          .asSequence()
          .mapNotNull { attr ->
            val name = Angular2AttributeNameParser.parse(attr.name, attr.parent)
                         .takeIf { Angular2PropertyBindingType.isPropertyBindingAttribute(it) }
                         ?.name
                       ?: return@mapNotNull null
            Pair(name, Angular2Binding.get(attr)?.expression ?: return@mapNotNull null)
          }
      }

    private fun analyze(directives: List<Angular2Directive>,
                        element: PsiElement,
                        inputExpressionsProvider: () -> Sequence<Pair<String, JSExpression>>): Pair<JSType?, JSTypeSubstitutor?> {
      val inputsMap = inputExpressionsProvider()
        .distinctBy { it.first }
        .toMap()

      val config = TypeScriptConfigUtil.getConfigForPsiFile(element.containingFile)
      if (Angular2TypeScriptConfigCustomizer.isStrictTemplates(config)) {
        return analyzeStrictTemplates(directives, element, inputsMap)
      }
      else {
        return analyzeNonStrict(directives, element, inputsMap)
      }
    }

    private fun analyzeStrictTemplates(directives: List<Angular2Directive>,
                                       element: PsiElement,
                                       inputsMap: Map<String, JSExpression>): Pair<JSType?, JSTypeSubstitutor?> {
      val genericArguments = MultiMap<JSTypeGenericId, JSType?>()
      val templateContextTypes: MutableList<JSType> = SmartList()
      directives.forEach { directive ->
        val cls = directive.typeScriptClass ?: return@forEach

        val directiveInputs = mutableListOf<Pair<JSType, JSExpression?>>()
        val processingContext = JSTypeComparingContextService.createProcessingContextWithCache(cls)
        directive.inputs.forEach { property ->
          val inputExpression = inputsMap[property.name]
          val propertyType = property.type

          if (propertyType != null) {
            directiveInputs.add(Pair(propertyType, inputExpression))
          }

          if (inputExpression != null && propertyType != null) {
            // todo delete. For now it's only used for input type assignability checks, which should rely on concrete instance type
            collectGenericArgumentsNonStrict(inputExpression, propertyType, processingContext, genericArguments)
          }
        }

        val elementTypeSource = JSTypeSourceFactory.createTypeSource(element, true)
          .copyWithNewLanguage(JSTypeSource.SourceLanguage.TS) // sometimes we get the <ng-template> element, so we need to force TS
        val classTypeSource = cls.staticJSType.source

        val directiveInstanceType = calculateDirectiveInstanceType(cls, classTypeSource, directiveInputs, element, elementTypeSource)

        val guardElement = cls.findFunctionByName(NG_TEMPLATE_CONTEXT_GUARD)
        if (guardElement != null && guardElement.jsContext == JSContext.STATIC) {
          val guardFunctionType = JSPsiBasedTypeOfType(guardElement, false).substitute() // expected JSFunctionType

          val callType = JSApplyCallType(guardFunctionType, listOf(directiveInstanceType, JSUnknownType.TS_INSTANCE), classTypeSource)
          val predicateType = callType.substitute()

          var contextType: JSType? = null
          if (predicateType is TypeScriptTypePredicateTypeImpl) {
            contextType = predicateType.guardType
          }
          if (contextType != null) {
            templateContextTypes.add(contextType.substitute())
          }
        }
      }

      val typeSource = getTypeSource(element, templateContextTypes, genericArguments) ?: return Pair(null, null)
      // we merge templateContextTypes but Angular does something more akin to reduce
      val mergedTemplateContextType = if (templateContextTypes.isEmpty()) null else merge(typeSource, templateContextTypes, true)
      val typeSubstitutor = if (genericArguments.isEmpty) null else intersectGenerics(genericArguments, typeSource)
      return Pair(mergedTemplateContextType, typeSubstitutor)
    }

    private fun calculateDirectiveInstanceType(cls: TypeScriptClass,
                                               classTypeSource: JSTypeSource,
                                               directiveInputs: MutableList<Pair<JSType, JSExpression?>>,
                                               element: PsiElement,
                                               elementTypeSource: JSTypeSource): JSType? {
      val genericConstructorReturnType = TypeScriptTypeParser.createConstructorReturnType(cls, classTypeSource)

      // conceptually: const Directive = (...) => {...}, it misses the type arguments <...> part, so we patch that later
      val directiveFactoryType = JSFunctionTypeImpl(
        classTypeSource,
        directiveInputs.map { (paramType, _) -> JSParameterTypeDecoratorImpl(paramType, false, false, true) },
        genericConstructorReturnType // could pass null because we currently don't use JSApplyCallType anyway
      )

      val directiveFactoryCall = WebJSSyntheticFunctionCall(element) { typeFactory ->
        directiveInputs.map { (_, expression) ->
          when (expression) {
            null -> JSStringType(true, elementTypeSource, JSTypeContext.INSTANCE)
            is JSEmptyExpression -> JSUndefinedType(elementTypeSource)
            else -> typeFactory.evaluate(expression)
          }
        }
      }

      // JSApplyCallType accepting TypeScriptJSFunctionTypeImpl would be the cleanest solution,
      // but we use base JSFunctionTypeImpl that doesn't contain List<TypeScriptGenericDeclarationTypeImpl>
      val substitutor = TypeScriptGenericTypesEvaluator.getInstance()
        .getTypeSubstitutorForCallItem(directiveFactoryType, directiveFactoryCall, null)
      return JSTypeUtils.applyGenericArguments(genericConstructorReturnType, substitutor)
    }

    private fun analyzeNonStrict(directives: List<Angular2Directive>,
                                 element: PsiElement,
                                 inputsMap: Map<String, JSExpression>): Pair<JSType?, JSTypeSubstitutor?> {
      val genericArguments = MultiMap<JSTypeGenericId, JSType?>()
      val templateContextTypes: MutableList<JSType> = SmartList()
      directives.forEach { directive ->
        val cls = directive.typeScriptClass ?: return@forEach

        getTemplateContextTypeNonStrict(cls)?.let { templateContextType ->
          templateContextTypes.add(templateContextType)
        }

        val processingContext = JSTypeComparingContextService.createProcessingContextWithCache(cls)
        directive.inputs.forEach { property ->
          val inputExpression = inputsMap[property.name]
          val propertyType = property.type
          if (inputExpression != null && propertyType != null) {
            collectGenericArgumentsNonStrict(inputExpression, propertyType, processingContext, genericArguments)
          }
        }
      }

      val typeSource = getTypeSource(element, templateContextTypes, genericArguments) ?: return Pair(null, null)
      val mergedTemplateContextType = if (templateContextTypes.isEmpty()) null else merge(typeSource, templateContextTypes, true)
      val typeSubstitutor = if (genericArguments.isEmpty) null else intersectGenerics(genericArguments, typeSource)
      return Pair(mergedTemplateContextType, typeSubstitutor)
    }

    private fun collectGenericArgumentsNonStrict(inputExpression: JSExpression,
                                                 propertyType: JSType,
                                                 processingContext: ProcessingContext,
                                                 genericArguments: MultiMap<JSTypeGenericId, JSType?>) {
      val inputType = JSPsiBasedTypeOfType(inputExpression, true)
      // todo getApparentType is supposed to be used only in JS according to usages in JS plugin
      val apparentType = JSTypeUtils.getApparentType(JSTypeWithIncompleteSubstitution.substituteCompletely(inputType))
      if (JSTypeUtils.isAnyType(apparentType)) {
        // This workaround is needed, because many users expect to have ngForOf working with variable of type `any`.
        // This is not correct according to TypeScript inferring rules for generics, but it's better for Angular type
        // checking to be less strict here. Additionally, if `any` type is passed to e.g. async pipe it's going to be resolved
        // with `null`, so we need to check for `null` and `undefined` as well
        val anyType = JSAnyType.get(inputType.source)
        TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(propertyType).accept(object : JSRecursiveTypeVisitor(true) {
          override fun visitJSType(type: JSType) {
            if (type is JSGenericParameterType) {
              genericArguments.putValue(type.genericId, anyType)
            }
            super.visitJSType(type)
          }
        })
      }
      else {
        JSGenericTypesEvaluatorBase
          .matchGenericTypes(JSGenericMappings(genericArguments), processingContext, inputType, propertyType)
        JSGenericTypesEvaluatorBase
          .widenInferredTypes(genericArguments, listOf(propertyType), null, null, processingContext)
      }
    }

    private fun intersectGenerics(arguments: MultiMap<JSTypeGenericId, JSType?>,
                                  source: JSTypeSource): JSTypeSubstitutor {
      val result = JSTypeSubstitutorImpl()
      for ((key, value) in arguments.entrySet()) {
        result.put(key, merge(source, value.filterNotNull(), false))
      }
      return result
    }

    private fun merge(source: JSTypeSource, types: List<JSType?>, union: Boolean): JSType {
      val type = if (union) createUnionType(source, types) else createIntersectionType(types, source)
      return JSCompositeTypeImpl.optimizeTypeIfComposite(type, OptimizedKind.OPTIMIZED_SIMPLE)
    }

    private fun getTypeSource(element: PsiElement,
                              templateContextTypes: List<JSType?>,
                              genericArguments: MultiMap<JSTypeGenericId, JSType?>): JSTypeSource? {
      val source = getTypeSource(element, templateContextTypes)
      return source
             ?: genericArguments.values().find { it != null }?.source
    }

    private fun getTypeSource(element: PsiElement,
                              types: List<JSType?>): JSTypeSource? {
      val componentClass = findComponentClass(element)
      return if (componentClass != null) {
        JSTypeSourceFactory.createTypeSource(componentClass, true)
      }
      else types.firstOrNull()?.source
    }

    private fun getTemplateContextTypeNonStrict(cls: TypeScriptClass): JSType? {
      var templateRefType: JSType? = null
      for (ctor in cls.constructors) {
        for (param in ctor.parameterVariables) {
          if (param.jsType.let { it != null && it.typeText.startsWith("$TEMPLATE_REF<") }) {
            templateRefType = param.jsType
            break
          }
        }
      }
      return if (templateRefType !is JSGenericTypeImpl) {
        null
      }
      else templateRefType.arguments.firstOrNull()
    }
  }

  /**
   * This class doesn't simplify much, but allows using find usages to find related code for different web frameworks.
   */
  class WebJSSyntheticFunctionCall(val place: PsiElement?,
                                   val argumentListProvider: (typeFactory: JSExpressionTypeFactory) -> List<JSType?>) : JSCallItem {
    override fun getPsiContext(): PsiElement? = place

    override fun getArgumentTypes(argumentTypeFactory: JSExpressionTypeFactory): List<JSType?> {
      return argumentListProvider(argumentTypeFactory)
    }
  }
}