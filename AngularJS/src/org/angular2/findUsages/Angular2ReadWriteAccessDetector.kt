// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.lang.javascript.findUsages.JSDefaultReadWriteAccessDetector;
import com.intellij.lang.javascript.findUsages.JSDialectSpecificReadWriteAccessDetector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding;
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute;
import org.jetbrains.annotations.NotNull;

public class Angular2ReadWriteAccessDetector extends JSDefaultReadWriteAccessDetector
  implements JSDialectSpecificReadWriteAccessDetector {

  public static final Angular2ReadWriteAccessDetector INSTANCE = new Angular2ReadWriteAccessDetector();

  @Override
  public @NotNull ReadWriteAccessDetector.Access getExpressionAccess(@NotNull PsiElement _expression) {
    ReadWriteAccessDetector.Access result = super.getExpressionAccess(_expression);
    if (result == ReadWriteAccessDetector.Access.Read
        && _expression.getParent() instanceof Angular2Binding) {
      Angular2HtmlBoundAttribute attr = (Angular2HtmlBoundAttribute)PsiTreeUtil.findFirstParent(
        _expression, Angular2HtmlBoundAttribute.class::isInstance);
      if (attr instanceof Angular2HtmlBananaBoxBinding) {
        return ReadWriteAccessDetector.Access.ReadWrite;
      }
    }
    return result;
  }
}
