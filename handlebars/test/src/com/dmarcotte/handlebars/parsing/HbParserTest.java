// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.PropertiesComponentImpl;
import com.intellij.lang.ParserDefinition;
import com.intellij.mock.MockApplication;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.psi.templateLanguages.TemplateDataLanguagePatterns;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

/**
 * ParsingTestCase test are created by placing a MyTestName.hbs file in the test/data/parsing directory with the syntax
 * you would like to validate, and a MyTestName.txt file representing the expected Psi structure.
 * <p/>
 * You then create a test of the following form to validate the parser (note how the test name corresponds
 * to the name of the .hbs and .txt files above):
 * <p/>
 * <pre>{@code
 * public void testMyTestName() { doTest(true); }
 * }</pre>
 * <p/>
 * <b>TIP:</b> if you create a .hbs file without a .txt file, the test will auto generate .txt file from the current
 * parser. So, in practice when creating parser tests, you create the .hbs file, run the test, validate that
 * the .txt represents the desired Psi structure, then call it a day.
 */
public abstract class HbParserTest extends ParsingTestCase {
  public HbParserTest(ParserDefinition @NotNull ... additionalDefinitions) {
    super("parser", "hbs", ArrayUtil.prepend(new HbParseDefinition(), additionalDefinitions));
  }

  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH;
  }

  @Override
  protected boolean checkAllPsiRoots() {
    return false;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    MockApplication app = getApplication();

    myProject.registerService(TemplateDataLanguageMappings.class, TemplateDataLanguageMappings.class);

    // PropertiesComponent is used by HbConfig
    app.registerService(PropertiesComponent.class, PropertiesComponentImpl.class);
    myProject.registerService(PropertiesComponent.class, PropertiesComponentImpl.class);

    app.registerService(TemplateDataLanguagePatterns.class, new TemplateDataLanguagePatterns());
    registerParserDefinition(new HbParseDefinition());
  }
}
