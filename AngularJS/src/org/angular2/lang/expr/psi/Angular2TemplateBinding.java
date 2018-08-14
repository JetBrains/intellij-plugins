// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSStatement;

public interface Angular2TemplateBinding extends JSStatement {

  String getKey();

  boolean keyIsVar();

  String getName();

  JSExpression getExpression();
}
