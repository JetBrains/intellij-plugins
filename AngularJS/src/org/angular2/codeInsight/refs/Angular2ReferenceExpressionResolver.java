// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser;
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Angular2ReferenceExpressionResolver extends JSReferenceExpressionResolver {

  public Angular2ReferenceExpressionResolver(@NotNull JSReferenceExpressionImpl expression, boolean ignorePerformanceLimits) {
    super(expression, ignorePerformanceLimits);
  }

  @NotNull
  @Override
  public ResolveResult[] resolve(@NotNull JSReferenceExpressionImpl expression, boolean incompleteCode) {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY;
    if (myRef instanceof Angular2PipeReferenceExpression) {
      return resolvePipeNameReference(expression, incompleteCode);
    }
    else if (myQualifier == null) {
      return resolveTemplateVariable(expression);
    }
    return super.resolve(expression, incompleteCode);
  }

  @NotNull
  private ResolveResult[] resolvePipeNameReference(@NotNull JSReferenceExpressionImpl expression, boolean incompleteCode) {
    if (!incompleteCode) {
      ResolveResult[] results = expression.multiResolve(true);
      //expected type evaluator uses incomplete = true results so we have to cache it and reuse inside incomplete = false
      return new JSTypeSignatureChooser(((JSCallExpression)expression.getParent())).chooseOverload(results);
    }
    assert myReferencedName != null;
    final Angular2Pipe pipe = Angular2EntitiesProvider.findPipe(myParent.getProject(), myReferencedName);
    if (pipe != null) {
      if (!pipe.getTransformMethods().isEmpty()) {
        return pipe.getTransformMethods()
          .stream()
          .map(JSResolveResult::new)
          .toArray(ResolveResult[]::new);
      }
      return new ResolveResult[]{new JSResolveResult(ObjectUtils.notNull(pipe.getTypeScriptClass(), pipe.getSourceElement()))};
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @NotNull
  private ResolveResult[] resolveTemplateVariable(@NotNull JSReferenceExpressionImpl expression) {
    assert myReferencedName != null;
    final Collection<JSPsiElementBase> localVariables = getItemsByName(myReferencedName, myRef);
    ReadWriteAccessDetector.Access access = JSReadWriteAccessDetector.ourInstance
      .getExpressionAccess(expression);
    return localVariables.stream()
      .map(item -> remapSetterGetter(item, access))
      .flatMap(Collection::stream)
      .map(JSResolveResult::new)
      .toArray(ResolveResult[]::new);
  }

  @NotNull
  private static Collection<JSPsiElementBase> remapSetterGetter(@NotNull JSPsiElementBase item,
                                                                ReadWriteAccessDetector.Access access) {
    if (!(item instanceof TypeScriptFunction)) {
      return Collections.singleton(item);
    }
    TypeScriptFunction function = (TypeScriptFunction)item;
    List<JSPsiElementBase> result = new ArrayList<>();
    if (function.isGetProperty() && access == ReadWriteAccessDetector.Access.Write) {
      findPropertyAccessor(function, true, result::add);
    }
    else if (function.isSetProperty() && access == ReadWriteAccessDetector.Access.Read) {
      findPropertyAccessor(function, false, result::add);
    }
    else {
      result.add(function);
      if (access == ReadWriteAccessDetector.Access.ReadWrite) {
        findPropertyAccessor(function, function.isGetProperty(), result::add);
      }
    }
    return result;
  }

  public static void findPropertyAccessor(@NotNull TypeScriptFunction function,
                                           boolean isSetter,
                                           @NotNull Consumer<? super JSFunction> processor) {
    TypeScriptClass parent = (TypeScriptClass)function.getParent();
    String name = function.getName();
    if (name != null && parent != null) {
      JSClassUtils.processClassesInHierarchy(parent, true, (cls, typeSubst, isInterface) -> {
        for (JSFunction fun : cls.getFunctions()) {
          if (name.equals(fun.getName())
              && ((fun.isGetProperty() && !isSetter)
                  || (fun.isSetProperty() && isSetter))) {
            processor.consume(fun);
            return false;
          }
        }
        return true;
      });
    }
  }

  @NotNull
  private static Collection<JSPsiElementBase> getItemsByName(@NotNull final String name, @NotNull PsiElement element) {
    final Collection<JSPsiElementBase> result = new ArrayList<>();
    Angular2Processor.process(element, element1 -> {
      if (name.equals(element1.getName())) {
        result.add(element1);
      }
    });
    return result;
  }
}
