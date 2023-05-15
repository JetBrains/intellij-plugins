// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSBooleanLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.psi.util.getStubSafeChildren
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.model.Pointer
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.util.AstLoadingFilter
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.ALIAS_PROP
import org.angular2.Angular2DecoratorUtil.REQUIRED_PROP
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver
import org.angular2.entities.*
import org.angular2.entities.Angular2EntitiesProvider.withJsonMetadataFallback
import org.angular2.entities.Angular2EntityUtils.ELEMENT_REF
import org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF
import org.angular2.entities.Angular2EntityUtils.VIEW_CONTAINER_REF
import org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity
import org.angular2.entities.metadata.Angular2MetadataUtil
import org.angular2.index.Angular2IndexingHandler
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

open class Angular2SourceDirective(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceDeclaration(decorator, implicitElement), Angular2Directive {

  override val selector: Angular2DirectiveSelector
    get() = getCachedValue {
      val property = Angular2DecoratorUtil.getProperty(decorator, Angular2DecoratorUtil.SELECTOR_PROP)
      var value: String? = null
      if (property != null) {
        val initializer: JSLiteralExpression?
        val stub = (property as JSPropertyImpl).stub
        if (stub != null) {
          initializer = stub.childrenStubs.firstNotNullOfOrNull { it.psi as? JSLiteralExpression }
          value = initializer?.significantValue?.let { Angular2EntityUtils.unquote(it) }
        }
        else {
          initializer = property.value as? JSLiteralExpression
          value = initializer?.stringValue
        }
        if (value != null && initializer != null) {
          return@getCachedValue Result.create(
            Angular2DirectiveSelectorImpl(initializer, StringUtil.unquoteString(value), 1),
            property)
        }
        value = AstLoadingFilter.forceAllowTreeLoading<String, RuntimeException>(property.containingFile) {
          Angular2DecoratorUtil.getExpressionStringValue(property.value)
        }
      }
      Result.create(Angular2DirectiveSelectorImpl(decorator, value, null), decorator)
    }

  override val directiveKind: Angular2DirectiveKind
    get() = getCachedValue {
      Result.create(
        getDirectiveKindNoCache(typeScriptClass), classModificationDependencies)
    }

  override val exportAsList: List<String>
    get() = getCachedValue {
      val exportAsString = Angular2DecoratorUtil.getPropertyStringValue(decorator, Angular2DecoratorUtil.EXPORT_AS_PROP)
      Result.create(if (exportAsString == null) emptyList() else StringUtil.split(exportAsString, ","), decorator)
    }

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = getCachedValue {
      Result.create(attributeParameters, classModificationDependencies)
    }

  override val bindings: Angular2DirectiveProperties
    get() = getCachedValue {
      Result.create(propertiesNoCache, classModificationDependencies)
    }

  private val propertiesNoCache: Angular2DirectiveProperties
    get() {
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
            processProperty(clazz, prop, el, inputMap, Angular2DecoratorUtil.INPUT_DEC, KIND_NG_DIRECTIVE_INPUTS, inputs)
            processProperty(clazz, prop, el, outputMap, Angular2DecoratorUtil.OUTPUT_DEC, KIND_NG_DIRECTIVE_OUTPUTS, outputs)
          }
        }

      inputMap.values.forEach { input ->
        inputs[input] = Angular2SourceDirectiveVirtualProperty(clazz, input, KIND_NG_DIRECTIVE_INPUTS, false)
      }
      outputMap.values.forEach { output ->
        outputs[output] = Angular2SourceDirectiveVirtualProperty(clazz, output, KIND_NG_DIRECTIVE_OUTPUTS, false)
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

  private val attributeParameters: Collection<Angular2DirectiveAttribute>
    get() {
      val constructors = typeScriptClass.constructors
      return if (constructors.size == 1)
        processCtorParameters(constructors[0])
      else
        constructors.firstOrNull { it.isOverloadImplementation }
          ?.let { processCtorParameters(it) }
        ?: emptyList()
    }

  override fun createPointer(): Pointer<out Angular2Directive> {
    return createPointer { decorator, implicitElement ->
      Angular2SourceDirective(decorator, implicitElement)
    }
  }

  private fun readPropertyMappings(source: String): MutableMap<String, String> {
    val prop = Angular2DecoratorUtil.getProperty(decorator, source)
               ?: return LinkedHashMap()
    val stub = (prop as JSPropertyImpl).stub
    val seq: Sequence<String>
    if (stub != null) {
      seq = stub.childrenStubs.asSequence().mapNotNull {
        it.psi.asSafely<JSLiteralExpression>()?.significantValue?.let { str -> Angular2EntityUtils.unquote(str) }
      }
    }
    else {
      seq = prop.value
              .asSafely<JSArrayLiteralExpression>()
              ?.expressions
              ?.asSequence()
              ?.mapNotNull { expr ->
                expr.asSafely<JSLiteralExpression>()?.takeIf { it.isQuotedLiteral }?.stringValue
              }
            ?: emptySequence()
    }
    return seq
      .map { property -> Angular2EntityUtils.parsePropertyMapping(property) }
      .toMap(LinkedHashMap())
  }

  companion object {

    @JvmStatic
    internal fun getPropertySources(property: PsiElement?): List<JSAttributeListOwner> {
      if (property is TypeScriptFunction) {
        val `fun` = property as TypeScriptFunction?
        if (!`fun`!!.isSetProperty && !`fun`.isGetProperty) {
          return listOf<JSAttributeListOwner>(`fun`)
        }
        val result = ArrayList<JSAttributeListOwner>()
        result.add(`fun`)
        Angular2ReferenceExpressionResolver.findPropertyAccessor(`fun`, `fun`.isGetProperty) { result.add(it) }
        return result
      }
      else if (property is JSAttributeListOwner) {
        return listOf(property)
      }
      return emptyList()
    }

    private fun processProperty(sourceClass: TypeScriptClass,
                                property: JSRecordType.PropertySignature,
                                field: JSAttributeListOwner,
                                mappings: MutableMap<String, String>,
                                decorator: String,
                                kind: String,
                                result: MutableMap<String, Angular2DirectiveProperty>) {
      val info: Angular2PropertyInfo? =
        mappings.remove(property.memberName)
          ?.let { Angular2PropertyInfo(it, false) }
        ?: field.attributeList
          ?.decorators
          ?.firstOrNull { it.decoratorName == decorator }
          ?.let { createPropertyInfo(it, property.memberName) }
      if (info != null) {
        result.putIfAbsent(info.alias, Angular2SourceDirectiveProperty(sourceClass, property, kind, info.alias, info.required))
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
        ?.takeIf { Angular2IndexingHandler.isDecoratorStringArgStubbed(it) }
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

    private fun createPropertyInfo(decorator: ES6Decorator, defaultName: String): Angular2PropertyInfo =
      when (val param = getDecoratorParamValue(decorator)) {
        is JSObjectLiteralExpression -> {
          Angular2PropertyInfo(
            param.findProperty(ALIAS_PROP)?.jsType?.asSafely<JSStringLiteralTypeImpl>()?.literal
            ?: defaultName,
            param.findProperty(REQUIRED_PROP)?.jsType?.asSafely<JSBooleanLiteralTypeImpl>()?.literal
            ?: false
          )
        }
        else -> Angular2PropertyInfo((param as? JSLiteralExpression)?.stubSafeStringValue ?: defaultName, false)
      }
  }
}
