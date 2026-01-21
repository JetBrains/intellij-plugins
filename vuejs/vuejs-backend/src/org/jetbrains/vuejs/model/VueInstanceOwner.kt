// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolFromNodeModule
import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolMethodsFromAugmentations
import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolPropertiesFromAugmentations
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.JSTypedEntity
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.recordImpl.PropertySignatureImpl
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.util.UserDataHolder
import com.intellij.polySymbols.js.jsType
import com.intellij.polySymbols.js.toJSImplicitElement
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.CUSTOM_PROPERTIES
import org.jetbrains.vuejs.index.VUE_CORE_MODULES
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.types.VueCompleteRecordType
import org.jetbrains.vuejs.types.VueComponentInstanceType
import org.jetbrains.vuejs.types.VueRefsType
import org.jetbrains.vuejs.types.createStrictTypeSource
import java.util.*

interface VueInstanceOwner : VueScopeElement {
  val thisType: JSType
    get() = source?.let { source ->
      if (this !is UserDataHolder) return@let null
      CachedValuesManager.getManager(source.project).getCachedValue(this) {
        CachedValueProvider.Result.create(RecursionManager.doPreventingRecursion(this.source!!, true) {
          buildInstanceType(this)
        }, PsiModificationTracker.MODIFICATION_COUNT)
      }
    } ?: JSAnyType.get(source)
}

fun getDefaultVueComponentInstanceType(context: PsiElement?): JSType? =
  resolveSymbolFromNodeModule(context, VUE_MODULE, "ComponentPublicInstance", TypeScriptTypeAlias::class.java)
    ?.typeDeclaration?.jsType
  ?: resolveSymbolFromNodeModule(context, VUE_MODULE, VUE_NAMESPACE, TypeScriptInterface::class.java)?.jsType

private val VUE_INSTANCE_PROPERTIES: List<String> = listOf(
  "\$el", INSTANCE_OPTIONS_PROP, "\$parent", "\$root", "\$children", INSTANCE_REFS_PROP, INSTANCE_SLOTS_PROP,
  "\$scopedSlots", "\$isServer", INSTANCE_DATA_PROP, INSTANCE_PROPS_PROP,
  "\$ssrContext", "\$vnode", "\$attrs", "\$listeners")

private val VUE_INSTANCE_METHODS: List<String> = listOf(
  "\$mount", "\$forceUpdate", "\$destroy", "\$set", "\$delete", "\$watch", "\$on",
  "\$once", "\$off", INSTANCE_EMIT_METHOD, "\$nextTick", "\$createElement")

private fun buildInstanceType(instance: VueInstanceOwner): JSType? {
  val source = instance.source ?: return null

  val result = mutableMapOf<String, PropertySignature>()
  contributeCustomProperties(source, result)
  contributeDefaultInstanceProperties(source, result)
  contributeComponentProperties(instance, source, result)
  replaceStandardProperty(INSTANCE_REFS_PROP, VueRefsType(createStrictTypeSource(source), instance), source, result)
  contributePropertiesFromProviders(instance, result)

  val properties = result.values.toList()
  val methods = getCustomMethods(source, properties)

  return VueComponentInstanceType(
    source = JSTypeSourceFactory.createTypeSource(source, true),
    instanceOwner = instance,
    typeMembers = properties + methods,
  )
}

private fun contributeCustomProperties(
  source: PsiElement,
  result: MutableMap<String, PropertySignature>,
) {
  result.putAll(resolveSymbolPropertiesFromAugmentations(source, VUE_CORE_MODULES, CUSTOM_PROPERTIES))
}

private fun getCustomMethods(
  source: PsiElement,
  properties: List<PropertySignature>,
): List<PropertySignature> {
  val propertyNames = properties.asSequence()
    .map { it.memberName }
    .toSet()

  return resolveSymbolMethodsFromAugmentations(source, VUE_CORE_MODULES, CUSTOM_PROPERTIES)
    .filter { it.memberName !in propertyNames }
}

private fun contributeDefaultInstanceProperties(
  source: PsiElement,
  result: MutableMap<String, PropertySignature>,
) {
  val defaultInstanceType = getDefaultVueComponentInstanceType(source)
  if (defaultInstanceType != null) {
    defaultInstanceType.asRecordType()
      .properties
      .associateByTo(result) { it.memberName }
  }
  else {
    // Fallback to a predefined list of properties without any typings
    VUE_INSTANCE_PROPERTIES.forEach {
      result[it] = createImplicitPropertySignature(it, null, source)
    }
    VUE_INSTANCE_METHODS.forEach {
      result[it] = createImplicitPropertySignature(it, null, source, kind = JSImplicitElement.Type.Function)
    }
  }
}

