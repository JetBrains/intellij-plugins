package org.angularjs.lang.lexer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.psi.tree.IElementType;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSTokenTypes extends JSTokenTypes {
  AngularJSTokenType ESCAPE_SEQUENCE = new AngularJSTokenType("ESCAPE_SEQUENCE");
  AngularJSTokenType INVALID_ESCAPE_SEQUENCE = new AngularJSTokenType("INVALID_ESCAPE_SEQUENCE");
}
