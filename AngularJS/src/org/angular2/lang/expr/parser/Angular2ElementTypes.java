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
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2MessageFormatExpression;
import org.angular2.lang.expr.psi.impl.Angular2BindingPipeImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public interface Angular2ElementTypes {
  IFileElementType FILE = JSFileElementType.create(Angular2Language.INSTANCE);
  IElementType PIPE_EXPRESSION = new Angular2ElementType("PIPE_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new Angular2BindingPipeImpl(this);
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
  
  abstract class Angular2ElementType extends IElementType implements ICompositeElementType {

    public Angular2ElementType(@NotNull String debugName) {
      super(debugName,  Angular2Language.INSTANCE);
    }
    
  }
}
