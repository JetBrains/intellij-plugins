// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSImportType
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeKeyTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.TypeScriptIndexedAccessJSTypeImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyNewType
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.INSTANCE_EMIT_METHOD
import org.jetbrains.vuejs.model.source.INSTANCE_PROPS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_SLOTS_PROP

class VueTypedComponent(override val source: PsiElement,
                        override val defaultName: String) : VueRegularComponent {

  override val nameElement: PsiElement?
    get() = null

  override val parents: List<VueEntitiesContainer>
    get() = VueGlobalImpl.getParents(this)

  override val thisType: JSType
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        resolveElementTo(source, TypeScriptVariable::class, TypeScriptPropertySignature::class)
          ?.jsType
          ?.let { getFromVueFile(it) ?: JSApplyNewType(it, it.source).substitute().asRecordType() },
        PsiModificationTracker.MODIFICATION_COUNT)
    } ?: JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, false)

  private fun getFromVueFile(type: JSType): JSRecordType? {
    if (type is TypeScriptIndexedAccessJSTypeImpl
        && type.parameterType.let { it is JSStringLiteralTypeImpl && it.literal == "default" }) {
      val importType = type.owner as? JSImportType ?: return null
      val prefix = "typeof import("
      val contextFile = type.source.scope ?: return null
      return importType.qualifiedName.name
        .takeIf { it.startsWith(prefix) && it.endsWith(")") }
        ?.let {it.substring(prefix.length + 1, it.length - 2)}
        ?.takeIf { it.endsWith("." + VueFileType.INSTANCE.defaultExtension) }
        ?.let { contextFile.virtualFile?.parent?.findFileByRelativePath(it) }
        ?.let { contextFile.manager.findFile(it) }
        ?.let { VueModelManager.findEnclosingContainer(it) as? VueRegularComponent }
        ?.thisType
        ?.asRecordType()
    }
    return null
  }

  override val data: List<VueDataProperty>
    get() = emptyList()

  override val computed: List<VueComputedProperty>
    get() = emptyList()

  override val methods: List<VueMethod>
    get() = emptyList()

  override val props: List<VueInputProperty>
    get() = CachedValuesManager.getCachedValue(source) {
      val toFilterOut = getFilteredOutProperties(source)
      CachedValueProvider.Result(
        thisType.asRecordType().findPropertySignature(INSTANCE_PROPS_PROP)
          ?.jsType
          ?.asRecordType()
          ?.properties
          ?.filter { !toFilterOut.contains(it.memberName) }
          ?.mapNotNull {
            VueTypedInputProperty(it)
          }
        ?: emptyList(), PsiModificationTracker.MODIFICATION_COUNT)
    }


  override val emits: List<VueEmitCall>
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
              ?.castSafelyTo<JSStringLiteralTypeImpl>()
              ?.literal
              ?.let { name ->
                VueTypedEmit(name, signature)
              }
          } ?: emptyList(),
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  override val slots: List<VueSlot>
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result(
        thisType.asRecordType().findPropertySignature(INSTANCE_SLOTS_PROP)
          ?.jsType
          ?.asRecordType()
          ?.properties
          ?.mapNotNull { signature ->
            VueTypedSlot(signature.memberName, signature.memberSource.singleElement)
          } ?: emptyList(),
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  override val model: VueModelDirectiveProperties
    get() = VueModelDirectiveProperties()

  override val extends: List<VueContainer>
    get() = emptyList()
  override val components: Map<String, VueComponent>
    get() = emptyMap()
  override val directives: Map<String, VueDirective>
    get() = emptyMap()
  override val filters: Map<String, VueFilter>
    get() = emptyMap()
  override val mixins: List<VueMixin>
    get() = emptyList()

  override fun createPointer(): Pointer<out VueRegularComponent> {
    val sourcePtr = source.createSmartPointer()
    val defaultName = this.defaultName
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueTypedComponent(source, defaultName)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueTypedComponent
    && other.source == this.source

  override fun hashCode(): Int =
    source.hashCode()

  private abstract class VueTypedProperty(protected val property: JSRecordType.PropertySignature) : VueProperty {
    override val name: String get() = property.memberName
    override val jsType: JSType? get() = property.jsType
    override val source: PsiElement? get() = property.memberSource.singleElement
  }

  private class VueTypedInputProperty(property: JSRecordType.PropertySignature) : VueTypedProperty(property), VueInputProperty {
    override val required: Boolean
      get() = false
  }

  private class VueTypedEmit(override val name: String,
                             private val callSignature: JSRecordType.CallSignature) : VueEmitCall {
    override val eventJSType: JSType?
      get() = callSignature.functionType.parameters.getOrNull(1)?.inferredType

    override val source: PsiElement?
      get() = callSignature.functionType.parameters.getOrNull(0)
                ?.inferredType?.castSafelyTo<JSTypeKeyTypeImpl>()
                ?.keySourceElements?.firstOrNull()
              ?: callSignature.memberSource.singleElement
  }

  private class VueTypedSlot(override val name: String, override val source: PsiElement?) : VueSlot

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