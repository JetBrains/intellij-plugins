package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularUiRouterViewsIndex;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 2/11/2016.
 */
public class AngularJSUiRouterViewReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    final PsiElement identifier = element instanceof JSProperty ? ((JSProperty)element).getNameIdentifier() : element;
    return new PsiReference[] {new AngularJSUiRouterViewReference(identifier)};
  }

  private static class AngularJSUiRouterViewReference extends AngularPolyReferenceBase<PsiElement> {
    public AngularJSUiRouterViewReference(PsiElement element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getViewName() {
      final String text = StringUtil.unquoteString(getCanonicalText());
      final int idx = text.indexOf('@');
      if (idx >= 0) return text.substring(0, idx);
      return text;
    }

    @NotNull
    public ResolveResult[] resolveInner() {
      final String id = getViewName();
      final Condition<VirtualFile> filter = StringUtil.isEmptyOrSpaces(id) ? filterByTemplateUrl() : Condition.TRUE;
      return AngularIndexUtil.multiResolveAngularNamedDefinitionIndex(getElement().getProject(),
                                                                      AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, id, filter, false);
    }

    private Condition<VirtualFile> filterByTemplateUrl() {
      final PsiElement object = myElement.getParent() instanceof JSProperty ? ((JSProperty)myElement.getParent()).getValue() : null;
      if (object instanceof JSObjectLiteralExpression) {
        final JSProperty templateUrl = ((JSObjectLiteralExpression)object).findProperty("templateUrl");
        if (templateUrl != null && templateUrl.getValue() != null && templateUrl.getValue() instanceof JSLiteralExpression
            && ((JSLiteralExpression)templateUrl.getValue()).isQuotedLiteral()) {
          String templateUrlText = StringUtil.unquoteString(templateUrl.getValue().getText());
          if (!StringUtil.isEmptyOrSpaces(templateUrlText)) {
            templateUrlText = templateUrlText.trim().replace('\\', '/');
            final String finalTemplateUrlText = templateUrlText;
            return file -> {
              final String path = file.getPath();
              return path.endsWith(finalTemplateUrlText);
            };
          }
        }
      }
      return Condition.TRUE;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
