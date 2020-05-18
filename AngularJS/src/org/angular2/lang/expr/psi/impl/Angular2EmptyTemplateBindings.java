// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2EmptyTemplateBindings extends FakePsiElement implements Angular2TemplateBindings {

  private final PsiElement myParent;
  private final String myTemplateName;

  public Angular2EmptyTemplateBindings(@Nullable PsiElement parent, @NotNull String templateName) {
    myParent = parent;
    myTemplateName = templateName;
  }

  @Override
  public @NotNull String getTemplateName() {
    return myTemplateName;
  }

  @Override
  public Angular2TemplateBinding @NotNull [] getBindings() {
    return Angular2TemplateBinding.EMPTY_ARRAY;
  }

  @Override
  public IElementType getElementType() {
    return Angular2ElementTypes.TEMPLATE_BINDINGS_STATEMENT;
  }

  @Override
  public @Nullable PsiElement getParent() {
    return myParent;
  }
}
