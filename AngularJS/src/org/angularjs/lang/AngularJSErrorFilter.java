package org.angularjs.lang;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiErrorElement;
import org.angularjs.editor.AngularJSBracesUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSErrorFilter extends HighlightErrorFilter {
  @Override
  public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement error) {
    final Project project = error.getProject();
    if ("CSS".equals(error.getLanguage().getID()) && AngularIndexUtil.hasAngularJS(project)) {
      final String textNearError = error.getParent().getText().substring(error.getStartOffsetInParent());
      return textNearError.startsWith(AngularJSBracesUtil.getInjectionEnd(project));
    }
    return true;
  }
}
