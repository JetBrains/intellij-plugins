package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.parsing.CssParser2;
import com.intellij.psi.css.impl.stubs.CssStylesheetStubElementType;
import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ArrayUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssParser extends CssParser2 {
  private boolean myRulesetSeen;
  private boolean myAmpersandAllowed;
  private IElementType myAdditionalIdent;

  @Override
  protected CssStylesheetLazyElementType getStylesheetLazyElementType() {
    return PostCssElementTypes.POST_CSS_LAZY_STYLESHEET;
  }

  @Override
  protected CssStylesheetStubElementType getStylesheetElementType() {
    return PostCssElementTypes.POST_CSS_STYLESHEET;
  }

  @Override
  public boolean supportsNestedBlocks() {
    return true;
  }

  @Override
  protected TokenSet getCommentTokenTypes() {
    return PostCssTokenTypes.POST_CSS_COMMENTS;
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
    return parseCustomSelectorAtRule() || parseCustomMediaAtRule() || parseAtRuleNesting()
           || super.parseStylesheetItem();
  }

  @Override
  protected boolean parseSingleDeclarationInBlock(boolean withPageMarginRules, boolean inlineCss,
                                                  boolean requirePropertyValue, @NotNull IElementType elementType) {
    if (withPageMarginRules && getTokenType() == CssElementTypes.CSS_ATKEYWORD) {
      // to parse @page with error elements
      return super.parseSingleDeclarationInBlock(true, inlineCss, requirePropertyValue, elementType);
    }
    if (elementType == CssElementTypes.CSS_MEDIA_FEATURE) {
      return parseMediaFeatureRange() ||
             super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
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
        parseCustomMediaAtRule() ||
        parseAtRuleNesting() ||
        parseBadAtRule(false) ||
        tryToParseRuleset()) {
      myRulesetSeen = true;
      return true;
    }
    return super.parseSingleDeclarationInBlock(withPageMarginRules, inlineCss, requirePropertyValue, elementType);
  }

  private boolean parseCustomMediaAtRule() {
    if (getTokenType() != PostCssTokenTypes.POST_CSS_CUSTOM_MEDIA_SYM) {
      return false;
    }
    PsiBuilder.Marker customMediaAtRule = createCompositeElement();
    addSingleToken();
    if (isIdent()) {
      PsiBuilder.Marker customMedia = createCompositeElement();
      addSingleToken();
      customMedia.done(PostCssElementTypes.POST_CSS_CUSTOM_MEDIA);
    }
    else {
      createErrorElement(CssBundle.message("expected", CssBundle.message("an.identifier")));
    }
    parseMediumList();
    if (getTokenType() != CssElementTypes.CSS_RBRACE) {
      addSemicolonOrError();
    }
    customMediaAtRule.done(PostCssElementTypes.POST_CSS_CUSTOM_MEDIA_RULE);
    return true;
  }

  private boolean parseMediaFeatureRange() {
    boolean startsWithValue = isNumberTermStart();
    if (!startsWithValue && !(isIdent() && PostCssTokenTypes.COMPARISON_OPERATORS.contains(lookAhead(1)))) {
      return false;
    }
    PsiBuilder.Marker mediaFeature = createCompositeElement();
    if (startsWithValue) {
      parseNumberTerm();
      parseComparisonOperator();
      addIdentOrError();
      if (getTokenType() == CssElementTypes.CSS_RPAREN) {
        mediaFeature.done(CssElementTypes.CSS_MEDIA_FEATURE);
        return true;
      }
      parseComparisonOperator();
      parseNumberTerm();
    }
    else {
      addIdentOrError();
      parseComparisonOperator();
      parseNumberTerm();
    }
    mediaFeature.done(CssElementTypes.CSS_MEDIA_FEATURE);
    return true;
  }

  private void parseComparisonOperator() {
    if (PostCssTokenTypes.COMPARISON_OPERATORS.contains(getTokenType())) {
      addSingleToken();
    }
    else {
      createErrorElement(CssBundle.message("expected", PostCssBundle.message("postcss.operator.sign")));
    }
  }

  private boolean isNumberTermStart() {
    return getTokenType() == CssElementTypes.CSS_NUMBER ||
           getTokenType() == CssElementTypes.CSS_MINUS && lookAhead(1) == CssElementTypes.CSS_NUMBER;
  }

  private void parseNumberTerm() {
    if (!isNumberTermStart()) {
      createErrorElement(CssBundle.message("a.term.expected"));
      return;
    }
    PsiBuilder.Marker numberTerm = createCompositeElement();
    if (getTokenType() == CssElementTypes.CSS_MINUS) addSingleToken();
    addSingleToken();
    if (isIdent()) addSingleToken();
    numberTerm.done(CssElementTypes.CSS_NUMBER_TERM);
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
    if (getTokenType() != CssElementTypes.CSS_RBRACE) {
      addSemicolonOrError();
    }
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
    return super.isRulesetStart() || getTokenType() == PostCssTokenTypes.HASH_SIGN;
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
    return addIdentOrAmpersandOrError(null);
  }

  private boolean addIdentOrAmpersandOrError(@Nullable final IElementType toCollapse, final IElementType... validTypes) {
    if (!isIdentOrAmpersand() && !ArrayUtil.contains(getTokenType(), validTypes)) return super.addIdentOrError();
    PsiBuilder.Marker ident = createCompositeElement();
    addSingleToken();
    boolean lastIsAdditionalType = false;
    while (!hasWhitespaceBefore() && !isDone()) {
      IElementType type = getTokenType();
      if (isIdentOrAmpersand(type) || type == CssElementTypes.CSS_NUMBER || toCollapse == null && type == myAdditionalIdent) {
        addSingleToken();
        lastIsAdditionalType = type == myAdditionalIdent;
      }
      else {
        break;
      }
    }
    ident.collapse(toCollapse != null ? toCollapse :
                   (myAdditionalIdent != null && lastIsAdditionalType) ? myAdditionalIdent : CssElementTypes.CSS_IDENT);
    myAdditionalIdent = null;
    return true;
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
    addIdentOrAmpersandOrError(CssElementTypes.CSS_HASH, CssElementTypes.CSS_HASH, PostCssTokenTypes.HASH_SIGN);
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
        addIdentOrAmpersandOrError(CssElementTypes.CSS_HASH, CssElementTypes.CSS_HASH, PostCssTokenTypes.HASH_SIGN);
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
}
