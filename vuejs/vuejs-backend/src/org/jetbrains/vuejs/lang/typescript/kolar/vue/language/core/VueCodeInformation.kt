// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import com.intellij.lang.typescript.kolar.KolarCodeInformation

class VueCodeInformation(
  verification: VerificationInfo? = null,
  completion: CompletionInfo? = null,
  semantic: SemanticInfo? = null,
  navigation: NavigationInfo? = null,
  structure: Boolean = false,
  format: Boolean = false,
  types: Boolean = false,
  reverseTypes: Boolean = false,
  val __importCompletion: Boolean = false,
  val __propsCompletion: Boolean = false,
  val __shorthandExpression: ShorthandExpression? = null,
  val __combineToken: Any? = null, // symbol
  val __linkedToken: Any? = null,  // symbol
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

  override fun equals(other: Any?): Boolean {
    if (!super.equals(other)) return false
    other as VueCodeInformation
    if (__importCompletion != other.__importCompletion) return false
    if (__propsCompletion != other.__propsCompletion) return false
    if (__shorthandExpression != other.__shorthandExpression) return false
    if (__combineToken != other.__combineToken) return false
    if (__linkedToken != other.__linkedToken) return false
    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + __importCompletion.hashCode()
    result = 31 * result + __propsCompletion.hashCode()
    result = 31 * result + __shorthandExpression.hashCode()
    result = 31 * result + __combineToken.hashCode()
    result = 31 * result + __linkedToken.hashCode()
    return result
  }
}