private fun contributePropertiesFromProviders(
  instance: VueInstanceOwner,
  result: MutableMap<String, PropertySignature>,
) {
  val unmodifiableResult = Collections.unmodifiableMap(result)
  VueContainerInfoProvider.getProviders().asSequence()
    .flatMap { it.getThisTypeProperties(instance, unmodifiableResult).asSequence() }
    .associateByTo(result) { it.memberName }
}

private fun contributeComponentProperties(
  instance: VueInstanceOwner,
  source: PsiElement,
  result: MutableMap<String, PropertySignature>,
) {
  val proximityMap = mutableMapOf<String, VueModelVisitor.Proximity>()

  val props = mutableMapOf<String, PropertySignature>()
  val computed = mutableMapOf<String, PropertySignature>()
  val data = mutableMapOf<String, PropertySignature>()
  val methods = mutableMapOf<String, PropertySignature>()
  val injects = mutableMapOf<String, PropertySignature>()

  val provides by lazy(LazyThreadSafetyMode.NONE) { instance.global.provides }

  instance.asSafely<VueEntitiesContainer>()
    ?.acceptPropertiesAndMethods(object : VueModelProximityVisitor() {

      override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
        process(prop, proximity, props, true)
        return true
      }

      override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
        process(computedProperty, proximity, computed, true)
        return true
      }

      override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
        process(dataProperty, proximity, data, false)
        return true
      }

      override fun visitMethod(method: VueMethod, proximity: Proximity): Boolean {
        process(method, proximity, methods, false)
        return true
      }

      override fun visitInject(inject: VueInject, proximity: Proximity): Boolean {
        if (inject is VueCallInject) return true

        val sourceElement = inject.source
        val type = evaluateInjectedType(inject, provides)
        val implicitElement = VueImplicitElement(inject.name, type, sourceElement, JSImplicitElement.Type.Property, true)
        process(inject, proximity, injects, true,  implicitElement)
        return true
      }

      private fun process(
        symbol: VueSymbol,
        proximity: Proximity,
        dest: MutableMap<String, PropertySignature>,
        isReadOnly: Boolean,
        forcedSource: JSImplicitElement? = null,
      ) {
        if ((proximityMap.putIfAbsent(symbol.name, proximity) ?: proximity) >= proximity) {
          val jsType = if (forcedSource != null) forcedSource.jsType else symbol.jsType
          dest.merge(symbol.name,
                     PropertySignatureImpl(symbol.name, jsType, false,
                                           isReadOnly, forcedSource ?: symbol.toJSImplicitElement(source)),
                     ::mergeSignatures)
        }
      }

    }, onlyPublic = false)

  replaceStandardProperty(INSTANCE_PROPS_PROP, props.values.toList(), source, result)
  replaceStandardProperty(INSTANCE_DATA_PROP, data.values.toList(), source, result)

  replaceStandardProperty(INSTANCE_OPTIONS_PROP, buildOptionsType(instance, result[INSTANCE_OPTIONS_PROP]?.jsType), source, result)
  replaceStandardProperty(INSTANCE_SLOTS_PROP, buildSlotsType(instance, result[INSTANCE_SLOTS_PROP]?.jsType), source, result)

  replaceStandardProperty(INSTANCE_EMIT_METHOD, buildEmitType(instance), source, result)

  // Vue will not proxy data properties starting with _ or $
  // https://vuejs.org/v2/api/#data
  // Interestingly it doesn't apply to computed, methods and props.
  data.keys.removeIf { it.startsWith("_") || it.startsWith("\$") }

  result.keys.removeIf {
    props.containsKey(it) || data.containsKey(it) || computed.containsKey(it) || methods.containsKey(it) || injects.containsKey(it)
  }

  mergePut(result, props)
  mergePut(result, data)
  mergePut(result, computed)
  mergePut(result, methods)
  mergePut(result, injects)
}

private fun buildSlotsType(
  instance: VueInstanceOwner,
  originalType: JSType?,
): JSType {
  val typeSource = JSTypeSourceFactory.createTypeSource(instance.source!!, false)
  val slots = (instance as? VueContainer)?.slots ?: return originalType ?: JSAnyType.get(typeSource)
  val slotType = resolveSymbolFromNodeModule(instance.source, VUE_MODULE, "Slot", JSTypedEntity::class.java)?.jsType
  val slotsType = slots.asSequence()
    .filter { it !is PolySymbolWithPattern }
    .map { PropertySignatureImpl(it.name, slotType, true, true, it.source) }
    .toList()
    .let { JSRecordTypeImpl(typeSource, it) }

  return if (originalType != null)
    JSCompositeTypeFactory.createIntersectionType(listOf(originalType, slotsType), typeSource)
  else
    slotsType
}

