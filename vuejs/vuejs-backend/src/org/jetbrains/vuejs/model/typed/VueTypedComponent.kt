// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyNewType
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.lang.html.isVueFileName
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueRegularComponent

class VueTypedComponent(
  override val source: PsiElement,
  override val defaultName: String,
) : VueTypedContainer(source), VueRegularComponent {

  override val nameElement: PsiElement?
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
        ?.let { VueModelManager.findEnclosingContainer(it) as? VueRegularComponent }
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

}