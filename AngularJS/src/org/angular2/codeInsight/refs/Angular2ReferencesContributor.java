// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.util.ProcessingContext;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.codeInsight.refs.AngularJSTemplateReferencesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2ReferencesContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(STYLE_PATTERN, new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        return new AngularJSTemplateReferencesProvider.Angular2SoftFileReferenceSet(element).getAllReferences();
      }
    });
    registrar.registerReferenceProvider(VIEW_CHILD_PATTERN, new Angular2ViewChildReferencesProvider());
    registrar.registerReferenceProvider(PIPE_NAME_PATTERN, new Angular2PipeNameReferencesProvider());
  }

  private static final PsiElementPattern.Capture<JSLiteralExpression> PIPE_NAME_PATTERN =
    ng2LiteralInDecoratorProperty(NAME_PROP, PIPE_DEC);
  private static final PsiElementPattern.Capture<JSLiteralExpression> VIEW_CHILD_PATTERN =
    PlatformPatterns.psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, @Nullable PsiElement context) {
        return Optional.ofNullable(tryCast(element, JSLiteralExpression.class))
          .filter(literal -> literal.isQuotedLiteral())
          .map(literal -> tryCast(literal.getParent(), JSArgumentList.class))
          .map(list -> tryCast(list.getParent(), JSCallExpression.class))
          .map(call -> tryCast(call.getParent(), ES6Decorator.class))
          .map(decorator -> isAngularEntityDecorator(decorator, VIEW_CHILD_DEC, VIEW_CHILDREN_DEC))
          .orElse(false);
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
              final JSProperty property = tryCast(literal.getParent().getParent(), JSProperty.class);
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
               && isLiteralInNgDecorator((PsiElement)element, propertyName, decoratorNames);
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));
  }
}
