// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:Suppress("CanConvertToMultiDollarString")

package org.jetbrains.vuejs.model

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolFromNodeModule
import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolMethodsFromAugmentations
import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolPropertiesFromAugmentations
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.JSTypedEntity
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSModuleTypeImpl
import com.intellij.lang.javascript.psi.types.JSRecordMemberSourceFactory
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.JSTypeofTypeImpl
import com.intellij.lang.javascript.psi.types.TypeScriptJSFunctionTypeImpl
import com.intellij.lang.javascript.psi.types.recordImpl.PropertySignatureImpl
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.RecursionManager
import com.intellij.openapi.util.UserDataHolder
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JsSymbolSymbolKind
import com.intellij.polySymbols.js.PROP_JS_PROPERTY_SIGNATURE
import com.intellij.polySymbols.js.PROP_JS_SYMBOL_KIND
import com.intellij.polySymbols.js.jsType
import com.intellij.polySymbols.js.symbols.asJSSymbol
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.toPropertySignature
import com.intellij.polySymbols.js.types.JSSymbolScopeType
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolDelegate
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.CUSTOM_PROPERTIES
import org.jetbrains.vuejs.index.VUE_CORE_MODULES
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.source.INSTANCE_DATA_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_EMIT_METHOD
import org.jetbrains.vuejs.model.source.INSTANCE_OPTIONS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_PROPS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_REFS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_SLOTS_PROP
import org.jetbrains.vuejs.model.source.VUE_NAMESPACE
import org.jetbrains.vuejs.model.source.VueCallInject
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueSourceEntity
import org.jetbrains.vuejs.types.VueRefsType
import org.jetbrains.vuejs.types.createStrictTypeSource
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget
import org.jetbrains.vuejs.web.asPolySymbolPriority

interface VueInstanceOwner : VueScopeElement {

  val thisType: JSType
    get() = source
              ?.takeIf { this is UserDataHolder }
              ?.let { JSSymbolScopeType(VueInstanceOwnerPropertiesScope(this), it) }
            ?: JSAnyType.get(source)

  val instanceScope: PolySymbolScope?
    get() = source
      ?.takeIf { this is UserDataHolder }
      ?.let { VueInstanceOwnerPropertiesScope(this) }

  fun createPointer(): Pointer<out VueInstanceOwner>
}

fun getDefaultVueComponentInstance(context: PsiElement?): JSTypedEntity? =
  resolveSymbolFromNodeModule(context, VUE_MODULE, "ComponentPublicInstance", TypeScriptTypeAlias::class.java)
    ?.typeDeclaration
  ?: resolveSymbolFromNodeModule(context, VUE_MODULE, VUE_NAMESPACE, TypeScriptInterface::class.java)

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

private class VueInstanceOwnerPropertiesScope(
  instanceOwner: VueInstanceOwner,
) : PolySymbolScopeWithCache<UserDataHolder, Unit>(instanceOwner.source!!.project, instanceOwner as UserDataHolder, Unit) {

  override fun initialize(
    consumer: (PolySymbol) -> Unit,
    cacheDependencies: MutableSet<Any>,
  ) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val instanceOwner = dataHolder as VueInstanceOwner
    val source = instanceOwner.source ?: return

    RecursionManager.doPreventingRecursion(source, true) {
      val result = mutableMapOf<String, PolySymbol>()
      contributeCustomProperties(source, result)
      contributeDefaultInstanceProperties(source, result)
      contributeComponentProperties(instanceOwner, source, result)
      replaceStandardProperty(INSTANCE_REFS_PROP, instanceOwner, ::buildRefsType, result)
      contributePropertiesFromProviders(instanceOwner, result)

      val properties = result.values.toList()
      val methods = getCustomMethods(source, properties)

      for (symbol in properties.asSequence().plus(methods)) {
        if (symbol is PolySymbolScope && !symbol.isExclusiveFor(JS_PROPERTIES)) {
          throw IllegalStateException("Symbol ${symbol.name} (${symbol.javaClass.name} is not exclusive for JS properties")
        }
        consumer(symbol)
      }
    }
  }

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<UserDataHolder, Unit>> {
    val instanceOwnerPtr = (dataHolder as VueInstanceOwner).createPointer()
    return Pointer {
      val instanceOwner = instanceOwnerPtr.dereference() ?: return@Pointer null
      if (instanceOwner.source == null) return@Pointer null
      VueInstanceOwnerPropertiesScope(instanceOwner)
    }
  }

}

