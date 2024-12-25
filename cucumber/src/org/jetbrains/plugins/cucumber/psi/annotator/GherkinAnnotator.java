// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinAnnotator implements Annotator {

    @Override
    public void annotate(final @NotNull PsiElement psiElement, final @NotNull AnnotationHolder holder) {
        final GherkinAnnotatorVisitor visitor = new GherkinAnnotatorVisitor(holder);
        psiElement.accept(visitor);
    }
}
