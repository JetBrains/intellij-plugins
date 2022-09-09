package com.intellij.flex.flexunit.codeInsight;

import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.testFramework.LightProjectDescriptor;

import static com.intellij.flex.FlexTestOption.WithFlexSdk;

public class FlexUnitCompletionTest extends BaseJSCompletionTestCase implements FlexUnitLibs {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "flexUnit");
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("flexUnit/completion/");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(WithFlexSdk)
  public void testMeta1() {
    doTest("", "as");
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(WithFlexSdk)
  public void testMeta2() {
    doTest("", "as");
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta3() {
    assertNull(doTest("", "as"));
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta4() {
    assertNull(doTest("", "mxml"));
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta5() {
    assertNull(doTest("", "as"));
  }

  // disabled until IDEA-65789 fixed
  @FlexTestOptions(WithFlexSdk)
  public void _testMeta6() {
    assertNull(doTest("", "as"));
  }

  @FlexTestOptions(WithFlexSdk)
  public void testClassMeta1() {
    doTest("", "as");
  }

  @JSTestOptions(selectLookupItem = 0)
  @FlexTestOptions(WithFlexSdk)
  public void testFieldMeta1() {
    doTest("", "as");
  }

  @FlexTestOptions(WithFlexSdk)
  public void testCustomRunner() {
    setUpJdk();
    doTestForFiles(new String[]{getTestName(false) + ".as", "mypackage/FooRunner.as"}, "", "as");
  }
}