private fun contributeCustomProperties(
  source: PsiElement,
  result: MutableMap<String, PolySymbol>,
) {
  resolveSymbolPropertiesFromAugmentations(source, VUE_CORE_MODULES, CUSTOM_PROPERTIES)
    .forEach { (string, signature) ->
      signature.asJSSymbol()
        ?.let { result[string] = VueJsPropertyPropertyWithProximity.create(it, VueModelVisitor.Proximity.LOCAL) }
    }
}

private fun getCustomMethods(
  source: PsiElement,
  properties: List<PolySymbol>,
): List<PolySymbol> {
  val propertyNames = properties.asSequence()
    .map { it.name }
    .toSet()

  return resolveSymbolMethodsFromAugmentations(source, VUE_CORE_MODULES, CUSTOM_PROPERTIES)
    .filter { it.memberName !in propertyNames }
    .mapNotNull {
      it.asJSSymbol()?.let { symbol ->
        VueJsPropertyPropertyWithProximity.create(symbol, VueModelVisitor.Proximity.LOCAL)
      }
    }
}

private fun contributeDefaultInstanceProperties(
  source: PsiElement,
  result: MutableMap<String, PolySymbol>,
) {
  val defaultInstance = getDefaultVueComponentInstance(source)
  if (defaultInstance != null) {
    defaultInstance
      .asJSSymbol()
      .getJSPropertySymbols()
      .associateByTo(result) { it.name }
  }
  else {
    // Fallback to a predefined list of properties without any typings
    VUE_INSTANCE_PROPERTIES.forEach {
      result[it] = VueInstancePropertySymbol(it)
    }
    VUE_INSTANCE_METHODS.forEach {
      result[it] = VueInstancePropertySymbol(it, jsKind = JsSymbolSymbolKind.Method)
    }
  }
}

private fun contributePropertiesFromProviders(
  instance: VueInstanceOwner,
  result: MutableMap<String, PolySymbol>,
) {
  VueContainerInfoProvider.getProviders().asSequence()
    .flatMap { it.getThisTypePropertySymbols(instance, result.toMap()).asSequence() }
    .filter { it.kind == JS_PROPERTIES }
    .associateByTo(result) { it.name }
}

private fun contributeComponentProperties(
  instance: VueInstanceOwner,
  source: PsiElement,
  result: MutableMap<String, PolySymbol>,
) {
  val proximityMap = mutableMapOf<String, VueModelVisitor.Proximity>()

  val props = mutableMapOf<String, PolySymbol>()
  val computed = mutableMapOf<String, PolySymbol>()
  val data = mutableMapOf<String, PolySymbol>()
  val methods = mutableMapOf<String, PolySymbol>()
  val injects = mutableMapOf<String, PolySymbol>()

  val provides by lazy(LazyThreadSafetyMode.NONE) { instance.global.provides }

  fun mergeSymbols(existing: PolySymbol, updated: PolySymbol): PolySymbol =
    when {
      existing.isEquivalentTo(updated) -> existing
      existing is VueMergedPropertiesSymbol ->
        if (existing.properties.any { it.isEquivalentTo(updated) })
          existing
        else
          existing.add(updated)
      else -> VueMergedPropertiesSymbol(existing.name, listOf(existing, updated), source)
    }

  fun mergePut(result: MutableMap<String, PolySymbol>, contributions: MutableMap<String, PolySymbol>) {
    for ((name, value) in contributions) {
      result.merge(name, value, ::mergeSymbols)
    }
  }

  instance.asSafely<VueEntitiesContainer>()
    ?.acceptPropertiesAndMethods(object : VueModelProximityVisitor() {

      override fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
        process(prop, proximity, props)
        return true
      }

      override fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
        process(computedProperty, proximity, computed)
        return true
      }

      override fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
        process(dataProperty, proximity, data)
        return true
      }

      override fun visitMethod(method: VueMethod, proximity: Proximity): Boolean {
        process(method, proximity, methods)
        return true
      }

      override fun visitInject(inject: VueInject, proximity: Proximity): Boolean {
        if (inject is VueCallInject) return true

        process(VueJsPropertyPropertyWithProximity.create(inject, proximity, VueInjectedTypeProvider(inject, provides)), proximity, injects)
        return true
      }

      private fun process(
        symbol: PolySymbol,
        proximity: Proximity,
        dest: MutableMap<String, PolySymbol>,
      ) {
        if ((proximityMap.putIfAbsent(symbol.name, proximity) ?: proximity) >= proximity) {
          dest.merge(symbol.name,
                     if (symbol.kind != JS_PROPERTIES) VueJsPropertyPropertyWithProximity.create(symbol, proximity) else symbol,
                     ::mergeSymbols)
        }
      }

    }, onlyPublic = false)

  replaceStandardProperty(INSTANCE_PROPS_PROP, props.values.toList(), source, result)
  replaceStandardProperty(INSTANCE_DATA_PROP, data.values.toList(), source, result)

  replaceStandardProperty(INSTANCE_OPTIONS_PROP, instance, ::buildOptionsType, result)
  replaceStandardProperty(INSTANCE_SLOTS_PROP, instance, ::buildSlotsType, result)

  replaceStandardProperty(INSTANCE_EMIT_METHOD, instance, ::buildEmitType, result)

  // Vue will not proxy data properties starting with _ or $
  // https://vuejs.org/v2/api/#data
  // Interestingly it doesn't apply to computed, methods and props.
  data.keys.removeIf { it.startsWith("_") || it.startsWith("$") }

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

