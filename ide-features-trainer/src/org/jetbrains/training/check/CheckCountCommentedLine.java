package org.jetbrains.training.check;

import com.intellij.ide.IdeTooltipManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.tree.java.IJavaElementType;
import com.intellij.psi.util.PsiUtil;
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
        //get count of commented lines
//        System.out.println("check before works");
        countComments = countCommentedLines();
    }

    @Override
    public boolean check() {
        return countCommentedLines() == countComments - 1;
    }

    public int countCommentedLines(){

        final VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(eduEditor.getEditor().getDocument());
        final PsiElement psiElement = PsiManager.getInstance(project).findViewProvider(virtualFile).findElementAt(0);
        ASTNode astNode = psiElement.getNode();
        while (astNode.getTreeParent() != null) {
            astNode = astNode.getTreeParent();
        }
        return calc(astNode.getPsi());
    }

    private int calc(PsiElement psiElement){

        if (psiElement.getNode().getElementType() == IJavaElementType.find((short) 381 )) return 1;
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
