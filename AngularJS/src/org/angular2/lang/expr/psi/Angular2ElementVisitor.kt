// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSElementVisitor

open class Angular2ElementVisitor : JSElementVisitor() {
  open fun visitAngular2Action(action: Angular2Action) {
    visitJSSourceElement(action)
  }

  open fun visitAngular2Binding(binding: Angular2Binding) {
    visitJSSourceElement(binding)
  }

  open fun visitAngular2Chain(expressionChain: Angular2Chain) {
    visitJSStatement(expressionChain)
  }

  open fun visitAngular2Interpolation(interpolation: Angular2Interpolation) {
    visitJSSourceElement(interpolation)
  }

  open fun visitAngular2PipeExpression(pipe: Angular2PipeExpression) {
    visitJSCallExpression(pipe)
  }

  open fun visitAngular2Quote(quote: Angular2Quote) {
    visitJSStatement(quote)
  }

  open fun visitAngular2SimpleBinding(simpleBinding: Angular2SimpleBinding) {
    visitJSSourceElement(simpleBinding)
  }

  fun visitAngular2TemplateBinding(templateBinding: Angular2TemplateBinding) {
    visitJSStatement(templateBinding)
  }

  fun visitAngular2TemplateBindingKey(templateBindingKey: Angular2TemplateBindingKey) {
    visitJSExpression(templateBindingKey)
  }

  fun visitAngular2TemplateBindings(templateBindings: Angular2TemplateBindings) {
    visitJSSourceElement(templateBindings)
  }
}