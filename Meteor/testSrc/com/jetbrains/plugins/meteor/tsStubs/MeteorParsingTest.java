package com.jetbrains.plugins.meteor.tsStubs;

import com.dmarcotte.handlebars.parsing.HbParseDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsParseDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;

@TestApplication
public class MeteorParsingTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, MeteorTestUtil.getTestDataPath() + "/testParsing", "spacebars", new SpacebarsParseDefinition());

  @BeforeEach
  public void setUp() {
    ParsingTestFixture parsing = parsingFixture.get();
    parsing.setCheckAllPsiRoots(false);
    parsing.registerParserDefinition(new SpacebarsParseDefinition());
    parsing.registerParserDefinition(new HbParseDefinition());
  }

  @Test
  public void testInclusionTag() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testDecimalLiteralInPath() {
    parsingFixture.get().doTest(true);
  }

  @Test
  public void testDecimalLiteralWithName() {
    parsingFixture.get().doTest(true);
  }
}
