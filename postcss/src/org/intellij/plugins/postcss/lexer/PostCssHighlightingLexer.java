package org.intellij.plugins.postcss.lexer;

import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.CssHighlighterLexer;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PostCssHighlightingLexer extends CssHighlighterLexer {
  private static final int AFTER_AMPERSAND_FLAG = 0x20;
  private static final int BLOCK_LEVEL_MASK = 0xF; //last 4 bits for level
  private int blockNestingDepth = 0;
  private boolean afterAmpersand = false;

  public PostCssHighlightingLexer(@NotNull Set<String> propertyValues) {
    super(new PostCssLexer(), propertyValues);
  }

  @Override
  public void advance() {
    final IElementType type = getTokenType();
    if (type == CssElementTypes.CSS_LBRACE && !myAfterKeyframes) { blockNestingDepth++; }
    if (type == CssElementTypes.CSS_RBRACE && !myInKeyframes && blockNestingDepth > 0) { blockNestingDepth--; }
    afterAmpersand = PostCssTokenTypes.AMPERSAND == type;
    super.advance();
    myInsideBlock = blockNestingDepth > 0;
  }

  @Override
  protected void initState(int initialState) {
    super.initState(initialState);
    int state = initialState >> MY_BASE_STATE_SHIFT;
    afterAmpersand = (state & AFTER_AMPERSAND_FLAG) != 0;
    blockNestingDepth = state & BLOCK_LEVEL_MASK;
  }

  @Override
  public int getState() {
    int state = 0;
    state |= afterAmpersand ? AFTER_AMPERSAND_FLAG : 0;
    state |= blockNestingDepth <= BLOCK_LEVEL_MASK ? blockNestingDepth : BLOCK_LEVEL_MASK;
    return super.getState() | (state << MY_BASE_STATE_SHIFT);
  }

  @Override
  public IElementType getTokenType() {
    final IElementType tokenType = super.getTokenType();
    if((tokenType == CssElementTypes.CSS_IDENT || tokenType == CssElementTypes.CSS_HASH)
            && lookAhead(1) == CssElementTypes.CSS_LPAREN) {
      return CssElementTypes.CSS_FUNCTION_TOKEN;
    }
    return tokenType;
  }

  @Override
  public boolean isPropertyNameAllowed() {
    boolean detachedRuleset = (myAfterLeftBrace || myAfterSemicolon) && myFunctionNestingDepth > 0;
    return !afterAmpersand && (super.isPropertyNameAllowed() || detachedRuleset) && lookAhead(1) == CssElementTypes.CSS_COLON;
  }

  @Override
  public boolean isPropertyValueAllowed() {
    return (myInPropertyValue || myFunctionNestingDepth > 0) && (myInsideBlock || myAfterMediaOrSupports)
           && !myAfterLeftBrace && !myAfterSemicolon;
  }

  @Override
  public boolean isSelectorAllowed() {
    return !(myInPropertyValue || myFunctionNestingDepth > 0) && !myAfterMediaOrSupports && !isTerminatedDeclaration();
  }
}