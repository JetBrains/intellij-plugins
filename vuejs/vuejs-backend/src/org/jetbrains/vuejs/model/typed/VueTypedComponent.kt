// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSImportType
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.TypeScriptIndexedAccessJSTypeImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyNewType
import com.intellij.model.Pointer
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.lang.html.isVueFileName
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueLocallyDefinedComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueNamedComponent
import org.jetbrains.vuejs.model.VuePsiSourcedComponent
import org.jetbrains.vuejs.model.source.VueComponents

class VueTypedComponent private constructor(
  override val source: JSQualifiedNamedElement,
  override val name: String,
) : VueTypedContainer(source), VuePsiSourcedComponent {

  companion object {
    fun create(source: JSQualifiedNamedElement): VueNamedComponent? {
      val name = source.name ?: return null
      return if (source is TypeScriptPropertySignature && source.typeDeclaration is TypeScriptTypeofType)
        VueLocallyDefinedComponent.create(source, VueTypedComponentDelegateProvider(source))
      else
        VueTypedComponent(source, name)
    }
  }

  override val elementToImport: PsiElement
    get() = source

  override val renameTarget: PolySymbolRenameTarget?
    get() = null

  override val thisType: JSType
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        resolveElementTo(source, TypeScriptVariable::class, TypeScriptPropertySignature::class, TypeScriptClass::class)
          ?.let { componentDefinition ->
            when (componentDefinition) {
              is JSTypeOwner ->
                componentDefinition.jsType
                  ?.let { getFromVueFile(it) ?: JSApplyNewType(it, it.source).substitute() }
              is TypeScriptClass ->
                componentDefinition.jsType
              else -> null
            }
          },
        PsiModificationTracker.MODIFICATION_COUNT)
    } ?: JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS)

  private fun getFromVueFile(type: JSType): JSRecordType? {
    if (type is TypeScriptIndexedAccessJSTypeImpl
        && type.parameterType.let { it is JSStringLiteralTypeImpl && it.literal == "default" }) {
      val importType = type.owner as? JSImportType ?: return null
      val prefix = "typeof import("
      val contextFile = type.source.scope ?: return null
      return importType.qualifiedName.name
        .takeIf { it.startsWith(prefix) && it.endsWith(")") }
        ?.let { it.substring(prefix.length + 1, it.length - 2) }
        ?.takeIf { isVueFileName(it) }
        ?.let { contextFile.virtualFile?.parent?.findFileByRelativePath(it) }
        ?.let { contextFile.manager.findFile(it) }
        ?.let { VueModelManager.findEnclosingContainer(it) as? VueComponent }
        ?.thisType
        ?.asRecordType()
    }
    return null
  }

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = resolveElementTo(source, TypeScriptVariable::class, TypeScriptPropertySignature::class, TypeScriptClass::class)
              .asSafely<TypeScriptTypeParameterListOwner>()
              ?.typeParameters?.toList()
            ?: emptyList()

  override fun createPointer(): Pointer<VueTypedComponent> {
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { create(source) as? VueTypedComponent }
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueTypedComponent
    && other.source == this.source

  override fun hashCode(): Int =
    source.hashCode()

  private data class VueTypedComponentDelegateProvider(private val source: TypeScriptPropertySignature) :
    VueLocallyDefinedComponent.DelegateComponentProvider {

    override fun getDelegate(): VueComponent? =
      VueComponents.getSourceComponent(source)
      ?: VueTypedComponent(source, source.name ?: return null)

    override fun createPointer(): Pointer<VueLocallyDefinedComponent.DelegateComponentProvider> {
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { VueTypedComponentDelegateProvider(it) }
      }
    }
  }
}