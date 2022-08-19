// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi;

import com.intellij.lang.javascript.psi.*;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see Angular2HtmlTemplateBindings
 * @see Angular2TemplateBindings
 */
public interface Angular2TemplateBinding extends JSStatement {

  @NotNull
  String getKey();

  @Nullable
  Angular2TemplateBindingKey getKeyElement();

  @Nullable
  JSType getKeyJSType();

  boolean keyIsVar();

  @Override
  @Nullable
  String getName();

  @Nullable
  JSVariable getVariableDefinition();

  @Nullable
  JSExpression getExpression();

  Angular2TemplateBinding[] EMPTY_ARRAY = new Angular2TemplateBinding[0];
}
