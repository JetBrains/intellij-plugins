// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSElementVisitor;

public class Angular2ElementVisitor extends JSElementVisitor {

  public void visitAngular2Action(Angular2Action action) {
    visitJSSourceElement(action);
  }

  public void visitAngular2Binding(Angular2Binding binding) {
    visitJSSourceElement(binding);
  }

  public void visitAngular2Chain(Angular2Chain expressionChain) {
    visitJSStatement(expressionChain);
  }

  public void visitAngular2Interpolation(Angular2Interpolation interpolation) {
    visitJSSourceElement(interpolation);
  }

  public void visitAngular2PipeExpression(Angular2PipeExpression pipe) {
    visitJSCallExpression(pipe);
  }

  public void visitAngular2Quote(Angular2Quote quote) {
    visitJSStatement(quote);
  }

  public void visitAngular2SimpleBinding(Angular2SimpleBinding simpleBinding) {
    visitJSSourceElement(simpleBinding);
  }

  public void visitAngular2TemplateBinding(Angular2TemplateBinding templateBinding) {
    visitJSStatement(templateBinding);
  }

  public void visitAngular2TemplateBindingKey(Angular2TemplateBindingKey templateBindingKey) {
    visitJSExpression(templateBindingKey);
  }

  public void visitAngular2TemplateBindings(Angular2TemplateBindings templateBindings) {
    visitJSSourceElement(templateBindings);
  }
}
