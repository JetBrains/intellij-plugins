// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Angular2TemplateBindingImpl extends JSStatementImpl implements Angular2TemplateBinding {

  private final String myKey;
  private final boolean myVar;
  private final String myName;

  public Angular2TemplateBindingImpl(IElementType elementType, String key, boolean isVar, String name) {
    super(elementType);
    myKey = key;
    myVar = isVar;
    myName = name;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2TemplateBinding(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public String getKey() {
    return myKey;
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public boolean keyIsVar() {
    return myVar;
  }

  @Override
  public JSExpression getExpression() {
    return Arrays.stream(getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS))
      .filter(node -> node.getElementType() != Angular2ElementTypes.TEMPLATE_BINDING_KEY)
      .map(node -> node.getPsi(JSExpression.class))
      .findFirst()
      .orElse(null);
  }
}
