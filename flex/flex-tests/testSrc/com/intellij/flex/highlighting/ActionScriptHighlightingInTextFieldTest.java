package com.intellij.flex.highlighting;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.refactoring.ui.JSEditorTextField;
import com.intellij.lang.javascript.ui.ActionScriptPackageChooserDialog;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ActionScriptHighlightingInTextFieldTest extends JSDaemonAnalyzerTestCase {

  @NonNls private static final String BASE_PATH = "/js2_highlighting/";

  @Override
  public void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getExtension() {
    return "js2";
  }

  private void doTestForEditorTextField(JSExpressionCodeFragment fragment) {
    myFile = fragment;

    Document document = PsiDocumentManager.getInstance(myProject).getDocument(fragment);
    final JSEditorTextField editorTextField = new JSEditorTextField(myProject, document);
    editorTextField.addNotify(); // initialize editor
    myEditor = editorTextField.getEditor();

    try {
      checkHighlighting(new ExpectedHighlightingData(editorTextField.getDocument(), true, true, true));
    }
    finally {
      editorTextField.removeNotify();
      UIUtil.dispatchAllInvocationEvents();
    }
  }

  public void testPackageNameCombo() {
    configureByFiles(BASE_PATH + getTestName(false), BASE_PATH + getTestName(false) + "/foo/dummy.txt");
    PsiFile fragment =
      ActionScriptPackageChooserDialog.createPackageReferenceEditor("foo", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    doTestForEditorTextField((JSExpressionCodeFragment)fragment);
  }

  public void testPackageNameCombo2() {
    configureByFiles(BASE_PATH + getTestName(false), BASE_PATH + getTestName(false) + "/foo/dummy.txt");
    PsiFile fragment =
      ActionScriptPackageChooserDialog.createPackageReferenceEditor("<error>foo2</error>", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    doTestForEditorTextField((JSExpressionCodeFragment)fragment);
  }
}
