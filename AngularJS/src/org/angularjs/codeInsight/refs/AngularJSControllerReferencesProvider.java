package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSControllerReferencesProvider extends PsiReferenceProvider {
  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new AngularJSControllerReference((JSLiteralExpression)element)};
  }

  public static class AngularJSControllerReference extends AngularJSReferenceBase<JSLiteralExpression> {
    public AngularJSControllerReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Override
    public @NotNull String getCanonicalText() {
      final String text = super.getCanonicalText();
      final int idx = text.indexOf(AngularJSIndexingHandler.AS_CONNECTOR_WITH_SPACES);
      if (idx > 0) {
        return text.substring(0, idx);
      }
      return text;
    }

    @Override
    public @Nullable PsiElement resolveInner() {
      return AngularIndexUtil.resolve(getElement().getProject(), AngularControllerIndex.KEY, getCanonicalText());
    }

    @Override
    public Object @NotNull [] getVariants() {
      final Collection<String> controllers = AngularIndexUtil.getAllKeys(AngularControllerIndex.KEY, getElement().getProject());
      final LookupElement[] result = new LookupElement[controllers.size()];
      int i = 0;
      for (String controller : controllers) {
        final LookupElement item = JSLookupUtilImpl.createPrioritizedLookupItem(null, controller,
                                                                                JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY
        );
        result[i] = item;
        i++;
      }
      return result;
    }
  }
}
