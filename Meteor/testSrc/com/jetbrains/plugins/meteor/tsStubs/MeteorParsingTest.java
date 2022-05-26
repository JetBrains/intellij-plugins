package com.jetbrains.plugins.meteor.tsStubs;

import com.dmarcotte.handlebars.parsing.HbParseDefinition;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.psi.templateLanguages.TemplateDataLanguagePatterns;
import com.intellij.testFramework.ParsingTestCase;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsParseDefinition;

public class MeteorParsingTest extends ParsingTestCase {
  public MeteorParsingTest() {
    super("testParsing", "spacebars", new SpacebarsParseDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return MeteorTestUtil.getTestDataPath();
  }

  @Override
  protected boolean checkAllPsiRoots() {
    return false;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myProject.registerService(TemplateDataLanguageMappings.class, TemplateDataLanguageMappings.class);
    getApplication().registerService(TemplateDataLanguagePatterns.class, new TemplateDataLanguagePatterns());
    registerParserDefinition(new SpacebarsParseDefinition());
    registerParserDefinition(new HbParseDefinition());
  }

  public void testInclusionTag() {
    doTest(true);
  }

  public void testDecimalLiteralInPath() {
    doTest(true);
  }

  public void testDecimalLiteralWithName() {
    doTest(true);
  }
}
