package com.intellij.lang.javascript.linter.eslint.unit;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.assertj.core.api.Assertions.assertThat;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/javascript/eslint/testData/linter/eslint/config/completion")
public class EslintConfigCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = sharedCodeInsightFixture(projectFixture);
  private final TestFixture<String> testName = testNameFixture();

  @Test
  public void testRules() {
    List<String> result = getCompletionVariants();
    assertThat(result).contains("\"semi\"");
  }

  @Test
  public void testRulesInOverrides() {
    List<String> result = getCompletionVariants();
    assertThat(result).contains("\"semi\"");
  }

  private List<String> getCompletionVariants() {
    return codeInsight.get().getCompletionVariants(testName.get() + "/.eslintrc.json");
  }
}
