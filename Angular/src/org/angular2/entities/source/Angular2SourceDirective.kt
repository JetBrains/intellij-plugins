// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSWidenType
import com.intellij.lang.javascript.psi.types.typescript.TypeScriptCompilerType
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.psi.util.getStubSafeChildren
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.model.Pointer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.AstLoadingFilter
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolQualifiedKind
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.HOST_DIRECTIVES_PROP
import org.angular2.Angular2DecoratorUtil.INJECT_FUN
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.INPUT_FUN
import org.angular2.Angular2DecoratorUtil.MODEL_FUN
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_FROM_OBSERVABLE_FUN
import org.angular2.Angular2DecoratorUtil.OUTPUT_FUN
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver
import org.angular2.entities.*
import org.angular2.entities.Angular2DirectiveKind.Companion.plus
import org.angular2.entities.Angular2EntityUtils.ELEMENT_REF
import org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF
import org.angular2.entities.Angular2EntityUtils.VIEW_CONTAINER_REF
import org.angular2.entities.Angular2HostDirectivesResolver.HostDirectivesCollector
import org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity
import org.angular2.entities.ivy.Angular2IvyUtil.withJsonMetadataFallback
import org.angular2.entities.metadata.Angular2MetadataUtil
import org.angular2.entities.source.Angular2SourceUtil.getExportAs
import org.angular2.entities.source.Angular2SourceUtil.parseInputObjectLiteral
import org.angular2.entities.source.Angular2SourceUtil.readDirectivePropertyMappings
import org.angular2.index.getFunctionNameFromIndex
import org.angular2.index.isStringArgStubbed
import org.angular2.lang.types.Angular2TypeUtils
import org.angular2.web.ELEMENT_NG_TEMPLATE
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.NG_DIRECTIVE_IN_OUTS
import org.angular2.web.NG_DIRECTIVE_OUTPUTS

