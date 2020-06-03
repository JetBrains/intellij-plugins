// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.types.JSExpressionElementType;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.impl.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static com.intellij.lang.javascript.JSKeywordSets.IDENTIFIER_NAMES;
import static com.intellij.lang.javascript.JSTokenTypes.STRING_LITERAL;

public interface Angular2ElementTypes extends JSElementTypes, Angular2StubElementTypes {

  IElementType PIPE_EXPRESSION = new Angular2ExpressionElementType("NG:PIPE_EXPRESSION", node -> new Angular2PipeExpressionImpl(node));
  IElementType PIPE_ARGUMENTS_LIST = new Angular2ExpressionElementType("NG:PIPE_ARGUMENTS_LIST", node -> new Angular2PipeArgumentsListImpl(node));
  IElementType PIPE_LEFT_SIDE_ARGUMENT =
    new Angular2ExpressionElementType("NG:PIPE_LEFT_SIDE_ARGUMENT", node -> new Angular2PipeLeftSideArgumentImpl(node));
  IElementType PIPE_REFERENCE_EXPRESSION =
    new Angular2ExpressionElementType("NG:PIPE_REFERENCE_EXPRESSION", node -> new Angular2PipeReferenceExpressionImpl(node));
  IElementType CHAIN_STATEMENT = new Angular2ElementType("NG:CHAIN_STATEMENT", node -> new Angular2ChainImpl(node));
  IElementType QUOTE_STATEMENT = new Angular2ElementType("NG:QUOTE_STATEMENT", node -> new Angular2QuoteImpl(node));
  IElementType ACTION_STATEMENT = new Angular2ElementType("NG:ACTION", node -> new Angular2ActionImpl(node));
  IElementType BINDING_STATEMENT = new Angular2ElementType("NG:BINDING", node -> new Angular2BindingImpl(node));
  IElementType INTERPOLATION_STATEMENT = new Angular2ElementType("NG:INTERPOLATION", node -> new Angular2InterpolationImpl(node));
  IElementType SIMPLE_BINDING_STATEMENT = new Angular2ElementType("NG:SIMPLE_BINDING", node -> new Angular2SimpleBindingImpl(node));
  IElementType TEMPLATE_BINDINGS_STATEMENT = new Angular2ElementType("NG:TEMPLATE_BINDINGS_STATEMENT", (type) -> {
    throw new UnsupportedOperationException("Use createTemplateBindingsStatement method instead");
  });
  IElementType TEMPLATE_BINDING_KEY = new Angular2ElementType("NG:TEMPLATE_BINDING_KEY", node -> new Angular2TemplateBindingKeyImpl(node));
  IElementType TEMPLATE_BINDING_STATEMENT = new Angular2ElementType("NG:TEMPLATE_BINDING_STATEMENT", (type) -> {
    throw new UnsupportedOperationException("Use createTemplateBindingStatement method instead");
  });

  TokenSet PROPERTY_NAMES = TokenSet.orSet(IDENTIFIER_NAMES, TokenSet.create(STRING_LITERAL));

  static IElementType createTemplateBindingStatement(@NotNull String key, boolean isVar, @Nullable String name) {
    return new Angular2TemplateBindingType(key, isVar, name);
  }

  static IElementType createTemplateBindingsStatement(@NotNull String templateName) {
    return new Angular2TemplateBindingsType(templateName);
  }

  class Angular2ElementType extends IElementType implements ICompositeElementType {

    private final @NotNull Function<Angular2ElementType, ASTNode> myClassConstructor;

    public Angular2ElementType(@NotNull @NonNls String debugName, @NotNull Function<Angular2ElementType, ASTNode> classConstructor) {
      super(debugName, Angular2Language.INSTANCE);
      myClassConstructor = classConstructor;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
      return myClassConstructor.apply(this);
    }
  }

  class Angular2ExpressionElementType extends Angular2ElementType implements JSExpressionElementType {

    public Angular2ExpressionElementType(@NotNull @NonNls String debugName,
                                         @NotNull Function<Angular2ElementType, ASTNode> classConstructor) {
      super(debugName, classConstructor);
    }
  }

  class Angular2TemplateBindingType extends IElementType implements ICompositeElementType {

    private final @NotNull String myKey;
    private final boolean myVar;
    private final @Nullable String myName;

    public Angular2TemplateBindingType(@NotNull String key, boolean isVar, @Nullable String name) {
      super("NG:TEMPLATE_BINDING_STATEMENT", Angular2Language.INSTANCE, false);
      myKey = key;
      myVar = isVar;
      myName = name;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new Angular2TemplateBindingImpl(TEMPLATE_BINDING_STATEMENT, myKey, myVar, myName);
    }
  }

  class Angular2TemplateBindingsType extends IElementType implements ICompositeElementType {


    private final @NotNull String myTemplateName;

    public Angular2TemplateBindingsType(@NotNull String templateName) {
      super("NG:TEMPLATE_BINDINGS_STATEMENT", Angular2Language.INSTANCE, false);
      myTemplateName = templateName;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new Angular2TemplateBindingsImpl(TEMPLATE_BINDINGS_STATEMENT, myTemplateName);
    }
  }
}
