// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

public class FeatureRunLineMarkerContributor extends CucumberRunLineMarkerContributor implements DumbAware {

  @Override
  protected boolean isValidElement(PsiElement element) {
    IElementType type = ((LeafElement)element).getElementType();
    if (!GherkinTokenTypes.FEATURE_KEYWORD.equals(type)) return false;
    return true;
  }
}
