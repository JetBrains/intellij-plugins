// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.evaluation.JSExpressionTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.lang.javascript.psi.resolve.JSGenericMappings
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluatorBase
import com.intellij.lang.javascript.psi.resolve.generic.JSTypeSubstitutorImpl
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory.createIntersectionType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory.createUnionType
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor.EMPTY
import com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType.OptimizedKind
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.javascript.psi.types.typescript.TypeScriptCompilerType
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.resolve.TypeScriptCompilerEvaluationFacade
import com.intellij.lang.typescript.resolve.TypeScriptGenericTypesEvaluator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.*
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import com.intellij.util.SmartList
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import com.intellij.util.containers.MultiMap
import com.intellij.xml.util.XmlTagUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2LibrariesHacks.hackNgModelChangeType
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.config.Angular2Compiler.isStrictTemplates
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder.Companion.NG_TEMPLATE_CONTEXT_GUARD
import org.angular2.entities.*
import org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.expr.service.Angular2TypeScriptService
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveAndTopLevelSourceFile
import org.angular2.lang.types.Angular2TypeUtils.possiblyGenericJsType
import java.util.function.BiFunction
import java.util.function.Predicate

internal class BindingsTypeResolver private constructor(
  val element: PsiElement,
  provider: Angular2ApplicableDirectivesProvider,
  inputWeightProvider: () -> Map<String, Int>,
  plainAttributesProvider: () -> Map<String, String>,
  inputExpressionsProvider: () -> Map<String, JSExpression?>,
  elementNameRangeProvider: () -> TextRange,
) {
  private val analysisResult: AnalysisResult?

  init {
    val declarationsScope = Angular2DeclarationsScope(element)
    val directives = provider.matched.filter { declarationsScope.contains(it) }
    val service = if (element.project.service<TypeScriptCompilerEvaluationFacade>().isAnyEnabled())
      TypeScriptServiceHolder.getForElement(element)?.service
        ?.takeIf { it.isTypeEvaluationEnabled() }
    else
      null
    analysisResult = when {
      directives.isEmpty() -> AnalysisResult.EMPTY
      // Use service to analyze the file if there is an associated component file.
      // Fallback to WebStorm type inference engine otherwise.
      // Don't use fallback in tests to ensure we don't switch to WebStorm type inference unintentionally
      service is Angular2TypeScriptService && (ApplicationManager.getApplication().isUnitTestMode
                                               || Angular2EntitiesProvider.findTemplateComponent(element) != null) ->
        analyzeService(directives, element, elementNameRangeProvider(), service)
      isStrictTemplates(element) -> analyzeStrictTemplates(
        directives, element, inputWeightProvider(), plainAttributesProvider(), inputExpressionsProvider())
      else -> analyzeNonStrict(directives, element, inputExpressionsProvider())
    }
  }

  fun resolveDirectiveInputType(inputName: String): JSType? {
    val directives = analysisResult?.directives ?: return null
    val substitutors = analysisResult.substitutors
    val types: MutableList<JSType?> = SmartList()
    for (directive in directives) {
      findDirectivePropertyTypeAndOrigin(directive, directive.inputs, inputName)?.let { (propertyType, directive) ->
        val type = JSTypeUtils.applyGenericArguments(propertyType?.substituteIfCompilerType(element), substitutors[directive])
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
        val type = JSTypeUtils.applyGenericArguments(propertyType?.substituteIfCompilerType(element), substitutors[directive])
        types.add(hackNgModelChangeType(type, outputName))
      }
    }
    return postprocessTypes(types)
  }

  private fun findDirectivePropertyTypeAndOrigin(
    directive: Angular2Directive,
    properties: Collection<Angular2DirectiveProperty>,
    name: String,
  ): Pair<JSType?, Angular2Directive>? {
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
          val actualDirective = directive.exportAs[exportName]?.directive?.asSafely<Angular2ClassBasedDirective>()
          cls = actualDirective?.typeScriptClass ?: continue
          instanceSubstitutor = substitutors[actualDirective]
        }
        directive.isComponent -> {
          cls = directive.asSafely<Angular2ClassBasedDirective>()?.typeScriptClass ?: continue
          instanceSubstitutor = substitutors[directive]
        }
        else -> continue
      }

      return if (instanceSubstitutor != null)
        JSTypeUtils.applyGenericArguments(cls.possiblyGenericJsType, instanceSubstitutor)
      else {
        // In the case of references, it may happen that some generic params are not substituted.
        // Let's be permissive here and replace each generic param from directive definition
        // with `any` type to avoid type checking errors in such situation.
        val genericParameters = cls.typeParameters.mapTo(HashSet()) { it.genericId }
        postprocessTypes(listOf(cls.possiblyGenericJsType))?.transformTypeHierarchy {
          if (it is JSGenericParameterType && genericParameters.contains(it.genericId))
            JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS)
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

  fun substituteType(directive: Angular2Directive?, jsType: JSType?): JSType? =
    JSTypeUtils.applyGenericArguments(jsType, analysisResult?.substitutors?.get(directive)
                                              ?: analysisResult?.mergedSubstitutor)

  fun substituteTypeForDocumentation(directive: Angular2Directive?, jsType: JSType?): JSType? =
    JSTypeUtils.applyGenericArguments(jsType, analysisResult?.strictSubstitutors?.get(directive)
                                              ?: analysisResult?.mergedSubstitutor)

  fun getTypeSubstitutorForDocumentation(directive: Angular2Directive?): JSTypeSubstitutor? =
    analysisResult?.strictSubstitutors?.get(directive)
    ?: analysisResult?.mergedSubstitutor

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
    return JSCompositeTypeFactory.optimizeTypeForSubstitute(merge(source, notNullTypes, false), element)
  }

  companion object {

    fun resolve(
      attribute: XmlAttribute,
      expectedTypeKind: JSExpectedTypeKind?,
      infoValidation: Predicate<Angular2AttributeNameParser.AttributeInfo>,
      resolveMethod: BiFunction<BindingsTypeResolver, String, JSType?>,
    ): JSType? {
      val descriptor = attribute.descriptor as? Angular2AttributeDescriptor
      val tag = attribute.parent
      val info = Angular2AttributeNameParser.parse(attribute.name, attribute.parent)
      return if (descriptor == null || tag == null || !infoValidation.test(info)) {
        null
      }
      else {
        val resolver = if (expectedTypeKind == JSExpectedTypeKind.EXPECTED)
          create(tag, attribute)
        else
          get(tag)
        resolveMethod.apply(resolver, info.name)
      }
    }

    fun resolve(
      bindings: Angular2TemplateBindings,
      key: String,
      expectedTypeKind: JSExpectedTypeKind?,
      resolveMethod: BiFunction<BindingsTypeResolver, String, JSType?>,
    ): JSType? {
      val resolver = if (expectedTypeKind == JSExpectedTypeKind.EXPECTED)
        create(bindings, key)
      else
        get(bindings)
      return resolveMethod.apply(resolver, key)
    }

    fun get(tag: XmlTag): BindingsTypeResolver =
      CachedValuesManager.getCachedValue(tag) {
        CachedValueProvider.Result.create(create(tag), PsiModificationTracker.MODIFICATION_COUNT)
      }

    fun get(bindings: Angular2TemplateBindings): BindingsTypeResolver =
      CachedValuesManager.getCachedValue(bindings) {
        CachedValueProvider.Result.create(create(bindings), PsiModificationTracker.MODIFICATION_COUNT)
      }

    fun get(location: PsiElement?): BindingsTypeResolver? =
      location
        ?.parentOfTypes(XmlTag::class, Angular2HtmlTemplateBindings::class)
        ?.let {
          when (it) {
            is XmlTag -> get(it)
            is Angular2HtmlTemplateBindings -> get(it.bindings)
            else -> throw RuntimeException("unreachable")
          }
        }

    private fun create(bindings: Angular2TemplateBindings, filteredKey: String? = null) =
      BindingsTypeResolver(
        bindings, Angular2ApplicableDirectivesProvider(bindings),
        {
          bindings.bindings.asSequence()
            .mapIndexedNotNull { ind, binding ->
              if (!binding.keyIsVar())
                binding.key to ind
              else
                null
            }
            .plus(bindings.templateName to -1)
            .distinctBy { it.first }
            .toMap()
        },
        {
          if (bindings.templateName != filteredKey)
            mapOf(bindings.templateName to "")
          else
            emptyMap()
        }, {
          bindings.bindings
            .asSequence()
            .filter { !it.keyIsVar() && it.key != filteredKey }
            .mapNotNull { Pair(it.key, it.expression ?: return@mapNotNull null) }
            .distinctBy { it.first }
            .toMap()
        }, {
          val attribute = (InjectedLanguageManager.getInstance(bindings.project).getInjectionHost(bindings).takeIf { it !is JSElement }
                           ?: bindings)
            .parentOfType<XmlAttribute>(true)
          attribute?.nameElement?.textRange ?: bindings.textRange
        })

    private fun create(tag: XmlTag, filteredAttribute: XmlAttribute? = null) =
      BindingsTypeResolver(
        tag, Angular2ApplicableDirectivesProvider(tag),
        {
          tag.mapAttrsToMapIndexed(filteredAttribute) { index, attr ->
            Angular2AttributeNameParser.parse(attr.name, attr.parent)
              .takeIf { Angular2PropertyBindingType.isPropertyBindingAttribute(it) || it.type == Angular2AttributeType.REGULAR }
              ?.let { it.name to index }
          }
        },
        {
          tag.mapAttrsToMapIndexed(filteredAttribute) { _, attr ->
            if (Angular2AttributeNameParser.parse(attr.name, attr.parent).type == Angular2AttributeType.REGULAR)
              attr.name to (attr.value ?: "")
            else null
          }
        }, {
          tag.mapAttrsToMapIndexed(filteredAttribute) { _, attr ->
            val name = Angular2AttributeNameParser.parse(attr.name, attr.parent)
                         .takeIf { Angular2PropertyBindingType.isPropertyBindingAttribute(it) }
                         ?.name
                       ?: return@mapAttrsToMapIndexed null
            Pair(name, Angular2Binding.get(attr)?.expression)
          }
        }, {
          XmlTagUtil.getStartTagNameElement(tag)?.textRange
          ?: XmlTagUtil.getStartTagRange(tag)
          ?: tag.textRange
        })

    private fun <T> XmlTag.mapAttrsToMapIndexed(
      filteredAttribute: XmlAttribute?,
      transform: (index: Int, XmlAttribute) -> Pair<String, T>?,
    ): Map<String, T> =
      attributes
        .asSequence()
        .filter { it != filteredAttribute }
        .mapIndexedNotNull(transform)
        .distinctBy { it.first }
        .toMap()

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

    private fun analyzeService(directives: List<Angular2Directive>, element: PsiElement, nameRange: TextRange, service: Angular2TypeScriptService): AnalysisResult? {
      val (transpiledComponentFile, templateFile) = getTranspiledDirectiveAndTopLevelSourceFile(element)
                                                    ?: return null

      val injectedLanguageManager = InjectedLanguageManager.getInstance(element.project)
      val templateMappings = transpiledComponentFile.fileMappings[templateFile]
                             ?: return null

      val adjustedNameRange = if (templateFile != element.containingFile)
        injectedLanguageManager.injectedToHost(element.containingFile, nameRange)
      else
        nameRange

      val templateContextType = templateMappings.contextVarMappings[adjustedNameRange]
        ?.let { service.typeEvaluationSupport.getGeneratedElementType(transpiledComponentFile, templateFile, it) }
        ?.substitute(element)
      val substitutors = directives.associateBy({ it }) { directive ->
        val directiveInstanceType = templateMappings.directiveVarMappings[Pair(adjustedNameRange, directive)]
          ?.let { service.typeEvaluationSupport.getGeneratedElementType(transpiledComponentFile, templateFile, it)?.substitute(element) }
        buildJSTypeSubstitutor(directive, directiveInstanceType)
      }
      // TODO - `strictSubstitutors` should not contain implicit `any` substitution,
      //        which happens when a directive does not have an input, which
      //        determines some generic parameter.
      //        E.g. Angular2DocumentationTest.testDirectiveWithGenerics
      val strictSubstitutors = substitutors

      return AnalysisResult(
        null,
        templateContextType = templateContextType,
        directives = directives,
        substitutors = substitutors,
        strictSubstitutors = strictSubstitutors,
      )
    }

    private fun buildJSTypeSubstitutor(directive: Angular2Directive, directiveInstanceType: JSType?): JSTypeSubstitutor? {
      if (directiveInstanceType !is JSGenericTypeImpl)
        return null
      val cls = (directive as? Angular2ClassBasedDirective)?.typeScriptClass
                ?: return null

      val result = JSTypeSubstitutorImpl()
      val arguments = directiveInstanceType.arguments

      cls.typeParameters.forEachIndexed { index, typeScriptTypeParameter ->
        if (index < arguments.size) {
          result.put(typeScriptTypeParameter.genericId, arguments[index])
        }
      }

      return result
    }

    private fun analyzeStrictTemplates(
      directives: List<Angular2Directive>,
      element: PsiElement,
      weightMap: Map<String, Int>,
      attrsMap: Map<String, String>,
      inputsMap: Map<String, JSExpression?>,
    ): AnalysisResult {
      val substitutors = mutableMapOf<Angular2Directive, JSTypeSubstitutor?>()
      val strictSubstitutors = mutableMapOf<Angular2Directive, JSTypeSubstitutor?>()
      val templateContextTypes: MutableList<JSType> = SmartList()

      val directives2inputs = mutableMapOf<Angular2Directive, MutableList<DirectiveInput>>()

      directives.forEach {
        directives2inputs.computeIfAbsent(it) { mutableListOf() }
        it.inputs.forEach { input ->
          val directive = (input as? Angular2AliasedDirectiveProperty)?.directive ?: it
          val inputName = input.name
          val propertyType = input.type

          if (propertyType != null) {
            directives2inputs.computeIfAbsent(directive) { mutableListOf() }
              .add(DirectiveInput(propertyType.substituteIfCompilerType(element),
                                  inputsMap.containsKey(inputName),
                                  inputsMap[inputName],
                                  attrsMap[inputName],
                                  weightMap[inputName] ?: 0))
          }
        }
      }


      directives2inputs.forEach { (directive, directiveInputs) ->
        val cls = directive.asSafely<Angular2ClassBasedDirective>()?.typeScriptClass ?: return@forEach
        val elementTypeSource = JSTypeSourceFactory.createTypeSource(element, true)
          .copyWithNewLanguage(JSTypeSource.SourceLanguage.TS) // sometimes we get the <ng-template> element, so we need to force TS
        val classTypeSource = cls.staticJSType.source

        val genericConstructorReturnType = cls.possiblyGenericJsType
        val typeSubstitutor: JSTypeSubstitutor
        if (genericConstructorReturnType is JSGenericTypeImpl) {
          val clsTypeParams = cls.typeParameters
          typeSubstitutor = calculateDirectiveTypeSubstitutor(
            classTypeSource, clsTypeParams, directiveInputs.sortedBy { it.weight }, element, elementTypeSource)

          strictSubstitutors[directive] = typeSubstitutor
          substitutors[directive] = JSTypeSubstitutorImpl(typeSubstitutor).also {
            clsTypeParams.forEach { typeParameter ->
              if (it.get(typeParameter.genericId) is JSUnknownType
                  || !it.containsId(typeParameter.genericId)) {
                it.put(typeParameter.genericId, JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS))
              }
            }
          }
        }
        else {
          typeSubstitutor = EMPTY
          strictSubstitutors[directive] = EMPTY
          substitutors[directive] = EMPTY
        }

        val guardElement = cls.findFunctionByName(NG_TEMPLATE_CONTEXT_GUARD)
        if (guardElement != null && guardElement.jsContext == JSContext.STATIC) {
          val guardFunctionType = JSPsiBasedTypeOfType(guardElement, false).substitute(element) // expected JSFunctionType

          val instanceType = JSTypeUtils.applyGenericArguments(genericConstructorReturnType, typeSubstitutor)
          val callType = JSApplyCallType(guardFunctionType, listOf(instanceType, JSUnknownType.TS_INSTANCE), classTypeSource)
          val predicateType = callType.substitute(element)

          var contextType: JSType? = null
          if (predicateType is TypeScriptTypePredicateTypeImpl) {
            contextType = predicateType.guardType
          }
          if (contextType != null) {
            templateContextTypes.add(contextType.substitute(element))
          }
        }
      }

      val typeSource = getTypeSource(element, templateContextTypes, MultiMap.empty()) ?: return AnalysisResult.EMPTY
      // we merge templateContextTypes but Angular does something more akin to reduce
      val mergedTemplateContextType = if (templateContextTypes.isEmpty()) null else merge(typeSource, templateContextTypes, true)
      return AnalysisResult(null, mergedTemplateContextType, directives, substitutors, strictSubstitutors)
    }

    private fun calculateDirectiveTypeSubstitutor(
      classTypeSource: JSTypeSource,
      clsTypeParams: Array<TypeScriptTypeParameter>,
      directiveInputs: List<DirectiveInput>,
      element: PsiElement,
      elementTypeSource: JSTypeSource,
    ): JSTypeSubstitutor {
      val directiveFactoryType = TypeScriptJSFunctionTypeImpl(
        classTypeSource,
        TypeScriptGenericTypesEvaluator.buildGenericParameterDeclarations(clsTypeParams),
        directiveInputs.map { (expectedType) -> JSParameterTypeDecoratorImpl(expectedType, false, false, true) },
        null, null,
      )

      val directiveFactoryCall = WebJSSyntheticFunctionCall(element, directiveInputs.size) { typeFactory ->
        directiveInputs.map { (_, isBindingPresent, bindingExpression, attrValue) ->
          when (bindingExpression) {
            null -> when {
              attrValue != null -> JSStringLiteralTypeImpl(attrValue, true, elementTypeSource)
              isBindingPresent -> JSNamedTypeFactory.createUndefinedType(elementTypeSource)
              else -> JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS)
            }
            is JSEmptyExpression -> JSNamedTypeFactory.createUndefinedType(elementTypeSource)
            else -> typeFactory.evaluate(bindingExpression)
          }
        }
      }

      return TypeScriptGenericTypesEvaluator.getInstance()
        .getTypeSubstitutorForCallItem(directiveFactoryType, directiveFactoryCall, null)
    }

    private fun analyzeNonStrict(
      directives: List<Angular2Directive>,
      element: PsiElement,
      inputsMap: Map<String, JSExpression?>,
    ): AnalysisResult {
      val genericArguments = MultiMap<JSTypeGenericId, JSType?>()
      val templateContextTypes: MutableList<JSType> = SmartList()
      directives.forEach { directive ->
        val cls = directive.asSafely<Angular2ClassBasedDirective>()?.typeScriptClass ?: return@forEach

        getTemplateContextTypeNonStrict(cls, element)?.let { templateContextType ->
          templateContextTypes.add(templateContextType)
        }

        val processingContext = JSTypeComparingContextService.createProcessingContextWithCache(cls)
        directive.inputs.forEach { property ->
          val inputExpression = inputsMap[property.name]
          val propertyType = property.type
          if (inputExpression != null && propertyType != null) {
            collectGenericArgumentsNonStrict(inputExpression, propertyType.substituteIfCompilerType(element), processingContext, genericArguments)
          }
        }
      }

      val typeSource = getTypeSource(element, templateContextTypes, genericArguments) ?: return AnalysisResult.EMPTY
      val mergedTemplateContextType = if (templateContextTypes.isEmpty()) null else merge(typeSource, templateContextTypes, true)
      val mergedSubstitutor = if (genericArguments.isEmpty) null else intersectGenerics(genericArguments, typeSource)
      return AnalysisResult(mergedSubstitutor, mergedTemplateContextType, directives, emptyMap(), emptyMap())
    }

    private fun collectGenericArgumentsNonStrict(
      inputExpression: JSExpression,
      propertyType: JSType,
      processingContext: ProcessingContext,
      genericArguments: MultiMap<JSTypeGenericId, JSType?>,
    ) {
      val inputType = JSPsiBasedTypeOfType(inputExpression, true).substituteIfCompilerType(inputExpression)
      // todo getApparentType is supposed to be used only in JS according to usages in JS plugin
      val apparentType = JSTypeUtils.getApparentType(JSTypeWithIncompleteSubstitution.substituteCompletely(inputType))
      if (JSTypeUtils.isAnyType(apparentType)) {
        // This workaround is needed, because many users expect to have ngForOf working with variable of type `any`.
        // This is not correct according to TypeScript inferring rules for generics, but it's better for Angular type
        // checking to be less strict here. Additionally, if `any` type is passed to e.g. async pipe it's going to be resolved
        // with `null`, so we need to check for `null` and `undefined` as well
        val anyType = JSAnyType.get(inputType.source)
        TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(propertyType, inputExpression)
          .accept(object : JSRecursiveTypeVisitor(true) {
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

    private fun intersectGenerics(
      arguments: MultiMap<JSTypeGenericId, JSType?>,
      source: JSTypeSource,
    ): JSTypeSubstitutor {
      val result = JSTypeSubstitutorImpl()
      for ((key, value) in arguments.entrySet()) {
        result.put(key, merge(source, value.filterNotNull(), false))
      }
      return result
    }

    private fun merge(source: JSTypeSource, types: List<JSType?>, union: Boolean): JSType {
      val type = if (union) createUnionType(source, types) else createIntersectionType(types, source)
      return JSCompositeTypeFactory.optimizeTypeIfComposite(type, OptimizedKind.OPTIMIZED_SIMPLE)
    }

    private fun getTypeSource(
      element: PsiElement,
      templateContextTypes: List<JSType?>,
      genericArguments: MultiMap<JSTypeGenericId, JSType?>,
    ): JSTypeSource? {
      val source = getTypeSource(element, templateContextTypes)
      return source
             ?: genericArguments.values().find { it != null }?.source
    }

    private fun getTypeSource(
      element: PsiElement,
      types: List<JSType?>,
    ): JSTypeSource? {
      val resolveScope = Angular2EntitiesProvider.findTemplateComponent(element)?.jsResolveScope
      return if (resolveScope != null) {
        JSTypeSourceFactory.createTypeSource(resolveScope, true)
      }
      else types.firstOrNull()?.source
    }

    private fun getTemplateContextTypeNonStrict(cls: TypeScriptClass, context: PsiElement): JSType? {
      var templateRefType: JSType? = null
      for (ctor in cls.constructors) {
        for (param in ctor.parameterVariables) {
          val paramType = param.getJSType(context)
          if (paramType.let { it != null && it.typeText.startsWith("$TEMPLATE_REF<") }) {
            templateRefType = paramType
            break
          }
        }
      }
      return if (templateRefType !is JSGenericTypeImpl) {
        null
      }
      else templateRefType.arguments.firstOrNull()
    }

    private fun JSType.substituteIfCompilerType(location: PsiElement): JSType =
      applyIf(this is TypeScriptCompilerType) { substitute(location) }
  }

  /**
   * This class doesn't simplify much, but allows using find usages to find related code for different web frameworks.
   */
  class WebJSSyntheticFunctionCall(
    val place: PsiElement,
    private val argSize: Int,
    val argumentListProvider: (typeFactory: JSExpressionTypeFactory) -> List<JSType?>,
  ) : JSCallItem {
    override fun getPsiContext(): PsiElement = place

    override fun getArgumentTypes(argumentTypeFactory: JSExpressionTypeFactory): List<JSType?> {
      return argumentListProvider(argumentTypeFactory)
    }

    override fun getArgumentSize() = argSize
  }

  private data class DirectiveInput(
    val expectedType: JSType?,
    val isBindingPresent: Boolean,
    val bindingExpression: JSExpression?,
    val attributeValue: String?,
    val weight: Int,
  )
}