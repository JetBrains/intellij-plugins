// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
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
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolQualifiedKind
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.HOST_DIRECTIVES_PROP
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.INPUT_FUN
import org.angular2.Angular2DecoratorUtil.MODEL_FUN
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_FUN
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver
import org.angular2.entities.*
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
import org.angular2.lang.Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
import org.angular2.web.NG_DIRECTIVE_INPUTS
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
      Result.create(getDirectiveKindNoCache(typeScriptClass), classModificationDependencies)
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
    val inputs = LinkedHashMap<String, Angular2DirectiveProperty>()
    val outputs = LinkedHashMap<String, Angular2DirectiveProperty>()

    val inputMap = readPropertyMappings(Angular2DecoratorUtil.INPUTS_PROP)
    val outputMap = readPropertyMappings(Angular2DecoratorUtil.OUTPUTS_PROP)

    val clazz = typeScriptClass

    TypeScriptTypeParser
      .buildTypeFromClass(clazz, false)
      .properties
      .forEach { prop ->
        for (el in getPropertySources(prop.memberSource.singleElement)) {
          if (!processModelSignal(clazz, prop, el, inputs, outputs)) {
            processProperty(clazz, prop, el, inputMap, INPUT_DEC, INPUT_FUN, NG_DIRECTIVE_INPUTS, inputs)
            processProperty(clazz, prop, el, outputMap, OUTPUT_DEC, OUTPUT_FUN, NG_DIRECTIVE_OUTPUTS, outputs)
          }
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
    return Angular2DirectiveProperties(inputs.values, outputs.values)
  }

  private fun getAttributesNoCache(): Collection<Angular2DirectiveAttribute> {
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

    private fun processModelSignal(
      sourceClass: TypeScriptClass,
      property: JSRecordType.PropertySignature,
      field: JSAttributeListOwner,
      inputs: MutableMap<String, Angular2DirectiveProperty>,
      outputs: MutableMap<String, Angular2DirectiveProperty>,
    ): Boolean {
      val modelInfo = field.asSafely<TypeScriptField>()
        ?.initializerOrStub
        ?.asSafely<JSCallExpression>()
        ?.let { Angular2SourceUtil.createPropertyInfo(it, MODEL_FUN, property.memberName, ::getFunctionNameFromIndex) }
      if (modelInfo != null) {
        inputs.putIfAbsent(modelInfo.name, Angular2SourceDirectiveProperty.create(sourceClass, property, NG_DIRECTIVE_INPUTS, modelInfo))
        val outputModelInfo = modelInfo.copy(name = modelInfo.name + OUTPUT_CHANGE_SUFFIX)
        outputs.putIfAbsent(outputModelInfo.name, Angular2SourceDirectiveProperty.create(sourceClass, property, NG_DIRECTIVE_OUTPUTS, outputModelInfo))
        return true
      }
      else return false
    }

    private fun processProperty(sourceClass: TypeScriptClass,
                                property: JSRecordType.PropertySignature,
                                field: JSAttributeListOwner,
                                mappings: MutableMap<String, Angular2PropertyInfo>,
                                decorator: String,
                                functionName: String?,
                                qualifiedKind: WebSymbolQualifiedKind,
                                result: MutableMap<String, Angular2DirectiveProperty>) {
      val info: Angular2PropertyInfo? =
        mappings.remove(property.memberName)
        ?: field.attributeList
          ?.decorators
          ?.firstOrNull { it.decoratorName == decorator }
          ?.let { createPropertyInfo(it, property.memberName) }
        ?: field.asSafely<TypeScriptField>()
          ?.initializerOrStub
          ?.asSafely<JSCallExpression>()
          ?.let { Angular2SourceUtil.createPropertyInfo(it, functionName, property.memberName, ::getFunctionNameFromIndex) }
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
    fun getDirectiveKindNoCache(clazz: TypeScriptClass): Angular2DirectiveKind {
      val result = Ref<Angular2DirectiveKind>(null)
      JSClassUtils.processClassesInHierarchy(clazz, false) { aClass, _, _ ->
        if (aClass is TypeScriptClass) {
          val types = aClass.constructors
            .mapNotNull { it.parameterList }
            .flatMap { it.parameters.toList() }
            .mapNotNull { it.jsType }
            .map { type -> type.typeText }
            .toList()
          result.set(Angular2DirectiveKind.get(
            types.any { t -> t.contains(ELEMENT_REF) },
            types.any { t -> t.contains(TEMPLATE_REF) },
            types.any { t ->
              t.contains(VIEW_CONTAINER_REF)
            }))
        }
        result.isNull
      }
      return if (result.isNull) Angular2DirectiveKind.REGULAR else result.get()
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
