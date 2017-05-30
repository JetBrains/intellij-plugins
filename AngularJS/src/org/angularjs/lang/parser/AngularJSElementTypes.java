package org.angularjs.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.angularjs.lang.AngularJSLanguage;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSMessageFormatExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSElementTypes {
  IFileElementType FILE = JSFileElementType.create(AngularJSLanguage.INSTANCE);
  IElementType REPEAT_EXPRESSION = new AngularJSElementType("REPEAT_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new AngularJSRepeatExpression(this);
    }
  };
  IElementType FOR_EXPRESSION = new AngularJSElementType("REPEAT_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new AngularJSRepeatExpression(this);
    }
  };
  IElementType FILTER_EXPRESSION = new AngularJSElementType("FILTER_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new AngularJSFilterExpression(this);
    }
  };
  IElementType AS_EXPRESSION = new AngularJSElementType("AS_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new AngularJSAsExpression(this);
    }
  };
  

  IElementType MESSAGE_FORMAT_EXPRESSION_NAME = new IElementType("MESSAGE_FORMAT_EXPRESSION_NAME", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_EXPRESSION = new AngularJSElementType("MESSAGE_FORMAT_EXPRESSION") {
    @NotNull
    @Override
    public ASTNode createCompositeNode() {
      return new AngularJSMessageFormatExpression(this);
    }
  };
  IElementType MESSAGE_FORMAT_MESSAGE = new IElementType("MESSAGE_FORMAT_MESSAGE", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_OPTION = new IElementType("MESSAGE_FORMAT_OPTION", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_SELECTION_KEYWORD = new IElementType("MESSAGE_FORMAT_SELECTION_KEYWORD", AngularJSLanguage.INSTANCE);

  IElementType EMBEDDED_CONTENT = new JSEmbeddedContentElementType(AngularJSLanguage.INSTANCE, "ANG_") {
    @Override
    protected Lexer createStripperLexer(Language baseLanguage) {
      return null;
    }
  };
  
  abstract class AngularJSElementType extends IElementType implements ICompositeElementType {

    public AngularJSElementType(@NotNull String debugName) {
      super(debugName,  AngularJSLanguage.INSTANCE);
    }
    
  }
}
