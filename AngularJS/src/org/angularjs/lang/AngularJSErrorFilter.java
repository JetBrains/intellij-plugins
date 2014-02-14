package org.angularjs.lang;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSErrorFilter extends HighlightErrorFilter {
  @Override
  public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement error) {
    final Project project = error.getProject();
    if ("CSS".equals(error.getLanguage().getID()) && PsiTreeUtil.getParentOfType(error, XmlAttribute.class) != null &&
        AngularIndexUtil.hasAngularJS(project)) {
      final PsiFile file = error.getContainingFile();

      PsiErrorElement nextError = error;
      while (nextError != null) {
        if (hasAngularInjectionAt(project, file, nextError.getTextOffset())) return false;
        nextError = PsiTreeUtil.getNextSiblingOfType(nextError, PsiErrorElement.class);
      }
    }
    return true;
  }

  private static boolean hasAngularInjectionAt(final Project project, final PsiFile file, final int offset) {
    final PsiElement injection =
      InjectedLanguageManager.getInstance(project).findInjectedElementAt(file, offset);
    return injection != null && injection.getContainingFile().getLanguage() == AngularJSLanguage.INSTANCE;
  }
}
