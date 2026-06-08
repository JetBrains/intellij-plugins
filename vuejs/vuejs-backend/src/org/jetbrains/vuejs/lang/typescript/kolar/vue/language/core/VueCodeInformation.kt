// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import com.intellij.lang.typescript.kolar.KolarCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.js.symbol.Symbol

data class VueCodeInformation(
  override val verification: VerificationInfo? = null,
  override val completion: CompletionInfo? = null,
  override val semantic: SemanticInfo? = null,
  override val navigation: NavigationInfo? = null,
  override val structure: Boolean = false,
  override val format: Boolean = false,
  override val types: Boolean = false,
  override val reverseTypes: Boolean = false,
  val __importCompletion: Boolean = false,
  val __propsCompletion: Boolean = false,
  val __shorthandExpression: ShorthandExpression? = null,
  val __combineToken: Symbol? = null,
  val __linkedToken: Symbol? = null,
) : KolarCodeInformation(
  verification = verification,
  completion = completion,
  semantic = semantic,
  navigation = navigation,
  structure = structure,
  format = format,
  types = types,
  reverseTypes = reverseTypes,
) {

  enum class ShorthandExpression {
    html,
    js,

    ;
  }
}