private data class StandardTypeProvider(
  private val instance: VueInstanceOwner,
  private val method: (VueInstanceOwner, JSType?) -> JSType?,
  private val originalProperty: PolySymbol?,
) : VueTypeProvider {

  override fun getType(): JSType? = method(instance, originalProperty?.jsType)

  override fun createPointer(): Pointer<out VueTypeProvider> {
    val instancePtr = instance.createPointer()
    val originalPropertyPtr = originalProperty?.createPointer()
    val method = method
    return Pointer {
      val instance = instancePtr.dereference() ?: return@Pointer null
      if (instance.source == null) return@Pointer null
      val originalProperty = originalPropertyPtr?.let { it.dereference() ?: return@Pointer null }
      StandardTypeProvider(instance, method, originalProperty)
    }
  }
}

private fun buildEmitType(instance: VueInstanceOwner, @Suppress("unused") originalType: JSType?): JSType {
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

private fun buildRefsType(instance: VueInstanceOwner, @Suppress("unused") originalType: JSType?): JSType? {
  return VueRefsType(createStrictTypeSource(instance.source ?: return null), instance)
}

private fun replaceStandardProperty(
  propName: String,
  properties: List<PolySymbol>,
  psiContext: PsiElement,
  result: MutableMap<String, PolySymbol>,
) {
  result[propName] = VueStandardPropertySymbol(propName, properties, psiContext)
}

private fun replaceStandardProperty(
  propName: String,
  instance: VueInstanceOwner,
  method: (VueInstanceOwner, JSType?) -> JSType?,
  result: MutableMap<String, PolySymbol>,
) {
  val originalProperty = result[propName]
  val typeProvider = StandardTypeProvider(instance, method, originalProperty)
  result[propName] = VueInstancePropertySymbol(
    propName, typeProvider, isReadOnly = true,
    navigationTarget = originalProperty?.getNavigationTargets(instance.source!!.project)?.singleOrNull()
  )
}

open class VueJsPropertyPropertyWithProximity private constructor(
  override val delegate: PolySymbol,
  val vueProximity: VueModelVisitor.Proximity,
  protected val typeProvider: VueTypeProvider?,
) : PolySymbolDelegate<PolySymbol> {

  companion object {
    fun create(
      delegate: PolySymbol,
      vueProximity: VueModelVisitor.Proximity,
      typeProvider: VueTypeProvider? = null,
    ): VueJsPropertyPropertyWithProximity =
      if (delegate is PsiSourcedPolySymbol)
        VuePsiSourcedJsPropertyWithProximity(delegate, vueProximity, typeProvider)
      else
        VueJsPropertyPropertyWithProximity(delegate, vueProximity, typeProvider)
  }

  private val type by lazy(LazyThreadSafetyMode.NONE) {
    typeProvider?.getType() ?: delegate.jsType
  }

  override val kind: PolySymbolKind
    get() = JS_PROPERTIES

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    symbol === this
    || symbol is VueJsPropertyPropertyWithProximity
    && symbol.delegate.isEquivalentTo(delegate)
    || delegate.isEquivalentTo(symbol)

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_VUE_PROXIMITY -> property.tryCast(vueProximity)
      else -> delegate[property]
    }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueJsPropertyPropertyWithProximity
    && other.delegate == delegate
    && other.typeProvider == typeProvider

  override fun hashCode(): Int {
    var result = delegate.hashCode()
    result = 31 * result + (typeProvider?.hashCode() ?: 0)
    return result
  }

  override fun createPointer(): Pointer<out PolySymbolDelegate<PolySymbol>> {
    val delegatePtr = delegate.createPointer()
    val typeProviderPtr = typeProvider?.createPointer()
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val typeProvider = typeProviderPtr?.let { it.dereference() ?: return@Pointer null }
      VueJsPropertyPropertyWithProximity(delegate, vueProximity, typeProvider)
    }
  }

  private class VuePsiSourcedJsPropertyWithProximity(
    delegate: PsiSourcedPolySymbol,
    proximity: VueModelVisitor.Proximity,
    typeProvider: VueTypeProvider?,
  ) : VueJsPropertyPropertyWithProximity(delegate, proximity, typeProvider), PsiSourcedPolySymbol {

    override val source: PsiElement?
      get() = (delegate as PsiSourcedPolySymbol).source

    override val psiContext: PsiElement?
      get() = delegate.psiContext

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<VueJsPropertyPropertyWithProximity>.isEquivalentTo(symbol)
      || super<VueJsPropertyPropertyWithProximity>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<VuePsiSourcedJsPropertyWithProximity> {
      val delegatePtr = delegate.createPointer()
      val typeProviderPtr = typeProvider?.createPointer()
      return Pointer {
        val delegate = delegatePtr.dereference() ?: return@Pointer null
        val typeProvider = typeProviderPtr?.let { it.dereference() ?: return@Pointer null }
        VuePsiSourcedJsPropertyWithProximity(delegate as PsiSourcedPolySymbol, vueProximity, typeProvider)
      }
    }
  }

}

