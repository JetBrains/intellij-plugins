// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.util.parentOfType
import org.angular2.codeInsight.blocks.*
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockVariableType : Angular2BaseType<Angular2BlockParameterVariableImpl> {

  constructor(variable: Angular2BlockParameterVariableImpl) : super(variable, Angular2BlockParameterVariableImpl::class.java)

  private constructor(source: JSTypeSource) : super(source, Angular2BlockParameterVariableImpl::class.java)

  override val typeOfText: String?
    get() = sourceElement.name

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    val parameter = sourceElement.parentOfType<Angular2BlockParameter>() ?: return null
    val block = parameter.parentOfType<Angular2HtmlBlock>() ?: return null

    return when (block.getName()) {
      BLOCK_IF ->
        if (parameter.name == PARAMETER_AS) {
          block.parameters.firstOrNull()
            ?.takeIf { it.isPrimaryExpression }
            ?.expression
            ?.let { JSPsiBasedTypeOfType(it, false) }
            ?.substituteOrNull()
            ?.let { JSTypeUtils.removeFalsyComponents(it) }
        }
        else null
      BLOCK_FOR ->
        if (parameter.isPrimaryExpression) {
          parameter.expression
            ?.let { JSPsiBasedTypeOfType(it, false) }
            ?.substitute(parameter.expression)
            ?.let { JSTypeUtils.removeNullableComponents(it) }
            ?.let { JSTypeUtils.getIterableComponentType(it) }
        }
        else if (parameter.name == PARAMETER_LET) {
          sourceElement.initializer
            ?.let { JSPsiBasedTypeOfType(it, false) }
        }
        else null
      BLOCK_LET ->
        sourceElement
          .initializer
          ?.let { JSPsiBasedTypeOfType(it, false) }
          ?.substituteOrNull()
      else -> null
    }
  }

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    Angular2BlockVariableType(source)
}