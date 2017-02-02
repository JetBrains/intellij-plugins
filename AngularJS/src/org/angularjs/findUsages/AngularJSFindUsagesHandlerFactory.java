package org.angularjs.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesHandlerFactory;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.psi.PsiElement;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFindUsagesHandlerFactory extends JavaScriptFindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return DirectiveUtil.getDirective(element) != null || element instanceof JSClass;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
    if (!forHighlightUsages && element instanceof JSClass && AngularIndexUtil.hasAngularJS2(element.getProject())) {
      return new JavaScriptFindUsagesHandlerFactory.JavaScriptFindUsagesHandler(element) {
        @NotNull
        @Override
        public PsiElement[] getSecondaryElements() {
          JSAttributeList list = ((JSClass)element).getAttributeList();
          if (list != null && list.getFirstChild() instanceof ES6Decorator) {
            PsiElement call = list.getFirstChild().getLastChild();
            if (call instanceof JSCallExpression) {
              JSElementIndexingData data = ((JSCallExpression)call).getIndexingData();
              if (data != null && data.getImplicitElements() != null) {
                return data.getImplicitElements().toArray(PsiElement.EMPTY_ARRAY);
              }
            }
          }
          return PsiElement.EMPTY_ARRAY;
        }
      };
    }
    return super.createFindUsagesHandler(element, forHighlightUsages);
  }
}
