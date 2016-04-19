package training.check;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Collection;

/**
 * Created by karashevich on 21/08/15.
 */
public class CheckStatementCompletionIf implements Check{

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
        final Document document = editor.getDocument();
        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

        final Object[] psiForStatements = (Object[]) PsiTreeUtil.findChildrenOfType(psiFile, PsiForStatement.class).toArray();
        if (psiForStatements.length < 2) return false;

        final PsiForStatement psiForStatement = (PsiForStatement) psiForStatements[1];
        final PsiElement[] children = psiForStatement.getBody().getChildren()[0].getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiIfStatement) return true;
        }

        return false;
    }

    @Override
    public boolean listenAllKeys() {
        return false;
    }

}
