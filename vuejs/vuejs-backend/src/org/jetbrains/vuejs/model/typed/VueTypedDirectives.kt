// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.javascript.typeEngine.JSTypeEngineMode
import com.intellij.javascript.typeEngine.getJSType
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSRecordType.TypeMember
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.source.DIRECTIVE_MODIFIERS_PROP
import org.jetbrains.vuejs.model.source.DIRECTIVE_MOUNTED_FUN
import org.jetbrains.vuejs.model.source.VueDeclarations.findDeclaration

internal object VueTypedDirectives {
  fun getDirectiveModifiers(
    source: PsiElement,
    mode: VueMode,
  ): List<VueDirectiveModifier> =
    getDirectiveModifiersInternal(
      source = findDeclaration(source) ?: source,
      mode = mode,
    )

  fun getDirectiveModifiers(
    source: TypeScriptPropertySignature,
    mode: VueMode,
  ): List<VueDirectiveModifier> {
    return getDirectiveModifiersInternal(source, mode)
  }

  private fun getDirectiveModifiersInternal(
    source: PsiElement,
    mode: VueMode,
  ): List<VueDirectiveModifier> {
    val modifiersType = when (mode) {
      VueMode.CLASSIC -> getClassicDirectiveModifiersType(source)
      VueMode.VAPOR -> source.functionParameterType(3)
    }

    modifiersType ?: return emptyList()

    return modifiersType.asRecordType()
      .properties
      .map { property ->
        val source = when (property) {
          is PsiElement -> property
          is TypeMember -> property.memberSource.singleElement
          else -> null
        }

        VueTypedDirectiveModifier(property.memberName, source)
      }
  }

  private fun getClassicDirectiveModifiersType(
    source: PsiElement,
  ): JSType? {
    if (source !is JSTypeOwner)
      return null

    val jsType = source.getJSType(JSTypeEngineMode.Service)
                 ?: return null

    val mounted = jsType.asRecordType()
                    .findPropertySignature(DIRECTIVE_MOUNTED_FUN)
                  ?: return null

    val bindingType = mounted.jsType
                        ?.functionParameterType(1)
                      ?: return null

    return bindingType.asRecordType()
      .findPropertySignature(DIRECTIVE_MODIFIERS_PROP)
      ?.jsType
  }

  private fun PsiElement.functionParameterType(
    parameterIndex: Int,
  ): JSType? =
    // WEB-80110 getJSType(JSTypeEngineMode.Service) breaks VueHighlightingTest$WithLegacyPluginTest#testDirectivesFromNestedObjectPlugin_vapor
    // and VueHighlightingTest$WithLegacyPluginTest#testDirectivesFromNestedFunctionPlugin_vapor
    when (this) {
      is JSFunction -> parameters.getOrNull(parameterIndex)?.jsType
      is JSTypeOwner -> jsType?.functionParameterType(parameterIndex)
      else -> null
    }

  private fun JSType.functionParameterType(
    parameterIndex: Int,
  ): JSType? {
    val functionType = substitute() as? JSFunctionType
                       ?: return null

    return functionType.parameters
      .getOrNull(parameterIndex)
      ?.simpleType
  }
}
