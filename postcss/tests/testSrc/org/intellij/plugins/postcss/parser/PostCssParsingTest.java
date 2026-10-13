package org.intellij.plugins.postcss.parser;

import com.intellij.lang.css.CSSParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssTestUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;

@TestDataPath("$CONTENT_ROOT/../testData/parser/")
public abstract class PostCssParsingTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  private final TestFixture<ParsingTestFixture> parsingFixture;

  public PostCssParsingTest(@NonNls @NotNull String dataPath) {
    parsingFixture = parsingFixture(projectFixture, PostCssTestUtils.getFullTestDataPath(getClass()) + "/" + dataPath, "pcss", false,
                                    new PostCssParserDefinition(), new CSSParserDefinition());
  }

  protected void doTest() {
    parsingFixture.get().doTest(true);
  }
}