private fun buildOptionsType(
  instance: VueInstanceOwner,
  originalType: JSType?,
): JSType {
  val result = mutableListOf<JSType>()
  originalType?.let(result::add)
  instance.acceptEntities(object : VueModelVisitor() {
    override fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean = visitInstanceOwner(mixin)

    override fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean = visitInstanceOwner(component)

    override fun visitSelfApplication(application: VueApp, proximity: Proximity): Boolean = visitInstanceOwner(application)

    fun visitInstanceOwner(instanceOwner: VueInstanceOwner): Boolean {
      when (val initializer = (instanceOwner as? VueSourceEntity)?.initializer) {
        is JSObjectLiteralExpression,
          -> result += JSTypeofTypeImpl(initializer, JSTypeSourceFactory.createTypeSource(initializer, false))

        is JSFile,
          -> result += JSModuleTypeImpl(initializer, false)
      }
      return true
    }
  }, VueModelVisitor.Proximity.LOCAL)
  return JSCompositeTypeFactory.createIntersectionType(
    result, originalType?.source ?: JSTypeSourceFactory.createTypeSource(instance.source!!, false))
}

private fun buildEmitType(instance: VueInstanceOwner): JSType {
  val source = JSTypeSourceFactory.createTypeSource(instance.source!!, true)
    .copyWithNewLanguage(JSTypeSource.SourceLanguage.TS)
  val emitCalls = instance.asSafely<VueContainer>()?.emits ?: emptyList()
  val hasUniqueSignatures = emitCalls.any { it.params.isNotEmpty() || it.hasStrictSignature }

  val eventTypes = if (emitCalls.isNotEmpty()) {
    if (!hasUniqueSignatures) {
      val combinedEventsType = JSCompositeTypeFactory.createUnionType(
        source,
        emitCalls.map {
          JSStringLiteralTypeImpl(it.name, false, source)
        }
      )
      val parameters = listOf(createEmitEventParam(combinedEventsType), createEmitRestParam(source))
      listOf(TypeScriptJSFunctionTypeImpl(source, emptyList(), parameters, null, null))
    }
    else {
      emitCalls.map { it.callSignature }
    }
  }
  else {
    emptyList()
  }

  return if (eventTypes.isEmpty()) {
    createDefaultEmitCallSignature(source)
  }
  else {
    JSCompositeTypeFactory.createIntersectionType(eventTypes, source)
  }
}

private fun replaceStandardProperty(
  propName: String,
  properties: List<PropertySignature>,
  defaultSource: PsiElement,
  result: MutableMap<String, PropertySignature>,
) {
  val propSource = result[propName]?.memberSource?.singleElement ?: defaultSource
  result[propName] = createImplicitPropertySignature(
    propName, VueCompleteRecordType(propSource, properties), propSource)
}

private fun replaceStandardProperty(
  propName: String,
  type: JSType,
  defaultSource: PsiElement,
  result: MutableMap<String, PropertySignature>,
) {
  val propSource = result[propName]?.memberSource?.singleElement ?: defaultSource
  result[propName] = createImplicitPropertySignature(propName, type, propSource)
}

private fun mergeSignatures(
  existing: PropertySignature,
  updated: PropertySignature,
): PropertySignature {
  val existingType = existing.jsType
  val updatedType = updated.jsType
  val type: JSType? = if (existingType != null && updatedType != null)
    JSCompositeTypeFactory.createUnionType(existingType.source, existingType, updatedType)
  else
    null

  return PropertySignatureImpl(
    existing.memberName,
    type,
    existing.isOptional && updated.isOptional,
    false,
    JSRecordMemberSourceFactory.createSource(
      existing.memberSource.allSourceElements + updated.memberSource.allSourceElements,
      JSRecordType.MemberSourceKind.Union,
      true,
    )
  )
}

private fun mergePut(
  result: MutableMap<String, PropertySignature>,
  contributions: MutableMap<String, PropertySignature>,
) {
  for ((name, value) in contributions) {
    result.merge(name, value, ::mergeSignatures)
  }
}

fun createImplicitPropertySignature(
  name: String,
  type: JSType?,
  source: PsiElement,
  equivalentToSource: Boolean = false,
  isReadOnly: Boolean = false,
  kind: JSImplicitElement.Type = JSImplicitElement.Type.Property,
): PropertySignature {
  return PropertySignatureImpl(
    name,
    type,
    false,
    isReadOnly,
    VueImplicitElement(
      name = name,
      jsType = type,
      provider = source,
      kind = kind,
      equivalentToProvider = equivalentToSource,
    ),
  )
}
