package org.intellij.plugins.postcss.lexer;

import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.CssHighlighterLexer;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PostCssHighlightingLexer extends CssHighlighterLexer {
  private static final int AFTER_AMPERSAND_FLAG = 0x20;
  private static final int AFTER_NUMBER_FLAG = 0x40;
  private static final int BLOCK_LEVEL_MASK = 0xF; //last 4 bits for level
  private int blockNestingDepth = 0;
  private boolean afterAmpersand = false;
  private boolean afterNumber = false;

  public PostCssHighlightingLexer(@NotNull Set<String> propertyValues) {
    super(new PostCssLexer(), propertyValues);
  }

  @Override
  public void advance() {
    final IElementType type = getTokenType();
    if (type == CssElementTypes.CSS_LBRACE && !myAfterKeyframes) { blockNestingDepth++; }
    if (type == CssElementTypes.CSS_RBRACE && !myInKeyframes && blockNestingDepth > 0) { blockNestingDepth--; }
    if (type == PostCssTokenTypes.POST_CSS_CUSTOM_MEDIA_SYM) {
      myAfterMediaOrSupports = true;
      //little hack for @media screen proper highlighting, we deny property names for 'screen'
      myInPropertyValue = true;
      myAfterColon = false;
    }
    afterAmpersand = PostCssTokenTypes.AMPERSAND == type;
    afterNumber = CssElementTypes.CSS_NUMBER == type;
    super.advance();
    myInsideBlock = blockNestingDepth > 0;
  }

  @Override
  protected void initState(int initialState) {
    super.initState(initialState);
    int state = initialState >> MY_BASE_STATE_SHIFT;
    afterAmpersand = (state & AFTER_AMPERSAND_FLAG) != 0;
    afterNumber = (state & AFTER_NUMBER_FLAG) != 0;
    blockNestingDepth = state & BLOCK_LEVEL_MASK;
  }

  @Override
  public int getState() {
    int state = 0;
    state |= afterAmpersand ? AFTER_AMPERSAND_FLAG : 0;
    state |= afterAmpersand ? AFTER_NUMBER_FLAG : 0;
    state |= Math.min(blockNestingDepth, BLOCK_LEVEL_MASK);
    return super.getState() | (state << MY_BASE_STATE_SHIFT);
  }

  @Override
  public IElementType getTokenType() {
    final IElementType tokenType = super.getTokenType();
    if (!myAfterMediaOrSupports &&
        (tokenType == CssElementTypes.CSS_IDENT || tokenType == CssElementTypes.CSS_HASH) && lookAhead(1) == CssElementTypes.CSS_LPAREN) {
      return CssElementTypes.CSS_FUNCTION_TOKEN;
    }
    return tokenType;
  }

  @Override
  public boolean isPropertyNameAllowed() {
    boolean detachedRuleset = (myAfterLeftBrace || myAfterSemicolon) && myFunctionNestingDepth > 0;
    IElementType next = lookAhead(1);
    boolean isRightContext = next == CssElementTypes.CSS_COLON;
    if (myAfterMediaOrSupports) {
      isRightContext |= !afterNumber && (PostCssTokenTypes.COMPARISON_OPERATORS.contains(next) || next == CssElementTypes.CSS_RPAREN);
    }
    return !afterAmpersand && isRightContext && (super.isPropertyNameAllowed() || detachedRuleset);
  }

  @Override
  public boolean isPropertyValueAllowed() {
    if (myAfterMediaOrSupports && afterNumber) return true;
    return (myInPropertyValue || myFunctionNestingDepth > 0) && (myInsideBlock || myAfterMediaOrSupports)
           && !myAfterLeftBrace && !myAfterSemicolon;
  }

  @Override
  public boolean isSelectorAllowed() {
    return !(myInPropertyValue || myFunctionNestingDepth > 0) && !myAfterMediaOrSupports && !isTerminatedDeclaration();
  }
}