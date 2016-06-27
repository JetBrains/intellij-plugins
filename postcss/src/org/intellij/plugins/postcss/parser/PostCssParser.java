package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.parsing.CssParser2;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssParser extends CssParser2 {
  private boolean rulesetSeen;

  public PostCssParser(PsiBuilder builder) {
    super(builder);
  }

  @Override
  protected CssStyleSheetElementType getStyleSheetElementType() {
    return PostCssElementTypes.POST_CSS_STYLESHEET;
  }

  @Override
  public boolean isSupportNestedBlocks() {
    return true;
  }

  @Override
  protected boolean isSemicolonRequired() {
    return !rulesetSeen && super.isSemicolonRequired();
  }

  @Override
  protected boolean parseSingleDeclarationInBlock(boolean withPageMarginRules, boolean inlineCss,
                                                  boolean requirePropertyValue, @NotNull IElementType elementType) {
    if (!myIsMediaFeature) {
      rulesetSeen = false;
      // Nesting
      if (parseRulesetMedia() /*|| //TODO @nest rules: parseStylesheetItemStartingWithNest()*/ ||
          parseKeyframes() ||
          parseAllSimpleAtRules() ||
          parseImport() ||
          parseSupports() ||
          parsePage() ||
          parseDocument() ||
          parseRegion() ||
          parseScope() ||
          parseCounterStyle() ||
          parseKeyframesRuleset() || tryToParseRuleset()) {
        rulesetSeen = true;
        return true;
      }
    }

    return super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
  }

  private boolean parseRulesetMedia() {
    if (getTokenType() != CssElementTypes.CSS_MEDIA_SYM) {
      return false;
    }
    PsiBuilder.Marker media = createCompositeElement();
    addTokenAndSkipWhitespace();
    parseMediumList();

    expect(CssElementTypes.CSS_LBRACE, "'{'");
    parseDeclarationBlock();
    media.done(CssElementTypes.CSS_MEDIA);
    return true;
  }

  protected boolean isRulesetStart() {
    return super.isRulesetStart()
           || SELECTORS_HIERARCHY_TOKENS.contains(getTokenType())
           || getTokenType() == PostCssTokenTypes.AMPERSAND
           //TODO @nest rules
           ;
  }

  private boolean tryToParseRuleset() {
    if (!isRulesetStart()) {
      return false;
    }
    // Lookahead
    final PsiBuilder.Marker position = myBuilder.mark();
    IElementType prevTokenType = null;
    boolean first = true;
    final String tokenText = getTokenText();
    boolean filterProperty = getTokenType() == CssElementTypes.CSS_IDENT && tokenText != null && tokenText.endsWith("filter");
    while (!isDone() && (first ||
                         getTokenType() == CssElementTypes.CSS_HASH ||
                         getTokenType() == CssElementTypes.CSS_IDENT ||
                         getTokenType() == CssElementTypes.CSS_PERIOD ||
                         getTokenType() == CssElementTypes.CSS_COLON ||
                         getTokenType() == CssElementTypes.CSS_COMMA ||
                         SELECTORS_HIERARCHY_TOKENS.contains(getTokenType()) ||
                         getTokenType() == PostCssTokenTypes.AMPERSAND ||
                         getTokenType() == CssElementTypes.CSS_BAD_CHARACTER ||
                         getTokenType() == CssElementTypes.CSS_RBRACKET ||
                         getTokenType() == CssElementTypes.CSS_EQ ||
                         getTokenType() == CssElementTypes.CSS_ASTERISK ||
                         getTokenType() == CssElementTypes.CSS_FUNCTION_TOKEN ||
                         getTokenType() == CssElementTypes.CSS_CONTAINS ||
                         getTokenType() == CssElementTypes.CSS_STRING_TOKEN)) {

      IElementType rawLookup1 = myBuilder.rawLookup(1);
      if (getTokenType() == CssElementTypes.CSS_COLON) {
        if ((rawLookup1 == CssElementTypes.CSS_FUNCTION_TOKEN ||
             rawLookup1 == CssElementTypes.CSS_IDENT ||
             rawLookup1 == CssElementTypes.CSS_COLON)
            && parsePseudo()) {
          if (first) {
            position.rollbackTo();
            return parseRuleset();
          }
          first = false;
          continue;
        }
        else {
          position.rollbackTo();
          return false;
        }
      }

      if (first && getTokenType() == CssElementTypes.CSS_HASH) {
        position.rollbackTo();
        return parseRuleset();
      }
      first = false;
      if (getTokenType() == CssElementTypes.CSS_GT || getTokenType() == PostCssTokenTypes.AMPERSAND
          || (getTokenType() == CssElementTypes.CSS_PERIOD && !filterProperty)) {
        position.rollbackTo();
        return parseRuleset();
      }
      if (getTokenType() == CssElementTypes.CSS_TILDA && prevTokenType == CssElementTypes.CSS_LPAREN) {
        position.rollbackTo();
        return parseRuleset();
      }
      prevTokenType = getTokenType();
      addSingleToken();
    }

    if ((getTokenType() == CssElementTypes.CSS_LBRACE || getTokenType() == CssElementTypes.CSS_LBRACKET)
        && prevTokenType != CssElementTypes.CSS_COLON) {
      position.rollbackTo();
      return parseRuleset();
    }
    if (prevTokenType == CssElementTypes.CSS_LPAREN && getTokenType() == CssElementTypes.CSS_TILDA) {
      position.rollbackTo();
      return parseRuleset();
    }
    position.rollbackTo();
    return false;
  }

  @Override
  protected boolean parseRuleset() {
    if (!isRulesetStart()) {
      return false;
    }
    PsiBuilder.Marker ruleset = createCompositeElement();
    parseSelectorList();
    if (!parseDeclarationBlock()) {
      expect(CssElementTypes.CSS_LBRACE, "'{");
    }
    ruleset.done(CssElementTypes.CSS_RULESET);
    return true;
  }

  protected void parseSimpleSelector() {
    if (getTokenType() == CssElementTypes.CSS_LPAREN) {
      PsiBuilder.Marker simpleSelector = createCompositeElement();
      addTokenAndSkipWhitespace();
      addRParenOrError();
      simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
    }
    else if (getTokenType() == PostCssTokenTypes.AMPERSAND) {
      PsiBuilder.Marker simpleSelector = createCompositeElement();
      addSingleToken();
      if (!hasWhitespaceBefore()) {
        innerParseSimpleSelector();
      }
      else {
        parseSelectorSuffixList(true);
      }
      simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
    }
    else {
      PsiBuilder.Marker simpleSelector = createCompositeElement();
      innerParseSimpleSelector();
      simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
    }
  }

  private void innerParseSimpleSelector() {
    boolean hasPrefix = false;
    if (isSimpleSelectorStart()) {
      addToken();
      hasPrefix = true;
      addAllAmpersandSelectorTokens();
    }
    if (getTokenType() == CssElementTypes.CSS_PIPE) {
      addSingleToken();
      if (getTokenType() == CssElementTypes.CSS_ASTERISK) {
        addSingleToken();
      }
      else {
        addIdentOrError();
      }
    }
    parseSelectorSuffixList(hasPrefix);
  }

  @Override
  protected boolean isSimpleSelectorStart() {
    return getTokenType() == PostCssTokenTypes.AMPERSAND || super.isSimpleSelectorStart();
  }

  private void addAllAmpersandSelectorTokens() {
    while (!hasWhitespaceBefore() &&
           (getTokenType() == PostCssTokenTypes.AMPERSAND || isIdent() || CssElementTypes.CSS_NUMBER == getTokenType())) {
      if (getTokenType() == PostCssTokenTypes.AMPERSAND) {
        addSingleToken();
      }
      else if (!hasWhitespaceBefore()) {
        if (CssElementTypes.CSS_NUMBER == getTokenType()) {
          addSingleToken();
        }
      }
    }
  }
}
