// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.structure;

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.*;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFeatureHeaderImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTableImpl;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinTagImpl;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@NotNullByDefault
public final class GherkinStructureViewElement extends PsiTreeElementBase<PsiElement> {
  GherkinStructureViewElement(PsiElement psiElement) {
    super(psiElement);
  }

  @Override
  public Collection<StructureViewTreeElement> getChildrenBase() {
    List<StructureViewTreeElement> result = new ArrayList<>();
    PsiElement element = getElement();
    if (element == null) return result;
    for (PsiElement child : element.getChildren()) {
      if (child instanceof GherkinPsiElement &&
          !(child instanceof GherkinFeatureHeaderImpl) &&
          !(child instanceof GherkinTableImpl) &&
          !(child instanceof GherkinTagImpl) &&
          !(child instanceof GherkinPystring)) {
        result.add(new GherkinStructureViewElement(child));
      }
    }
    return result;
  }

  @Override
  public @Nullable Icon getIcon(boolean open) {
    final PsiElement element = getElement();
    if (element instanceof GherkinFeature || element instanceof GherkinStepsHolder) {
      return AllIcons.Nodes.LogFolder;
    }
    if (element instanceof GherkinStep) {
      return CucumberIcons.Cucumber;
    }
    return null;
  }


  @Override
  public @Nullable String getPresentableText() {
    if (getElement() instanceof NavigationItem navigationItem) {
      if (navigationItem.getPresentation() != null) {
        return navigationItem.getPresentation().getPresentableText();
      }
    }
    return null;
  }
}
