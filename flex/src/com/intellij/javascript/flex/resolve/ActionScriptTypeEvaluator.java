package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageWrapper;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.VECTOR_CLASS_NAME;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptTypeEvaluator extends JSTypeEvaluator {
  public ActionScriptTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                      BaseJSSymbolProcessor.TypeProcessor processor, boolean ecma) {
    super(context, processor, ecma);
  }

  @Override
  protected boolean addTypeFromDialectSpecificElements(JSReferenceExpression expression, PsiElement resolveResult, boolean wasPrototype) {
    if (resolveResult instanceof JSPackageWrapper) {
      if (myTypeProcessor instanceof PsiScopeProcessor) {
        if (myTypeProcessor instanceof ResolveProcessor) ((ResolveProcessor)myTypeProcessor).prefixResolved();
        resolveResult.processDeclarations((PsiScopeProcessor)myTypeProcessor, ResolveState.initial(), expression, expression);
      } else {
        addType(((JSQualifiedNamedElement)resolveResult).getQualifiedName(),
                expression);
      }
      return true;
    }
    return false;
  }

  @Override
  protected void evaluateNewExpressionTypes(JSNewExpression newExpression) {
    JSExpression methodExpr = newExpression.getMethodExpression();
    if (methodExpr != null) {
      if (methodExpr instanceof JSArrayLiteralExpression) {
        String type = VECTOR_CLASS_NAME;

        PsiElement arrayInitializingType = newExpression.getArrayInitializingType();
        if (arrayInitializingType != null) {
          type += ".<" + JSImportHandlingUtil.resolveTypeName(arrayInitializingType.getText(), newExpression) + ">";
        }
        addType(type, methodExpr);
      }
      else {
        String text = methodExpr.getText();
        JSResolveUtil.GenericSignature signature = JSResolveUtil.extractGenericSignature(text);
        if (signature != null) {
          text = signature.elementType;
        }
        if (methodExpr instanceof JSReferenceExpression && ((JSReferenceExpression)methodExpr).resolve() instanceof JSVariable) {
          text = "*";
        } else {
          BaseJSSymbolProcessor.SimpleTypeProcessor subProcessor = new BaseJSSymbolProcessor.SimpleTypeProcessor();
          evaluateTypes(methodExpr, myContext.targetFile, subProcessor);

          final JSType type = subProcessor.getType();
          if (type != JSType.NO_TYPE && (type != JSType.ANY || isNotValidType(text))) {
            text = "Class".equals(type.getTypeText())? "*": type.getTypeText();
          }

          if(!"*".equals(text)) {
            text = JSImportHandlingUtil.resolveTypeName(text, methodExpr);
          }
        }
        addType(JSTypeUtils.createType(text, new JSTypeSource(methodExpr, JSTypeSourceFactory
          .sourceFileLanguage(methodExpr.getContainingFile()), false, JSTypeSource.StaticOrInstance.INSTANCE)), methodExpr);
      }
    }
  }

  private static boolean isNotValidType(String text) {
    return text.indexOf('[') != -1 || text.indexOf('(') != -1 || text.indexOf('{') != -1;
  }
}
