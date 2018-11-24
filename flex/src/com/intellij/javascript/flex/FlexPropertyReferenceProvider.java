package com.intellij.javascript.flex;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author yole
 */
public class FlexPropertyReferenceProvider extends PsiReferenceProvider {
  private static final Set<String> ourMethodsWithPropertyReferences = new THashSet<>(
    Arrays.asList("findResourceBundleWithResource", "getString", "getObject", "getClass", "getStringArray", "getNumber", "getInt",
                  "getUint", "getBoolean"));

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
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
              isSoft = name == null || (name.indexOf("ResourceManager") == -1 && name.indexOf("ResourceBundle") == -1);
            }
          }
        }

        if (propertyRef) {
          FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl> provider =
            isSoft ? ourSoftPropertyInfoProvider : ourPropertyInfoProvider;

          if (args.length > 1 && !isSoft && args[0] instanceof JSLiteralExpression) {
            final String myText = args[0].getText();

            provider = new FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl>() {
              public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
                return getValueRange(element);
              }

              public String getBundleName(JSLiteralExpressionImpl element) {
                return StringUtil.stripQuotesAroundValue(myText);
              }

              public boolean isSoft(JSLiteralExpressionImpl element) {
                return false;
              }
            };
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
    return result.toArray(new PsiReference[result.size()]);
  }

  private static final FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl> ourPropertyInfoProvider =
    new FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl>() {
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      public String getBundleName(JSLiteralExpressionImpl element) {
        return null;
      }

      public boolean isSoft(JSLiteralExpressionImpl element) {
        return false;
      }
    };

  private static final FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl> ourSoftPropertyInfoProvider =
    new FlexPropertiesSupport.PropertyReferenceInfoProvider<JSLiteralExpressionImpl>() {
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      public String getBundleName(JSLiteralExpressionImpl element) {
        return null;
      }

      public boolean isSoft(JSLiteralExpressionImpl element) {
        return true;
      }
    };


  private static final FlexPropertiesSupport.BundleReferenceInfoProvider<JSLiteralExpressionImpl> ourBundleInfoProvider =
    new FlexPropertiesSupport.BundleReferenceInfoProvider<JSLiteralExpressionImpl>() {
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

      public boolean isSoft(JSLiteralExpressionImpl element) {
        return false;
      }
    };

  private static final FlexPropertiesSupport.BundleReferenceInfoProvider<JSLiteralExpressionImpl> ourSoftBundleInfoProvider =
    new FlexPropertiesSupport.BundleReferenceInfoProvider<JSLiteralExpressionImpl>() {
      public TextRange getReferenceRange(JSLiteralExpressionImpl element) {
        return getValueRange(element);
      }

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
