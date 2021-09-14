// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSExtendedLanguagesTokenSetProvider;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlAttribute;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.angular2.lang.expr.psi.Angular2Quote;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.types.Angular2PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public final class Angular2BindingImpl extends Angular2EmbeddedExpressionImpl implements Angular2Binding {
  public Angular2BindingImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2ElementVisitor) {
      ((Angular2ElementVisitor)visitor).visitAngular2Binding(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public @Nullable JSType getExpectedType() {
    var attribute = getEnclosingAttribute(this);
    var descriptor = tryCast(doIfNotNull(attribute, XmlAttribute::getDescriptor), Angular2AttributeDescriptor.class);
    if (descriptor == null) return null;
    var info = descriptor.getInfo();
    if (info.type == Angular2AttributeType.PROPERTY_BINDING || info.type == Angular2AttributeType.BANANA_BOX_BINDING) {
      return new Angular2PropertyBindingType(attribute);
    }
    return null;
  }

  static @Nullable JSExpression getExpression(Angular2EmbeddedExpressionImpl expression) {
    return Arrays.stream(expression.getChildren(JSExtendedLanguagesTokenSetProvider.EXPRESSIONS))
      .map(node -> node.getPsi(JSExpression.class))
      .findFirst()
      .orElse(null);
  }

  static @Nullable Angular2Quote getQuote(Angular2EmbeddedExpressionImpl expression) {
    return Arrays.stream(expression.getChildren(TokenSet.create(Angular2ElementTypes.QUOTE_STATEMENT)))
      .map(node -> node.getPsi(Angular2Quote.class))
      .findFirst()
      .orElse(null);
  }

  static @Nullable XmlAttribute getEnclosingAttribute(Angular2EmbeddedExpression expression) {
    XmlAttribute attribute = getParentOfType(expression, XmlAttribute.class);
    if (attribute == null) {
      attribute = getParentOfType(InjectedLanguageManager.getInstance(expression.getProject()).getInjectionHost(expression),
                                  XmlAttribute.class);
    }
    return attribute;
  }

  @Override
  public @Nullable JSExpression getExpression() {
    return getExpression(this);
  }

  @Override
  public @Nullable Angular2Quote getQuote() {
    return getQuote(this);
  }
}
