// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.resolve

import groovy.lang.Closure
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.GrFunctionalExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.resolve.delegatesTo.DelegatesToInfo
import org.jetbrains.plugins.groovy.lang.resolve.delegatesTo.GrDelegatesToProvider
import org.jetbrains.plugins.groovy.lang.resolve.delegatesTo.getContainingCall

class CustomWorldDelegatesToProvider : GrDelegatesToProvider {

  override fun getDelegatesToInfo(expression: GrFunctionalExpression): DelegatesToInfo? {
    val stepFile = expression.containingFile as? GroovyFile ?: return null
    val call = getContainingCall(expression) as? GrMethodCall ?: return null
    return if (GrCucumberUtil.isStepDefinition(call) || GrCucumberUtil.isHook(call)) {
      DelegatesToInfo(CustomWorldType(stepFile), Closure.DELEGATE_FIRST)
    }
    else {
      null
    }
  }
}
