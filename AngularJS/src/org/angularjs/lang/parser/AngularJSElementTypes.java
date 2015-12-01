package org.angularjs.lang.parser;

import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.AngularJSLanguage;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSElementTypes {
  IElementType REPEAT_EXPRESSION = new IElementType("REPEAT_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType FILTER_EXPRESSION = new IElementType("FILTER_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType AS_EXPRESSION     = new IElementType("AS_EXPRESSION", AngularJSLanguage.INSTANCE);

  IElementType MESSAGE_FORMAT_EXPRESSION_NAME = new IElementType("MESSAGE_FORMAT_EXPRESSION_NAME", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_EXPRESSION = new IElementType("MESSAGE_FORMAT_EXPRESSION", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_MESSAGE = new IElementType("MESSAGE_FORMAT_MESSAGE", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_OPTION = new IElementType("MESSAGE_FORMAT_OPTION", AngularJSLanguage.INSTANCE);
  IElementType MESSAGE_FORMAT_SELECTION_KEYWORD = new IElementType("MESSAGE_FORMAT_SELECTION_KEYWORD", AngularJSLanguage.INSTANCE);
}
