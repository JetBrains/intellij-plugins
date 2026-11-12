package com.dmarcotte.handlebars.completion;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class HbKeywordCompletionTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  public void doBasicTest(String text, String... expected) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByText(HbFileType.INSTANCE, text);
    myFixture.complete(CompletionType.BASIC);
    List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue(strings.containsAll(Arrays.asList(expected)), () -> "Expected " + Arrays.toString(expected) + " in " + strings);
  }

  @Test
  public void testSimple() {
    doBasicTest("{{#<caret>}}", "if", "each");
  }
}
