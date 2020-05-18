// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.types.Angular2TemplateBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static com.intellij.util.containers.ContainerUtil.find;
import static com.intellij.util.containers.ContainerUtil.findInstance;

public class Angular2TemplateBindingImpl extends JSStatementImpl implements Angular2TemplateBinding {

  private final @NotNull String myKey;
  private final boolean myVar;
  private final @Nullable String myName;

  public Angular2TemplateBindingImpl(@NotNull IElementType elementType, @NotNull String key, boolean isVar, @Nullable String name) {
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
  public @NotNull String getKey() {
    return myKey;
  }

  @Override
  public @Nullable Angular2TemplateBindingKey getKeyElement() {
    return findInstance(getChildren(), Angular2TemplateBindingKey.class);
  }

  @Override
  public @Nullable JSType getKeyJSType() {
    if (!keyIsVar()) {
      Angular2TemplateBindings bindings = tryCast(getParent(), Angular2TemplateBindings.class);
      if (bindings != null) {
        return new Angular2TemplateBindingType(bindings, myKey);
      }
    }
    return null;
  }

  @Override
  public @Nullable String getName() {
    return myName;
  }

  @Override
  public @Nullable JSVariable getVariableDefinition() {
    return doIfNotNull(findInstance(getChildren(), JSVarStatement.class),
                       s -> ArrayUtil.getFirstElement(s.getVariables()));
  }

  @Override
  public boolean keyIsVar() {
    return myVar;
  }

  @Override
  public @Nullable JSExpression getExpression() {
    return doIfNotNull(find(getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS),
                            node -> node.getElementType() != Angular2ElementTypes.TEMPLATE_BINDING_KEY),
                       node -> node.getPsi(JSExpression.class));
  }

  @Override
  public String toString() {
    return "Angular2TemplateBinding <" + getKey() + ", " + keyIsVar() + ", " + getName() + ">";
  }
}
