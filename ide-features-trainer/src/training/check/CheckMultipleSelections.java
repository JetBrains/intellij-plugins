package training.check;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by karashevich on 21/08/15.
 */
public class CheckMultipleSelections implements Check{

    Project project;
    Editor editor;
    ArrayList<HtmlTag> htmlTagsTH;

    @Override
    public void set(Project project, Editor editor) {
        this.project = project;
        this.editor = editor;
        htmlTagsTH = new ArrayList<>();
    }

    @Override
    public void before() {
        final Document document = editor.getDocument();
        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);

        final Collection<HtmlTag> childrenOfType1 = PsiTreeUtil.findChildrenOfType(psiFile, HtmlTag.class);
        for (HtmlTag htmlTag: childrenOfType1){
            if (htmlTag.getName().equals("th")) htmlTagsTH.add(htmlTag);
        }
    }

    @Override
    public boolean check() {
        for (HtmlTag htmlTag : htmlTagsTH) {
            if (!htmlTag.getName().equals("td")) return false;
        }
        return true;

    }

    @Override
    public boolean listenAllKeys() {
        return false;
    }

}
