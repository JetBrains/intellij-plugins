package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferenceExpressionResolver extends JSReferenceExpressionResolver {
  public AngularJSReferenceExpressionResolver(JSReferenceExpressionImpl expression, PsiFile file) {
    super(expression, file);
  }

  public static Collection<JSVariable> getItemsByName(final String name, PsiElement element) {
    final Collection<JSVariable> result = new ArrayList<JSVariable>();
    AngularJSProcessor.process(element, new Consumer<JSVariable>() {
      @Override
      public void consume(JSVariable element) {
        if (name.equals(element.getName())) {
          result.add(element);
        }
      }
    });
    return result;
  }

  @Override
  public ResolveResult[] doResolve() {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY;

    final Collection<JSVariable> localVariables = getItemsByName(myReferencedName, myRef);
    if (!localVariables.isEmpty()) {
      return ContainerUtil.map2Array(localVariables, JSResolveResult.class, new Function<JSVariable, JSResolveResult>() {
        @Override
        public JSResolveResult fun(JSVariable item) {
          return new JSResolveResult(item);
        }
      });
    }
    return super.doResolve();
  }
}
