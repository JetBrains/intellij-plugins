package org.angularjs.lang.lexer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSLexer extends MergingLexerAdapter {
  public AngularJSLexer() {
    super(new FlexAdapter(new _AngularJSLexer(null)), TokenSet.create(JSTokenTypes.STRING_LITERAL));
  }
}
