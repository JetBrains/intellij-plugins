package com.intellij.flex.flexunit.codeInsight;

import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.lang.javascript.JSCompletionFixture;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.flex.FlexTestOption.WithFlexSdk;
import static com.intellij.lang.javascript.JSCompletionFixture.jsCompletionFixture;
import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestApplication
public class FlexUnitCompletionTest implements FlexUnitLibs {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSCompletionFixture> js =
    jsCompletionFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("flexUnit/completion/"), "as")
        .withModuleType(FlexModuleType.MODULE_TYPE_ID));

  @BeforeEach
  void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(js.get().getFixture().getTestRootDisposable(), "flexUnit");
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(js.get().getFixture().getModule(), js.get().getTestName(false), getClass(), js.get().getFixture().getTestRootDisposable());
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(WithFlexSdk)
  @Test
  public void testMeta1() {
    js.get().doTest("", "as");
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(WithFlexSdk)
  @Test
  public void testMeta2() {
    js.get().doTest("", "as");
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta3() {
    assertNull(js.get().doTest("", "as"));
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta4() {
    assertNull(js.get().doTest("", "mxml"));
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta5() {
    assertNull(js.get().doTest("", "as"));
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta6() {
    assertNull(js.get().doTest("", "as"));
  }

  @FlexTestOptions(WithFlexSdk)
  @Test
  public void testClassMeta1() {
    js.get().doTest("", "as");
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(WithFlexSdk)
  @Test
  public void testFieldMeta1() {
    js.get().doTest("", "as");
  }

  @FlexTestOptions(WithFlexSdk)
  @Test
  public void testCustomRunner() {
    setUpJdk();
    js.get().doTestForFiles(new String[]{js.get().getTestName(false) + ".as", "mypackage/FooRunner.as"}, "", "as");
  }
}
