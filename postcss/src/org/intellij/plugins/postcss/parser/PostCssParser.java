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
  private boolean myAmpersandAllowed;
  private IElementType myAdditionalIdent;

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

  @Override
  protected boolean parseStylesheetItem() {
    return parseCustomSelectorAtRule() || parseAtRuleNesting() || super.parseStylesheetItem();
  }

  @Override
  protected boolean parseSingleDeclarationInBlock(boolean withPageMarginRules, boolean inlineCss,
                                                  boolean requirePropertyValue, @NotNull IElementType elementType) {
    if (withPageMarginRules && getTokenType() == CssElementTypes.CSS_ATKEYWORD) {
      // to parse @page with error elements
      return super.parseSingleDeclarationInBlock(true, inlineCss, requirePropertyValue, elementType);
    }
    if (elementType == CssElementTypes.CSS_MEDIA_FEATURE && !parseMediaFeatureRange()) {
      // parse plain media feature
      return super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
    }
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
        parseCustomSelectorAtRule() ||
        parseAtRuleNesting() ||
        parseBadAtRule(false) ||
        tryToParseRuleset()) {
      myRulesetSeen = true;
      return true;
    }
    return super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
  }

  private boolean parseMediaFeatureRange() {
    PsiBuilder.Marker mediaFeature = createCompositeElement();
    if (!parseMediaFeatureRangeInner()) {
      mediaFeature.rollbackTo();
      return false;
    }
    mediaFeature.done(CssElementTypes.CSS_MEDIA_FEATURE);
    return true;
  }

  private boolean parseMediaFeatureRangeInner() {
    boolean startsWithValue = parseNumberTerm();
    if (!startsWithValue && !addIdentOrError()) return false;
    if (!parseOperatorSign()) return false;
    if (!startsWithValue && parseNumberTerm()) return true;
    if (!addIdentOrError()) return false;
    if (getTokenType() == CssElementTypes.CSS_RPAREN) return true;
    return parseOperatorSign() && parseNumberTerm();
  }

  private boolean parseOperatorSign() {
    if (!PostCssTokenTypes.OPERATORS.contains(getTokenType())) return false;
    addSingleToken();
    return true;
  }

  private boolean parseNumberTerm() {
    if (getTokenType() == CssElementTypes.CSS_MINUS) addSingleToken();
    if (getTokenType() != CssElementTypes.CSS_NUMBER) return false;
    PsiBuilder.Marker numberTerm = createCompositeElement();
    addSingleToken();
    addIdentOrError();
    numberTerm.done(CssElementTypes.CSS_NUMBER_TERM);
    return true;
  }

  @Override
  protected void parseNestedRulesetList() {
    parseDeclarationBlock();
  }

  private boolean parseCustomSelectorAtRule() {
    if (getTokenType() != PostCssTokenTypes.POST_CSS_CUSTOM_SELECTOR_SYM) {
      return false;
    }
    PsiBuilder.Marker customSelectorRule = createCompositeElement();
    addSingleToken();
    parseCustomSelector();
    parseSelectorList();
    addSemicolonOrError();
    customSelectorRule.done(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR_RULE);
    return true;
  }

  private boolean parseCustomSelector() {
    PsiBuilder.Marker customSelectorName = createCompositeElement();
    if (getTokenType() == CssElementTypes.CSS_COLON) {
      addSingleToken();
    }
    if (getTokenType() == CssElementTypes.CSS_IDENT) {
      addSingleToken();
    }
    customSelectorName.done(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR);
    return true;
  }

  private boolean parseAtRuleNesting() {
    if (getTokenType() != PostCssTokenTypes.POST_CSS_NEST_SYM) {
      return false;
    }
    PsiBuilder.Marker nest = createCompositeElement();
    addSingleToken();
    parseSelectorList();

    if (!parseDeclarationBlock()) {
      expect(CssElementTypes.CSS_LBRACE, "'{");
    }
    nest.done(PostCssElementTypes.POST_CSS_NEST);
    return true;
  }

  @Override
  protected boolean isRulesetStart() {
    return super.isRulesetStart()
           || SELECTORS_HIERARCHY_TOKENS.contains(getTokenType())
           || getTokenType() == PostCssTokenTypes.HASH_SIGN;
  }

  private boolean tryToParseRuleset() {
    if (!isRulesetStart()) {
      return false;
    }
    if (getTokenType() == PostCssTokenTypes.AMPERSAND) {
      return parseRuleset();
    }
    //// Lookahead
    final PsiBuilder.Marker position = myBuilder.mark();
    IElementType prevTokenType = null;
    boolean first = true;
    final String tokenText = getTokenText();
    boolean filterProperty = getTokenType() == CssElementTypes.CSS_IDENT && tokenText != null && tokenText.endsWith("filter");
    while (!isDone() && (first ||
                         getTokenType() == CssElementTypes.CSS_HASH ||
                         getTokenType() == PostCssTokenTypes.HASH_SIGN ||
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
             rawLookup1 == PostCssTokenTypes.AMPERSAND ||
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

  @Override
  protected void parseSelectorList() {
    myAmpersandAllowed = true;
    super.parseSelectorList();
    myAmpersandAllowed = false;
  }

  @Override
  public void addToken() {
    if (myAmpersandAllowed && isIdentOrAmpersand()) {
      addIdentOrAmpersandOrError();
    }
    super.addToken();
  }

  @Override
  public boolean addIdentOrError() {
    return myAmpersandAllowed ? addIdentOrAmpersandOrError() : super.addIdentOrError();
  }

  @Override
  public boolean isIdent(IElementType type) {
    return myAmpersandAllowed ? isIdentOrAmpersand(type) : super.isIdent(type);
  }

  private boolean addIdentOrAmpersandOrError() {
    if (!isIdentOrAmpersand()) return super.addIdentOrError();
    addSingleToken();
    addIdentOrAmpersandSuffix();
    return true;
  }

  private void addIdentOrAmpersandSuffix() {
    while (!hasWhitespaceBefore() && !isDone()) {
      IElementType type = getTokenType();
      boolean isAdditionalType = type == myAdditionalIdent;
      if (type == PostCssTokenTypes.AMPERSAND || type == CssElementTypes.CSS_NUMBER ||
          type == CssElementTypes.CSS_IDENT || isAdditionalType) {
        if (isAdditionalType) myAdditionalIdent = null;
        addSingleToken();
      }
      else {
        return;
      }
    }
  }

  private boolean isIdentOrAmpersand() {
    return isIdentOrAmpersand(getTokenType());
  }

  private static boolean isIdentOrAmpersand(IElementType type) {
    return type == CssElementTypes.CSS_IDENT || type == PostCssTokenTypes.AMPERSAND;
  }

  @Override
  protected boolean parseIdSelector() {
    IElementType type = getTokenType();
    if (type != CssElementTypes.CSS_HASH && type != PostCssTokenTypes.HASH_SIGN) {
      return false;
    }
    PsiBuilder.Marker idSelector = createCompositeElement();
    addSingleToken();
    addIdentOrAmpersandSuffix();
    idSelector.done(CssElementTypes.CSS_ID_SELECTOR);
    return true;
  }

  @Override
  protected boolean parsePseudo() {
    myAdditionalIdent = CssElementTypes.CSS_FUNCTION_TOKEN;
    boolean result = super.parsePseudo();
    myAdditionalIdent = null;
    return result;
  }

  @Override
  protected boolean parsePseudoTerm() {
    IElementType tokenType = getTokenType();
    if (!isIdent(tokenType) &&
        tokenType != CssElementTypes.CSS_HASH &&
        tokenType != CssElementTypes.CSS_PERIOD &&
        tokenType != CssElementTypes.CSS_COLON &&
        tokenType != CssElementTypes.CSS_LBRACKET &&
        tokenType != CssElementTypes.CSS_NUMBER &&
        tokenType != CssElementTypes.CSS_PLUS &&
        tokenType != CssElementTypes.CSS_ASTERISK &&
        tokenType != CssElementTypes.CSS_STRING_TOKEN &&
        tokenType != CssElementTypes.CSS_MINUS &&
        tokenType != PostCssTokenTypes.AMPERSAND &&
        tokenType != PostCssTokenTypes.HASH_SIGN) {
      return false;
    }
    PsiBuilder.Marker term = createCompositeElement();
    tokenType = getTokenType();
    if (!parsePseudoExpression()) {
      if (isIdent()) {
        addIdentOrError();
      }
      else if (isSimpleSelectorStart()) {
        addToken();
      }
      else if (tokenType == CssElementTypes.CSS_HASH || tokenType == PostCssTokenTypes.HASH_SIGN) {
        addSingleToken();
        addIdentOrAmpersandSuffix();
        parseAttribute();
      }
      else if (tokenType == CssElementTypes.CSS_STRING_TOKEN) {
        parseCssString();
      }
      else if (!parseAttribute() && !parsePseudo() && !parseClass()) {
        if (tokenType != null) {
          createErrorElement(CssBundle.message("unexpected.token"));
          addToken();
        }
      }
    }
    term.done(CssElementTypes.CSS_TERM);
    return true;
  }

  //TODO make CssParser2#parseAttribute protected and remove
  private boolean parseAttribute() {
    if (getTokenType() != CssElementTypes.CSS_LBRACKET) {
      return false;
    }
    PsiBuilder.Marker attribute = createCompositeElement();
    addTokenAndSkipWhitespace();
    parseAttributeLSide();
    parseAttributeRSide();
    addTokenOrError(CssElementTypes.CSS_RBRACKET, "']'");
    attribute.done(CssElementTypes.CSS_ATTRIBUTE);
    return true;
  }

  //TODO make CssParser2#parseAttributeLSide protected and remove
  private boolean parseAttributeLSide() {
    if (!isIdent() && getTokenType() != CssElementTypes.CSS_PIPE && getTokenType() != CssElementTypes.CSS_ASTERISK) {
      return false;
    }
    if (isIdent()) {
      addIdentOrError();
    }
    else if (getTokenType() == CssElementTypes.CSS_ASTERISK) {
      if (myBuilder.lookAhead(1) != CssElementTypes.CSS_PIPE) {
        final PsiBuilder.Marker error = myBuilder.mark();
        addToken();
        error.error("unexpected asterisk");
      }
      else {
        addToken();
      }
    }
    if (getTokenType() == CssElementTypes.CSS_PIPE) {
      addToken();
      addIdentOrError();
    }
    return true;
  }

  //TODO make CssParser2#parsePseudoExpression protected and remove
  private boolean parsePseudoExpression() {
    if (!isIdent() &&
        CssElementTypes.CSS_NUMBER != getTokenType() &&
        CssElementTypes.CSS_PLUS != getTokenType() &&
        CssElementTypes.CSS_MINUS != getTokenType()) {
      return false;
    }
    if (isIdent() && !"n".equalsIgnoreCase(getTokenText())) return false;

    PsiBuilder.Marker expression = createCompositeElement();
    boolean op = false;
    while (!isDone()) {
      final IElementType tokenType = getTokenType();
      if (/*WHITE_SPACE != tokenType &&*/ CssElementTypes.CSS_MINUS != tokenType &&
                                          CssElementTypes.CSS_PLUS != tokenType &&
                                          CssElementTypes.CSS_NUMBER != tokenType &&
                                          !isIdent(tokenType)) {
        expression.done(CssElementTypes.CSS_EXPRESSION);
        return CssElementTypes.CSS_RPAREN == tokenType;
      }

      if (CssElementTypes.CSS_MINUS == tokenType || CssElementTypes.CSS_PLUS == tokenType) {
        addTokenAndSkipWhitespace();
        if (CssElementTypes.CSS_NUMBER == getTokenType()) {
          addTokenAndSkipWhitespace();
        }
        if (isIdent()) {
          addIdentOrError();
        }
        op = true;
      }
      else if (CssElementTypes.CSS_NUMBER == tokenType) {
        if (op) createErrorElement("'+' or '-' expected");
        addTokenAndSkipWhitespace();
        if (isIdent()) {
          addIdentOrError();
        }
        op = true;
      }
      else if (isIdent()) {
        if (op) createErrorElement("'+' or '-' expected");
        addIdentOrError();
      }
    }
    expression.done(CssElementTypes.CSS_EXPRESSION);
    return false;
  }
}
