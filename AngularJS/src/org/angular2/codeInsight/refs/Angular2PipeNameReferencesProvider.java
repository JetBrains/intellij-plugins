// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angularjs.codeInsight.refs.AngularJSReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2PipeNameReferencesProvider extends PsiReferenceProvider {

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new Angular2PipeNameReference((JSLiteralExpression)element)};
  }

  public static class Angular2PipeNameReference extends AngularJSReferenceBase<JSLiteralExpression> {

    public Angular2PipeNameReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
      String pipeName = getElement().getStringValue();
      return pipeName != null ?
             ObjectUtils.doIfNotNull(Angular2EntitiesProvider.findPipe(getElement().getProject(), pipeName),
                                     Angular2Pipe::getSourceElement)
                              : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
