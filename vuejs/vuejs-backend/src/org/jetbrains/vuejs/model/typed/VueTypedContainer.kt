// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolFromNodeModule
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.lang.javascript.documentation.JSDocumentationProvider
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeKeyTypeImpl
import com.intellij.model.Pointer
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.INSTANCE_EMIT_METHOD
import org.jetbrains.vuejs.model.source.INSTANCE_PROPS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_SLOTS_PROP
import java.util.*

abstract class VueTypedContainer(override val source: PsiElement) : VueContainer {

  abstract override val thisType: JSType

  final override val props: List<VueInputProperty>
    get() = CachedValuesManager.getCachedValue(source) {
      val toFilterOut = getFilteredOutProperties(source)
      val thisType = thisType.asRecordType()
      val props = TreeMap<String, PropertySignature>()

      thisType.findPropertySignature(INSTANCE_PROPS_PROP)
        ?.jsType
        ?.asRecordType()
        ?.properties
        ?.asSequence()
        ?.filter { !toFilterOut.contains(it.memberName) }
        ?.forEach { props.putIfAbsent(it.memberName, it) }

      if (source is TypeScriptClass)
        thisType
          .properties
          .asSequence()
          .filter { !it.memberName.startsWith("$") && !toFilterOut.contains(it.memberName) }
          .forEach { props.putIfAbsent(it.memberName, it) }

      CachedValueProvider.Result(props.values.map { VueTypedInputProperty(this, it) }, PsiModificationTracker.MODIFICATION_COUNT)
    }


