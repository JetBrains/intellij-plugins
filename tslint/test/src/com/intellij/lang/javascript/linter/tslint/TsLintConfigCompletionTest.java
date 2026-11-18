package com.intellij.lang.javascript.linter.tslint;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;

@TestApplication
public class TsLintConfigCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = sharedCodeInsightFixture(projectFixture);

  @BeforeEach
  void setUp() {
    codeInsightFixture.get().setTestDataPath(TsLintTestUtil.BASE_TEST_DATA_PATH + "/config/completion");
  }

  @Test
  public void testTopLevelRules() {
    List<String> result = codeInsightFixture.get().getCompletionVariants("/topLevelRules/tslint.json");
    final String[] variants = {"\"max-classes-per-file\"", "\"max-file-line-count\"",
      "\"max-line-length\""};
    assertThat(result).containsSubsequence(variants);
  }

  @Test
  public void testMemberOrdering() {
    List<String> result = codeInsightFixture.get().getCompletionVariants("/memberOrdering/tslint.json");
    final String[] variants = {"\"fields-first\"", "\"instance-sandwich\"", "\"statics-first\""};
    assertThat(result).containsSubsequence(variants);
  }

  @Test
  public void testTypedefWhitespace() {
    List<String> result = codeInsightFixture.get().getCompletionVariants("/typedefWhitespace/tslint.json");
    final String[] variants = {"\"call-signature\"", "\"index-signature\"", "\"parameter\"", "\"property-declaration\"",
      "\"variable-declaration\""};
    assertThat(result).containsSubsequence(variants);
  }

  @Test
  public void testTypedefWhitespaceValues() {
    List<String> result = codeInsightFixture.get().getCompletionVariants("/typedefWhitespaceValues/tslint.json");
    final String[] variants = {"nospace", "space"};

    assertThat(result).containsSubsequence(variants);
  }
}
