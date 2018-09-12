// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.metadata.AngularPipeMetadata;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
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
    Angular2Processor.process(element, element1 -> {
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
    if (Angular2PipeExpression.isPipeNameReference(myRef)) {
      final JSImplicitElement resolve = AngularIndexUtil.resolve(myParent.getProject(), AngularFilterIndex.KEY, myReferencedName);
      if (resolve != null) {
        AngularPipeMetadata pipeMetadata = AngularPipeMetadata.create(resolve);
        if (pipeMetadata.getTransformMethod() != null) {
          return pipeMetadata.getTransformMethod()
            .getMemberSource()
            .getAllSourceElements()
            .stream()
            .filter(e -> !(e instanceof TypeScriptFunctionSignature))
            .map(JSResolveResult::new)
            .toArray(ResolveResult[]::new);
        }
        return new ResolveResult[]{new JSResolveResult(ObjectUtils.notNull(pipeMetadata.getPipeClass(), resolve))};
      }
      return ResolveResult.EMPTY_ARRAY;
    }
    else if (myQualifier == null) {
      final Collection<JSPsiElementBase> localVariables = getItemsByName(myReferencedName, myRef);
      if (!localVariables.isEmpty()) {
        return ContainerUtil.map2Array(localVariables, JSResolveResult.class, item -> new JSResolveResult(item));
      }
      return ResolveResult.EMPTY_ARRAY;
    }
    return super.resolve(expression, incompleteCode);
  }
}
