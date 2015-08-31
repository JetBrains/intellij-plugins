package org.jetbrains.training.check;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.java.IJavaElementType;
import org.jetbrains.training.editor.EduEditor;

/**
 * Created by karashevich on 21/08/15.
 */
public class CheckSelectedLines implements Check{

    Project project;
    EduEditor eduEditor;
    int countComments;

    @Override
    public void set(Project project, EduEditor eduEditor) {
        this.project = project;
        this.eduEditor = eduEditor;
    }

    @Override
    public void before() {
    }

    @Override
    public boolean check() {
        return calc() >= 2;
    }


    private int calc(){

        final Editor editor = eduEditor.getEditor();
        final int lineStart = editor.getSelectionModel().getSelectionStartPosition().getLine();
        final int lineEnd = editor.getSelectionModel().getSelectionEndPosition().getLine();

        return lineEnd - lineStart;
    }
}
