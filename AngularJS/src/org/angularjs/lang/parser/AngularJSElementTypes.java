package org.angularjs.lang.parser;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.angularjs.lang.AngularJSLanguage;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSElementTypes {
  IFileElementType FILE = JSFileElementType.create(AngularJSLanguage.INSTANCE);
  IElementType REPEAT_EXPRESSION = new IElementType("REPEAT_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType FOR_EXPRESSION = new IElementType("REPEAT_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType FILTER_EXPRESSION = new IElementType("FILTER_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType AS_EXPRESSION     = new IElementType("AS_EXPRESSION", AngularJSLanguage.INSTANCE);

  IElementType MESSAGE_FORMAT_EXPRESSION_NAME = new IElementType("MESSAGE_FORMAT_EXPRESSION_NAME", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_EXPRESSION = new IElementType("MESSAGE_FORMAT_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_MESSAGE = new IElementType("MESSAGE_FORMAT_MESSAGE", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_OPTION = new IElementType("MESSAGE_FORMAT_OPTION", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_SELECTION_KEYWORD = new IElementType("MESSAGE_FORMAT_SELECTION_KEYWORD", AngularJSLanguage.INSTANCE);

  IElementType EMBEDDED_CONTENT = new JSEmbeddedContentElementType(AngularJSLanguage.INSTANCE, "ANG_") {
    @Override
    protected Lexer createStripperLexer(Language baseLanguage) {
      return null;
    }
  };
}
