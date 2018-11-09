// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.base;

import com.intellij.codeInsight.CodeInsightTestCase;
import com.intellij.codeInsight.actions.BaseCodeInsightAction;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiDocumentManager;

public abstract class BaseFlexRefactoringTestCase extends CodeInsightTestCase {

  protected void resetInjectedEditor(Editor injectedEditor) {
    if (injectedEditor instanceof EditorWindow) {
      myEditor = ((EditorWindow)injectedEditor).getDelegate();
      myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    }
  }

  protected Editor setupInjectedEditor() {
    Editor injectedEditor = BaseCodeInsightAction.getInjectedEditor(myProject, myEditor);
    if (injectedEditor != null) {
      myEditor = injectedEditor;
      myFile = PsiDocumentManager.getInstance(myProject).getPsiFile(myEditor.getDocument());
    }
    return injectedEditor;
  }

  protected String getExtension() {
    return "js";
  }
}
