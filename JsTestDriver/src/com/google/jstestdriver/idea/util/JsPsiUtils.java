package com.google.jstestdriver.idea.util;

import com.google.common.collect.Lists;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsPsiUtils {

  private JsPsiUtils() {}

  @Nullable
  public static String extractStringValue(JSExpression jsExpression) {
    {
      JSLiteralExpression jsLiteralExpression = CastUtils.tryCast(jsExpression, JSLiteralExpression.class);
      if (jsLiteralExpression != null && jsLiteralExpression.isQuotedLiteral()) {
        return StringUtil.stripQuotesAroundValue(StringUtil.notNullize(jsLiteralExpression.getText()));
      }
    }
    {
      JSBinaryExpression jsBinaryExpression = CastUtils.tryCast(jsExpression, JSBinaryExpression.class);
      if (jsBinaryExpression != null) {
        IElementType operationType = jsBinaryExpression.getOperationSign();
        if (operationType == JSTokenTypes.PLUS) {
          String lOperand = extractStringValue(jsBinaryExpression.getLOperand());
          String rOperand = extractStringValue(jsBinaryExpression.getROperand());
          if (lOperand != null && rOperand != null) {
            return lOperand + rOperand;
          }
        }
      }
    }
    {
      JSReferenceExpression jsReferenceExpression = CastUtils.tryCast(jsExpression, JSReferenceExpression.class);
      if (jsReferenceExpression != null) {
        JSExpression initializer = extractInitExpression(jsReferenceExpression);
        if (initializer != null) {
          return extractStringValue(initializer);
        }
      }
    }
    return null;
  }

  @Nullable
  private static JSExpression extractInitExpression(@NotNull JSReferenceExpression jsReferenceExpression) {
    PsiElement resolved = resolveUniquely(jsReferenceExpression);
    JSVariable jsVariable = CastUtils.tryCast(resolved, JSVariable.class);
    if (jsVariable != null) {
      JSExpression initializer = jsVariable.getInitializer();
      if (initializer != null) {
        return initializer;
      }
    }
    return null;
  }

  @Nullable
  public static PsiElement resolveUniquely(@NotNull PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    PsiElement candidate = null;
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement resolvedElement = resolveResult.getElement();
      if (resolvedElement != null && resolveResult.isValidResult()) {
        if (candidate != null) {
          return null;
        }
        candidate = resolvedElement;
      }
    }
    return candidate;
  }

  @Nullable
  public static JSObjectLiteralExpression extractObjectLiteralExpression(JSExpression expression) {
    {
      JSObjectLiteralExpression jsObjectLiteralExpression = CastUtils.tryCast(expression, JSObjectLiteralExpression.class);
      if (jsObjectLiteralExpression != null) {
        return jsObjectLiteralExpression;
      }
    }
    {
      JSReferenceExpression jsReferenceExpression = CastUtils.tryCast(expression, JSReferenceExpression.class);
      if (jsReferenceExpression != null) {
        JSExpression initializer = extractInitExpression(jsReferenceExpression);
        if (initializer != null) {
          return extractObjectLiteralExpression(initializer);
        }
      }
    }
    return null;
  }

  @Nullable
  public static String extractNumberLiteral(JSExpression jsExpression) {
    {
      JSLiteralExpression jsLiteralExpression = CastUtils.tryCast(jsExpression, JSLiteralExpression.class);
      if (jsLiteralExpression != null && jsLiteralExpression.isNumericLiteral()) {
        return jsLiteralExpression.getText();
      }
    }
    {
      JSReferenceExpression jsReferenceExpression = CastUtils.tryCast(jsExpression, JSReferenceExpression.class);
      if (jsReferenceExpression != null) {
        JSExpression initializer = extractInitExpression(jsReferenceExpression);
        if (initializer != null) {
          return extractNumberLiteral(initializer);
        }
      }
    }
    return null;
  }

  @Nullable
  public static JSFunctionExpression extractFunctionExpression(JSExpression expression) {
    {
      JSFunctionExpression jsFunctionExpression = CastUtils.tryCast(expression, JSFunctionExpression.class);
      if (jsFunctionExpression != null) {
        return jsFunctionExpression;
      }
    }
    {
      JSReferenceExpression jsReferenceExpression = CastUtils.tryCast(expression, JSReferenceExpression.class);
      if (jsReferenceExpression != null) {
        JSExpression initializer = extractInitExpression(jsReferenceExpression);
        if (initializer != null) {
          return extractFunctionExpression(initializer);
        }
      }
    }
    return null;
  }

  @NotNull
  public static List<JSElement> listJsElementsInExecutionOrder(JSFile jsFile) {
    List<JSElement> jsElements = Lists.newArrayList();
    for (PsiElement psiElement : jsFile.getChildren()) {
      JSElement jsElement = CastUtils.tryCast(psiElement, JSElement.class);
      if (jsElement != null) {
        collectJsElementsInExecutionOrder(jsElement, jsElements);
      }
    }
    return jsElements;
  }

  private static void collectJsElementsInExecutionOrder(JSElement jsElement, List<JSElement> jsElements) {
    JSExpressionStatement jsExpressionStatement = CastUtils.tryCast(jsElement, JSExpressionStatement.class);
    if (jsExpressionStatement != null) {
      JSFunctionExpression jsFunctionExpression = null;
      {
        JSCallExpression jsCallExpression = CastUtils.tryCast(jsExpressionStatement.getExpression(), JSCallExpression.class);
        if (jsCallExpression != null) {
          JSParenthesizedExpression jsParenthesizedExpression = CastUtils.tryCast(jsCallExpression.getMethodExpression(), JSParenthesizedExpression.class);
          if (jsParenthesizedExpression != null) {
            jsFunctionExpression = CastUtils.tryCast(jsParenthesizedExpression.getInnerExpression(), JSFunctionExpression.class);
          }
        }
      }
      {
        JSParenthesizedExpression jsParenthesizedExpression = CastUtils.tryCast(jsExpressionStatement.getExpression(), JSParenthesizedExpression.class);
        if (jsParenthesizedExpression != null) {
          JSCallExpression jsCallExpression = CastUtils.tryCast(jsParenthesizedExpression.getInnerExpression(), JSCallExpression.class);
          if (jsCallExpression != null) {
            jsFunctionExpression = CastUtils.tryCast(jsCallExpression.getMethodExpression(), JSFunctionExpression.class);
          }
        }
      }
      if (jsFunctionExpression != null) {
        JSSourceElement[] jsSourceElements = ObjectUtils.notNull(jsFunctionExpression.getBody(), JSSourceElement.EMPTY_ARRAY);
        for (JSSourceElement jsSourceElement : jsSourceElements) {
          if (jsSourceElement instanceof JSBlockStatement) {
            JSBlockStatement jsBlockStatement = (JSBlockStatement) jsSourceElement;
            for (JSStatement jsStatement : jsBlockStatement.getStatements()) {
              collectJsElementsInExecutionOrder(jsStatement, jsElements);
            }
          } else {
            collectJsElementsInExecutionOrder(jsSourceElement, jsElements);
          }
        }
        return;
      }
    }
    jsElements.add(jsElement);
  }

  public static boolean isStringElement(JSExpression jsExpression) {
    return extractStringValue(jsExpression) != null;
  }

  public static boolean isObjectElement(JSExpression jsExpression) {
    return extractObjectLiteralExpression(jsExpression) != null;
  }

  public static boolean isFunctionExpressionElement(JSExpression jsExpression) {
    return extractFunctionExpression(jsExpression) != null;
  }

  public static boolean isNumberElement(JSExpression jsExpression) {
    return extractNumberLiteral(jsExpression) != null;
  }

}
