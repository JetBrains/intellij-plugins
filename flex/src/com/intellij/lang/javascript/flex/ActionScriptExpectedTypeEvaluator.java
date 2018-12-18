package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.psi.types.JSTypeSource.EMPTY;
import static com.intellij.openapi.util.text.StringUtil.notNullize;

/**
 * @author yole
 */
public class ActionScriptExpectedTypeEvaluator extends ExpectedTypeEvaluator {
  public ActionScriptExpectedTypeEvaluator(JSExpression parent, JSExpectedTypeKind expectedTypeKind) {
    super(parent, expectedTypeKind);
  }

  @Override
  protected ActionScriptExpectedTypeEvaluator newExpectedTypeEvaluator(JSExpression parent, JSExpectedTypeKind expectedTypeKind) {
    return new ActionScriptExpectedTypeEvaluator(parent, expectedTypeKind);
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
                       JSNamedTypeFactory.createType(notNullize(((JSClass)element).getQualifiedName()), EMPTY, JSContext.INSTANCE) :
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
          qualifiedExpressionType = JSResolveUtil.getQualifiedExpressionJSType(qualifier, qualifier.getContainingFile());
        }
      }

      if (qualifiedExpressionType != null) {
        if ("push".equals(name) || "unshift".equals(name) || "splice".equals(name)) {
          if (qualifiedExpressionType instanceof JSGenericTypeImpl) {
            myResult = ContainerUtil.getFirstItem(((JSGenericTypeImpl)qualifiedExpressionType).getArguments());
          }
        }
        else if ("concat".equals(name)) {
          myResult = qualifiedExpressionType;
        }
      }
    }
    else {
      myResult = createNamedType(JSCommonTypeNames.OBJECT_CLASS_NAME, myParent);
    }
  }

  @Override
  protected void evaluateIndexedAccessType(JSIndexedPropertyAccessExpression node) {
    if (isASDictionaryAccess(node)) {
      myResult = createNamedType(JSCommonTypeNames.OBJECT_CLASS_NAME, myGrandParent);
    }
    else {
      final JSTypeSource typeSource = JSTypeSourceFactory.createTypeSource(myGrandParent, true);
      myResult = new JSCompositeTypeImpl(typeSource,
                                         JSNamedType.createType(JSCommonTypeNames.INT_TYPE_NAME, typeSource, JSContext.INSTANCE),
                                         JSNamedType.createType(JSCommonTypeNames.UINT_TYPE_NAME, typeSource, JSContext.INSTANCE));
    }
  }

  private static boolean isASDictionaryAccess(final JSIndexedPropertyAccessExpression expression) {
    if (expression.getContainingFile().getLanguage() != JavaScriptSupportLoader.ECMA_SCRIPT_L4) return false;

    final JSExpression qualifier = expression.getQualifier();
    final PsiElement resolve = qualifier instanceof JSReferenceExpression ? ((JSReferenceExpression)qualifier).resolve() : null;
    final String type = resolve instanceof JSVariable ? ((JSVariable)resolve).getTypeString() : null;

    return type != null && JSResolveUtil.isAssignableType(ValidateTypesUtil.FLASH_UTILS_DICTIONARY, type, expression);
  }

  @Override
  public void visitJSArgumentList(@NotNull JSArgumentList node) {
    JSParameterItem param = JSResolveUtil.findParameterForUsedArgument(myParent, node);

    if (param != null) {
      if (param.isRest()) {
        findRestParameterExpectedType(param);
      }
      else {
        myResult = param.getSimpleType();
      }
    }
  }

  @Override
  public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
    if (myGrandParent.getParent() instanceof JSNewExpression) {
      JSType type = JSResolveUtil.getQualifiedExpressionJSType((JSExpression)myGrandParent.getParent(), myGrandParent.getContainingFile());
      if (type instanceof JSGenericTypeImpl) {
        myResult = ContainerUtil.getFirstItem(((JSGenericTypeImpl)type).getArguments());
      }
    }
  }
}
