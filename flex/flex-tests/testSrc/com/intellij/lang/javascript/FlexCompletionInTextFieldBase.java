package com.intellij.lang.javascript;

import com.intellij.codeInsight.EditorInfo;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.completion.JSKeywordsCompletionProvider;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.refactoring.ui.JSEditorTextField;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * User: ksafonov
 */
public abstract class FlexCompletionInTextFieldBase extends BaseJSCompletionTestCase {

  protected static final String BASE_PATH = "/js2_completion/";

  static final String[] DEFALUT_VALUES =
    ArrayUtil.mergeArrays(JSKeywordsCompletionProvider.TYPE_LITERAL_VALUES, "NaN", "Infinity");

  private Collection<Editor> myEditorsToRelease;

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected String getExtension() {
    return "js2";
  }

  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myEditorsToRelease = new ArrayList<Editor>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (Editor editor : myEditorsToRelease) {
      EditorFactory.getInstance().releaseEditor(editor);
    }
    super.tearDown();
  }

  protected void checkTextFieldCompletion(JSExpressionCodeFragment fragment,
                                          String[] included,
                                          String[] excluded,
                                          @Nullable String choose,
                                          String file) throws Exception {
    doTestForEditorTextField(fragment, "", "js2", file);
    assertContains(myItems, true, included);
    assertContains(myItems, false, excluded);
    if (choose != null) {
      boolean found = false;
      for (LookupElement item : myItems) {
        if (choose.equals(item.getLookupString())) {
          selectItem(item);
          found = true;
          break;
        }
      }
      assertTrue("Item '" + choose + "' not found in lookup", found);
      checkResultByFile(BASE_PATH + getTestName(false) + "_after.txt");
    }
  }

  protected void doTestForEditorTextField(JSExpressionCodeFragment fragment, String suffix, String ext, final String file)
    throws Exception {
    JSTestUtils.initJSIndexes(getProject());

    JSEditorTextField editorTextField = null;
    try {
      myFile = fragment;
      Document document = PsiDocumentManager.getInstance(myProject).getDocument(fragment);
      editorTextField = new JSEditorTextField(myProject, document);

      final String text = StringUtil.convertLineSeparators(VfsUtil.loadText(getVirtualFile(file)));
      EditorInfo editorInfo = new EditorInfo(text);
      editorTextField.addNotify(); // initialize editor
      myEditor = editorTextField.getEditor();
      myEditorsToRelease.add(myEditor);
      editorTextField.setText(editorInfo.getNewFileText());
      editorInfo.applyToEditor(myEditor);

      if (myNeedImportReferenceAtCursor) {
        DaemonCodeAnalyzer.getInstance(myProject).autoImportReferenceAtCursor(myEditor, myFile);
      }
      complete();
    }
    finally {
      if (editorTextField != null) editorTextField.removeNotify(); // dispose editor
    }
  }

  private static void assertContains(LookupElement[] items, boolean contains, String... expected) {
    Collection<String> c = new HashSet<String>(Arrays.asList(expected));
    for (LookupElement item : items) {
      final String s = item.getLookupString();
      final boolean removed = c.remove(s);
      if (!contains) {
        assertTrue("'" + s + "' is not expected to be part of completion list", !removed);
      }
    }
    if (contains) {
      assertTrue("Items [" + toString(c, ",") + "] are expected to be part of completion list", c.isEmpty());
    }
  }

  protected JSClass createFakeClass() {
    return JSPsiImplUtils.findClass((JSFile)JSChangeUtil
      .createJSTreeFromText(myProject, "package {class Foo { function a() {}} }", JavaScriptSupportLoader.ECMA_SCRIPT_L4)
      .getPsi().getContainingFile());
  }


}
