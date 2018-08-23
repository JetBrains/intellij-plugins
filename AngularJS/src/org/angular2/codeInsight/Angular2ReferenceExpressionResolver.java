// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveProcessor;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.index.Angular2IndexingHandler;
import org.angularjs.codeInsight.AngularJSProcessor;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class Angular2ReferenceExpressionResolver extends JSReferenceExpressionResolver {
  public Angular2ReferenceExpressionResolver(JSReferenceExpressionImpl expression, boolean ignorePerformanceLimits) {
    super(expression, ignorePerformanceLimits);
  }

  public static Collection<JSPsiElementBase> getItemsByName(final String name, PsiElement element) {
    final Collection<JSPsiElementBase> result = new ArrayList<>();
    AngularJSProcessor.process(element, element1 -> {
      if (name.equals(element1.getName())) {
        result.add(element1);
      }
    });
    return result;
  }

  @NotNull
  @Override
  public ResolveResult[] resolve(@NotNull JSReferenceExpressionImpl expression, boolean incompleteCode) {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY;
    if (myRef.getParent() instanceof JSDefinitionExpression) {
      final PsiElement sibling = PsiTreeUtil.prevVisibleLeaf(myRef);
      if (sibling != null && sibling.getNode().getElementType() == JSTokenTypes.LET_KEYWORD) {
        return new JSResolveResult[]{new JSResolveResult(myRef)};
      }
      final AngularJSRepeatExpression repeat = PsiTreeUtil.getParentOfType(myRef, AngularJSRepeatExpression.class);
      if (repeat != null) {
        for (JSDefinitionExpression def : repeat.getDefinitions()) {
          if (PsiTreeUtil.isAncestor(def, myRef, true)) return new JSResolveResult[]{new JSResolveResult(myRef)};
        }
      }
      final JSBinaryExpression as = PsiTreeUtil.getParentOfType(myRef, JSBinaryExpression.class);
      if (isAsExpression(as)) {
        if (PsiTreeUtil.isAncestor(PsiTreeUtil.getChildOfType(as, JSDefinitionExpression.class), myRef, true)) return new JSResolveResult[]{new JSResolveResult(myRef)};
      }
      JSClass clazz = myRef.getQualifier() == null ? Angular2IndexingHandler.findDirectiveClass(myRef) : null;
      if (clazz != null && DialectDetector.isTypeScript(clazz)) {
        final TypeScriptResolveProcessor localProcessor = new TypeScriptResolveProcessor(myReferencedName, myContainingFile, myRef);
        localProcessor.setToProcessHierarchy(true);
        JSReferenceExpressionImpl.doProcessLocalDeclarations(clazz, myQualifier, localProcessor, false, false, null);

        return localProcessor.getResultsAsResolveResults();
      }
    }

    if (isAsControllerRef(myRef, myRef.getParent())) {
      final PsiElement resolve = AngularIndexUtil.resolve(myParent.getProject(), AngularControllerIndex.KEY, myReferencedName);
      if (resolve != null) {
        return new JSResolveResult[]{new JSResolveResult(resolve)};
      }
    } else if (AngularJSFilterExpression.isFilterNameRef(myRef, myParent)) {
      final PsiElement resolve = AngularIndexUtil.resolve(myParent.getProject(), AngularFilterIndex.KEY, myReferencedName);
      if (resolve != null) {
        return new JSResolveResult[] {new JSResolveResult(resolve)};
      }
    } else if (myQualifier == null) {
      final Collection<JSPsiElementBase> localVariables = getItemsByName(myReferencedName, myRef);
      if (!localVariables.isEmpty()) {
        return ContainerUtil.map2Array(localVariables, JSResolveResult.class, item -> new JSResolveResult(item));
      }
    }
    return super.resolve(expression, incompleteCode);
  }

  public static boolean isAsControllerRef(PsiReference ref, PsiElement parent) {
    return isAsExpression(parent) && ref == parent.getFirstChild();
  }

  public static boolean isAsExpression(PsiElement parent) {
    return parent instanceof JSBinaryExpression &&
           parent.getNode().findChildByType(JSTokenTypes.AS_KEYWORD) != null;
  }
}
