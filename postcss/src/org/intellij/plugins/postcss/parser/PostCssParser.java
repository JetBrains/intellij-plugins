package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.parsing.CssParser2;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssParser extends CssParser2 {
  private boolean myRulesetSeen;
  private boolean myInsideParentRuleset;
  private int mySimpleSelectorIndex;
  private boolean myInsideAtRuleNest;

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
    return !myRulesetSeen && super.isSemicolonRequired();
  }

  @Override
  protected boolean isSimpleSelectorStart() {
    return getTokenType() == PostCssTokenTypes.AMPERSAND || super.isSimpleSelectorStart();
  }

  private boolean parseDirectNest() {
    if (getTokenType() != PostCssTokenTypes.AMPERSAND) {
      return false;
    }
    PsiBuilder.Marker directNest = createCompositeElement();
    addSingleToken();
    directNest.done(PostCssElementTypes.POST_CSS_DIRECT_NEST);
    return true;
  }

  private boolean parseAtRuleNest() {
    if (getTokenType() != PostCssElementTypes.POST_CSS_NEST_SYM) {
      return false;
    }
    PsiBuilder.Marker atRuleNest = createCompositeElement();
    addSingleToken();
    atRuleNest.done(PostCssElementTypes.POST_CSS_NEST_SYM);
    return true;
  }

  @Override
  protected boolean parseSingleDeclarationInBlock(boolean withPageMarginRules, boolean inlineCss,
                                                  boolean requirePropertyValue, @NotNull IElementType elementType) {
    myRulesetSeen = false;
    // Nesting
    if (parseMedia() ||
        parseKeyframes() ||
        parseAllSimpleAtRules() ||
        parseImport() ||
        parseSupports() ||
        parsePage() ||
        parseDocument() ||
        parseRegion() ||
        parseScope() ||
        parseCounterStyle() ||
        parseKeyframesRuleset() ||
        parseAtRuleNesting() ||
        tryToParseRuleset()) {
      myRulesetSeen = true;
      return true;
    }
    return super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
  }

  private boolean parseAtRuleNesting() {
    if (getTokenType() != PostCssElementTypes.POST_CSS_NEST_SYM) {
      return false;
    }
    return parseNestedRuleset();
  }

  @Override
  protected boolean isRulesetStart() {
    return super.isRulesetStart()
           || SELECTORS_HIERARCHY_TOKENS.contains(getTokenType())
           || isNestSign();
  }

  private boolean isNestSign() {
    return getTokenType() == PostCssTokenTypes.AMPERSAND
           || getTokenType() == PostCssElementTypes.POST_CSS_NEST_SYM;
  }

  private boolean tryToParseRuleset() {
    if (!isRulesetStart()) {
      return false;
    }
    if (isNestSign()) {
      return parseNestedRuleset();
    }
    //// Lookahead
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
            return parseNestedRuleset();
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
        return parseNestedRuleset();
      }
      first = false;
      if (getTokenType() == CssElementTypes.CSS_GT || getTokenType() == PostCssTokenTypes.AMPERSAND
          || (getTokenType() == CssElementTypes.CSS_PERIOD && !filterProperty)) {
        position.rollbackTo();
        return parseNestedRuleset();
      }
      if (getTokenType() == CssElementTypes.CSS_TILDA && prevTokenType == CssElementTypes.CSS_LPAREN) {
        position.rollbackTo();
        return parseNestedRuleset();
      }
      prevTokenType = getTokenType();
      addSingleToken();
    }

    if ((getTokenType() == CssElementTypes.CSS_LBRACE || getTokenType() == CssElementTypes.CSS_LBRACKET)
        && prevTokenType != CssElementTypes.CSS_COLON) {
      position.rollbackTo();
      return parseNestedRuleset();
    }
    if (prevTokenType == CssElementTypes.CSS_LPAREN && getTokenType() == CssElementTypes.CSS_TILDA) {
      position.rollbackTo();
      return parseNestedRuleset();
    }
    position.rollbackTo();
    return false;
  }

  private boolean parseNestedRuleset() {
    myInsideParentRuleset = true;
    boolean rulesetParsed = parseRuleset();
    myInsideParentRuleset = false;
    return rulesetParsed;
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

  @Override
  protected void parseSelectorList() {
    if (getTokenType() == PostCssElementTypes.POST_CSS_NEST_SYM) {
      PsiBuilder.Marker atRuleNest = createCompositeElement();
      parseAtRuleNest();
      myInsideAtRuleNest = true;
      super.parseSelectorList();
      myInsideAtRuleNest = false;
      atRuleNest.done(PostCssElementTypes.POST_CSS_AT_RULE_NEST);
    }
    else {
      super.parseSelectorList();
    }
  }

  @Override
  protected void parseSelector() {
    mySimpleSelectorIndex = 0;
    PsiBuilder.Marker selector = createCompositeElement();
    while (!isDone()) {
      parseSimpleSelector();
      mySimpleSelectorIndex++;
      if (SELECTORS_HIERARCHY_TOKENS.contains(getTokenType())) {
        addTokenAndSkipWhitespace();
      }
      else if (!isRulesetStart()) {
        break;
      }
      if (getTokenType() == CssElementTypes.CSS_LBRACE) {
        break;
      }
    }
    selector.done(CssElementTypes.CSS_SELECTOR);
  }

  @Override
  protected void parseSimpleSelector() {
    if (!myInsideParentRuleset) {
      super.parseSimpleSelector();
    }
    else {
      if (mySimpleSelectorIndex != 0 || myInsideAtRuleNest) {
        super.parseSimpleSelector();
      }
      else {
        if (getTokenType() == PostCssTokenTypes.AMPERSAND) {
          PsiBuilder.Marker simpleSelector = createCompositeElement();
          parseDirectNest();
          parseSelectorSuffixList(true);
          simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
        }
        else {
          addTokenOrError(PostCssTokenTypes.AMPERSAND, "&");
        }
      }
    }
  }

  @Override
  protected boolean parseClass() {
    if (getTokenType() != CssElementTypes.CSS_PERIOD) {
      return false;
    }
    PsiBuilder.Marker cssClass = createCompositeElement();
    addToken();
    addIdentOrError();
    cssClass.done(CssElementTypes.CSS_CLASS);
    return true;
  }
}
