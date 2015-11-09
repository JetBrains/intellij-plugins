package com.intellij.lang.javascript.flexunit;

import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.lang.javascript.JSTestOption.SelectFirstItem;
import static com.intellij.lang.javascript.JSTestOption.WithFlexSdk;

public class FlexUnitCompletionTest extends BaseJSCompletionTestCase implements FlexUnitLibs {

  @Override
  protected String getExtension() {
    return "as";
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);

    FlexTestUtils.addFlexUnitLib(getClass(), getTestName(false), getModule(), getTestDataPath(), FLEX_UNIT_0_9_SWC, FLEX_UNIT_4_SWC);
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("flexUnit");
  }

  @Override
  protected String getBasePath() {
    return "/completion/";
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }


  @JSTestOptions({WithFlexSdk, SelectFirstItem})
  public void testMeta1() throws Exception {
    doTest("", "as");
  }

  @JSTestOptions({WithFlexSdk, SelectFirstItem})
  public void testMeta2() throws Exception {
    doTest("", "as");
  }

  // disabled until IDEA-65789 fixed
  @JSTestOptions({WithFlexSdk})
  public void _testMeta3() throws Exception {
    assertNull(doTest("", "as"));
  }

  // disabled until IDEA-65789 fixed
  @JSTestOptions({WithFlexSdk})
  public void _testMeta4() throws Exception {
    assertNull(doTest("", "mxml"));
  }

  // disabled until IDEA-65789 fixed
  @JSTestOptions({WithFlexSdk})
  public void _testMeta5() throws Exception {
    assertNull(doTest("", "as"));
  }

  // disabled until IDEA-65789 fixed
  @JSTestOptions({WithFlexSdk})
  public void _testMeta6() throws Exception {
    assertNull(doTest("", "as"));
  }

  @JSTestOptions({WithFlexSdk})
  public void testClassMeta1() throws Exception {
    doTest("", "as");
  }

  @JSTestOptions({WithFlexSdk, SelectFirstItem})
  public void testFieldMeta1() throws Exception {
    doTest("", "as");
  }

  @JSTestOptions({WithFlexSdk})
  public void testCustomRunner() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".as"), getVirtualFile(getBasePath() + "mypackage/FooRunner.as")};
    doTestForFiles(files, "", "as", new File(getTestDataPath() + getBasePath()));
  }


}