interface VueTypeProvider {
  fun getType(): JSType?
  fun createPointer(): Pointer<out VueTypeProvider>
  override fun hashCode(): Int
  override fun equals(other: Any?): Boolean
}

data class VueInstancePropertySymbol(
  override val name: String,
  private val typeProvider: VueTypeProvider? = null,
  private val jsKind: JsSymbolSymbolKind = JsSymbolSymbolKind.Property,
  private val isReadOnly: Boolean = false,
  private val navigationTarget: NavigationTarget? = null,
) : PolySymbol {

  private val type by lazy(LazyThreadSafetyMode.NONE) { typeProvider?.getType() }

  override val kind: PolySymbolKind
    get() = JS_PROPERTIES

  override val modifiers: Set<PolySymbolModifier>
    get() =
      if (isReadOnly) setOf(PolySymbolModifier.READONLY) else emptySet()

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_SYMBOL_KIND -> property.tryCast(jsKind)
      PROP_JS_TYPE -> property.tryCast(type)
      else -> super.get(property)
    }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    listOfNotNull(navigationTarget ?: type?.sourceElement?.let { VueComponentSourceNavigationTarget(it) })

  override fun createPointer(): Pointer<out PolySymbol> {
    val typeProviderPtr = typeProvider?.createPointer()
    val name = name
    val jsKind = jsKind
    val isReadOnly = isReadOnly
    val navigationTargetPtr = navigationTarget?.createPointer()
    return Pointer {
      val typeProvider = typeProviderPtr?.let { it.dereference() ?: return@Pointer null }
      val navigationTarget = navigationTargetPtr?.let { it.dereference() ?: return@Pointer null }
      VueInstancePropertySymbol(name, typeProvider, jsKind, isReadOnly, navigationTarget)
    }
  }

}

