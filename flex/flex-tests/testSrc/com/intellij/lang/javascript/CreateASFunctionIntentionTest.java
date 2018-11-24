package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.refactoring.extractMethod.ExtractedFunctionSignatureGenerator;
import com.intellij.lang.javascript.refactoring.extractMethod.JSExtractFunctionHandler;
import com.intellij.lang.javascript.refactoring.extractMethod.JSSignatureContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.Pass;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() throws Exception {
    doTestAll();
  }

  public void testCanGetIntroductionScopeForActionScriptClassMember() throws Exception {
    configureByText(ActionScriptFileType.INSTANCE, "package {\n" +
                                                   "class test{\n" +
                                                   "    function foo() {\n" +
                                                   "        <caret>bar();\n" +
                                                   "    }\n" +
                                                   "}\n" +
                                                   "}");
    JSExtractFunctionHandler extractFunctionHandler = new JSExtractFunctionHandler();
    JSFunction function = findFirstFunction(myFile);
    List<JSExtractFunctionHandler.IntroductionScope> scopes =
      extractFunctionHandler.findBases(function);

    JSSignatureContext context = new JSSignatureContext() {
      @Override
      public boolean isActionScript() {
        return true;
      }

      @Override
      public boolean isAsync() {
        return false;
      }

      @Nullable
      @Override
      public PsiElement getAnchor() {
        return null;
      }

      @NotNull
      @Override
      public List<JSExtractFunctionHandler.IntroductionScope> getIntroductionScopes() {
        return scopes;
      }
    };
    Pass<JSExtractFunctionHandler.IntroductionScope> callback = new Pass<JSExtractFunctionHandler.IntroductionScope>() {
      @Override
      public void pass(JSExtractFunctionHandler.IntroductionScope scope) {

      }
    };
    JSExtractFunctionHandler.IntroductionScope actualScope =
      extractFunctionHandler
        .getIntroductionScope(myEditor, new ExtractedFunctionSignatureGenerator(), context, callback, "extractedFunction");
    Assert.assertEquals(ContainerUtil.getFirstItem(scopes), actualScope);
  }

  @Nullable
  private JSFunction findFirstFunction(@NotNull PsiFile file) {
    int offset = myEditor.getCaretModel().getOffset();
    return PsiTreeUtil.getParentOfType(file.findElementAt(offset), JSFunction.class);
  }

  protected VirtualFile configureByFiles(@Nullable final File rawProjectRoot, @NotNull final VirtualFile... vFiles) throws IOException {
    return super.configureByFiles(null, vFiles);
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
