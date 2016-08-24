package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTemplateReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return ArrayUtil.mergeArrays(new Angular2SoftFileReferenceSet(element).getAllReferences(),
                                 new PsiReference[] {new AngularJSTemplateCacheReference((JSLiteralExpression)element)});
  }

  static class Angular2SoftFileReferenceSet extends SoftFileReferenceSet {
    public Angular2SoftFileReferenceSet(PsiElement element) {
      super(element);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> getDefaultContexts() {
      final PsiElement element = getElement();
      final Project project = element.getProject();
      if (AngularIndexUtil.hasAngularJS2(project)) {
        final PsiFile file = element.getContainingFile().getOriginalFile();
        return Collections.singleton(file.getContainingDirectory());
      }

      return super.getDefaultContexts();
    }
  }
}
