// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.refactoring.moveMembers.ActionScriptMoveMembersDialog;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.newfile.CreateFlexSkinDialog;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
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
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;

public class ActionScriptCompletionInTextFieldTest extends FlexCompletionInTextFieldBase {
  private static final LightProjectDescriptor DESCRIPTOR = new FlexProjectDescriptor();

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return DESCRIPTOR;
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(BASE_PATH);
  }

  private JSFunction createFakeFunction() {
    return createFakeClass().findFunctionByName("a");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureReturnType() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    JSFunction function = createFakeFunction();
    JSExpressionCodeFragment fragment =
      JSChangeSignatureDialog.createReturnTypeCodeFragment(new JSMethodDescriptor(function, false).getReturnType(), function,
                                                           JavaScriptSupportLoader.ECMA_SCRIPT_L4);
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    checkTextFieldCompletion(fragment, included, excluded, "Z111", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureParameterTypeCell() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment = JSParameterTableModel.createParameterTypeCellFragment("", createFakeFunction());
    String[] included = new String[]{"Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher", "void", "*"};
    String[] excluded = ArrayUtil.mergeArrays(DEFALUT_VALUES, "public", "function", "while");
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureDefaultValueCell() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment = JSParameterTableModel.createDefaultValueCellFragment("", createFakeClass());
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testChangeSignatureInitializerCell() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    JSExpressionCodeFragment fragment = JSParameterTableModel.createInitializerCellFragment("", createFakeClass());
    String[] included = DEFALUT_VALUES;
    // TODO classes should be removed from completion list
    included = ArrayUtil.mergeArrays(included, "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher");
    String[] excluded = new String[]{"public", "function", "while"};
    checkTextFieldCompletion(fragment, included, excluded, "EventDispatcher", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPackageNameCombo() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("", getProject(), "", GlobalSearchScope.projectScope(getProject()), "").getPsiFile();
    String[] included = new String[]{"com"};
    String[] excluded = new String[]{"public", "function", "while", "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "com", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testPackageNameCombo2() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("", getProject(), "", GlobalSearchScope.projectScope(getProject()), "").getPsiFile();
    String[] included = new String[]{"foo"};
    String[] excluded = new String[]{"public", "function", "while", "Z111", "Z222", "int", "String", "uint", "Number", "EventDispatcher"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "foo", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testCreateFlexSkinHostComponent() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    PsiFile fragment = CreateFlexSkinDialog.createHostComponentCombo("", getModule()).getPsiFile();
    String[] included = new String[]{"Z111", "Z222"};
    // TODO primitive types (and e.g. not subclasses of SkinnableComponent?) should be removed from completion list
    String[] excluded = new String[]{"public", "function", "while", "Z333", "EventDispatcher", "int", "String", "uint", "Number"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z111", getTestName(false) + ".txt");
  }


  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testIntroduceConstantTargetClass() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    // scope calculated same way as in constructor of JSIntroduceConstantDialog
    Module module = ModuleUtilCore.findModuleForPsiElement(myFixture.getFile());
    GlobalSearchScope targetClassScope =
      module != null ? GlobalSearchScope.moduleWithDependenciesScope(module) : GlobalSearchScope.projectScope(getProject());
    PsiFile fragment =
      JSIntroduceConstantDialog.createTargetClassField(getProject(), "", targetClassScope).getPsiFile();
    String[] included = new String[]{"Z111", "Z222", "com"};
    String[] excluded = new String[]{"EventDispatcher", "int", "String", "uint", "Number", "public", "function", "while"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z222", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveMembersTargetClass() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    PsiFile fragment =
      ActionScriptMoveMembersDialog.createTargetClassField(getProject(), "", ActionScriptMoveMembersDialog.getScope(getProject()), myFixture.getFile()).getPsiFile();
    String[] included = new String[]{"Z111", "Z222"};
    String[] excluded = new String[]{"EventDispatcher", "int", "String", "uint", "Number", "public", "function", "while"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, excluded, "Z222", getTestName(false) + ".txt");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testMoveMembersTargetInnerClass() {
    setUpJdk();
    myFixture.configureByFiles(getTestName(false) + "_2.js2");
    PsiFile fragment =
      ActionScriptMoveMembersDialog.createTargetClassField(getProject(), "", ActionScriptMoveMembersDialog.getScope(getProject()), myFixture.getFile()).getPsiFile();
    String[] included = new String[]{"Inner"};
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, included, ArrayUtilRt.EMPTY_STRING_ARRAY, "Inner",
                             getTestName(false) + ".txt");
  }

  private void doTestCustomScope(String activeBcName, String selectedBcName, int numberOfVariants) {
    String buildConfigName = "AIR";
    String filename = getTestName(false).replaceAll("\\d+", "");
    myFixture.configureByFiles(filename + "_2.mxml", filename + "_3.mxml");

    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, myFixture.getTestRootDisposable());

    FlexTestUtils.modifyConfigs(getProject(), e -> {
      {
        final ModifiableFlexBuildConfiguration bc = e.getConfigurations(getModule())[0];
        bc.setName("Flex");
        bc.setNature(new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Application));
        FlexTestUtils.setSdk(bc, sdk);
      }
      {
        final ModifiableFlexBuildConfiguration bc = e.createConfiguration(getModule());
        bc.setName(buildConfigName);
        bc.setNature(new BuildConfigurationNature(TargetPlatform.Desktop, false, OutputType.Application));
        FlexTestUtils.setSdk(bc, sdk);
      }
    });

    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(getModule());
    FlexBuildConfiguration old = manager.getActiveConfiguration();
    Disposer.register(myFixture.getTestRootDisposable(), () ->  manager.setActiveBuildConfiguration(old));
    manager.setActiveBuildConfiguration(manager.findConfigurationByName(activeBcName));

    final GlobalSearchScope scope =
      FlexUtils.getModuleWithDependenciesAndLibrariesScope(getModule(), manager.findConfigurationByName(selectedBcName), false);
    PublicInheritorFilter filter =
      new PublicInheritorFilter(getProject(), FlashRunConfigurationForm.SPRITE_CLASS_NAME, scope, true);

    PsiFile fragment =
      JSReferenceEditor.forClassName("", getProject(), null, GlobalSearchScope.moduleScope(getModule()), null, filter, "").getPsiFile();

    doTestTextFieldFromFile((JSExpressionCodeFragment)fragment, filename + ".txt");
    assertEquals(numberOfVariants, myFixture.getLookupElements().length);

    FlexTestUtils.modifyConfigs(getProject(), e -> {
      ModifiableFlexBuildConfiguration[] configurations = e.getConfigurations(getModule());
      if (configurations == null || configurations.length <= 1) return;
      for (ModifiableFlexBuildConfiguration configuration : configurations) {
        if (configuration.getName().equals(buildConfigName)) e.configurationRemoved(configuration);
      }
    });
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope1() {
    doTestCustomScope("Flex", "Flex", 0);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope2() {
    doTestCustomScope("Flex", "AIR", 2);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope3() {
    doTestCustomScope("AIR", "Flex", 0);
  }

  @JSTestOptions(JSTestOption.WithGumboSdk)
  public void testCustomScope4() {
    doTestCustomScope("AIR", "AIR", 2);
  }
}
