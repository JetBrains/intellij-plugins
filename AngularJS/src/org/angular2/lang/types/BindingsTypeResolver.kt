// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.javascript.web.js.WebJSTypesUtil.jsGenericType
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
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.typescript.resolve.TypeScriptGenericTypesEvaluator
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfTypes
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
import org.angular2.entities.Angular2AliasedDirectiveProperty
import org.angular2.entities.Angular2ComponentLocator.findComponentClass
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings
import java.util.function.BiFunction
import java.util.function.Predicate

internal class BindingsTypeResolver private constructor(val element: PsiElement,
                                                        provider: Angular2ApplicableDirectivesProvider,
                                                        inputExpressionsProvider: () -> Sequence<Pair<String, JSExpression>>) {
  private val analysisResult: AnalysisResult?

  init {
    val declarationsScope = Angular2DeclarationsScope(element)
    val directives = provider.matched.filter { declarationsScope.contains(it) }
    if (directives.isEmpty()) {
      analysisResult = AnalysisResult.EMPTY
    }
    else {
      analysisResult = analyze(directives, element, inputExpressionsProvider)
    }
  }

  fun resolveDirectiveInputType(inputName: String): JSType? {
    val directives = analysisResult?.directives ?: return null
    val substitutors = analysisResult.substitutors
    val types: MutableList<JSType?> = SmartList()
    for (directive in directives) {
      findDirectivePropertyTypeAndOrigin(directive, directive.inputs, inputName)?.let { (propertyType, directive) ->
        val type = JSTypeUtils.applyGenericArguments(propertyType, substitutors[directive])
        types.add(type)
      }
    }
    return postprocessTypes(types)
  }

  fun resolveDirectiveEventType(outputName: String): JSType? {
    val directives = analysisResult?.directives ?: return null
    val substitutors = analysisResult.substitutors
    val types: MutableList<JSType?> = SmartList()
    for (directive in directives) {
      findDirectivePropertyTypeAndOrigin(directive, directive.outputs, outputName)?.let { (propertyType, directive) ->
        val type = JSTypeUtils.applyGenericArguments(propertyType, substitutors[directive])
        types.add(hackNgModelChangeType(type, outputName))
      }
    }
    return postprocessTypes(types)
  }

  private fun findDirectivePropertyTypeAndOrigin(directive: Angular2Directive,
                                                 properties: Collection<Angular2DirectiveProperty>,
                                                 name: String): Pair<JSType?, Angular2Directive>? {
    return properties.find { it.name == name }?.let {
      Pair(it.type, if (it is Angular2AliasedDirectiveProperty) it.directive else directive)
    }
  }

  fun resolveDirectiveExportAsType(exportName: String?): JSType? {
    val directives = analysisResult?.directives ?: return null
    val substitutors = analysisResult.substitutors
    val hasExport = !exportName.isNullOrEmpty()

    for (directive in directives) {
      val cls: TypeScriptClass
      val instanceSubstitutor: JSTypeSubstitutor?
      when {
        hasExport -> {
          val actualDirective = directive.exportAs[exportName]?.directive
          cls = actualDirective?.typeScriptClass ?: continue
          instanceSubstitutor = substitutors[actualDirective]
        }
        directive.isComponent -> {
          cls = directive.typeScriptClass ?: continue
          instanceSubstitutor = substitutors[directive]
        }
        else -> continue
      }

      return if (instanceSubstitutor != null)
        JSTypeUtils.applyGenericArguments(cls.jsGenericType, instanceSubstitutor)
      else {
        // In the case of references, it may happen that some generic params are not substituted.
        // Let's be permissive here and replace each generic param from directive definition
        // with `any` type to avoid type checking errors in such situation.
        val genericParameters = cls.typeParameters.mapTo(HashSet()) { it.genericId }
        postprocessTypes(listOf(cls.jsGenericType))?.transformTypeHierarchy {
          if (it is JSGenericParameterType && genericParameters.contains(it.genericId))
            JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, false)
          else it
        }
      }
    }

    return null
  }

  fun resolveTemplateContextType(): JSType? {
    // substitutor is null in strictTemplates, substitution happens earlier
    return JSTypeUtils.applyGenericArguments(analysisResult?.templateContextType, analysisResult?.mergedSubstitutor)
  }

  fun substituteTypeForDocumentation(directive: Angular2Directive?, jsType: JSType?): JSType? =
    JSTypeUtils.applyGenericArguments(jsType, analysisResult?.strictSubstitutors?.get(directive)
                                              ?: analysisResult?.mergedSubstitutor)

  private fun postprocessTypes(types: List<JSType?>): JSType? {
    var notNullTypes = types.filterNotNull()
    val source = getTypeSource(element, notNullTypes)
    if (source == null || notNullTypes.isEmpty()) return null
    val mergedSubstitutor = analysisResult?.mergedSubstitutor
    if (mergedSubstitutor != null) { // substitutor is null in strictTemplates, substitution happens earlier
      notNullTypes = notNullTypes.mapNotNull {
        JSTypeUtils.applyGenericArguments(it, mergedSubstitutor)
      }
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

    fun get(location: PsiElement?) =
      location
        ?.parentOfTypes(XmlTag::class, Angular2HtmlTemplateBindings::class)
        ?.let {
          when (it) {
            is XmlTag -> get(it)
            is Angular2HtmlTemplateBindings -> get(it.bindings)
            else -> throw RuntimeException("unreachable")
          }
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

    data class AnalysisResult(
      val mergedSubstitutor: JSTypeSubstitutor?,
      val templateContextType: JSType?,
      val directives: List<Angular2Directive>,
      val substitutors: Map<Angular2Directive, JSTypeSubstitutor?>,
      val strictSubstitutors: Map<Angular2Directive, JSTypeSubstitutor?>,
    ) {
      companion object {
        val EMPTY = AnalysisResult(null, null, emptyList(), emptyMap(), emptyMap())
      }
    }

    private fun analyze(directives: List<Angular2Directive>,
                        element: PsiElement,
                        inputExpressionsProvider: () -> Sequence<Pair<String, JSExpression>>): AnalysisResult {
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
                                       inputsMap: Map<String, JSExpression>): AnalysisResult {
      val substitutors = mutableMapOf<Angular2Directive, JSTypeSubstitutor?>()
      val strictSubstitutors = mutableMapOf<Angular2Directive, JSTypeSubstitutor?>()
      val templateContextTypes: MutableList<JSType> = SmartList()

      val directives2inputs = mutableMapOf<Angular2Directive, MutableList<Pair<JSType, JSExpression?>>>()

      directives.forEach {
        directives2inputs.computeIfAbsent(it) { mutableListOf() }
        it.inputs.forEach { input ->
          val directive = (input as? Angular2AliasedDirectiveProperty)?.directive ?: it
          val inputExpression = inputsMap[input.name]
          val propertyType = input.type

          if (propertyType != null) {
            directives2inputs.computeIfAbsent(directive) { mutableListOf() }
              .add(Pair(propertyType, inputExpression))
          }
        }
      }


      directives2inputs.forEach { (directive, directiveInputs) ->
        val cls = directive.typeScriptClass ?: return@forEach
        val elementTypeSource = JSTypeSourceFactory.createTypeSource(element, true)
          .copyWithNewLanguage(JSTypeSource.SourceLanguage.TS) // sometimes we get the <ng-template> element, so we need to force TS
        val classTypeSource = cls.staticJSType.source

        val genericConstructorReturnType = JSTypeUtils.createNotSubstitutedGenericType(cls, cls.jsType)
        val typeSubstitutor = calculateDirectiveTypeSubstitutor(classTypeSource, directiveInputs, element, elementTypeSource)

        strictSubstitutors[directive] = typeSubstitutor
        substitutors[directive] = JSTypeSubstitutorImpl(typeSubstitutor).also {
          cls.typeParameters.forEach { typeParameter ->
            if (!it.containsId(typeParameter.genericId)) {
              it.put(typeParameter.genericId, JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, false))
            }
          }
        }

        val guardElement = cls.findFunctionByName(NG_TEMPLATE_CONTEXT_GUARD)
        if (guardElement != null && guardElement.jsContext == JSContext.STATIC) {
          val guardFunctionType = JSPsiBasedTypeOfType(guardElement, false).substitute() // expected JSFunctionType

          val instanceType = JSTypeUtils.applyGenericArguments(genericConstructorReturnType, typeSubstitutor)
          val callType = JSApplyCallType(guardFunctionType, listOf(instanceType, JSUnknownType.TS_INSTANCE), classTypeSource)
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

      val typeSource = getTypeSource(element, templateContextTypes, MultiMap.empty()) ?: return AnalysisResult.EMPTY
      // we merge templateContextTypes but Angular does something more akin to reduce
      val mergedTemplateContextType = if (templateContextTypes.isEmpty()) null else merge(typeSource, templateContextTypes, true)
      return AnalysisResult(null, mergedTemplateContextType, directives, substitutors, strictSubstitutors)
    }

    private fun calculateDirectiveTypeSubstitutor(classTypeSource: JSTypeSource,
                                                  directiveInputs: MutableList<Pair<JSType, JSExpression?>>,
                                                  element: PsiElement,
                                                  elementTypeSource: JSTypeSource): JSTypeSubstitutor {
      // conceptually: const Directive = (...) => {...}, it misses the type arguments <...> part, so we patch that later
      val directiveFactoryType = JSFunctionTypeImpl(
        classTypeSource,
        directiveInputs.map { (paramType, _) -> JSParameterTypeDecoratorImpl(paramType, false, false, true) },
        null // can pass null because we currently don't use JSApplyCallType anyway
      )

      val directiveFactoryCall = WebJSSyntheticFunctionCall(element, directiveInputs.size) { typeFactory ->
        directiveInputs.map { (_, expression) ->
          when (expression) {
            null -> JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, false)
            is JSEmptyExpression -> JSUndefinedType(elementTypeSource)
            else -> typeFactory.evaluate(expression)
          }
        }
      }

      // JSApplyCallType accepting TypeScriptJSFunctionTypeImpl would be the cleanest solution,
      // but we use base JSFunctionTypeImpl that doesn't contain List<TypeScriptGenericDeclarationTypeImpl>
      return TypeScriptGenericTypesEvaluator.getInstance()
        .getTypeSubstitutorForCallItem(directiveFactoryType, directiveFactoryCall, null)
    }

    private fun analyzeNonStrict(directives: List<Angular2Directive>,
                                 element: PsiElement,
                                 inputsMap: Map<String, JSExpression>): AnalysisResult {
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

      val typeSource = getTypeSource(element, templateContextTypes, genericArguments) ?: return AnalysisResult.EMPTY
      val mergedTemplateContextType = if (templateContextTypes.isEmpty()) null else merge(typeSource, templateContextTypes, true)
      val mergedSubstitutor = if (genericArguments.isEmpty) null else intersectGenerics(genericArguments, typeSource)
      return AnalysisResult(mergedSubstitutor, mergedTemplateContextType, directives, emptyMap(), emptyMap())
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
                                   private val argSize: Int,
                                   val argumentListProvider: (typeFactory: JSExpressionTypeFactory) -> List<JSType?>) : JSCallItem {
    override fun getPsiContext(): PsiElement? = place

    override fun getArgumentTypes(argumentTypeFactory: JSExpressionTypeFactory): List<JSType?> {
      return argumentListProvider(argumentTypeFactory)
    }

    override fun getArgumentSize() = argSize
  }
}