// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import org.jetbrains.vuejs.model.source.DIRECTIVE_MODIFIERS_PROP
import org.jetbrains.vuejs.model.source.DIRECTIVE_MOUNTED_FUN

internal object VueTypedDirectives {
  fun getDirectiveModifiers(
    source: TypeScriptPropertySignature,
  ): List<VueTypedDirectiveModifier> {
    val jsType = source.jsType
                 ?: return emptyList()

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
