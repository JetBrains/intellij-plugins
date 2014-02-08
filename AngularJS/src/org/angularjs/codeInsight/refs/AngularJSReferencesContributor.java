package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferencesContributor extends PsiReferenceContributor {

  private static final PsiElementPattern.Capture<JSLiteralExpression> TEMPLATE_PATTERN = literalInProperty("templateUrl");
  private static final PsiElementPattern.Capture<JSLiteralExpression> CONTROLLER_PATTERN = literalInProperty("controller");

  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(TEMPLATE_PATTERN, new AngularJSTemplateReferencesProvider());
    registrar.registerReferenceProvider(CONTROLLER_PATTERN, new AngularJSControllerReferencesProvider());
  }

  private static PsiElementPattern.Capture<JSLiteralExpression> literalInProperty(final String propertyName) {
    return PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final JSLiteralExpression literal = (JSLiteralExpression)element;
          if (literal.isQuotedLiteral()) {
            final PsiElement parent = literal.getParent();
            if (parent instanceof JSProperty && propertyName.equals(((JSProperty)parent).getName())) {
              return AngularIndexUtil.hasAngularJS(literal.getProject());
            }
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }
}
