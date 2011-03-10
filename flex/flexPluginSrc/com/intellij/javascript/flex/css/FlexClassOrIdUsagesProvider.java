package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssClassOrIdUsagesProvider;
import com.intellij.psi.css.CssTerm;
import com.intellij.psi.css.impl.CssTermTypes;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Mar 7, 2010
 * Time: 5:40:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlexClassOrIdUsagesProvider implements CssClassOrIdUsagesProvider {
  public boolean isUsage(PsiElement classOrIdSelector, PsiElement candidate, int offsetInCandidate) {
    if ((candidate instanceof CssTerm && ((CssTerm)candidate).getTermType() == CssTermTypes.IDENT) ||
        candidate instanceof JSLiteralExpression) {
      final PsiReference ref = candidate.findReferenceAt(offsetInCandidate);
      return ref != null && ref.isReferenceTo(classOrIdSelector);
    }
    return false;
  }
}
