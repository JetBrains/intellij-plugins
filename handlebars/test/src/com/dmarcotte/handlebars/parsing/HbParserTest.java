// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;

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
public abstract class HbParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  protected final TestFixture<ParsingTestFixture> parsingFixture;

  public HbParserTest(ParserDefinition @NotNull ... additionalDefinitions) {
    parsingFixture = parsingFixture(projectFixture, HbTestUtils.BASE_TEST_DATA_PATH + "/parser", "hbs",
                                    ArrayUtil.prepend(new HbParseDefinition(), additionalDefinitions));
  }

  protected boolean checkAllPsiRoots() {
    return false;
  }

  @BeforeEach
  void setUpParsing() {
    parsingFixture.get().setCheckAllPsiRoots(checkAllPsiRoots());
  }

  protected void doTest(boolean checkResult) {
    parsingFixture.get().doTest(checkResult);
  }
}