open class Angular2SourceDirective(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceDeclaration(decorator, implicitElement), Angular2ClassBasedDirective,
    Angular2HostDirectivesResolver.Angular2DirectiveWithHostDirectives {

  @Suppress("LeakingThis")
  private val hostDirectivesResolver = Angular2HostDirectivesResolver(this)

  override val selector: Angular2DirectiveSelector
    get() = getCachedValue {
      Result.create(
        Angular2SourceUtil.getComponentSelector(decorator, Angular2DecoratorUtil.getProperty(decorator, Angular2DecoratorUtil.SELECTOR_PROP)),
        decorator.containingFile)
    }

  override val directiveKind: Angular2DirectiveKind
    get() = getCachedValue {
      Result.create(getDirectiveKindNoCache(typeScriptClass, selector), classModificationDependencies)
    }

  override val exportAs: Map<String, Angular2DirectiveExportAs>
    get() = hostDirectivesResolver.exportAs

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = getCachedValue {
      Result.create(getAttributesNoCache(), classModificationDependencies)
    }

  override val bindings: Angular2DirectiveProperties
    get() = getCachedValue {
      Result.create(getPropertiesNoCache(), classModificationDependencies)
    }

  override val hostDirectives: Collection<Angular2HostDirective>
    get() = hostDirectivesResolver.hostDirectives

  override fun areHostDirectivesFullyResolved(): Boolean =
    hostDirectivesResolver.hostDirectivesFullyResolved

  override fun createPointer(): Pointer<out Angular2Directive> {
    return createPointer { decorator, implicitElement ->
      Angular2SourceDirective(decorator, implicitElement)
    }
  }

  override val directHostDirectivesSet: Angular2ResolvedSymbolsSet<Angular2HostDirective>
    get() = decorator.let { dec ->
      CachedValuesManager.getCachedValue(dec) {
        HostDirectivesCollector(dec).collect(Angular2DecoratorUtil.getProperty(dec, HOST_DIRECTIVES_PROP))
      }
    }

  override val directExportAs: Map<String, Angular2DirectiveExportAs>
    get() = getCachedValue { Result.create(getExportAsNoCache(), decorator) }

  private fun getExportAsNoCache(): Map<String, Angular2DirectiveExportAs> =
    AstLoadingFilter.forceAllowTreeLoading<Map<String, Angular2DirectiveExportAs>, Throwable>(decorator.containingFile) {
      getExportAs(this, Angular2DecoratorUtil.getProperty(decorator, Angular2DecoratorUtil.EXPORT_AS_PROP))
    }

  private fun getPropertiesNoCache(): Angular2DirectiveProperties {
    JSTypeEvaluationLocationProvider.assertLocationIsSet()
    val inputs = LinkedHashMap<String, Angular2DirectiveProperty>()
    val outputs = LinkedHashMap<String, Angular2DirectiveProperty>()
    val inOuts = LinkedHashMap<String, Angular2DirectiveProperty>()

    val inputMap = readPropertyMappings(Angular2DecoratorUtil.INPUTS_PROP)
    val outputMap = readPropertyMappings(Angular2DecoratorUtil.OUTPUTS_PROP)

    val clazz = typeScriptClass

    Angular2TypeUtils
      .buildTypeFromClass(clazz)
      .properties
      .forEach { prop ->
        for (el in getPropertySources(prop.memberSource.singleElement)) {
          processProperty(clazz, prop, el, inputMap, INPUT_DEC,
                          listOf(INPUT_FUN),
                          NG_DIRECTIVE_INPUTS, inputs)
          processProperty(clazz, prop, el, outputMap, OUTPUT_DEC,
                          listOf(OUTPUT_FUN, OUTPUT_FROM_OBSERVABLE_FUN),
                          NG_DIRECTIVE_OUTPUTS, outputs)
          processProperty(clazz, prop, el, null, null,
                          listOf(MODEL_FUN),
                          NG_DIRECTIVE_IN_OUTS, inOuts)
        }
      }

    inputMap.values.forEach { info ->
      inputs[info.name] = Angular2SourceDirectiveVirtualProperty(clazz, NG_DIRECTIVE_INPUTS, info)
    }
    outputMap.values.forEach { info ->
      outputs[info.name] = Angular2SourceDirectiveVirtualProperty(clazz, NG_DIRECTIVE_OUTPUTS, info)
    }

    val inheritedProperties = Ref<Angular2DirectiveProperties>()
    JSClassUtils.processClassesInHierarchy(clazz, false) { aClass, _, _ ->
      if (aClass is TypeScriptClass && Angular2EntitiesProvider.isDeclaredClass(aClass)) {
        val props = withJsonMetadataFallback(
          aClass,
          { getIvyEntity(it, true).asSafely<Angular2Directive>()?.bindings },
          { Angular2MetadataUtil.getMetadataClassDirectiveProperties(it) }
        )
        if (props != null) {
          inheritedProperties.set(props)
          return@processClassesInHierarchy false
        }
      }
      true
    }

    if (!inheritedProperties.isNull) {
      inheritedProperties.get().inputs.forEach { prop ->
        inputs.putIfAbsent(prop.name, prop)
      }
      inheritedProperties.get().outputs.forEach { prop ->
        outputs.putIfAbsent(prop.name, prop)
      }
    }
    return Angular2DirectiveProperties(inputs.values, outputs.values, inOuts.values)
  }

  private fun getAttributesNoCache(): Collection<Angular2DirectiveAttribute> {
    JSTypeEvaluationLocationProvider.assertLocationIsSet()
    val constructors = typeScriptClass.constructors
    return if (constructors.size == 1)
      processCtorParameters(constructors[0])
    else
      constructors.firstOrNull { it.isOverloadImplementation }
        ?.let { processCtorParameters(it) }
      ?: emptyList()
  }

  private fun readPropertyMappings(source: String): MutableMap<String, Angular2PropertyInfo> =
    readDirectivePropertyMappings(Angular2DecoratorUtil.getProperty(decorator, source))

  companion object {
    private fun processProperty(
      sourceClass: TypeScriptClass,
      property: JSRecordType.PropertySignature,
      field: JSAttributeListOwner,
      mappings: MutableMap<String, Angular2PropertyInfo>?,
      decorator: String?,
      functionNames: List<String>,
      qualifiedKind: WebSymbolQualifiedKind,
      result: MutableMap<String, Angular2DirectiveProperty>,
    ) {
      val info: Angular2PropertyInfo? =
        mappings?.remove(property.memberName)
        ?: field.attributeList
          ?.decorators
          ?.firstOrNull { it.decoratorName == decorator }
          ?.let { createPropertyInfo(it, property.memberName) }
        ?: field.asSafely<TypeScriptField>()
          ?.initializerOrStub
          ?.asSafely<JSCallExpression>()
          ?.let { Angular2SourceUtil.createPropertyInfo(it, functionNames, property.memberName, ::getFunctionNameFromIndex) }
      if (info != null) {
        result.putIfAbsent(info.name, Angular2SourceDirectiveProperty.create(sourceClass, property, qualifiedKind, info))
      }
    }

    private fun processCtorParameters(ctor: JSFunction): Collection<Angular2DirectiveAttribute> {
      return ctor.parameterVariables
        .flatMap { param ->
          param.attributeList
            ?.decorators
            ?.asSequence()
            ?.filter { Angular2DecoratorUtil.ATTRIBUTE_DEC == it.decoratorName }
            ?.mapNotNull { getStringParamValue(it)?.takeIf { value -> !value.isBlank() } }
            ?.map { Angular2SourceDirectiveAttribute(param, it) }
          ?: emptySequence()
        }
        .distinctBy { it.name }
    }

    private fun getStringParamValue(decorator: ES6Decorator?): String? =
      getDecoratorParamValue(decorator)
        ?.asSafely<JSLiteralExpression>()
        ?.stubSafeStringValue

    private fun getDecoratorParamValue(decorator: ES6Decorator?): PsiElement? =
      decorator
        ?.takeIf { it.isStringArgStubbed() }
        ?.getStubSafeChildren<JSCallExpression>()
        ?.firstOrNull()
        ?.stubSafeCallArguments
        ?.firstOrNull()

    @JvmStatic
    fun getDirectiveKindNoCache(clazz: TypeScriptClass, selector: Angular2DirectiveSelector): Angular2DirectiveKind {
      JSTypeEvaluationLocationProvider.assertLocationIsSet()
      val allNgTemplateSelectors = selector.simpleSelectors.all { it.elementName == ELEMENT_NG_TEMPLATE }
      if (allNgTemplateSelectors) {
        return Angular2DirectiveKind.STRUCTURAL
      }

      val anyNgTemplateSelector = selector.simpleSelectors.any { it.elementName == ELEMENT_NG_TEMPLATE }
      var anyNgTemplateContextGuard = false
      var result: Angular2DirectiveKind? = null
      JSClassUtils.processClassesInHierarchy(clazz, false) { aClass, _, _ ->
        if (aClass is TypeScriptClass) {
          anyNgTemplateContextGuard = anyNgTemplateContextGuard
                                      || aClass.members.any { it.name == Angular2ControlFlowBuilder.NG_TEMPLATE_CONTEXT_GUARD }
          result = getDirectiveKindFromConstructors(aClass, clazz, anyNgTemplateContextGuard) +
                   getDirectiveKindFromFields(aClass, clazz, anyNgTemplateContextGuard)
        }
        result == null
      }
      return (result ?: Angular2DirectiveKind.REGULAR)
        .applyIf(anyNgTemplateSelector || anyNgTemplateContextGuard) { this + Angular2DirectiveKind.STRUCTURAL }
    }

    private fun getDirectiveKindFromConstructors(
      aClass: TypeScriptClass,
      clazz: TypeScriptClass,
      anyNgTemplateContextGuard: Boolean,
    ): Angular2DirectiveKind? =
      aClass.constructors
        .mapNotNull { it.parameterList }
        .mapNotNull { paramList ->
          processJSTypeOwnersList(paramList.parameters.toList(), clazz, anyNgTemplateContextGuard)
        }
        .reduceOrNull { acc, kind -> acc + kind }

    private fun getDirectiveKindFromFields(
      aClass: TypeScriptClass,
      clazz: TypeScriptClass,
      anyNgTemplateContextGuard: Boolean,
    ): Angular2DirectiveKind? =
      processJSTypeOwnersList(
        aClass.fields.filter { it.initializerOrStub?.asSafely<JSCallExpression>()?.let { getFunctionNameFromIndex(it) } == INJECT_FUN },
        clazz, anyNgTemplateContextGuard
      )

    private fun processJSTypeOwnersList(
      list: List<JSTypeOwner>,
      clazz: TypeScriptClass,
      anyNgTemplateContextGuard: Boolean,
    ): Angular2DirectiveKind? {
      var hasElementRef = false
      var hasTemplateRef = false
      var hasViewContainerRef = false
      var isTemplateRefOptional = false
      list.forEach {
        val typeText = it.getJSType(clazz)
                         ?.let { type -> if (type is TypeScriptCompilerType || type is JSWidenType) type.substitute(clazz) else type }
                         ?.typeText
                       ?: return@forEach
        when {
          typeText.contains(ELEMENT_REF) -> hasElementRef = true
          typeText.contains(VIEW_CONTAINER_REF) -> hasViewContainerRef = true
          typeText.contains(TEMPLATE_REF) -> {
            hasTemplateRef = true
            isTemplateRefOptional = it is JSAttributeListOwner
                                    && Angular2DecoratorUtil.findDecorator(it, Angular2DecoratorUtil.OPTIONAL_DEC) != null
          }
        }
      }
      return Angular2DirectiveKind.get(hasElementRef, hasTemplateRef, hasViewContainerRef, isTemplateRefOptional, anyNgTemplateContextGuard)
    }

    @JvmStatic
    internal fun getPropertySources(property: PsiElement?): List<JSAttributeListOwner> {
      if (property is TypeScriptFunction) {
        if (!property.isSetProperty && !property.isGetProperty) {
          return listOf(property)
        }
        val result = mutableListOf<JSAttributeListOwner>(property)
        Angular2ReferenceExpressionResolver.findPropertyAccessor(property, property.isGetProperty) { result.add(it) }
        return result
      }
      else if (property is JSAttributeListOwner) {
        return listOf(property)
      }
      return emptyList()
    }

    private fun createPropertyInfo(decorator: ES6Decorator, defaultName: String): Angular2PropertyInfo =
      when (val param = getDecoratorParamValue(decorator)) {
        is JSObjectLiteralExpression -> parseInputObjectLiteral(param, defaultName)
        is JSLiteralExpression -> param.stubSafeStringValue.let { name ->
          Angular2PropertyInfo(name ?: defaultName, false, param, declaringElement = if (name != null) param else null)
        }
        else -> Angular2PropertyInfo(defaultName, false, decorator, declaringElement = null)
      }

  }
}
