package com.google.jstestdriver.idea.util;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    JSElement scopeElement = getNearestContainingScopeElement(jsReferenceExpression);
    if (scopeElement == null) {
      return null;
    }
    String refName = jsReferenceExpression.getReferencedName();
    if (refName == null || refName.isEmpty()) {
      return null;
    }
    List<JSStatement> statements = getTopLevelStatementsOf(scopeElement);
    int maxOffset = jsReferenceExpression.getTextRange().getStartOffset();
    JSExpression lastExpr = null;
    for (JSStatement statement : statements) {
      if (statement.getTextRange().getEndOffset() > maxOffset) {
        break;
      }
      {
        JSVarStatement varStmt = CastUtils.tryCast(statement, JSVarStatement.class);
        if (varStmt != null) {
          JSVariable[] vars = ObjectUtils.notNull(varStmt.getVariables(), JSVariable.EMPTY_ARRAY);
          for (JSVariable var : vars) {
            if (refName.equals(var.getQualifiedName())) {
              lastExpr = var.getInitializer();
            }
          }
        }
      }
      {
        JSExpressionStatement exprStmt = CastUtils.tryCast(statement, JSExpressionStatement.class);
        if (exprStmt != null) {
          JSAssignmentExpression assignmentExpr = CastUtils.tryCast(exprStmt.getExpression(), JSAssignmentExpression.class);
          if (assignmentExpr != null) {
            JSDefinitionExpression defExpr = CastUtils.tryCast(assignmentExpr.getLOperand(), JSDefinitionExpression.class);
            if (defExpr != null) {
              JSReferenceExpression refExpr = CastUtils.tryCast(defExpr.getExpression(), JSReferenceExpression.class);
              if (refExpr != null && refExpr.getQualifier() == null) {
                if (refName.equals(refExpr.getReferencedName())) {
                  lastExpr = assignmentExpr.getROperand();
                }
              }
            }
          }
        }
      }
    }
    return lastExpr;
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
  public static List<JSStatement> listStatementsInExecutionOrder(@NotNull JSFile jsFile) {
    List<JSStatement> jsElements = Lists.newArrayList();
    for (PsiElement psiElement : jsFile.getChildren()) {
      JSStatement statement = CastUtils.tryCast(psiElement, JSStatement.class);
      if (statement != null) {
        collectJsElementsInExecutionOrder(statement, jsElements);
      }
    }
    return jsElements;
  }

  private static void collectJsElementsInExecutionOrder(@NotNull JSStatement statement, @NotNull List<JSStatement> jsElements) {
    JSExpressionStatement jsExpressionStatement = CastUtils.tryCast(statement, JSExpressionStatement.class);
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
        JSSourceElement[] sourceElements = ObjectUtils.notNull(jsFunctionExpression.getBody(), JSSourceElement.EMPTY_ARRAY);
        for (JSSourceElement sourceElement : sourceElements) {
          if (sourceElement instanceof JSBlockStatement) {
            JSBlockStatement jsBlockStatement = (JSBlockStatement) sourceElement;
            for (JSStatement jsStatement : jsBlockStatement.getStatements()) {
              collectJsElementsInExecutionOrder(jsStatement, jsElements);
            }
          }
          else if (sourceElement instanceof JSStatement) {
            JSStatement childStatement = (JSStatement) sourceElement;
            collectJsElementsInExecutionOrder(childStatement, jsElements);
          }
        }
        return;
      }
    }
    jsElements.add(statement);
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

  public static boolean isResolvedToFunction(@NotNull PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    for (ResolveResult resolveResult : resolveResults) {
      boolean resolvedCorrectly = isResolveResultFunction(resolveResult);
      if (resolvedCorrectly) {
        return true;
      }
    }
    return false;
  }

  private static boolean isResolveResultFunction(@NotNull ResolveResult resolveResult) {
    PsiElement resolvedElement = resolveResult.getElement();
    if (resolvedElement == null || !resolveResult.isValidResult()) {
      return false;
    }
    if (resolvedElement instanceof JSNamedElementProxy) {
      JSNamedElementProxy proxy = (JSNamedElementProxy) resolvedElement;
      PsiElement element = proxy.getElement();
      return element != null && !(element instanceof PsiComment);
    }
    return !(resolvedElement instanceof PsiComment);
  }

  @NotNull
  public static List<JSStatement> listStatementsInExecutionOrderNextTo(@NotNull JSStatement stmt) {
    JSElement nearestScopeElement = getNearestContainingScopeElement(stmt);
    if (nearestScopeElement == null) {
      return Collections.emptyList();
    }
    List<JSStatement> statements = getTopLevelStatementsOf(nearestScopeElement);
    int minStartOffset = stmt.getTextRange().getEndOffset();
    for (Iterator<JSStatement> it = statements.iterator(); it.hasNext(); ) {
      JSStatement statement = it.next();
      if (minStartOffset > statement.getTextRange().getStartOffset()) {
        it.remove();
      }
    }
    return statements;
  }

  @Nullable
  private static JSElement getNearestContainingScopeElement(@NotNull JSElement element) {
    PsiElement parent = element.getParent();
    while (parent != null) {
      if (parent instanceof JSFunction) {
        return (JSFunction) parent;
      }
      if (parent instanceof PsiFile) {
        return CastUtils.tryCast(parent, JSFile.class);
      }
      PsiElement newParent = parent.getParent();
      if (newParent == parent) {
        break;
      }
      parent = newParent;
    }
    return null;
  }

  @NotNull
  private static List<JSStatement> getTopLevelStatementsOf(@NotNull JSElement parent) {
    JSSourceElement[] elements = JSSourceElement.EMPTY_ARRAY;
    if (parent instanceof JSFile) {
      JSFile jsFile = (JSFile) parent;
      elements = jsFile.getStatements();
    }
    else if (parent instanceof JSFunction) {
      JSFunction f = (JSFunction) parent;
      elements = f.getBody();
    }
    List<JSStatement> out = new ArrayList<JSStatement>();
    for (JSSourceElement element : elements) {
      if (element instanceof JSStatement) {
        JSStatement statement = (JSStatement) element;
        collectJsElementsInExecutionOrder(statement, out);
      }
    }
    return out;
  }

}
