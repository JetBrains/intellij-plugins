package org.angularjs.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.AngularJSLanguage;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSMessageFormatExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSElementTypes {
  IElementType REPEAT_EXPRESSION = new AngularJSElementType("REPEAT_EXPRESSION") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new AngularJSRepeatExpression(this);
    }
  };
  IElementType FOR_EXPRESSION = new AngularJSElementType("REPEAT_EXPRESSION") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new AngularJSRepeatExpression(this);
    }
  };
  IElementType FILTER_EXPRESSION = new AngularJSElementType("FILTER_EXPRESSION") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new AngularJSFilterExpression(this);
    }
  };


  IElementType MESSAGE_FORMAT_EXPRESSION_NAME = new IElementType("MESSAGE_FORMAT_EXPRESSION_NAME", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_EXPRESSION = new AngularJSElementType("MESSAGE_FORMAT_EXPRESSION") {
    @Override
    public @NotNull ASTNode createCompositeNode() {
      return new AngularJSMessageFormatExpression(this);
    }
  };
  IElementType MESSAGE_FORMAT_MESSAGE = new IElementType("MESSAGE_FORMAT_MESSAGE", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_OPTION = new IElementType("MESSAGE_FORMAT_OPTION", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_SELECTION_KEYWORD = new IElementType("MESSAGE_FORMAT_SELECTION_KEYWORD", AngularJSLanguage.INSTANCE);

  abstract class AngularJSElementType extends IElementType implements ICompositeElementType {

    public AngularJSElementType(@NotNull String debugName) {
      super(debugName, AngularJSLanguage.INSTANCE);
    }
  }
}
