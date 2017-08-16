package com.intellij.flex.intentions;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.CreateJSFunctionIntentionTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class CreateASFunctionIntentionTest extends CreateJSFunctionIntentionTestBase {
  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  @NonNls
  public String getBasePath() {
    return "/createfunction_as";
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() {
    doTestAll();
  }

  public void testCreateConstructor() throws Exception {
    doTestTwoFiles();
  }

  private void doTestTwoFiles() throws Exception {
    enableInspectionTool(new JSValidateTypesInspection());
    String name = getTestName(false);
    String directory = "/" + name;
    String first = directory + "/" + name + ".as";
    String secondName = name + "_2.as";
    String second = directory + "/" + secondName;
    doTestFor(first, second);

    PsiFile secondFile = myFile.getContainingDirectory().findFile(secondName);
    setActiveEditor(createEditor(secondFile.getVirtualFile()));

    checkResultByFile(getBasePath() + "/after" + second);
  }
}
