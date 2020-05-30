package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class ActionScriptExpectedTypeEvaluator extends ExpectedTypeEvaluator {
  public ActionScriptExpectedTypeEvaluator(PsiElement parent, JSExpectedTypeKind expectedTypeKind) {
    super(parent, expectedTypeKind);
  }

  @Override
  protected void findRestParameterExpectedType(JSParameterItem parameterItem) {
    if (!(parameterItem instanceof JSParameter)) {
      super.findRestParameterExpectedType(parameterItem);
      return;
    }

    final JSParameter param = (JSParameter)parameterItem;
    final JSFunction fun = param.getDeclaringFunction();
    if (fun == null) {
      super.findRestParameterExpectedType(parameterItem);
      return;
    }
    PsiElement element = JSResolveUtil.findParent(fun);

    JSType classType = element instanceof JSClass ?
                       ((JSClass)element).getJSType():
                       null;
    if (classType != null && JSTypeUtils.isActionScriptVectorType(classType)) {
      String name = fun.getName();
      JSType qualifiedExpressionType = null;

      PsiElement originalElement = JSTypeUtils.getScopeInOriginalTree(myGrandParent);
      if (originalElement == null) return;
      PsiElement originalElementParent = originalElement.getParent();
      if (!(originalElementParent instanceof JSCallExpression)) return;

      JSExpression methodExpression = ((JSCallExpression)originalElementParent).getMethodExpression();
      if (methodExpression instanceof JSReferenceExpression) {
        JSExpression qualifier = ((JSReferenceExpression)methodExpression).getQualifier();
        if (qualifier != null) {
          qualifiedExpressionType = getQualifiedExpressionType(qualifier);
        }
      }

      if (qualifiedExpressionType != null) {
        if ("push".equals(name) || "unshift".equals(name) || "splice".equals(name)) {
          if (qualifiedExpressionType instanceof JSGenericTypeImpl) {
            setResult(ContainerUtil.getFirstItem(((JSGenericTypeImpl)qualifiedExpressionType).getArguments()));
          }
        }
        else if ("concat".equals(name)) {
          setResult(qualifiedExpressionType);
        }
      }
    }
    else {
      setResult(createNamedType(JSCommonTypeNames.OBJECT_CLASS_NAME, myParent));
    }
  }

  @Override
  protected JSType getQualifiedExpressionType(JSExpression qualifier) {
    return ActionScriptResolveUtil.getQualifiedExpressionJSType(qualifier, qualifier.getContainingFile());
  }

  @Override
  protected void evaluateIndexedAccessType(JSIndexedPropertyAccessExpression node) {
    if (isASDictionaryAccess(node)) {
      setResult(createNamedType(JSCommonTypeNames.OBJECT_CLASS_NAME, myGrandParent));
    }
    else {
      final JSTypeSource typeSource = JSTypeSourceFactory.createTypeSource(myGrandParent, true);
      setResult(JSCompositeTypeFactory.createUnionType(typeSource,
                                         JSNamedTypeFactory.createType(JSCommonTypeNames.INT_TYPE_NAME, typeSource, JSContext.INSTANCE),
                                        JSNamedTypeFactory.createType(JSCommonTypeNames.UINT_TYPE_NAME, typeSource, JSContext.INSTANCE)));
    }
  }

  private static boolean isASDictionaryAccess(final JSIndexedPropertyAccessExpression expression) {
    if (expression.getContainingFile().getLanguage() != JavaScriptSupportLoader.ECMA_SCRIPT_L4) return false;

    final JSExpression qualifier = expression.getQualifier();
    final PsiElement resolve = qualifier instanceof JSReferenceExpression ? ((JSReferenceExpression)qualifier).resolve() : null;
    final String type = resolve instanceof JSVariable ? ((JSVariable)resolve).getTypeString() : null;

    return type != null && ActionScriptResolveUtil.isAssignableType(ValidateTypesUtil.FLASH_UTILS_DICTIONARY, type, expression);
  }

  @Override
  public void visitJSArgumentList(@NotNull JSArgumentList node) {
    if (!(myParent instanceof JSExpression)) return;
    JSParameterItem param = JSResolveUtil.findParameterForUsedArgument((JSExpression)myParent, node);

    if (param != null) {
      if (param.isRest()) {
        findRestParameterExpectedType(param);
      }
      else {
        setResult(param.getSimpleType());
      }
    }
  }

  @Override
  public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
    if (myGrandParent.getParent() instanceof JSNewExpression) {
      JSType type = getQualifiedExpressionType((JSExpression)myGrandParent.getParent());
      if (type instanceof JSGenericTypeImpl) {
        setResult(ContainerUtil.getFirstItem(((JSGenericTypeImpl)type).getArguments()));
      }
    }
  }
}
