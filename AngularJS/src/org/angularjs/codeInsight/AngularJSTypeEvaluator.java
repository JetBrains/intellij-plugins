package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSTypeEvaluator extends JSTypeEvaluator {
  public AngularJSTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                BaseJSSymbolProcessor.TypeProcessor processor, boolean ecma) {
    super(context, processor, ecma);
  }

  @Override
  protected boolean addTypeFromElementResolveResult(JSReferenceExpression expression,
                                                    PsiElement parent,
                                                    PsiElement resolveResult,
                                                    boolean hasSomeType) {
    if (resolveResult instanceof JSDefinitionExpression) {
      final PsiElement resolveParent = resolveResult.getParent();
      if (resolveParent instanceof AngularJSAsExpression) {
        final String name = resolveParent.getFirstChild().getText();
        final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
        final JSType type = JSNamedType.createType(name, source, JSContext.INSTANCE);
        addType(type, resolveResult);
        return true;
      } else if (resolveParent instanceof AngularJSRepeatExpression) {
        if (calculateRepeatParameterType((AngularJSRepeatExpression)resolveParent)) {
          return true;
        }
      }
    }
    if (resolveResult instanceof JSParameter && AngularJSIndexingHandler.isInjectable(resolveResult) &&
        AngularIndexUtil.hasAngularJS(resolveResult.getProject())) {
      final String name = ((JSParameter)resolveResult).getName();
      final JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
      final JSType type = JSNamedType.createType(name, source, JSContext.INSTANCE);
      addType(type, resolveResult);
    }
    return super.addTypeFromElementResolveResult(expression, parent, resolveResult, hasSomeType);
  }

  private boolean calculateRepeatParameterType(AngularJSRepeatExpression resolveParent) {
    final PsiElement last = findReferenceExpression(resolveParent);
    JSExpression arrayExpression = null;
    if (last instanceof JSReferenceExpression) {
      PsiElement resolve = ((JSReferenceExpression)last).resolve();
      if (resolve != null) {
        resolve = JSPsiImplUtils.getAssignedExpression(resolve);
        if (resolve != null) {
          arrayExpression = (JSExpression)resolve;
        }
      }
    }
    else if (last instanceof JSExpression) {
      arrayExpression = (JSExpression)last;
    }
    if (last != null && arrayExpression != null) {
      return evalComponentTypeFromArrayExpression(resolveParent, arrayExpression) != null;
    }
    return false;
  }

  private static PsiElement findReferenceExpression(AngularJSRepeatExpression parent) {
    JSExpression collection = parent.getCollection();
    while (collection instanceof JSBinaryExpression && ((JSBinaryExpression)collection).getROperand() instanceof AngularJSFilterExpression) {
      collection = ((JSBinaryExpression)collection).getLOperand();
    }
    return collection;
  }
}
