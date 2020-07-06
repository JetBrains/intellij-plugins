// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.completion;

import com.intellij.codeInsight.EditorInfo;
import com.intellij.codeInsight.completion.JavaCompletionTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.refactoring.ui.JSEditorTextField;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.PsiTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.flex.completion.FlexCompletionInTextFieldBase.assertContains;

public class ActionScriptMultiModuleTextFieldCompletionTest extends JavaCompletionTestCase {
  protected static final String BASE_PATH = "/js2_completion/";

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myEditorsToRelease = new ArrayList<>();
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testComponentFromIndependentModule() {
    final Module module2 = doCreateRealModule("module2");
    final VirtualFile contentRoot =
      LocalFileSystem.getInstance().findFileByPath(getTestDataPath() + getBasePath() + "/" + getTestName(false) + "_module2");
    PsiTestUtil.addSourceRoot(module2, contentRoot);

    configureByFiles(null, getBasePath() + getTestName(false) + "_2.mxml");

    PsiFile fragment =
      JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.moduleScope(myModule), null, null, "").getPsiFile();
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, new String[]{"ComponentFromIndependentModule_2"}, new String[]{"C1"},
                             getBasePath() + getTestName(false) + ".txt");

    fragment =
      JSReferenceEditor.forClassName("", getProject(), null, GlobalSearchScope.moduleScope(module2), null, null, "").getPsiFile();
    checkTextFieldCompletion((JSExpressionCodeFragment)fragment, new String[]{"C1"}, new String[]{"ComponentFromIndependentModule_2"},
                             getBasePath() + getTestName(false) + ".txt");
  }


  private Collection<Editor> myEditorsToRelease;

  @Override
  protected void tearDown() throws Exception {
    try {
      for (Editor editor : myEditorsToRelease) {
        EditorFactory.getInstance().releaseEditor(editor);
      }
      myEditorsToRelease = null;
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  protected void checkTextFieldCompletion(JSExpressionCodeFragment fragment,
                                          String[] included,
                                          String[] excluded,
                                          String file) {
    try {
      final String text = StringUtil.convertLineSeparators(VfsUtilCore.loadText(findVirtualFile(file)));
      JSTestUtils.initJSIndexes(getProject());

      EditorInfo editorInfo = new EditorInfo(text);
      JSEditorTextField editorTextField = null;
      try {
        myFile = fragment;
        Document document = PsiDocumentManager.getInstance(myProject).getDocument(fragment);
        editorTextField = new JSEditorTextField(myProject, document);


        editorTextField.addNotify(); // initialize editor
        myEditor = editorTextField.getEditor();
        myEditorsToRelease.add(myEditor);
        editorTextField.setText(editorInfo.getNewFileText());
        editorInfo.applyToEditor(myEditor);

        complete();
      }
      finally {
        if (editorTextField != null) editorTextField.removeNotify(); // dispose editor
      }
    }
    catch (IOException e) {
      addSuppressedException(e);
    }
    assertContains(myItems, true, included);
    assertContains(myItems, false, excluded);
  }
}
