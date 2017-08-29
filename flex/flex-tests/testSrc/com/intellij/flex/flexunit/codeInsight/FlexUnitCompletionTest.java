package com.intellij.flex.flexunit.codeInsight;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.BaseJSCompletionTestCase;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.lang.javascript.JSTestOption.WithFlexSdk;
import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexUnitCompletionTest extends BaseJSCompletionTestCase implements FlexUnitLibs {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

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
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }


  @JSTestOptions(value = {WithFlexSdk}, selectLookupItem = 0)
  public void testMeta1() throws Exception {
    doTest("", "as");
  }

  @JSTestOptions(value = {WithFlexSdk}, selectLookupItem = 0)
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

  @JSTestOptions(value = {WithFlexSdk}, selectLookupItem = 0)
  public void testFieldMeta1() throws Exception {
    doTest("", "as");
  }

  @JSTestOptions({WithFlexSdk})
  public void testCustomRunner() throws Exception {
    VirtualFile[] files = new VirtualFile[]{getVirtualFile(getBasePath() + getTestName(false) + ".as"),
      getVirtualFile(getBasePath() + "mypackage/FooRunner.as")};
    doTestForFiles(files, "", "as", new File(getTestDataPath() + getBasePath()));
  }
}
