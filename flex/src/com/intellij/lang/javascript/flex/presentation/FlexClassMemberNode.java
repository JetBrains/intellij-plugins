// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiFormatUtilBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class FlexClassMemberNode extends ProjectViewNode<JSElement> {

  public FlexClassMemberNode(@NotNull JSElement element, ViewSettings settings) {
    super(element.getProject(), element, settings);
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return false;
  }

  @Override
  public boolean canRepresent(Object element) {
    JSElement value = getValue();
    return value != null && value.isValid() && element instanceof JSElement && value.isEquivalentTo((PsiElement)element);
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode<?>> getChildren() {
    return new ArrayList<>();
  }

  @Override
  protected void update(@NotNull PresentationData presentation) {
    String text;
    JSElement value = getValue();
    if (value == null || !value.isValid()) {
      return;
    }

    if (value instanceof JSFunction) {
      text = JSFormatUtil
        .formatMethod(((JSFunction)value), PsiFormatUtilBase.SHOW_NAME |
                                           PsiFormatUtilBase.SHOW_PARAMETERS |
                                           PsiFormatUtilBase.SHOW_TYPE |
                                           PsiFormatUtilBase.TYPE_AFTER,
                      PsiFormatUtilBase.SHOW_TYPE);
    }
    else if (value instanceof JSVariable) {
      text = JSFormatUtil
        .formatField(((JSVariable)value), PsiFormatUtilBase.SHOW_NAME | PsiFormatUtilBase.SHOW_TYPE | PsiFormatUtilBase.TYPE_AFTER);
    }
    else {
      text = value.getName();
    }
    presentation.setPresentableText(text);
    presentation.setIcon(value.getIcon(Iconable.ICON_FLAG_VISIBILITY));
  }

  @Override
  public boolean canNavigate() {
    return true;
  }

  @Override
  public boolean canNavigateToSource() {
    return true;
  }

  @Override
  public void navigate(boolean requestFocus) {
    final JSElement value = getValue();
    if (value != null && value.isValid()) {
      value.navigate(true);
    }
  }

  @Override
  public int getTypeSortWeight(boolean sortByType) {
    if (sortByType) {
      int weight = FlexTreeStructureProvider.getElementWeight(getValue());
      if (weight != -1) {
        return weight;
      }
    }
    return super.getTypeSortWeight(sortByType);
  }
}
