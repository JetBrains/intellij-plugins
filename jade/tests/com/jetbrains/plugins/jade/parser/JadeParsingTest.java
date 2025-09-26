// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.parser;

import com.intellij.application.options.CodeStyle;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.jetbrains.plugins.jade.JadeBaseParsingTestCase;
import com.jetbrains.plugins.jade.JadeTestUtil;
import com.jetbrains.plugins.jade.psi.JadeFileType;

public class JadeParsingTest extends JadeBaseParsingTestCase {

  @Override
  protected String getTestDataPath() {
    return JadeTestUtil.getBaseTestDataPath() + "/parser";
  }

  @Override
  protected boolean isCheckNoPsiEventsOnReparse() {
    // todo please fix JadeAstFactory.createComposite and remove this method
    //
    // The problem is that Jade PSI hierarchy does not pass the proper IElementTypes to its super-calls in constructors.
    // So quite often, a JadePsiElement.getElementType() returns not a Jade element type but an element type of its base xml psi element.
    // E.g., XmlElementType.XML_ATTRIBUTE instead of JadeElementTypes.ATTRIBUTE.
    //
    // Because of that, when DiffTree tries to match new AST with the old one, it miserably fails.
    //
    // To fix this, you need to go through JadAstFactory#createComposite and ensure that every created jade psi element is created
    // with the corresponding element type.

    return false;
  }

  private void defaultTest() {
    doTest(true);
  }

  public void testEmpty() {
    defaultTest();
  }

  public void testSimple() {
    defaultTest();
  }

  public void testAttributes1() {
    defaultTest();
  }

  public void testAttributes2() {
    defaultTest();
  }

  public void testAttributes3() {
    defaultTest();
  }

  public void testAttributes4() {
    defaultTest();
  }

  public void testAttributes5() {
    defaultTest();
  }

  public void testAttributeNames() {
    defaultTest();
  }

  public void testAttributesJs() {
    defaultTest();
  }

  public void testAttributesEs6Strings() {
    defaultTest();
  }

  public void testAttributesJsTotal() {
    defaultTest();
  }

  public void testInterp1() {
    defaultTest();
  }

  public void testInterp3() {
    defaultTest();
  }

  public void testInterpInJs() {
    defaultTest();
  }

  public void testInterpolatedTagNames() {
    defaultTest();
  }

  public void testFilters() {
    defaultTest();
  }

  public void testBufferedOutput() {
    defaultTest();
  }

  public void testScriptStyle() {
    defaultTest();
  }

  public void testScriptStyleOneliner() {
    defaultTest();
  }

  public void testLeadingComment() {
    defaultTest();
  }

  public void testWhitespaceMix() {
    defaultTest();
  }

  public void testMixin() {
    defaultTest();
  }

  public void testScriptWithDot() {
    defaultTest();
  }

  public void testCase() {
    defaultTest();
  }

  public void testConditionals() {
    defaultTest();
  }

  public void testJsChained() {
    defaultTest();
  }

  public void testPlainExpressionLine() {
    defaultTest();
  }

  public void testEmbeddedHtmlPlainText() {
    defaultTest();
  }

  public void testMixinInvocation() {
    defaultTest();
  }

  public void testMixins() {
    defaultTest();
  }

  public void testIncludeWithInternals() {
    defaultTest();
  }

  public void testIncludeWithFilters() {
    defaultTest();
  }

  public void testWhitespaceMix2() {
    CodeStyleSettings tempSettings = CodeStyle.createTestSettings(CodeStyle.getSettings(getProject()));
    CommonCodeStyleSettings.IndentOptions jadeIndentOptions = tempSettings.getIndentOptions(JadeFileType.INSTANCE);
    assertNotNull(jadeIndentOptions);
    jadeIndentOptions.TAB_SIZE = 2;
    try {
      CodeStyleSettingsManager.getInstance(getProject()).setTemporarySettings(tempSettings);
      defaultTest();
    }
    finally {
      CodeStyleSettingsManager.getInstance(getProject()).dropTemporarySettings();
    }
  }

  public void testWeb12935() {
    defaultTest();
  }

  public void testWeb13234() {
    defaultTest();
  }

  public void testWeb13621() {
    defaultTest();
  }

  public void testWeb14564() {
    defaultTest();
  }

  public void testWeb17268() {
    defaultTest();
  }

  public void testEa59518() {
    defaultTest();
  }

  public void testEa64759() {
    defaultTest();
  }

  public void testEa64844() {
    defaultTest();
  }

  public void testTestcoffee() {
    defaultTest();
  }

  public void testIfelse() {
    defaultTest();
  }

  public void testForLoops() {
    defaultTest();
  }

  public void testNestedMeta() {
    defaultTest();
  }

  public void testEach() {
    defaultTest();
  }

  public void testMixinRestArgs() {
    defaultTest();
  }

  public void testMixinDefaultArgumentValues() {
    defaultTest();
  }

  public void testMixinsObjectDestructuring() {
    defaultTest();
  }

  public void testAngular2() {
    defaultTest();
  }

  public void testUnbufferedBlock() {
    defaultTest();
  }

  public void testUnbufferedBlock2() {
    defaultTest();
  }

  public void testUnbufferedBlock3() {
    defaultTest();
  }

  public void testTagsIndentation() {
    defaultTest();
  }
}
