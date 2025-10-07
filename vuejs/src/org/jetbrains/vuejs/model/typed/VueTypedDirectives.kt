// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.source.DIRECTIVE_MODIFIERS_PROP
import org.jetbrains.vuejs.model.source.DIRECTIVE_MOUNTED_FUN
import org.jetbrains.vuejs.model.source.VueDeclarations.findDeclaration

internal object VueTypedDirectives {
  fun getDirectiveModifiers(
    source: PsiElement,
  ): List<VueTypedDirectiveModifier> {
    val declaration = findDeclaration(source) as? JSTypeOwner
                      ?: return emptyList()

    return getDirectiveModifiers(declaration.jsType)
  }

  fun getDirectiveModifiers(
    source: TypeScriptPropertySignature,
  ): List<VueTypedDirectiveModifier> {
    return getDirectiveModifiers(source.jsType)
  }

  private fun getDirectiveModifiers(
    jsType: JSType?,
  ): List<VueTypedDirectiveModifier> {
    jsType ?: return emptyList()

    val mounted = jsType.asRecordType()
                    .findPropertySignature(DIRECTIVE_MOUNTED_FUN)
                  ?: return emptyList()

    val bindingType = mounted.jsType
                        ?.substitute()
                        ?.let { it as? JSFunctionType }
                        ?.let { it.parameters.getOrNull(1) }
                        ?.let { it.simpleType }
                      ?: return emptyList()

    val modifiers = bindingType.asRecordType()
                      .findPropertySignature(DIRECTIVE_MODIFIERS_PROP)
                      ?.jsType
                      ?.asRecordType()
                    ?: return emptyList()

    return modifiers.properties.asSequence()
      .filterIsInstance<TypeScriptPropertySignature>()
      .map(::VueTypedDirectiveModifier)
      .toList()
  }
}
