package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SwfPackageElementNode extends ProjectViewNode<String> {

  private final String myDisplayText;
  private final List<JSQualifiedNamedElement> myElements;
  private final int myFrom;
  private final int myTo;

  public SwfPackageElementNode(Project project,
                               String aPackage,
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
    AbstractTreeNode parent = getParent();
    while (parent instanceof SwfPackageElementNode) {
      parent = parent.getParent();
    }
    return ((PsiFileNode)parent).contains(file);
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    return SwfProjectViewStructureProvider.getChildrenForPackage(getValue(), myElements, myFrom, myTo, myProject, getSettings());
  }

  @Override
  protected void update(PresentationData presentation) {
    presentation.setPresentableText(myDisplayText);
    presentation.setOpenIcon(Icons.PACKAGE_OPEN_ICON);
    presentation.setClosedIcon(Icons.PACKAGE_ICON);
  }

  @Override
  public int getTypeSortWeight(boolean sortByType) {
    return 3;
  }
}
