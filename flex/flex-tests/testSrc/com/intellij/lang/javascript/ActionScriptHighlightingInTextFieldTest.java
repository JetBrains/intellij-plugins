package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.refactoring.ui.JSEditorTextField;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.ExpectedHighlightingData;
import org.jetbrains.annotations.NonNls;

/**
 * @author ksafonov
 */
public class ActionScriptHighlightingInTextFieldTest extends JSDaemonAnalyzerTestCase {

  @NonNls private static final String BASE_PATH = "/js2_highlighting/";

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getExtension() {
    return "js2";
  }

  private void doTestForEditorTextField(JSExpressionCodeFragment fragment) throws Exception {
    myFile = fragment;

    Document document = PsiDocumentManager.getInstance(myProject).getDocument(fragment);
    final JSEditorTextField editorTextField = new JSEditorTextField(myProject, document);
    editorTextField.addNotify(); // initialize editor
    myEditor = editorTextField.getEditor();
    checkHighlighting(new ExpectedHighlightingData(editorTextField.getDocument(), true, true, true, myFile));
    editorTextField.removeNotify();
  }

  public void testPackageNameCombo() throws Exception {
    configureByFiles(BASE_PATH + getTestName(false), BASE_PATH + getTestName(false) + "/foo/dummy.txt");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("foo", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    doTestForEditorTextField((JSExpressionCodeFragment)fragment);
  }

  public void testPackageNameCombo2() throws Exception {
    configureByFiles(BASE_PATH + getTestName(false), BASE_PATH + getTestName(false) + "/foo/dummy.txt");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("<error>foo2</error>", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    doTestForEditorTextField((JSExpressionCodeFragment)fragment);
  }
}
