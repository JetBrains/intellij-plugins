// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import org.angular2.lang.Angular2EmbeddedContentTokenType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.lexer.Angular2Lexer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class Angular2EmbeddedExprTokenType extends Angular2EmbeddedContentTokenType {

  public static final Angular2EmbeddedExprTokenType ACTION_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:ACTION_EXPR", Angular2EmbeddedExprTokenType.ExpressionType.ACTION);
  public static final Angular2EmbeddedExprTokenType BINDING_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:BINDING_EXPR", Angular2EmbeddedExprTokenType.ExpressionType.BINDING);
  public static final Angular2EmbeddedExprTokenType INTERPOLATION_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:INTERPOLATION_EXPR", Angular2EmbeddedExprTokenType.ExpressionType.INTERPOLATION);
  public static final Angular2EmbeddedExprTokenType SIMPLE_BINDING_EXPR = new Angular2EmbeddedExprTokenType(
    "NG:SIMPLE_BINDING_EXPR", ExpressionType.SIMPLE_BINDING);

  public static Angular2EmbeddedExprTokenType createTemplateBindings(String templateKey) {
    return new Angular2EmbeddedExprTokenType("NG:TEMPLATE_BINDINGS_EXPR", ExpressionType.TEMPLATE_BINDINGS, templateKey);
  }

  private final @NotNull ExpressionType myExpressionType;
  private final @Nullable String myTemplateKey;

  private Angular2EmbeddedExprTokenType(@NotNull @NonNls String debugName, @NotNull ExpressionType expressionType) {
    super(debugName, Angular2Language.INSTANCE);
    myExpressionType = expressionType;
    myTemplateKey = null;
  }

  private Angular2EmbeddedExprTokenType(@NotNull @NonNls String debugName,
                                        @NotNull ExpressionType expressionType,
                                        @Nullable @NonNls String templateKey) {
    super(debugName, Angular2Language.INSTANCE, false);
    myExpressionType = expressionType;
    myTemplateKey = templateKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2EmbeddedExprTokenType type = (Angular2EmbeddedExprTokenType)o;
    return myExpressionType == type.myExpressionType &&
           Objects.equals(myTemplateKey, type.myTemplateKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), myExpressionType, myTemplateKey);
  }

  @Override
  protected @NotNull Lexer createLexer() {
    return new Angular2Lexer();
  }

  @Override
  protected void parse(@NotNull PsiBuilder builder) {
    myExpressionType.parse(builder, this, myTemplateKey);
  }

  public enum ExpressionType {
    ACTION(Angular2Parser::parseAction),
    BINDING(Angular2Parser::parseBinding),
    INTERPOLATION(Angular2Parser::parseInterpolation),
    SIMPLE_BINDING(Angular2Parser::parseSimpleBinding),
    TEMPLATE_BINDINGS(null);

    private final BiConsumer<? super PsiBuilder, ? super IElementType> myParseMethod;

    ExpressionType(BiConsumer<? super PsiBuilder, ? super IElementType> parseMethod) {
      myParseMethod = parseMethod;
    }

    public void parse(@NotNull PsiBuilder builder, @NotNull IElementType root, @Nullable String templateKey) {
      if (this == TEMPLATE_BINDINGS) {
        assert templateKey != null;
        Angular2Parser.parseTemplateBindings(builder, root, templateKey);
      }
      else {
        myParseMethod.accept(builder, root);
      }
    }
  }
}