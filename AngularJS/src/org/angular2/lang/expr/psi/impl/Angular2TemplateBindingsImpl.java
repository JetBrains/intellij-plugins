// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Angular2TemplateBindingsImpl extends Angular2EmbeddedExpressionImpl implements Angular2TemplateBindings {

  private final @NotNull String myTemplateName;

  public Angular2TemplateBindingsImpl(IElementType elementType, @NotNull String templateName) {
    super(elementType);
    myTemplateName = templateName;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2TemplateBindings(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public @NotNull String getTemplateName() {
    return myTemplateName;
  }

  @Override
  public Angular2TemplateBinding @NotNull [] getBindings() {
    return Arrays.stream(getChildren(TokenSet.create(Angular2ElementTypes.TEMPLATE_BINDING_STATEMENT)))
      .map(n -> n.getPsi(Angular2TemplateBinding.class))
      .toArray(Angular2TemplateBinding[]::new);
  }
}
