package org.angularjs.codeInsight.refs;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferencesContributor extends PsiReferenceContributor {
  private static final PsiElementPattern.Capture<JSLiteralExpression> TEMPLATE_PATTERN = literalInProperty("templateUrl");
  private static final PsiElementPattern.Capture<JSLiteralExpression> CONTROLLER_PATTERN = literalInProperty("controller");
  private static final PsiElementPattern.Capture<JSLiteralExpression> NG_INCLUDE_PATTERN =
    PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final JSLiteralExpression literal = (JSLiteralExpression)element;
          if (literal.isQuotedLiteral()) {
            final PsiElement original = CompletionUtil.getOriginalOrSelf(literal);
            final PsiLanguageInjectionHost host = InjectedLanguageUtil.findInjectionHost(original);
            if (host instanceof XmlAttributeValue) {
              final PsiElement parent = host.getParent();
              return parent instanceof XmlAttribute && "ng-include".equals(((XmlAttribute)parent).getName());
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

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    final AngularJSTemplateReferencesProvider templateProvider = new AngularJSTemplateReferencesProvider();
    registrar.registerReferenceProvider(TEMPLATE_PATTERN, templateProvider);
    registrar.registerReferenceProvider(NG_INCLUDE_PATTERN, templateProvider);
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
