// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlStructureViewElement extends PsiTreeElementBase<PsiElement> {
  protected CfmlStructureViewElement(final PsiElement psiElement) {
    super(psiElement);
  }

  private static void collectResults(Collection<StructureViewTreeElement> result, PsiElement element) {
    if (element instanceof CfmlComponent) {
      result.addAll(makeCollection(((CfmlComponent)element).getFunctions()));
    }
    else if (element instanceof CfmlFunction) {
      result.add(new CfmlStructureViewElement(element));
    }
    else if (element instanceof CfmlTag) {
      collectResultsFromChildren(result, element);
    }
  }

  private static void collectResultsFromChildren(Collection<StructureViewTreeElement> result, PsiElement element) {
    final PsiElement[] children = element.getChildren();
    for (PsiElement child : children) {
      collectResults(result, child);
    }
  }

  @Override
  @NotNull
  public Collection<StructureViewTreeElement> getChildrenBase() {
    PsiElement element = getElement();
    Collection<StructureViewTreeElement> result = new LinkedList<>();

    if (element != null && !(element instanceof CfmlFunction)) {
      collectResultsFromChildren(result, element);
    }

    return result;
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    return new PresentationData(getPresentableText(), null, getIcon(false), null);
  }

  @Override
  public String getPresentableText() {
    PsiElement element = getElement();
    if (element instanceof CfmlFunction) {
      return ((CfmlFunction)element).getFunctionInfo().getPresentableText();
    }
    else if (element instanceof CfmlFile) {
      return ((CfmlFile)element).getName();
    }
    return "";
  }

  private static Collection<StructureViewTreeElement> makeCollection(PsiElement @Nullable [] tags) {
    if (tags == null) {
      return Collections.emptyList();
    }
    return ContainerUtil.map(tags,
                                  cfmlTag -> new CfmlStructureViewElement(cfmlTag));
  }
}
