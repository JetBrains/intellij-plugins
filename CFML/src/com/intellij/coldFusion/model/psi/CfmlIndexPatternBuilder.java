package com.intellij.coldFusion.model.psi;

/**
 * User: Nadya.Zabrodina
 * Date: 9/5/11
 */

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.XmlHighlightingLexer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.sql.psi.impl.lexer.SqlLexer;


/**
 */
public class CfmlIndexPatternBuilder implements IndexPatternBuilder {
  public Lexer getIndexingLexer(final PsiFile file) {
    if (file instanceof CfmlFile) {


      LayeredLexer cfmlLayeredLexer = new LayeredLexer(new CfmlLexer(true, file.getProject()));

      cfmlLayeredLexer.registerLayer(new XmlHighlightingLexer(), CfmlElementTypes.TEMPLATE_TEXT);
      cfmlLayeredLexer.registerLayer(new SqlLexer(), CfmlElementTypes.SQL);

      return cfmlLayeredLexer;
    }
    return null;
  }

  public TokenSet getCommentTokenSet(final PsiFile file) {
    if (file instanceof CfmlFile) {
      return CfmlTokenTypes.tsCOMMENTS;
    }
    return null;
  }

  public int getCommentStartDelta(final IElementType tokenType) {
    return 0;
  }

  public int getCommentEndDelta(final IElementType tokenType) {
    return 0;
  }
}