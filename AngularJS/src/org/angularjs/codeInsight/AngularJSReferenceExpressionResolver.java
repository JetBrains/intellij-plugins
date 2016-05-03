package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularControllerIndex;
import org.angularjs.index.AngularFilterIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.lang.psi.AngularJSAsExpression;
import org.angularjs.lang.psi.AngularJSFilterExpression;
import org.angularjs.lang.psi.AngularJSRepeatExpression;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSReferenceExpressionResolver extends JSReferenceExpressionResolver {
  public AngularJSReferenceExpressionResolver(JSReferenceExpressionImpl expression, PsiFile file) {
    super(expression, file);
  }

  public static Collection<JSPsiElementBase> getItemsByName(final String name, PsiElement element) {
    final Collection<JSPsiElementBase> result = new ArrayList<JSPsiElementBase>();
    AngularJSProcessor.process(element, new Consumer<JSPsiElementBase>() {
      @Override
      public void consume(JSPsiElementBase element) {
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
    if (myRef.getParent() instanceof JSDefinitionExpression) {
      //final PsiElement sibling = myRef.getPrevSibling();
      //if (sibling != null && sibling.getNode().getElementType() == AngularJSTokenTypes.HASH) {
      //  return new JSResolveResult[]{new JSResolveResult(myRef)};
      //}
      final AngularJSRepeatExpression repeat = PsiTreeUtil.getParentOfType(myRef, AngularJSRepeatExpression.class);
      if (repeat != null) {
        for (JSDefinitionExpression def : repeat.getDefinitions()) {
          if (PsiTreeUtil.isAncestor(def, myRef, true)) return new JSResolveResult[]{new JSResolveResult(myRef)};
        }
      }
      final AngularJSAsExpression as = PsiTreeUtil.getParentOfType(myRef, AngularJSAsExpression.class);
      if (as != null) {
        if (PsiTreeUtil.isAncestor(as.getDefinition(), myRef, true)) return new JSResolveResult[]{new JSResolveResult(myRef)};
      }
      return ResolveResult.EMPTY_ARRAY;
    }

    if (AngularJSAsExpression.isAsControllerRef(myRef, myRef.getParent())) {
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
        return ContainerUtil.map2Array(localVariables, JSResolveResult.class, new Function<JSPsiElementBase, JSResolveResult>() {
          @Override
          public JSResolveResult fun(JSPsiElementBase item) {
            return new JSResolveResult(item);
          }
        });
      }
    }
    return super.doResolve();
  }
}
