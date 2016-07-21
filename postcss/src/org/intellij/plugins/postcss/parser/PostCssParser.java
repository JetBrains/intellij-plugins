package org.intellij.plugins.postcss.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.parsing.CssParser2;
import com.intellij.psi.css.impl.util.CssStyleSheetElementType;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssBundle;
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

  private boolean parseDirectNest() {
    if (getTokenType() != PostCssTokenTypes.AMPERSAND) {
      return false;
    }
    addSingleToken();
    return true;
  }

  private boolean parseAtRuleNest() {
    if (getTokenType() != PostCssTokenTypes.POST_CSS_NEST_SYM) {
      return false;
    }
    addSingleToken();
    return true;
  }

  @Override
  protected boolean parseStylesheetItem() {
    return super.parseStylesheetItem() || parseCustomSelectorAtRule();
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
    parseCustomSelector(true);
    parseSelectorList();
    addSemicolonOrError();
    customSelectorRule.done(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR_RULE);
    return true;
  }

  private boolean parseCustomSelector(boolean strict) {
    if (strict){
      PsiBuilder.Marker customSelectorName = createCompositeElement();
      boolean hasColon = addTokenOrError(CssElementTypes.CSS_COLON, "':'");
      if (hasColon && hasWhitespaceBefore()){
        createErrorElement(PostCssBundle.message("no.whitespaces.between.colon.and.extension.name.allowed"));
      }
      addIdentOrError();
      customSelectorName.done(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR);
      return true;
    } else {
      if (getTokenType() != CssElementTypes.CSS_COLON || rawLookup(1) != CssElementTypes.CSS_IDENT) return false;
      PsiBuilder.Marker customSelectorName = createCompositeElement();
      addSingleToken();
      if (getTokenText() == null || !StringUtil.startsWith(getTokenText(), "--")) {
        customSelectorName.rollbackTo();
        return false;
      }
      addSingleToken();
      customSelectorName.drop();
      return true;
    }
  }

  private boolean parseAtRuleNesting() {
    if (getTokenType() != PostCssTokenTypes.POST_CSS_NEST_SYM) {
      return false;
    }
    PsiBuilder.Marker nest = createCompositeElement();
    parseAtRuleNest();
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
           || isNestingSelector();
  }

  private boolean isNestingSelector() {
    return getTokenType() == PostCssTokenTypes.AMPERSAND
           || getTokenType() == PostCssTokenTypes.POST_CSS_NEST_SYM;
  }

  private boolean tryToParseRuleset() {
    if (!isRulesetStart()) {
      return false;
    }
    if (isNestingSelector()) {
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

  @Override
  protected void parseSimpleSelector() {
    if (getTokenType() == PostCssTokenTypes.AMPERSAND) {
      PsiBuilder.Marker simpleSelector = createCompositeElement();
      parseDirectNest();
      parseSelectorSuffixList(true);
      simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
    }
    // @nest could be selector if this is top level ruleset
    // it will be annotated as error by inspector or annotator
    else if (getTokenType() == PostCssTokenTypes.POST_CSS_NEST_SYM) {
      PsiBuilder.Marker simpleSelector = createCompositeElement();
      parseAtRuleNest();
      simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
    }
    else {
      PsiBuilder.Marker simpleSelector = createCompositeElement();
      if (parseCustomSelector(false)){
        parseSelectorSuffixList(true);
        simpleSelector.done(CssElementTypes.CSS_SIMPLE_SELECTOR);
      } else {
        simpleSelector.drop();
        super.parseSimpleSelector();
      }
    }
  }

  @Override
  protected boolean parsePseudo() {
    PsiBuilder.Marker pseudoClass = createCompositeElement();
    if (parseCustomSelector(false)){
      pseudoClass.done(PostCssElementTypes.POST_CSS_CUSTOM_SELECTOR);
      return true;
    } else {
      pseudoClass.drop();
      return super.parsePseudo();
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
