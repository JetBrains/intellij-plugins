package com.intellij.flex.refactoring;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionFixture.jsInlineVarOrFunctionFixture;

@TestApplication
public class FlexInlineVariableTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSInlineVarOrFunctionFixture> js =
    jsInlineVarOrFunctionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("as_refactoring/inlineVariable/"))
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = js.get().getFixture();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "as_refactoring/inlineVariable/");
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  private void ecmaTest() {
    js.get().doTest(js.get().getTestName(false), "js2");
  }

  @Test
  public void testClassMember() {
    ecmaTest();
  }

  @Test
  public void testClassMember2() {
    ecmaTest();
  }

  @Test
  public void testClassMember3() {
    ecmaTest();
  }

  @Test
  public void testInMxml() {
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  @Test
  public void testIntoAttribute() {
    js.get().doTest(js.get().getTestName(false), "mxml");
  }

  @Test
  public void testInlineOneFieldUsage() {
    js.get().doTest(new String[]{js.get().getTestName(false) + ".js2"}, true);
  }

  @Test
  public void testReportAccessibilityProblems() {
    js.get().doTestConflicts(js.get().getTestName(false), "js2", new String[]{
      "Method Test.foo() with private visibility won't be accessible from file ReportAccessibilityProblems.js2"});
  }
}
