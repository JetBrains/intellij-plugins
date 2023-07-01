// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolFromNodeModule
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.JSTypedEntity
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl.PropertySignatureImpl
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.source.*
import org.jetbrains.vuejs.types.*
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
    } ?: JSAnyType.get(source, false)
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
  val result = mutableMapOf<String, JSRecordType.PropertySignature>()
  contributeDefaultInstanceProperties(source, result)
  contributeComponentProperties(instance, source, result)
  replaceStandardProperty(INSTANCE_REFS_PROP, VueRefsType(createStrictTypeSource(source), instance), source, result)
  contributePropertiesFromProviders(instance, result)
  return VueComponentInstanceType(JSTypeSourceFactory.createTypeSource(source, true), instance, result.values.toList())
}

private fun contributeDefaultInstanceProperties(source: PsiElement,
                                                result: MutableMap<String, JSRecordType.PropertySignature>): MutableMap<String, JSRecordType.PropertySignature> {
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
  return result
}

private fun contributePropertiesFromProviders(instance: VueInstanceOwner, result: MutableMap<String, JSRecordType.PropertySignature>) {
  val unmodifiableResult = Collections.unmodifiableMap(result)
  VueContainerInfoProvider.getProviders().asSequence()
    .flatMap { it.getThisTypeProperties(instance, unmodifiableResult).asSequence() }
    .associateByTo(result) { it.memberName }
}

private fun contributeComponentProperties(instance: VueInstanceOwner,
                                          source: PsiElement,
                                          result: MutableMap<String, JSRecordType.PropertySignature>) {
  val proximityMap = mutableMapOf<String, VueModelVisitor.Proximity>()

  val props = mutableMapOf<String, JSRecordType.PropertySignature>()
  val computed = mutableMapOf<String, JSRecordType.PropertySignature>()
  val data = mutableMapOf<String, JSRecordType.PropertySignature>()
  val methods = mutableMapOf<String, JSRecordType.PropertySignature>()
  val injects = mutableMapOf<String, JSRecordType.PropertySignature>()

  val provides = instance.global.collectProvides()

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

        val sourceElement = inject.source ?: return true
        val defaultValue = inject.defaultValue
        val isOptional = defaultValue == null || defaultValue is JSUndefinedType || defaultValue is JSVoidType
        val type = provides.asSequence().map { it.provide }.find { provide ->
          provide.injectionKey?.isEquivalentTo(inject.injectionKey) ?: (provide.name == (inject.from ?: inject.name))
        }?.jsType?.optionalIf(isOptional)
        val implicitElement = VueImplicitElement(inject.name, type, sourceElement, JSImplicitElement.Type.Property, true)
        process(inject, proximity, injects, true, type, implicitElement)
        return true
      }

      private fun process(symbol: VueNamedSymbol,
                          proximity: Proximity,
                          dest: MutableMap<String, JSRecordType.PropertySignature>,
                          isReadOnly: Boolean,
                          type: JSType? = null,
                          source: PsiElement? = null) {
        if ((proximityMap.putIfAbsent(symbol.name, proximity) ?: proximity) >= proximity) {
          val jsType = type ?: symbol.asSafely<VueProperty>()?.jsType
          dest.merge(symbol.name,
                     PropertySignatureImpl(symbol.name, jsType, false, isReadOnly, source ?: symbol.source),
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

private fun buildSlotsType(instance: VueInstanceOwner, originalType: JSType?): JSType {
  val typeSource = JSTypeSourceFactory.createTypeSource(instance.source!!, false)
  val slots = (instance as? VueContainer)?.slots ?: return originalType ?: JSAnyType.get(typeSource)
  val slotType = resolveSymbolFromNodeModule(instance.source, VUE_MODULE, "Slot", JSTypedEntity::class.java)?.jsType
  val slotsType = slots.asSequence().filter {
    it.pattern == null
  }.map {
    PropertySignatureImpl(it.name, slotType, true, true, it.source)
  }
    .toList()
    .let { JSRecordTypeImpl(typeSource, it) }
  return if (originalType == null)
    slotsType
  else
    JSCompositeTypeFactory.createIntersectionType(listOf(originalType, slotsType), typeSource)
}

private fun buildOptionsType(instance: VueInstanceOwner, originalType: JSType?): JSType {
  val result = mutableListOf<JSType>()
  originalType?.let(result::add)
  instance.acceptEntities(object : VueModelVisitor() {
    override fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean = visitInstanceOwner(mixin)

    override fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean = visitInstanceOwner(component)

    override fun visitSelfApplication(application: VueApp, proximity: Proximity): Boolean = visitInstanceOwner(application)

    fun visitInstanceOwner(instanceOwner: VueInstanceOwner): Boolean {
      when (val initializer = (instanceOwner as? VueSourceEntity)?.initializer) {
        is JSObjectLiteralExpression -> result.add(JSTypeofTypeImpl(
          initializer, JSTypeSourceFactory.createTypeSource(initializer, false)))
        is JSFile -> result.add(JSModuleTypeImpl(initializer, false))
      }
      return true
    }
  }, VueModelVisitor.Proximity.LOCAL)
  return JSCompositeTypeFactory.createIntersectionType(
    result, originalType?.source ?: JSTypeSourceFactory.createTypeSource(instance.source!!, false))
}

private fun buildEmitType(instance: VueInstanceOwner): JSType {
  val source =
    JSTypeSourceFactory.createTypeSource(instance.source!!, true).copyWithNewLanguage(JSTypeSource.SourceLanguage.TS)
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

private fun replaceStandardProperty(propName: String, properties: List<JSRecordType.PropertySignature>,
                                    defaultSource: PsiElement, result: MutableMap<String, JSRecordType.PropertySignature>) {
  val propSource = result[propName]?.memberSource?.singleElement ?: defaultSource
  result[propName] = createImplicitPropertySignature(
    propName, VueCompleteRecordType(propSource, properties), propSource)
}

private fun replaceStandardProperty(propName: String, type: JSType,
                                    defaultSource: PsiElement, result: MutableMap<String, JSRecordType.PropertySignature>) {
  val propSource = result[propName]?.memberSource?.singleElement ?: defaultSource
  result[propName] = createImplicitPropertySignature(propName, type, propSource)
}

private fun mergeSignatures(existing: JSRecordType.PropertySignature,
                            updated: JSRecordType.PropertySignature): JSRecordType.PropertySignature {
  val existingType = existing.jsType
  val updatedType = updated.jsType
  val type: JSType? = if (existingType == null || updatedType == null)
    null
  else
    JSCompositeTypeFactory.createUnionType(existingType.source, existingType, updatedType)
  return PropertySignatureImpl(
    existing.memberName, type, existing.isOptional && updated.isOptional,
    false, JSRecordMemberSourceFactory.createSource(existing.memberSource.allSourceElements +
                                                    updated.memberSource.allSourceElements,
                                                    JSRecordType.MemberSourceKind.Union))
}

private fun mergePut(result: MutableMap<String, JSRecordType.PropertySignature>,
                     contributions: MutableMap<String, JSRecordType.PropertySignature>) =
  contributions.forEach { (name, value) ->
    result.merge(name, value, ::mergeSignatures)
  }

fun createImplicitPropertySignature(name: String,
                                    type: JSType?,
                                    source: PsiElement,
                                    equivalentToSource: Boolean = false,
                                    isReadOnly: Boolean = false,
                                    kind: JSImplicitElement.Type = JSImplicitElement.Type.Property): JSRecordType.PropertySignature {
  return PropertySignatureImpl(name, type, false, isReadOnly,
                               VueImplicitElement(name, type, source, kind, equivalentToSource))
}