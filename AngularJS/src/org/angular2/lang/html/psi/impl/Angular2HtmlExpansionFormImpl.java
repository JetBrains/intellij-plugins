// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlExpansionForm;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlExpansionFormImpl extends CompositePsiElement implements Angular2HtmlExpansionForm {

  public Angular2HtmlExpansionFormImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitExpansionForm(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public JSStatement getSwitchValue() {
    return null;
  }
}
