package com.intellij.flex.highlighting;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.refactoring.ui.JSEditorTextField;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class ActionScriptHighlightingInTextFieldTest extends JSDaemonAnalyzerTestCase {

  @NonNls private static final String BASE_PATH = "/js2_highlighting/";

  @Override
  public void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
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

  private void doTestForEditorTextField(JSExpressionCodeFragment fragment) {
    myFile = fragment;

    Document document = PsiDocumentManager.getInstance(myProject).getDocument(fragment);
    final JSEditorTextField editorTextField = new JSEditorTextField(myProject, document);
    editorTextField.addNotify(); // initialize editor
    myEditor = editorTextField.getEditor();

    try {
      checkHighlighting(new ExpectedHighlightingData(editorTextField.getDocument(), true, true, true, myFile));
    }
    finally {
      editorTextField.removeNotify();
      UIUtil.dispatchAllInvocationEvents();
    }
  }

  public void testPackageNameCombo() {
    configureByFiles(BASE_PATH + getTestName(false), BASE_PATH + getTestName(false) + "/foo/dummy.txt");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("foo", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    doTestForEditorTextField((JSExpressionCodeFragment)fragment);
  }

  public void testPackageNameCombo2() {
    configureByFiles(BASE_PATH + getTestName(false), BASE_PATH + getTestName(false) + "/foo/dummy.txt");
    PsiFile fragment =
      JSReferenceEditor.forPackageName("<error>foo2</error>", myProject, "", GlobalSearchScope.projectScope(myProject), "").getPsiFile();
    doTestForEditorTextField((JSExpressionCodeFragment)fragment);
  }
}
