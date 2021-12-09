// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public final class FlexPropertyReferenceProvider extends PsiReferenceProvider {
  private static final Set<String> ourMethodsWithPropertyReferences =
    Set.of("findResourceBundleWithResource", "getString", "getObject", "getClass", "getStringArray", "getNumber", "getInt",
           "getUint", "getBoolean");

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    PsiElement parent = element.getParent();

    JSReferenceExpression invokedMethod = JSUtils.getMethodNameIfInsideCall(parent);

    List<PsiReference> result = new ArrayList<>();
    if (invokedMethod != null) {
      String invokedMethodName;
      boolean justResourceBundleRef = false;
      PsiElement qualifier;

      if ((ourMethodsWithPropertyReferences.contains(invokedMethodName = (invokedMethod.getReferencedName())) ||
           (justResourceBundleRef = "getResourceBundle".equals(invokedMethodName))) &&
          ((qualifier = invokedMethod.getQualifier()) instanceof JSReferenceExpression ||
           (qualifier instanceof JSCallExpression &&
            ((JSCallExpression)qualifier).getMethodExpression() instanceof JSReferenceExpression))) {
        final JSExpression[] args = ((JSArgumentList)parent).getArguments();

        boolean propertyRef = false;
        boolean bundleRef = false;

        if (justResourceBundleRef) {
          bundleRef = args.length > 1 && args[1] == element;
        }
        else {
          propertyRef = args.length > 1 && args[1] == element;
          bundleRef = args.length > 0 && args[0] == element;
          if (bundleRef && args.length == 1) { // ResourceBundle.getString deprecated, without bundle name
            bundleRef = false;
            propertyRef = true;
          }
        }

        boolean isSoft = true;
        if (propertyRef || bundleRef) {
          PsiElement resolved = invokedMethod.resolve();

          if (resolved instanceof JSFunction) {
            PsiElement parentClass = JSResolveUtil.findParent(resolved);
            if (parentClass instanceof JSClass) {
              String name = ((JSClass)parentClass).getName();
              isSoft = name == null || (!name.contains("ResourceManager") && !name.contains("ResourceBundle"));
            }
          }
        }

        if (propertyRef) {
          FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl> provider =
            isSoft ? ourSoftPropertyInfoProvider : ourPropertyInfoProvider;

          if (args.length > 1 && !isSoft) {
            JSExpression bundleExpression = args[0];
            if (bundleExpression instanceof JSReferenceExpression) {
              PsiElement resolved = ((JSReferenceExpression)bundleExpression).resolve();
              if (resolved instanceof JSVariable) {
                bundleExpression = ((JSVariable)resolved).getInitializer();
              }
            }
            if (bundleExpression instanceof JSLiteralExpression) {
              final String expressionValue = ((JSLiteralExpression)bundleExpression).getStringValue();
              if (expressionValue != null) {
                provider = new FlexPropertiesSupport.PropertyReferenceInfoProvider<>() {
                  @Override
                  public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
                    return getValueRange(element);
                  }

                  @Override
                  public String getBundleName(JSLiteralExpressionImpl element) {
                    return expressionValue;
                  }

                  @Override
                  public boolean isSoft(JSLiteralExpressionImpl element) {
                    return false;
                  }
                };
              }
            }
          }
          Collections.addAll(result, FlexPropertiesSupport.getPropertyReferences((JSLiteralExpressionImpl)element, provider));
        }
        else if (bundleRef) {
          PsiReference[] reference = FlexPropertiesSupport.getResourceBundleReference((JSLiteralExpressionImpl)element,
                                                                                  isSoft
                                                                                  ? ourSoftBundleInfoProvider
                                                                                  : ourBundleInfoProvider);
          Collections.addAll(result, reference);
        }
      }
    }
    return result.toArray(PsiReference.EMPTY_ARRAY);
  }

  private static final FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl> ourPropertyInfoProvider =
    new FlexPropertiesSupport.PropertyReferenceInfoProvider<>() {
      @Override
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      @Override
      public String getBundleName(JSLiteralExpressionImpl element) {
        return null;
      }

      @Override
      public boolean isSoft(JSLiteralExpressionImpl element) {
        return false;
      }
    };

  private static final FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl> ourSoftPropertyInfoProvider =
    new FlexPropertiesSupport.PropertyReferenceInfoProvider<>() {
      @Override
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      @Override
      public String getBundleName(JSLiteralExpressionImpl element) {
        return null;
      }

      @Override
      public boolean isSoft(JSLiteralExpressionImpl element) {
        return true;
      }
    };


  private static final FlexPropertiesSupport.BundleReferenceInfoProvider<JSLiteralExpressionImpl> ourBundleInfoProvider =
    new FlexPropertiesSupport.BundleReferenceInfoProvider<>() {
      @Override
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      @Override
      public boolean isSoft(JSLiteralExpressionImpl element) {
        return false;
      }
    };

  private static final FlexPropertiesSupport.BundleReferenceInfoProvider<JSLiteralExpressionImpl> ourSoftBundleInfoProvider =
    new FlexPropertiesSupport.BundleReferenceInfoProvider<>() {
      @Override
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      @Override
      public boolean isSoft(JSLiteralExpressionImpl element) {
        return true;
      }
    };

  private static TextRange getValueRange(JSLiteralExpressionImpl element) {
    int textLength = element.getTextLength();
    if (textLength < 2) return null;
    return new TextRange(1, textLength - 1);
  }

}
