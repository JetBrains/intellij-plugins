// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;


public class AngularJSTemplateReferencesProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        return ArrayUtil.mergeArrays(new SoftFileReferenceSet(element).getAllReferences(),
                new PsiReference[]{new AngularJSTemplateCacheReference((JSLiteralExpression) element)});
    }
}
