// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.source.DIRECTIVE_MODIFIERS_PROP
import org.jetbrains.vuejs.model.source.DIRECTIVE_MOUNTED_FUN
import org.jetbrains.vuejs.model.source.VueDeclarations.findDeclaration

internal object VueTypedDirectives {
  fun getDirectiveModifiers(
    source: PsiElement,
    mode: VueMode,
  ): List<VueTypedDirectiveModifier> {
    val declaration = findDeclaration(source) as? JSTypeOwner
                      ?: return emptyList()

    return getDirectiveModifiers(declaration.jsType, mode)
  }

  fun getDirectiveModifiers(
    source: TypeScriptPropertySignature,
    mode: VueMode,
  ): List<VueTypedDirectiveModifier> {
    return getDirectiveModifiers(source.jsType, mode)
  }

  private fun getDirectiveModifiers(
    jsType: JSType?,
    mode: VueMode,
  ): List<VueTypedDirectiveModifier> {
    jsType ?: return emptyList()

    val modifiers = when (mode) {
      VueMode.CLASSIC -> {
        val mounted = jsType.asRecordType()
                        .findPropertySignature(DIRECTIVE_MOUNTED_FUN)
                      ?: return emptyList()

        val bindingType = mounted.jsType
                            ?.functionParameterType(1)
                          ?: return emptyList()

        bindingType.asRecordType()
          .findPropertySignature(DIRECTIVE_MODIFIERS_PROP)
          ?.jsType
      }

      VueMode.VAPOR -> {
        // implement
        null
      }
    }

    modifiers ?: return emptyList()

    return modifiers.asRecordType()
      .properties.asSequence()
      .filterIsInstance<TypeScriptPropertySignature>()
      .map(::VueTypedDirectiveModifier)
      .toList()
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
