package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.resolve.VariantsProcessor;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSControllerReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[] { new AngularJSControllerReference((JSLiteralExpression)element) };
  }

  public static class AngularJSControllerReference extends AngularJSReferenceBase<JSLiteralExpression> {
    public AngularJSControllerReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
      return AngularIndexUtil.resolve(getElement().getProject(), AngularControllerIndex.INDEX_ID, getCanonicalText());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      final Collection<String> controllers = AngularIndexUtil.getAllKeys(AngularControllerIndex.INDEX_ID, getElement().getProject());
      final LookupElement[] result = new LookupElement[controllers.size()];
      int i = 0;
      for (String controller : controllers) {
        final LookupElement item = JSLookupUtilImpl.createPrioritizedLookupItem(null, controller,
                                                                                VariantsProcessor.LookupPriority.LOCAL_SCOPE_MAX_PRIORITY,
                                                                                false, false);
        result[i] = item;
        i++;
      }
      return result;
    }
  }
}
