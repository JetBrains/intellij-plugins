// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSElementVisitor;

public class Angular2ElementVisitor extends JSElementVisitor {

  public void visitAngular2Chain(Angular2Chain expressionChain) {
    visitJSStatement(expressionChain);
  }

  public void visitAngular2Pipe(Angular2Pipe pipe) {
    visitJSExpression(pipe);
  }

  public void visitAngular2Quote(Angular2Quote quote) {
    visitJSStatement(quote);
  }
}
