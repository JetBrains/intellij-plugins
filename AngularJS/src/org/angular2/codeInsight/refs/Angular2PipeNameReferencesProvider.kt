// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ProcessingContext;
import org.angular2.entities.Angular2Element;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angularjs.codeInsight.refs.AngularJSReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.Angular2DecoratorUtil.PIPE_DEC;

public class Angular2PipeNameReferencesProvider extends PsiReferenceProvider {

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new Angular2PipeNameReference((JSLiteralExpression)element)};
  }

  public static class Angular2PipeNameReference extends AngularJSReferenceBase<JSLiteralExpression> {

    public Angular2PipeNameReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Override
    public boolean isSoft() {
      return false;
    }

    @Override
    public @Nullable PsiElement resolveInner() {
      ES6Decorator decorator = PsiTreeUtil.getParentOfType(getElement(), ES6Decorator.class);
      if (decorator != null && PIPE_DEC.equals(decorator.getDecoratorName())) {
        return doIfNotNull(Angular2EntitiesProvider.getPipe(decorator), Angular2Element::getSourceElement);
      }
      return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }
  }
}
