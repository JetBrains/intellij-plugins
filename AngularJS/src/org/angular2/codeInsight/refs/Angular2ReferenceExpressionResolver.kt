// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptReferenceExpressionResolver;
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser;
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2ComponentPropertyResolveResult;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2ReferenceExpressionResolver extends TypeScriptReferenceExpressionResolver {

  public Angular2ReferenceExpressionResolver(@NotNull JSReferenceExpressionImpl expression, boolean ignorePerformanceLimits) {
    super(expression, ignorePerformanceLimits);
  }

  @Override
  public ResolveResult @NotNull [] resolve(@NotNull JSReferenceExpressionImpl expression, boolean incompleteCode) {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY;
    if (myRef instanceof Angular2PipeReferenceExpression) {
      return resolvePipeNameReference(expression, incompleteCode);
    }
    else if (myQualifier == null || myQualifier instanceof JSThisExpression) {
      return resolveTemplateVariable(expression);
    }
    return super.resolve(expression, incompleteCode);
  }

  @Override
  protected ResolveResult[] postProcessIndexResults(ResolveResult[] results) {
    return results;
  }

  private ResolveResult @NotNull [] resolvePipeNameReference(@NotNull JSReferenceExpressionImpl expression, boolean incompleteCode) {
    if (!incompleteCode) {
      ResolveResult[] results = expression.multiResolve(true);
      //expected type evaluator uses incomplete = true results so we have to cache it and reuse inside incomplete = false
      return new JSTypeSignatureChooser(((JSCallExpression)expression.getParent())).chooseOverload(results);
    }
    assert myReferencedName != null;

    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(expression);
    Angular2Pipe pipe = ContainerUtil.find(Angular2EntitiesProvider.findPipes(expression.getProject(), myReferencedName),
                                           scope::contains);
    if (pipe == null) {
      return ResolveResult.EMPTY_ARRAY;
    }
    if (!pipe.getTransformMethods().isEmpty()) {
      return ContainerUtil.map2Array(pipe.getTransformMethods(), ResolveResult.EMPTY_ARRAY,
                                     JSResolveResult::new);
    }
    return new ResolveResult[]{new JSResolveResult(ObjectUtils.notNull(pipe.getTypeScriptClass(), pipe.getSourceElement()))};
  }

  private ResolveResult @NotNull [] resolveTemplateVariable(@NotNull JSReferenceExpressionImpl expression) {
    assert myReferencedName != null;
    ReadWriteAccessDetector.Access access = JSReadWriteAccessDetector.ourInstance
      .getExpressionAccess(expression);

    List<ResolveResult> results = new SmartList<>();
    Angular2TemplateScopesResolver.resolve(myRef, resolveResult -> {
      JSPsiElementBase element = tryCast(resolveResult.getElement(), JSPsiElementBase.class);
      if (element != null
          && myReferencedName.equals(element.getName())) {
        remapSetterGetterIfNeeded(results, resolveResult, access);
        return false;
      }
      return true;
    });
    return results.toArray(ResolveResult.EMPTY_ARRAY);
  }

  private static void remapSetterGetterIfNeeded(@NotNull List<ResolveResult> results,
                                                @NotNull ResolveResult resolveResult,
                                                @NotNull ReadWriteAccessDetector.Access access) {
    JSPsiElementBase element = (JSPsiElementBase)resolveResult.getElement();
    if (!(element instanceof TypeScriptFunction)) {
      results.add(resolveResult);
      return;
    }
    Consumer<JSFunction> add = resolveResult instanceof Angular2ComponentPropertyResolveResult
                               ? function -> results.add(((Angular2ComponentPropertyResolveResult)resolveResult).copyWith(function))
                               : function -> results.add(new JSResolveResult(function));
    TypeScriptFunction function = (TypeScriptFunction)element;
    if (function.isGetProperty() && access == ReadWriteAccessDetector.Access.Write) {
      findPropertyAccessor(function, true, add);
    }
    else if (function.isSetProperty() && access == ReadWriteAccessDetector.Access.Read) {
      findPropertyAccessor(function, false, add);
    }
    else {
      add.accept(function);
      if (access == ReadWriteAccessDetector.Access.ReadWrite) {
        findPropertyAccessor(function, function.isGetProperty(), add);
      }
    }
  }

  public static void findPropertyAccessor(@NotNull TypeScriptFunction function,
                                          boolean isSetter,
                                          @NotNull Consumer<? super JSFunction> processor) {
    TypeScriptClass parent = tryCast(function.getParent(), TypeScriptClass.class);
    String name = function.getName();
    if (name != null && parent != null) {
      JSClassUtils.processClassesInHierarchy(parent, true, (cls, typeSubst, isInterface) -> {
        for (JSFunction fun : cls.getFunctions()) {
          if (name.equals(fun.getName())
              && ((fun.isGetProperty() && !isSetter)
                  || (fun.isSetProperty() && isSetter))) {
            processor.accept(fun);
            return false;
          }
        }
        return true;
      });
    }
  }
}
