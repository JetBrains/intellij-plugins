// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IconManager;
import com.intellij.ui.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SwfPackageElementNode extends ProjectViewNode<String> {

  private final String myDisplayText;
  private final List<JSQualifiedNamedElement> myElements;
  private final int myFrom;
  private final int myTo;

  public SwfPackageElementNode(Project project,
                               @NotNull String aPackage,
                               String displayText,
                               ViewSettings viewSettings,
                               List<JSQualifiedNamedElement> elements,
                               int from,
                               int to) {
    super(project, aPackage, viewSettings);
    myDisplayText = displayText;
    myElements = elements;
    myFrom = from;
    myTo = to;
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return SwfProjectViewStructureProvider.nodeContainsFile(this, file);
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode<?>> getChildren() {
    return SwfProjectViewStructureProvider.getChildrenForPackage(getValue(), myElements, myFrom, myTo, myProject, getSettings());
  }

  @Override
  protected void update(@NotNull PresentationData presentation) {
    presentation.setPresentableText(myDisplayText);
    presentation.setIcon(IconManager.getInstance().getPlatformIcon(PlatformIcons.Package));
  }

  @Override
  public int getTypeSortWeight(boolean sortByType) {
    return 3;
  }
}