  final override val emits: List<VueEmitCall>
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result(
        thisType.asRecordType().findPropertySignature(INSTANCE_EMIT_METHOD)
          ?.jsType
          ?.asRecordType()
          ?.callSignatures
          ?.mapNotNull { signature ->
            signature.functionType
              .parameters.getOrNull(0)
              ?.inferredType
              ?.asSafely<JSStringLiteralTypeImpl>()
              ?.literal
              ?.let { name ->
                VueTypedEmit(this, name, signature)
              }
          } ?: emptyList(),
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  final override val slots: List<VueSlot>
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result(
        thisType.asRecordType().findPropertySignature(INSTANCE_SLOTS_PROP)
          ?.jsType
          ?.asRecordType()
          ?.properties
          ?.mapNotNull { signature ->
            VueTypedSlot(signature.memberName, signature.memberSource.singleElement, signature.jsType)
          }
        ?: emptyList(),
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  final override val model: VueModelDirectiveProperties
    get() = VueModelDirectiveProperties()

  final override val parents: List<VueEntitiesContainer>
    get() = VueGlobalImpl.getParents(this)

  final override val data: List<VueDataProperty>
    get() = emptyList()
  final override val computed: List<VueComputedProperty>
    get() = emptyList()
  final override val methods: List<VueMethod>
    get() = emptyList()
  final override val extends: List<VueContainer>
    get() = emptyList()
  final override val components: Map<String, VueComponent>
    get() = emptyMap()
  final override val directives: Map<String, VueDirective>
    get() = emptyMap()
  final override val filters: Map<String, VueFilter>
    get() = emptyMap()
  final override val mixins: List<VueMixin>
    get() = emptyList()
  final override val provides: List<VueProvide>
    get() = emptyList()
  final override val injects: List<VueInject>
    get() = emptyList()

  abstract override fun createPointer(): Pointer<out VueTypedContainer>

  private abstract class VueTypedDocumentedElement : VueNamedSymbol {

    override val description: String? by lazy {
      val doc = JSDocumentationProvider()
                  .generateDoc(source ?: return@lazy null, null) ?: return@lazy null
      val contentStart = doc.indexOf(DocumentationMarkup.CONTENT_START)
      val sectionsStart = doc.indexOf(DocumentationMarkup.SECTIONS_START)
      if (contentStart < 0 || contentStart > sectionsStart)
        null
      else
        doc.substring(contentStart + DocumentationMarkup.CONTENT_START.length, sectionsStart)
          .trim()
          .removeSuffix(DocumentationMarkup.CONTENT_END)
    }

  }

  private abstract class VueTypedProperty(
    val property: PropertySignature,
  ) : VueTypedDocumentedElement(), VueProperty, PsiSourcedPolySymbol {
    override val name: String get() = property.memberName
    override val type: JSType? get() = property.jsType
    override val source: PsiElement?
      get() = property.memberSource.singleElement.let {
        if (it is JSProperty)
          VueImplicitElement(property.memberName, property.jsType, it, JSImplicitElement.Type.Property, true)
        else
          it
      }

    abstract override fun createPointer(): Pointer<out VueTypedProperty>
  }

  private class VueTypedInputProperty(
    private val container: VueTypedContainer,
    property: PropertySignature,
  ) : VueTypedProperty(property), VueInputProperty, PsiSourcedPolySymbol {

    override val required: Boolean
      get() = false

    override fun equals(other: Any?): Boolean =
      other is VueTypedInputProperty
      && other.property.memberName == property.memberName
      && other.container == container

    override fun hashCode(): Int {
      var result = property.memberName.hashCode()
      result = 31 * result + container.hashCode()
      return result
    }

    override fun createPointer(): Pointer<VueTypedInputProperty> {
      val name = name
      val containerPtr = container.createPointer()
      return Pointer {
        return@Pointer containerPtr
          .dereference()
          ?.props
          ?.firstNotNullOfOrNull { prop -> (prop as? VueTypedInputProperty)?.takeIf { it.name == name } }
      }
    }
  }

  private class VueTypedEmit(
    private val owner: VueTypedContainer,
    override val name: String,
    private val callSignature: JSRecordType.CallSignature,
  ) : VueTypedDocumentedElement(), VueEmitCall, PsiSourcedPolySymbol {
    override val params: List<JSParameterTypeDecorator>
      get() = callSignature.functionType.parameters.drop(1)

    override val source: PsiElement?
      get() = callSignature.functionType.parameters.getOrNull(0)
                ?.inferredType?.asSafely<JSTypeKeyTypeImpl>()
                ?.keySourceElements?.firstOrNull()
              ?: callSignature.memberSource.singleElement

    override val hasStrictSignature: Boolean
      get() = true

    override fun createPointer(): Pointer<VueTypedEmit> {
      val ownerPtr = owner.createPointer()
      val name = name
      return Pointer {
        ownerPtr.dereference()?.emits?.firstNotNullOfOrNull { emit ->
          (emit as? VueTypedEmit)?.takeIf { it.name == name }
        }
      }
    }

    override fun equals(other: Any?): Boolean =
      other === this
      || other is VueTypedEmit
      && other.owner == owner
      && other.name == name

    override fun hashCode(): Int {
      var result = owner.hashCode()
      result = 31 * result + name.hashCode()
      return result
    }
  }

  private class VueTypedSlot(
    override val name: String,
    override val source: PsiElement?,
    typeSignature: JSType?,
  ) : VueTypedDocumentedElement(), VueSlot {
    override val scope: JSType? = (typeSignature as? JSFunctionType)?.parameters?.getOrNull(0)?.simpleType
  }

  companion object {

    private fun getFilteredOutProperties(context: PsiElement): Set<String> {
      val containingFile = context.containingFile
      return CachedValuesManager.getCachedValue(containingFile) {
        val result = mutableSetOf<String>()
        resolveSymbolFromNodeModule(containingFile, VUE_MODULE, "VNodeProps", JSClass::class.java)
          ?.jsType?.asRecordType()?.properties?.mapTo(result) { it.memberName }

        resolveSymbolFromNodeModule(containingFile, VUE_MODULE, "AllowedComponentProps", JSClass::class.java)
          ?.jsType?.asRecordType()?.properties?.mapTo(result) { it.memberName }

        CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  }

}