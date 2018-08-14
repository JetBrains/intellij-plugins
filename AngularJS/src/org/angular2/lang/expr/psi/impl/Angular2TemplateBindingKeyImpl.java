// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey;
import org.jetbrains.annotations.NotNull;

public class Angular2TemplateBindingKeyImpl extends JSExpressionImpl implements Angular2TemplateBindingKey {

  public Angular2TemplateBindingKeyImpl(IElementType elementType) {
    super(elementType);
  }

  @NotNull
  @Override
  public String getName() {
    return getText();
  }
}
