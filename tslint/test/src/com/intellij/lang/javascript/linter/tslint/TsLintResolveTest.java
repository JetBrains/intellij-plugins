package com.intellij.lang.javascript.linter.tslint;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
public class TsLintResolveTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = sharedCodeInsightFixture(projectFixture);
  private String testName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    codeInsightFixture.get().setTestDataPath(TsLintTestUtil.BASE_TEST_DATA_PATH + "/config/resolve/");
  }

  @Test
  public void testExtendsArray() {
    final PsiReference ref = codeInsightFixture.get().getReferenceAtCaretPosition(testName + "/tslint.json");
    assertNotNull(ref);
  }

  @Test
  public void testExtendsProperty() {
    final PsiReference ref = codeInsightFixture.get().getReferenceAtCaretPosition(testName + "/tslint.json");
    assertNotNull(ref);
  }
}
