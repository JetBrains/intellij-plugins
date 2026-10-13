// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.parser;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.lang.dtd.DTDParserDefinition;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.jade.JadeParserDefinition;
import com.jetbrains.plugins.jade.JadeTestUtil;
import com.jetbrains.plugins.jade.js.JavascriptInJadeParserDefinition;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import org.coffeescript.lang.parser.CoffeeScriptParserDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
public class JadeParsingTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, JadeTestUtil.getBaseTestDataPath() + "/parser", "jade", true,
                   new JadeParserDefinition(), new JavascriptInJadeParserDefinition(), new JavascriptParserDefinition(),
                   new XMLParserDefinition(), new DTDParserDefinition(), new CSSParserDefinition(), new CoffeeScriptParserDefinition());

  @BeforeEach
  void setUp() {
    // todo please fix JadeAstFactory.createComposite and remove this setting
    //
    // The problem is that Jade PSI hierarchy does not pass the proper IElementTypes to its super-calls in constructors.
    // So quite often, a JadePsiElement.getElementType() returns not a Jade element type but an element type of its base xml psi element.
    // E.g., XmlElementType.XML_ATTRIBUTE instead of JadeElementTypes.ATTRIBUTE.
    //
    // Because of that, when DiffTree tries to match new AST with the old one, it miserably fails.
    //
    // To fix this, you need to go through JadAstFactory#createComposite and ensure that every created jade psi element is created
    // with the corresponding element type.
    parsingFixture.get().setCheckNoPsiEventsOnReparse(false);

    CodeStyleSettings settings = CodeStyle.createTestSettings();
    CodeStyleSettingsManager.getInstance(projectFixture.get()).setTemporarySettings(settings);
  }

  @AfterEach
  void tearDown() {
    CodeStyleSettingsManager.getInstance(projectFixture.get()).dropTemporarySettings();
  }

  private void defaultTest() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testEmpty() {
    defaultTest();
  }

  @Test
  public void testSimple() {
    defaultTest();
  }

  @Test
  public void testAttributes1() {
    defaultTest();
  }

  @Test
  public void testAttributes2() {
    defaultTest();
  }

  @Test
  public void testAttributes3() {
    defaultTest();
  }

  @Test
  public void testAttributes4() {
    defaultTest();
  }

  @Test
  public void testAttributes5() {
    defaultTest();
  }

  @Test
  public void testAttributeNames() {
    defaultTest();
  }

  @Test
  public void testAttributesJs() {
    defaultTest();
  }

  @Test
  public void testAttributesEs6Strings() {
    defaultTest();
  }

  @Test
  public void testAttributesJsTotal() {
    defaultTest();
  }

  @Test
  public void testInterp1() {
    defaultTest();
  }

  @Test
  public void testInterp3() {
    defaultTest();
  }

  @Test
  public void testInterpInJs() {
    defaultTest();
  }

  @Test
  public void testInterpolatedTagNames() {
    defaultTest();
  }

  @Test
  public void testFilters() {
    defaultTest();
  }

  @Test
  public void testBufferedOutput() {
    defaultTest();
  }

  @Test
  public void testScriptStyle() {
    defaultTest();
  }

  @Test
  public void testScriptStyleOneliner() {
    defaultTest();
  }

  @Test
  public void testLeadingComment() {
    defaultTest();
  }

  @Test
  public void testWhitespaceMix() {
    defaultTest();
  }

  @Test
  public void testMixin() {
    defaultTest();
  }

  @Test
  public void testScriptWithDot() {
    defaultTest();
  }

  @Test
  public void testCase() {
    defaultTest();
  }

  @Test
  public void testConditionals() {
    defaultTest();
  }

  @Test
  public void testJsChained() {
    defaultTest();
  }

  @Test
  public void testPlainExpressionLine() {
    defaultTest();
  }

  @Test
  public void testEmbeddedHtmlPlainText() {
    defaultTest();
  }

  @Test
  public void testMixinInvocation() {
    defaultTest();
  }

  @Test
  public void testMixins() {
    defaultTest();
  }

  @Test
  public void testIncludeWithInternals() {
    defaultTest();
  }

  @Test
  public void testIncludeWithFilters() {
    defaultTest();
  }

  @Test
  public void testWhitespaceMix2() {
    CodeStyleSettings tempSettings = CodeStyle.createTestSettings(CodeStyle.getSettings(projectFixture.get()));
    CommonCodeStyleSettings.IndentOptions jadeIndentOptions = tempSettings.getIndentOptions(JadeFileType.INSTANCE);
    assertNotNull(jadeIndentOptions);
    jadeIndentOptions.TAB_SIZE = 2;
    try {
      CodeStyleSettingsManager.getInstance(projectFixture.get()).setTemporarySettings(tempSettings);
      defaultTest();
    }
    finally {
      CodeStyleSettingsManager.getInstance(projectFixture.get()).dropTemporarySettings();
    }
  }

  @Test
  public void testWeb12935() {
    defaultTest();
  }

  @Test
  public void testWeb13234() {
    defaultTest();
  }

  @Test
  public void testWeb13621() {
    defaultTest();
  }

  @Test
  public void testWeb14564() {
    defaultTest();
  }

  @Test
  public void testWeb17268() {
    defaultTest();
  }

  @Test
  public void testEa59518() {
    defaultTest();
  }

  @Test
  public void testEa64759() {
    defaultTest();
  }

  @Test
  public void testEa64844() {
    defaultTest();
  }

  @Test
  public void testTestcoffee() {
    defaultTest();
  }

  @Test
  public void testIfelse() {
    defaultTest();
  }

  @Test
  public void testForLoops() {
    defaultTest();
  }

  @Test
  public void testNestedMeta() {
    defaultTest();
  }

  @Test
  public void testEach() {
    defaultTest();
  }

  @Test
  public void testMixinRestArgs() {
    defaultTest();
  }

  @Test
  public void testMixinDefaultArgumentValues() {
    defaultTest();
  }

  @Test
  public void testMixinsObjectDestructuring() {
    defaultTest();
  }

  @Test
  public void testAngular2() {
    defaultTest();
  }

  @Test
  public void testUnbufferedBlock() {
    defaultTest();
  }

  @Test
  public void testUnbufferedBlock2() {
    defaultTest();
  }

  @Test
  public void testUnbufferedBlock3() {
    defaultTest();
  }

  @Test
  public void testTagsIndentation() {
    defaultTest();
  }

  @Test
  public void testAttributeWithConditional() {
    defaultTest();
  }

}
