// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.expr.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType;
import com.intellij.lang.javascript.types.JSExpressionElementType;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2MessageFormatExpression;
import org.angular2.lang.expr.psi.impl.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public interface Angular2ElementTypes extends JSElementTypes {

  IFileElementType FILE = JSFileElementType.create(Angular2Language.INSTANCE);

  IElementType PIPE_EXPRESSION = new Angular2ExpressionElementType("NG:PIPE_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2PipeImpl(this);
    }
  };
  IElementType CHAIN_STATEMENT = new Angular2ElementType("NG:CHAIN_STATEMENT") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2ChainImpl(this);
    }
  };
  IElementType QUOTE_STATEMENT = new Angular2ElementType("NG:QUOTE_STATEMENT") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2QuoteImpl(this);
    }
  };
  IElementType TEMPLATE_BINDINGS_STATEMENT = new Angular2ElementType("NG:TEMPLATE_BINDINGS_STATEMENT") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2TemplateBindingsImpl(this);
    }
  };
  IElementType TEMPLATE_BINDING_STATEMENT = new Angular2ElementType("NG:TEMPLATE_BINDING_STATEMENT") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      throw new UnsupportedOperationException("Use createTemplateBindingStatement method instead");
    }
  };
  IElementType TEMPLATE_BINDING_KEY = new Angular2ElementType("NG:TEMPLATE_BINDING_KEY") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2TemplateBindingKeyImpl(this);
    }
  };


  IElementType MESSAGE_FORMAT_EXPRESSION_NAME = new IElementType("MESSAGE_FORMAT_EXPRESSION_NAME", Angular2Language.INSTANCE);
  IElementType MESSAGE_FORMAT_EXPRESSION = new Angular2ElementType("MESSAGE_FORMAT_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2MessageFormatExpression(this);
    }
  };
  IElementType MESSAGE_FORMAT_MESSAGE = new IElementType("MESSAGE_FORMAT_MESSAGE", Angular2Language.INSTANCE);
  IElementType MESSAGE_FORMAT_OPTION = new IElementType("MESSAGE_FORMAT_OPTION", Angular2Language.INSTANCE);
  IElementType MESSAGE_FORMAT_SELECTION_KEYWORD = new IElementType("MESSAGE_FORMAT_SELECTION_KEYWORD", Angular2Language.INSTANCE);

  IElementType EMBEDDED_CONTENT = new JSEmbeddedContentElementType(Angular2Language.INSTANCE, "ANG_") {
    @Override
    protected Lexer createStripperLexer(Language baseLanguage) {
      return null;
    }
  };

  static IElementType createTemplateBindingStatement(String key, boolean isVar, String name) {
    return new Angular2TemplateBindingType(key, isVar, name);
  }

  abstract class Angular2ElementType extends IElementType implements ICompositeElementType {

    public Angular2ElementType(@NotNull String debugName) {
      super(debugName, Angular2Language.INSTANCE);
    }
  }

  abstract class Angular2ExpressionElementType extends Angular2ElementType implements JSExpressionElementType {

    public Angular2ExpressionElementType(@NotNull String debugName) {
      super(debugName);
    }
  }

  class Angular2TemplateBindingType extends  IElementType implements ICompositeElementType {

    private final String myKey;
    private final boolean myVar;
    private final String myName;

    public Angular2TemplateBindingType(String key, boolean isVar, String name) {
      super ("NG:TEMPLATE_BINDING_STATEMENT", Angular2Language.INSTANCE, false);
      myKey = key;
      myVar = isVar;
      myName = name;
    }

    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2TemplateBindingImpl(TEMPLATE_BINDING_STATEMENT, myKey, myVar, myName);
    }

  }
}
