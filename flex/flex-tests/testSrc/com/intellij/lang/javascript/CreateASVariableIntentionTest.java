package com.intellij.lang.javascript;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.flex.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import org.jetbrains.annotations.NonNls;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class CreateASVariableIntentionTest extends BaseJSIntentionTestCase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[]{new JSUnresolvedVariableInspection()};
  }

  @Override
  @NonNls
  public String getBasePath() {
    return "/createvariable_as";
  }

  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() throws Exception {
    doTestAll();
  }
}
