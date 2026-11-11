package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSInplaceIntroduceVariableFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.introduceVariable.JSInplaceIntroduceVariableFixture.jsInplaceIntroduceVariableFixture;

@TestApplication
public class ActionScriptInPlaceIntroduceVariableTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSInplaceIntroduceVariableFixture> introduce = jsInplaceIntroduceVariableFixture(
    projectFixture,
    new JSFixtureOptions(FlexTestUtils.getTestDataPath("refactoring/introduceVariable/")).withModuleType(FlexModuleType.MODULE_TYPE_ID)
  );

  @BeforeEach
  void setUp() {
    JSInplaceIntroduceVariableFixture introduceFixture = introduce.get();
    FlexTestUtils.allowFlexVfsRootsFor(introduceFixture.getTestRootDisposable(), "refactoring/introduceVariable/");
    FlexTestUtils.setupFlexSdk(introduceFixture.getFixture().getModule(), introduceFixture.getTestName(false),
                               ActionScriptInPlaceIntroduceVariableTest.class, introduceFixture.getTestRootDisposable());
  }

  @Test
  public void testInplaceBasicAS() {
    introduce.get().doTest(introduce.get().getTestName(false), ".as");
  }

  @Test
  public void testInplaceSecondOccurrence() {
    introduce.get().doTest(introduce.get().getTestName(false), ".as");
  }

  @Test
  public void testInplaceWithNamespace() {
    JSInplaceIntroduceVariableFixture introduceFixture = introduce.get();
    CodeInsightTestFixture myFixture = introduceFixture.getFixture();
    String testName = introduceFixture.getTestName(false);
    String name1 = "test1/" + testName + "1";
    String name2 = "test2/" + testName + "2";
    EdtTestUtil.runInEdtAndWait(() -> {
      myFixture.configureByFiles(name2 + ".as", name1 + ".as");
      introduceFixture.performActionIntroduce();
      myFixture.checkResultByFile(testName + "2_after.as");
    });
  }
}
