package com.google.jstestdriver.idea.util;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsPsiUtils {

  private static final JSProperty[] EMPTY_ARRAY = new JSProperty[] {};

  private JsPsiUtils() {}

  @Nullable
  public static String extractStringValue(@Nullable JSExpression jsExpression) {
    if (jsExpression == null) {
      return null;
    }
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
    if (resolved == null) {
      return null;
    }
    JSVariable jsVariable = CastUtils.tryCast(resolved, JSVariable.class);
    if (jsVariable != null) {
      return jsVariable.getInitializer();
    }
    JSDefinitionExpression jsDefinitionExpression = CastUtils.tryCast(resolved, JSDefinitionExpression.class);
    if (jsDefinitionExpression != null) {
      JSAssignmentExpression jsAssignmentExpression = CastUtils.tryCast(resolved.getParent(), JSAssignmentExpression.class);
      if (jsAssignmentExpression != null) {
        return jsAssignmentExpression.getROperand();
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
  public static JSObjectLiteralExpression extractObjectLiteralExpression(@Nullable JSExpression expression) {
    if (expression == null) {
      return null;
    }
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
  public static String extractNumberLiteral(@Nullable JSExpression jsExpression) {
    if (jsExpression == null) {
      return null;
    }
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
  public static JSFunctionExpression extractFunctionExpression(@Nullable JSExpression expression) {
    if (expression == null) {
      return null;
    }
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
  public static List<JSElement> listJsElementsInExecutionOrder(@NotNull JSFile jsFile) {
    List<JSElement> jsElements = Lists.newArrayList();
    for (PsiElement psiElement : jsFile.getChildren()) {
      JSElement jsElement = CastUtils.tryCast(psiElement, JSElement.class);
      if (jsElement != null) {
        collectJsElementsInExecutionOrder(jsElement, jsElements);
      }
    }
    return jsElements;
  }

  private static void collectJsElementsInExecutionOrder(@NotNull JSElement jsElement, @NotNull List<JSElement> jsElements) {
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

  public static boolean isStringElement(@Nullable JSExpression jsExpression) {
    return extractStringValue(jsExpression) != null;
  }

  public static boolean isObjectElement(@Nullable JSExpression jsExpression) {
    return extractObjectLiteralExpression(jsExpression) != null;
  }

  public static boolean isFunctionExpressionElement(@Nullable JSExpression jsExpression) {
    return extractFunctionExpression(jsExpression) != null;
  }

  public static boolean isNumberElement(@Nullable JSExpression jsExpression) {
    return extractNumberLiteral(jsExpression) != null;
  }

  @Nullable
  public static JSCallExpression asCallExpressionStatement(JSElement element) {
    JSExpressionStatement expressionStatement = CastUtils.tryCast(element, JSExpressionStatement.class);
    if (expressionStatement != null) {
      return CastUtils.tryCast(expressionStatement.getExpression(), JSCallExpression.class);
    }
    return null;
  }

  @NotNull
  public static JSProperty[] getProperties(@NotNull JSObjectLiteralExpression objectLiteralExpression) {
    JSProperty[] properties = objectLiteralExpression.getProperties();
    if (properties == null) {
      properties = EMPTY_ARRAY;
    }
    int cnt = 0;
    for (JSProperty property : properties) {
      if (property != null) {
        cnt++;
      }
    }
    if (cnt < properties.length) {
      JSProperty[] a = new JSProperty[cnt];
      int id = 0;
      for (JSProperty property : properties) {
        if (property != null) {
          a[id] = property;
          id++;
        }
      }
      return a;
    }
    return properties;
  }

  public static boolean containsOffsetStrictly(@NotNull TextRange textRange, int offset) {
    return textRange.getStartOffset() < offset && offset < textRange.getEndOffset();
  }

  @NotNull
  public static JSExpression[] getArguments(@Nullable JSArgumentList argumentList) {
    JSExpression[] expressions = null;
    if (argumentList != null) {
      expressions = argumentList.getArguments();
    }
    if (expressions == null) {
      expressions = JSExpression.EMPTY_ARRAY;
    }
    return expressions;
  }

  @NotNull
  public static JSExpression[] getArguments(@NotNull JSCallExpression jsCallExpression) {
    return getArguments(jsCallExpression.getArgumentList());
  }

  @Nullable
  public static PsiElement getPropertyNamePsiElement(@NotNull JSProperty property) {
    return CastUtils.tryCast(property.getFirstChild(), LeafPsiElement.class);
  }

  @Nullable
  public static String getPropertyName(@NotNull JSProperty property) {
    PsiElement testMethodNameDeclaration = getPropertyNamePsiElement(property);
    if (testMethodNameDeclaration == null) {
      return null;
    }
    return StringUtil.stripQuotesAroundValue(testMethodNameDeclaration.getText());
  }

  public static boolean isElementOfType(@Nullable PsiElement psiElement, @NotNull IElementType type) {
    if (psiElement instanceof ASTNode) {
      ASTNode node = (ASTNode)psiElement;
      return node.getElementType() == type;
    }
    return false;
  }

  public static boolean isElementOfType(@Nullable PsiElement psiElement,
                                        @NotNull IElementType type1,
                                        @NotNull IElementType type2) {
    if (psiElement instanceof ASTNode) {
      ASTNode node = (ASTNode)psiElement;
      IElementType type = node.getElementType();
      return type == type1 || type == type2;
    }
    return false;
  }

  public static boolean isElementOfType(@Nullable PsiElement psiElement,
                                        @NotNull IElementType type1,
                                        @NotNull IElementType type2,
                                        @NotNull IElementType type3) {
    if (psiElement instanceof ASTNode) {
      ASTNode node = (ASTNode)psiElement;
      IElementType type = node.getElementType();
      return type == type1 || type == type2 || type == type3;
    }
    return false;
  }

  public static boolean isElementOfType(@Nullable PsiElement psiElement,
                                        @NotNull IElementType type1,
                                        @NotNull IElementType type2,
                                        @NotNull IElementType type3,
                                        @NotNull IElementType type4) {
    if (psiElement instanceof ASTNode) {
      ASTNode node = (ASTNode)psiElement;
      IElementType type = node.getElementType();
      return type == type1 || type == type2 || type == type3 || type == type4;
    }
    return false;
  }

  @Nullable
  public static PsiElement getFunctionLeftBrace(@Nullable JSFunction function) {
    if (function == null) {
      return null;
    }
    JSSourceElement[] jsSourceElements = ObjectUtils.notNull(function.getBody(), JSSourceElement.EMPTY_ARRAY);
    for (JSSourceElement jsSourceElement : jsSourceElements) {
      if (jsSourceElement instanceof JSBlockStatement) {
        JSBlockStatement jsBlockStatement = (JSBlockStatement) jsSourceElement;
        return jsBlockStatement.getFirstChild();
      }
    }
    return null;
  }

  @Nullable
  public static Document getDocument(@NotNull PsiElement element) {
    PsiFile psiFile = element.getContainingFile();
    if (psiFile == null) {
      return null;
    }
    return PsiDocumentManager.getInstance(element.getProject()).getDocument(psiFile);
  }
}
