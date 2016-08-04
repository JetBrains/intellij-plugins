package org.intellij.plugins.postcss.parser;

import com.intellij.lang.LanguageASTFactory;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorFactory2;
import com.intellij.psi.css.impl.util.scheme.CssElementDescriptorProviderImpl;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.intellij.plugins.postcss.descriptors.PostCssElementDescriptorProvider;
import org.intellij.plugins.postcss.psi.impl.PostCssTreeElementFactory;

@TestDataPath("$CONTENT_ROOT/testData/parser/")
public class PostCssParsingTest extends ParsingTestCase {
  public PostCssParsingTest() {
    super("", "pcss", new PostCssParserDefinition(), new CSSParserDefinition());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerExtensionPoint(CssElementDescriptorProvider.EP_NAME, CssElementDescriptorProvider.class);
    registerExtension(CssElementDescriptorProvider.EP_NAME, new CssElementDescriptorProviderImpl());
    registerExtension(CssElementDescriptorProvider.EP_NAME, new PostCssElementDescriptorProvider());

    registerApplicationService(CssElementDescriptorFactory2.class, new CssElementDescriptorFactory2("css-parsing-tests.xml"));

    addExplicitExtension(LanguageASTFactory.INSTANCE, PostCssLanguage.INSTANCE, new PostCssTreeElementFactory());
    addExplicitExtension(LanguageASTFactory.INSTANCE, CSSLanguage.INSTANCE, new CssTreeElementFactory());
  }

  public void testNestingAmpersand() {
    doTest();
  }
  public void testNestingNestRule() {
    doTest();
  }
  public void testSemicolonsCorrect() {
    doTest();
  }
  public void testSemicolonsIncorrect() {
    doTest();
  }
  public void testDirectNestingIncorrect() {
    doTest();
  }
  public void testTopLevelRulesetNesting() {
    doTest();
  }
  public void testNestRulesetInsideAtRule() {
    doTest();
  }
  public void testPartOfNestKeywordTopLevel() {
    doTest();
  }
  public void testPartOfNestKeywordInsideRuleset() {
    doTest();
  }
  public void testPartOfNestKeywordInsideAtRule() {
    doTest();
  }
  public void testPartOfNestKeywordInsidePageAtRule() {
    doTest();
  }
  public void testPartOfNestInsideApplyFunction() {
    doTest();
  }
  public void testNestAtRuleIncorrectSelectorList() {
    doTest();
  }
  public void testCustomSelector() {
    doTest();
  }
  public void testCustomSelectorDefinitionWithoutSemicolon() {
    doTest();
  }
  public void testCustomSelectorDefinitionWithoutColon() {
    doTest();
  }
  public void testCustomSelectorDefinitionWithPseudoClasses() {
    doTest();
  }
  public void testCustomSelectorUsageWithPseudoClasses() {
    doTest();
  }
  public void testCustomSelectorInsideRuleset() {
    doTest();
  }
  public void testCustomSelectorInsideAtRule() {
    doTest();
  }
  public void testCustomSelectorWithWhitespace() {
    doTest();
  }
  public void testCustomSelectorWithoutTwoDashes() {
    doTest();
  }
  public void testAmpersandInSimpleSelector() {
    doTest();
  }
  public void testAmpersandInClass() {
    doTest();
  }
  public void testAmpersandIdSelector() {
    doTest();
  }
  public void testAmpersandInPseudoClasses() {
    doTest();
  }
  public void testAmpersandInPseudoFunction() {
    doTest();
  }
  public void testAmpersandInAttributes() {
    doTest();
  }
  public void testAmpersandWithOperators() {
    doTest();
  }
  public void testDeclarationBlockInMedia() {
    doTest();
  }
  public void testDeclarationBlockInDocument() {
    doTest();
  }
  public void testDeclarationBlockInSupports() {
    doTest();
  }
  public void testDeclarationBlockInRegion() {
    doTest();
  }
  public void testDeclarationBlockInScope() {
    doTest();
  }
  public void testDeclarationBlockInBadAtRule() {
    doTest();
  }
  public void testMediaRangeNameValue() {
    doTest();
  }
  public void testMediaRangeValueName() {
    doTest();
  }
  public void testMediaRangeValueNameValue() {
    doTest();
  }
  public void testMediaPlainFeature() {
    doTest();
  }
  public void testMediaRangeAndOtherFeatures() {
    doTest();
  }
  public void testMediaRangeWithoutUnit() {
    doTest();
  }
  public void testMediaRangeWithMinus() {
    doTest();
  }

  private void doTest() {
    super.doTest(true);
  }

  @Override
  protected String getTestDataPath() {
    return PostCssTestUtils.getFullTestDataPath(getClass());
  }
}
