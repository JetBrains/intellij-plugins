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

  IElementType PIPE_EXPRESSION = new Angular2ExpressionElementType("NG:PIPE_EXPRESSION", Angular2PipeExpressionImpl::new);
  IElementType PIPE_ARGUMENTS_LIST = new Angular2ExpressionElementType("NG:PIPE_ARGUMENTS_LIST", Angular2PipeArgumentsListImpl::new);
  IElementType PIPE_LEFT_SIDE_ARGUMENT =
    new Angular2ExpressionElementType("NG:PIPE_LEFT_SIDE_ARGUMENT", Angular2PipeLeftSideArgumentImpl::new);
  IElementType PIPE_REFERENCE_EXPRESSION =
    new Angular2ExpressionElementType("NG:PIPE_REFERENCE_EXPRESSION", Angular2PipeReferenceExpressionImpl::new);
  IElementType CHAIN_STATEMENT = new Angular2ElementType("NG:CHAIN_STATEMENT", Angular2ChainImpl::new);
  IElementType QUOTE_STATEMENT = new Angular2ElementType("NG:QUOTE_STATEMENT", Angular2QuoteImpl::new);
  IElementType ACTION_STATEMENT = new Angular2ElementType("NG:ACTION", Angular2ActionImpl::new);
  IElementType BINDING_STATEMENT = new Angular2ElementType("NG:BINDING", Angular2BindingImpl::new);
  IElementType INTERPOLATION_STATEMENT = new Angular2ElementType("NG:INTERPOLATION", Angular2InterpolationImpl::new);
  IElementType SIMPLE_BINDING_STATEMENT = new Angular2ElementType("NG:SIMPLE_BINDING", Angular2SimpleBindingImpl::new);
  IElementType TEMPLATE_BINDINGS_STATEMENT = new Angular2ElementType("NG:TEMPLATE_BINDINGS_STATEMENT", (type) -> {
    throw new UnsupportedOperationException("Use createTemplateBindingsStatement method instead");
  });
  IElementType TEMPLATE_BINDING_KEY = new Angular2ElementType("NG:TEMPLATE_BINDING_KEY", Angular2TemplateBindingKeyImpl::new);
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
      //noinspection HardCodedStringLiteral
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
      //noinspection HardCodedStringLiteral
      super("NG:TEMPLATE_BINDINGS_STATEMENT", Angular2Language.INSTANCE, false);
      myTemplateName = templateName;
    }

    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new Angular2TemplateBindingsImpl(TEMPLATE_BINDINGS_STATEMENT, myTemplateName);
    }
  }
}
