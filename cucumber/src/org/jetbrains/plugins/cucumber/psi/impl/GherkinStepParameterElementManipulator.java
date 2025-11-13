// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;

public class GherkinStepParameterElementManipulator extends AbstractElementManipulator<GherkinStepParameter> {
  @Override
  public @Nullable GherkinStepParameter handleContentChange(@NotNull GherkinStepParameter element,
                                                            @NotNull TextRange range,
                                                            String newContent) throws IncorrectOperationException {
    final Project project = element.getProject();
    final LeafPsiElement content = PsiTreeUtil.getChildOfType(element, LeafPsiElement.class);
    if (content != null) {
      PsiElement[] elements = GherkinElementFactory.getTopLevelElements(project, newContent);
      element.getNode().replaceChild(content, elements[0].getNode());
    }
    return element;
  }

  @Override
  public @NotNull TextRange getRangeInElement(@NotNull GherkinStepParameter element) {
    return new TextRange(0, element.getTextLength());
  }
}
