package com.intellij.flex.completion;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.newfile.CreateFlexSkinDialog;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationForm;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.impl.PublicInheritorFilter;
import com.intellij.lang.javascript.refactoring.changeSignature.JSChangeSignatureDialog;
import com.intellij.lang.javascript.refactoring.changeSignature.JSMethodDescriptor;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterTableModel;
import com.intellij.lang.javascript.refactoring.introduceConstant.JSIntroduceConstantDialog;
import com.intellij.lang.javascript.refactoring.moveMembers.JSMoveMembersDialog;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.ArrayUtil;

public class ActionScriptCompletionInTextFieldTest extends FlexCompletionInTextFieldBase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  private JSFunction createFakeFunction() {
    return createFakeClass().findFunctionByName("a");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureReturnType() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    JSFunction function = createFakeFunction();
    JSExpressionCodeFragment fragment =
      JSChangeSignatureDialog.createReturnTypeCodeFragment(new JSMethodDescriptor(function, false).getReturnType(), function,
                                                           JavaScriptSupportLoader.ECMA_SCRIPT_L4);
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    checkTextFieldCompletion(fragment, included, excluded, "Z111", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureParameterTypeCell() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment = JSParameterTableModel.createParameterTypeCellFragment("", myProject);
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureDefaultValueCell() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment = JSParameterTableModel.createDefaultValueCellFragment("", createFakeClass());
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureInitializerCell() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment = JSParameterTableModel.createInitializerCellFragment("", createFakeClass());
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPackageNameCombo() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    String[] included = new String[]{"com"};
    String[] excluded = new String[]{"public", "function", "while", "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "com", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPackageNameCombo2() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    String[] included = new String[]{"foo"};
    String[] excluded = new String[]{"public", "function", "while", "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "foo", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateFlexSkinHostComponent() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    PsiFile fragment = CreateFlexSkinDialog.createHostComponentCombo("", myModule).getPsiFile();
    String[] included = new String[]{"Z111", "Z222"};
    // TODO primitive types (and e.g. not subclasses of SkinnableComponent?) should be removed from completion list
    String[] excluded = new String[]{"public", "function", "while", "Z333", "EventDispatcher", "int", "String", "uint", "Number"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z111", BASE_PATH + getTestName(false) + ".txt");
  }


  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIntroduceConstantTargetClass() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    // scope calculated same way as in constructor of JSIntroduceConstantDialog
    Module module = ModuleUtilCore.findModuleForPsiElement(myFile);
    GlobalSearchScope targetClassScope =
      module != null ? GlobalSearchScope.moduleWithDependenciesScope(module) : GlobalSearchScope.projectScope(myProject);
    PsiFile fragment =
      JSIntroduceConstantDialog.createTargetClassField(myProject, "", targetClassScope).getPsiFile();
    String[] included = new String[]{"Z111", "Z222", "com"};
    String[] excluded = new String[]{"EventDispatcher", "int", "String", "uint", "Number", "public", "function", "while"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z222", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveMembersTargetClass() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSMoveMembersDialog.createTargetClassField(myProject, "", JSMoveMembersDialog.getScope(myProject), myFile).getPsiFile();
    String[] included = new String[]{"Z111", "Z222"};
    String[] excluded = new String[]{"EventDispatcher", "int", "String", "uint", "Number", "public", "function", "while"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z222", BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveMembersTargetInnerClass() throws Exception {
    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSMoveMembersDialog.createTargetClassField(myProject, "", JSMoveMembersDialog.getScope(myProject), myFile).getPsiFile();
    String[] included = new String[]{"Inner"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, ArrayUtil.EMPTY_STRING_ARRAY, "Inner",
                             BASE_PATH + getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testComponentFromIndependentModule() throws Exception {
    final Module module2 = doCreateRealModule("module2");
    final VirtualFile contentRoot =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + getTestName(false) + "_module2");
    PsiTestUtil.addSourceRoot(module2, contentRoot);

    configureByFiles(null, BASE_PATH + getTestName(false) + "_2.mxml");

    PsiFile fragment =
      JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.moduleScope(myModule), null, null, "").getPsiFile();
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, new String[]{"ComponentFromIndependentModule_2"}, new String[]{"C1"}, null,
                             BASE_PATH + getTestName(false) + ".txt");

    fragment =
      JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.moduleScope(module2), null, null, "").getPsiFile();
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, new String[]{"C1"}, new String[]{"ComponentFromIndependentModule_2"}, null,
                             BASE_PATH + getTestName(false) + ".txt");
  }

  private void doTestCustomScope(String activeBcName, String selectedBcName, int numberOfVariants) throws Exception {
    String filename = getTestName(false).replaceAll("\\d+", "");
    configureByFiles(null, BASE_PATH + filename + "_2.mxml", BASE_PATH + filename + "_3.mxml");

    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true);

    FlexTestUtils.modifyConfigs(myProject, e -> {
      {
        final ModifiableFlexBuildConfiguration bc = e.getConfigurations(myModule)[0];
        bc.setName("Flex");
        bc.setNature(new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Application));
        FlexTestUtils.setSdk(bc, sdk);
      }
      {
        final ModifiableFlexBuildConfiguration bc = e.createConfiguration(myModule);
        bc.setName("AIR");
        bc.setNature(new BuildConfigurationNature(TargetPlatform.Desktop, false, OutputType.Application));
        FlexTestUtils.setSdk(bc, sdk);
      }
    });

    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(myModule);
    manager.setActiveBuildConfiguration(manager.findConfigurationByName(activeBcName));

    final ModuleWithDependenciesScope scope =
      FlexUtils.getModuleWithDependenciesAndLibrariesScope(myModule, manager.findConfigurationByName(selectedBcName), false);
    PublicInheritorFilter filter =
      new PublicInheritorFilter(myProject, FlashRunConfigurationForm.SPRITE_CLASS_NAME, scope, true);

    PsiFile fragment =
      JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.moduleScope(myModule), null, filter, "").getPsiFile();

    doTestForEditorTextField((JSExpressionCodeFragment)fragment, "", "js2", BASE_PATH + filename + ".txt");
    if (numberOfVariants == 0) {
      assertNull(myItems);
    }
    else {
      assertEquals(numberOfVariants, myItems.length);
    }
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope1() throws Exception {
    doTestCustomScope("Flex", "Flex", 0);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope2() throws Exception {
    doTestCustomScope("Flex", "AIR", 2);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope3() throws Exception {
    doTestCustomScope("AIR", "Flex", 0);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope4() throws Exception {
    doTestCustomScope("AIR", "AIR", 2);
  }
}
