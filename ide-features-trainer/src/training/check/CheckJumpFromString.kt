/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class CheckJumpFromString implements Check {

    Project project;
    Editor editor;

    @Override
    public void set(Project project, Editor editor) {
        this.project = project;
        this.editor = editor;
    }

    @Override
    public void before() {

    }

    @Override
    public boolean check() {
        final CaretModel caretModel = editor.getCaretModel();
        final Document document = editor.getDocument();
        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        assert psiFile != null;
        final PsiElement elementAt = psiFile.findElementAt(caretModel.getOffset());
        assert elementAt != null;
        return (elementAt.getParent() != null) && (elementAt.getParent().getText().equals("String"));

    }

    @Override
    public boolean listenAllKeys() {
        return false;
    }

}
