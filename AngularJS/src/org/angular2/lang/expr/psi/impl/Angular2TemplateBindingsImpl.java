// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;

import java.util.Arrays;

public class Angular2TemplateBindingsImpl extends JSStatementImpl implements Angular2TemplateBindings {

  public Angular2TemplateBindingsImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public Angular2TemplateBinding[] getBindings() {
    return Arrays.stream(getChildren(TokenSet.create(Angular2ElementTypes.TEMPLATE_BINDING_STATEMENT)))
      .map(n -> n.getPsi(Angular2TemplateBinding.class))
      .toArray(Angular2TemplateBinding[]::new);
  }
}
