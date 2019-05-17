// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2ReferencesContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(STYLE_PATTERN, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        return new AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet(element).getAllReferences();
      }
    });
    registrar.registerReferenceProvider(VIEW_CHILD_PATTERN, new Angular2ViewChildReferencesProvider());
    registrar.registerReferenceProvider(PIPE_NAME_PATTERN, new Angular2PipeNameReferencesProvider());
    registrar.registerReferenceProvider(SELECTOR_PATTERN, new Angular2SelectorReferencesProvider());
    registrar.registerReferenceProvider(NG_CONTENT_SELECTOR_PATTERN, new Angular2SelectorReferencesProvider());
  }

  private static final PsiElementPattern.Capture<Angular2HtmlNgContentSelector> NG_CONTENT_SELECTOR_PATTERN =
    PlatformPatterns.psiElement(Angular2HtmlNgContentSelector.class);
  private static final PsiElementPattern.Capture<JSLiteralExpression> SELECTOR_PATTERN =
    ng2LiteralInDecoratorProperty(SELECTOR_PROP, COMPONENT_DEC, DIRECTIVE_DEC);
  private static final PsiElementPattern.Capture<JSLiteralExpression> PIPE_NAME_PATTERN =
    ng2LiteralInDecoratorProperty(NAME_PROP, PIPE_DEC);
  private static final PsiElementPattern.Capture<JSLiteralExpression> VIEW_CHILD_PATTERN =
    PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final JSLiteralExpression literal = (JSLiteralExpression)element;
          if (literal.isQuotedLiteral() && literal.getParent() instanceof JSArgumentList) {
            final JSCallExpression call = ObjectUtils.tryCast(literal.getParent().getParent(), JSCallExpression.class);
            if (call != null && call.getParent() instanceof ES6Decorator) {
              JSReferenceExpression ref = ObjectUtils.tryCast(call.getMethodExpression(), JSReferenceExpression.class);
              return ref != null
                     && VIEW_CHILD_DEC.equals(ref.getReferenceName())
                     && Angular2LangUtil.isAngular2Context(literal);
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
  private static final PsiElementPattern.Capture<JSLiteralExpression> STYLE_PATTERN =
    PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        if (element instanceof JSLiteralExpression) {
          final JSLiteralExpression literal = (JSLiteralExpression)element;
          if (literal.isQuotedLiteral()) {
            if ((literal.getParent() instanceof JSArrayLiteralExpression)) {
              final JSProperty property = ObjectUtils.tryCast(literal.getParent().getParent(), JSProperty.class);
              if (property != null && STYLE_URLS_PROP.equals((property).getName())) {
                return Angular2LangUtil.isAngular2Context(literal);
              }
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

  private static PsiElementPattern.Capture<JSLiteralExpression> ng2LiteralInDecoratorProperty(final String propertyName,
                                                                                              final String... decoratorNames) {
    return PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        return element instanceof PsiElement
               && isLiteralInNgDecorator((PsiElement)element, propertyName, decoratorNames)
               && Angular2LangUtil.isAngular2Context((PsiElement)element);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }
}
