package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.psi.TokenType;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.parsing.CssParser2;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;

public class PostCssParser extends CssParser2 {
  private boolean myRulesetSeen;

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
        tryToParseRuleset()) {
      myRulesetSeen = true;
      return true;
    }
    return super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
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
  protected void parseSimpleSelector() {
    PsiBuilder.Marker simpleSelector = createCompositeElement();
    boolean hasPrefix = false;
    if (isSimpleSelectorStart()) {
      if (isIdentOrAmpersand()) {
        addIdentOrAmpersandOrError();
      }
      else {
        addToken();
      }
      hasPrefix = true;
    }
    if (getTokenType() == CssElementTypes.CSS_PIPE) {
      addToken();
      if (getTokenType() == CssElementTypes.CSS_ASTERISK) {
        addToken();
      }
      else {
        addIdentOrAmpersandOrError();
      }
    }
    parseSelectorSuffixList(hasPrefix);
    simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
  }

  //TODO make CssParser2#parseAttribute protected and remove
  @Override
  protected void parseSelectorSuffixList(boolean hasPrefix) {
    PsiBuilder.Marker selectorSuffixList = createCompositeElement();
    if (hasPrefix && getTokenType() != TokenType.BAD_CHARACTER && hasWhitespaceBefore()) {
      selectorSuffixList.done(CssElementTypes.CSS_SELECTOR_SUFFIX_LIST);
      selectorSuffixList.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null);
      return;
    }
    while (!isDone()) {
      if (parseClass() || parseIdSelector() || parsePseudo()) {
        if (hasWhitespaceBefore()) break;
      }
      else if (!parseAttribute()) {
        if (getTokenType() == TokenType.BAD_CHARACTER) {
          final PsiBuilder.Marker element = createRootErrorElement();
          if (element != null) {
            addToken();
            element.error(CssBundle.message("expected", "valid token"));
          }
        }
        break;
      }
    }
    selectorSuffixList.done(CssElementTypes.CSS_SELECTOR_SUFFIX_LIST);
    selectorSuffixList.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null);
  }

  @Override
  protected boolean parseClass() {
    if (getTokenType() != CssElementTypes.CSS_PERIOD) {
      return false;
    }
    PsiBuilder.Marker cssClass = createCompositeElement();
    addToken();
    if (!hasWhitespaceBefore()) {
      addIdentOrAmpersandOrError();
    }
    cssClass.done(CssElementTypes.CSS_CLASS);
    return true;
  }

  @Override
  protected boolean parseIdSelector() {
    IElementType type = getTokenType();
    if (type != CssElementTypes.CSS_HASH && type != PostCssTokenTypes.HASH_SIGN) {
      return false;
    }
    PsiBuilder.Marker idSelector = createCompositeElement();
    addToken();
    addIdentOrAmpersandSuffix();
    idSelector.done(CssElementTypes.CSS_ID_SELECTOR);
    return true;
  }

  @Override
  protected boolean parsePseudo() {
    if (getTokenType() != CssElementTypes.CSS_COLON) {
      return false;
    }

    PsiBuilder.Marker pseudo = createCompositeElement();
    addToken();
    if (!hasWhitespaceBefore() && getTokenType() == CssElementTypes.CSS_COLON) {
      addToken();
    }

    IElementType tokenType = getTokenType();
    String tokenText = getTokenText();

    IElementType type = suggestPseudoType(tokenText);
    if (hasWhitespaceBefore()) {
      createErrorElement(CssBundle.message("expected", CssBundle.message("an.identifier")));
      pseudo.done(type);
      return true;
    }

    if (isIdentOrAmpersand(tokenType)) {
      PsiBuilder.Marker possibleFunction = createCompositeElement();
      addIdentOrAmpersandOrError(CssElementTypes.CSS_FUNCTION_TOKEN);
      if (getTokenType() == CssElementTypes.CSS_LPAREN) {
        addTokenAndSkipWhitespace();
        parsePseudoTermList();
        addRParenOrError();
        possibleFunction.done(CssElementTypes.CSS_FUNCTION);
      }
      else {
        possibleFunction.drop();
      }
    }
    else if (tokenType == CssElementTypes.CSS_FUNCTION_TOKEN) {
      parsePseudoFunction();
    }
    else {
      createErrorElement(CssBundle.message("expected", CssBundle.message("an.identifier")));
    }
    pseudo.done(type);
    return true;
  }

  //TODO make CssParser2#parsePseudoTermList protected and remove
  private void parsePseudoTermList() {
    PsiBuilder.Marker termList = createCompositeElement();
    boolean taken = parsePseudoTerm();
    if (!taken) {
      createTermExpectedErrorElement();
    }
    while (!isDone()) {
      IElementType type = getTokenType();
      if (type == CssElementTypes.CSS_COMMA) {
        addTokenAndSkipWhitespace();
      }
      if (!parsePseudoTerm()) {
        type = getTokenType();
        if (isDone() ||
            type == CssElementTypes.CSS_SEMICOLON ||
            type == CssElementTypes.CSS_RBRACE ||
            type == CssElementTypes.CSS_IMPORTANT ||
            type == CssElementTypes.CSS_RPAREN) {
          break;
        }
        else {
          createTermExpectedErrorElement(); // ???
          addToken();
        }
      }
    }

    termList.done(CssElementTypes.CSS_TERM_LIST);
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
      if (isIdentOrAmpersand()) {
        addIdentOrAmpersandOrError();
      }
      else if (isSimpleSelectorStart()) {
        addToken();
      }
      else if (tokenType == CssElementTypes.CSS_HASH || tokenType == PostCssTokenTypes.HASH_SIGN) {
        addTokenAndSkipWhitespace();
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

  private boolean parseAttributeLSide() {
    if(!isIdentOrAmpersand() && getTokenType() != CssElementTypes.CSS_PIPE && getTokenType() != CssElementTypes.CSS_ASTERISK) {
      return false;
    }
    if(isIdentOrAmpersand()) {
      addIdentOrAmpersandOrError();
    } else if (getTokenType() == CssElementTypes.CSS_ASTERISK) {
      if(myBuilder.lookAhead(1) != CssElementTypes.CSS_PIPE) {
        final PsiBuilder.Marker error = myBuilder.mark();
        addToken();
        error.error("unexpected asterisk");
      } else {
        addToken();
      }
    }
    if(getTokenType() == CssElementTypes.CSS_PIPE) {
      addToken();
      addIdentOrAmpersandOrError();
    }
    return true;
  }

  @Override
  protected void parseAttributeRSide() {
    if (!CssElementTypes.ATTRIBUTE_OPERATORS.contains(getTokenType())) {
      return;
    }
    addTokenAndSkipWhitespace();
    PsiBuilder.Marker attributeRSide = createCompositeElement();
    if (!parseCssString()) {
      if (isIdentOrAmpersand()) {
        addIdentOrAmpersandOrError();
      }
      else {
        createErrorElement(CssBundle.message("expected.a.string.or.an.identifier"));
      }
    }
    attributeRSide.done(CssElementTypes.CSS_ATTRIBUTE_RSIDE);
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

  private boolean addIdentOrAmpersandOrError(IElementType... additionalTypesToAdd) {
    if (isIdentOrAmpersand()) {
      addSingleToken();
      addIdentOrAmpersandSuffix(additionalTypesToAdd);
      return true;
    }
    else {
      return super.addIdentOrError();
    }
  }

  private void addIdentOrAmpersandSuffix(IElementType... additionalTypesToAdd) {
    while (!hasWhitespaceBefore() && !isDone()) {
      IElementType type = getTokenType();
      if (type == PostCssTokenTypes.AMPERSAND || type == CssElementTypes.CSS_NUMBER || type == CssElementTypes.CSS_IDENT ||
          ArrayUtil.contains(type, additionalTypesToAdd)) {
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

  private boolean isIdentOrAmpersand(IElementType type) {
    return isIdent(type) || type == PostCssTokenTypes.AMPERSAND;
  }
}
