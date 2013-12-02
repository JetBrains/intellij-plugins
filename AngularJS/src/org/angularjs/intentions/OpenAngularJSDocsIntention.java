package org.angularjs.intentions;

import com.intellij.codeInsight.intention.AbstractIntentionAction;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class OpenAngularJSDocsIntention extends AbstractIntentionAction implements LowPriorityAction {
  @NotNull
  @Override
  public String getText() {
    return "Open Angular Docs";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
    final PsiElement element = PsiUtilBase.getElementAtCaret(editor);

    return element instanceof XmlTokenImpl && element.getText().startsWith("ng");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
    final PsiElement element = PsiUtilBase.getElementAtCaret(editor);
    assert element != null;
    String text = element.getText();
    String[] words = text.split("-");
    StringBuilder name = new StringBuilder("http://docs.angularjs.org/api/ng.directive:");
    for (String word : words) {
      name.append(word.equals(words[0]) ? word : StringUtil.capitalize(word));
    }
    BrowserUtil.open(name.toString());
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
