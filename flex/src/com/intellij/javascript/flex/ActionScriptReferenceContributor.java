package com.intellij.javascript.flex;

import com.intellij.javascript.flex.css.CssClassValueReference;
import com.intellij.javascript.flex.css.CssPropertyValueReference;
import com.intellij.javascript.flex.css.FlexCssUtil;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ActionScriptReferenceContributor extends PsiReferenceContributor {

  public static final String SET_STYLE_METHOD_NAME = "setStyle";

  @Override
  public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, PsiElement context) {
        PsiElement parent = ((JSLiteralExpression)element).getParent();
        if (parent instanceof JSArgumentList) {
          JSExpression[] arguments = ((JSArgumentList)parent).getArguments();
          if (arguments.length > 0 && arguments[0] == element) {
            parent = parent.getParent();
            if (parent instanceof JSCallExpression) {
              JSExpression invokedMethod = ((JSCallExpression)parent).getMethodExpression();
              if (invokedMethod instanceof JSReferenceExpression) {
                String methodName = ((JSReferenceExpression)invokedMethod).getReferencedName();
                if (SET_STYLE_METHOD_NAME.equals(methodName)) {
                  Module module = findModuleForPsiElement(parent);
                  return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
                }
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
    })), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String value = element.getText();
        if (FlexCssUtil.inQuotes(value)) {
          return new PsiReference[]{new CssPropertyValueReference(element)};
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });

    registrar.registerReferenceProvider(psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, PsiElement context) {
        PsiElement parent = ((JSLiteralExpression)element).getParent();
        if (parent instanceof JSAssignmentExpression) {
          PsiElement assignee = parent.getChildren()[0];
          if (assignee instanceof JSDefinitionExpression) {
            JSExpression expression = ((JSDefinitionExpression)assignee).getExpression();
            if (expression instanceof JSReferenceExpression) {
              String refName = ((JSReferenceExpression)expression).getReferencedName();
              if (refName != null && FlexCssUtil.isStyleNameProperty(refName)) {
                Module module = findModuleForPsiElement(parent);
                return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
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
    })), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String value = element.getText();
        if (FlexCssUtil.inQuotes(value)) {
          return new PsiReference[]{new CssClassValueReference(element)};
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });

    registrar.registerReferenceProvider(psiElement(JSLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
      @Override
      public boolean isAcceptable(Object element, PsiElement context) {
        PsiElement parent = ((JSLiteralExpression)element).getParent();
        if (parent instanceof JSArgumentList) {
          final JSExpression[] arguments = ((JSArgumentList)parent).getArguments();
          if (arguments.length > 0 && arguments[0] == element) {
            parent = parent.getParent();
            if (parent instanceof JSCallExpression) {
              final JSExpression invokedMethod = ((JSCallExpression)parent).getMethodExpression();
              if (invokedMethod instanceof JSReferenceExpression) {
                final String methodName = ((JSReferenceExpression)invokedMethod).getReferencedName();
                if (methodName != null && FlexCssUtil.isStyleNameMethod(methodName)) {
                  Module module = findModuleForPsiElement(parent);
                  return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
                }
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
    })), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String value = element.getText();
        if (FlexCssUtil.inQuotes(value)) {
          return new PsiReference[]{new CssClassValueReference(element)};
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });

    registrar.registerReferenceProvider(psiElement(JSLiteralExpression.class), new FlexPropertyReferenceProvider());
    registrar.registerReferenceProvider(psiElement(JSAttributeNameValuePair.class), new FlexAttributeReferenceProvider());
  }
}
