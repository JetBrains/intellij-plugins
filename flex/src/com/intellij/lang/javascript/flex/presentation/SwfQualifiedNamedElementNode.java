// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class SwfQualifiedNamedElementNode extends ProjectViewNode<JSQualifiedNamedElement> {

  public SwfQualifiedNamedElementNode(Project project, @NotNull JSQualifiedNamedElement element, ViewSettings settings) {
    super(project, element, settings);
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return SwfProjectViewStructureProvider.nodeContainsFile(this, file);
  }

  @Override
  public boolean canRepresent(Object element) {
    JSQualifiedNamedElement value = getValue();
    return value != null && value.isValid() && element != null && value.getClass() == element.getClass() &&
           Objects.equals(value.getQualifiedName(), ((JSQualifiedNamedElement)element).getQualifiedName());
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode<?>> getChildren() {
    return new ArrayList<>();
  }

  @Override
  protected void update(@NotNull PresentationData presentation) {
    final JSQualifiedNamedElement value = getValue();
    if (value != null && value.isValid()) {
      presentation.setPresentableText(value.getName());
      presentation.setIcon(value.getIcon(Iconable.ICON_FLAG_VISIBILITY));
    }
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
    final JSQualifiedNamedElement value = getValue();
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
