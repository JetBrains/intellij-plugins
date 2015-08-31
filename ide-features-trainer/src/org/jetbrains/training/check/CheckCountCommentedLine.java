package org.jetbrains.training.check;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.java.IJavaElementType;
import org.jetbrains.training.editor.EduEditor;

/**
 * Created by karashevich on 21/08/15.
 */
public class CheckCountCommentedLine implements Check{

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
        countComments = countCommentedLines();
    }

    @Override
    public boolean check() {
        return countCommentedLines() == countComments - 1;
    }

    public int countCommentedLines(){

        final PsiElement psiElement = PsiDocumentManager.getInstance(project).getPsiFile(eduEditor.getEditor().getDocument());
        ASTNode astNode = psiElement.getNode();
        while (astNode.getTreeParent() != null) {
            astNode = astNode.getTreeParent();
        }
        return calc(astNode.getPsi());
    }

    private int calc(PsiElement psiElement){

        if (psiElement.getNode().getElementType() == JavaTokenType.END_OF_LINE_COMMENT) return 1;
        else if(psiElement.getChildren().length == 0) return 0;
        else {
            int result = 0;
            for (PsiElement psiChild : psiElement.getChildren()) {
                result += calc(psiChild);
            }
            return result;
        }
    }
}