private class VueStandardPropertySymbol(
  override val name: String,
  private val properties: List<PolySymbol>,
  override val psiContext: PsiElement,
) : PolySymbol, PolySymbolScope {

  private val type: JSType? by lazy(LazyThreadSafetyMode.NONE) {
    JSSymbolScopeType(this, psiContext)
  }

  override val kind: PolySymbolKind
    get() = JS_PROPERTIES

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      else -> super.get(property)
    }

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    if (kind == JS_PROPERTIES) properties else emptyList()

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueStandardPropertySymbol
    && other.name == name
    && other.psiContext == psiContext
    && other.properties == properties

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + psiContext.hashCode()
    result = 31 * result + properties.hashCode()
    return result
  }

  override fun createPointer(): Pointer<VueStandardPropertySymbol> {
    val name = name
    val propertiesPtr = properties.map { it.createPointer() }
    val psiContextPtr = psiContext.createSmartPointer()
    return Pointer {
      val name = name
      val properties = propertiesPtr.map { it.dereference() ?: return@Pointer null }
      val psiContext = psiContextPtr.dereference() ?: return@Pointer null
      VueStandardPropertySymbol(name, properties, psiContext)
    }
  }

  override fun getModificationCount(): Long = -1

}

private data class VueInjectedTypeProvider(private val inject: VueInject, private val provides: List<VueProvide>) : VueTypeProvider {
  override fun getType(): JSType? =
    evaluateInjectedType(inject, provides)

  override fun createPointer(): Pointer<out VueTypeProvider> {
    val injectPtr = inject.createPointer()
    val providesPtr = provides.map { it.createPointer() }
    return Pointer {
      val inject = injectPtr.dereference() ?: return@Pointer null
      val provides = providesPtr.map { it.dereference() ?: return@Pointer null }
      VueInjectedTypeProvider(inject, provides)
    }
  }
}

private data class VueMergedPropertiesSymbol(
  override val name: @NlsSafe String,
  val properties: List<PolySymbol>,
  override val psiContext: PsiElement,
) : PolySymbol {

  override val kind: PolySymbolKind
    get() = JS_PROPERTIES

  override val modifiers: Set<PolySymbolModifier> =
    setOfNotNull(
      PolySymbolModifier.READONLY.takeIf { properties.all { it.modifiers.contains(PolySymbolModifier.READONLY) } },
      PolySymbolModifier.OPTIONAL.takeIf { properties.all { it.modifiers.contains(PolySymbolModifier.OPTIONAL) } }
    )

  private val type: JSType? by lazy(LazyThreadSafetyMode.NONE) {
    properties
      .mapNotNull { it.jsType }
      .takeIf { it.size == properties.size }
      ?.let {
        JSCompositeTypeFactory.createUnionType(it[0].source, it)
      }
  }

  private val propertySignature: PropertySignatureImpl? by lazy(LazyThreadSafetyMode.NONE) {
    PropertySignatureImpl(
      name, type, modifiers.contains(PolySymbolModifier.OPTIONAL),
      modifiers.contains(PolySymbolModifier.READONLY),
      JSRecordMemberSourceFactory.createSource(
        properties.flatMap { it.toPropertySignature(psiContext).memberSource.allSourceElements },
        JSRecordType.MemberSourceKind.Union,
        true,
      )
    )
  }

  fun add(updated: PolySymbol): VueMergedPropertiesSymbol =
    VueMergedPropertiesSymbol(name, properties + updated, psiContext)

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_JS_PROPERTY_SIGNATURE -> property.tryCast(propertySignature)
      PROP_VUE_PROXIMITY -> property.tryCast(properties.asSequence().mapNotNull { it[PROP_VUE_PROXIMITY] }.maxBy { it.ordinal })
      else -> super.get(property)
    }

  override fun createPointer(): Pointer<out PolySymbol> {
    val propertiesPtr = properties.map { it.createPointer() }
    val name = name
    val psiContextPtr = psiContext.createSmartPointer()
    return Pointer {
      val properties = propertiesPtr.map { it.dereference() ?: return@Pointer null }
      val psiContext = psiContextPtr.element ?: return@Pointer null
      VueMergedPropertiesSymbol(name, properties, psiContext)
    }
  }

}