package com.intellij.plugins.drools.lang.psi;

import com.intellij.ide.presentation.Presentation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTarget;

@Presentation(typeName = DroolsFunction.FUNCTION, icon = "AllIcons.Nodes.Method")
public interface DroolsFunction extends DroolsPsiCompositeElement, PsiMethod, PsiTarget {
   String FUNCTION = "Function";

   String getFunctionName();
}
