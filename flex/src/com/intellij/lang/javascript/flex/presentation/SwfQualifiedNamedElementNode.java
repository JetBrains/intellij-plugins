package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class SwfQualifiedNamedElementNode extends ProjectViewNode<JSQualifiedNamedElement> {

  public SwfQualifiedNamedElementNode(Project project, JSQualifiedNamedElement element, ViewSettings settings) {
    super(project, element, settings);
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return SwfProjectViewStructureProvider.nodeContainsFile(this, file);
  }

  @Override
  public boolean canRepresent(Object element) {
    JSQualifiedNamedElement value = getValue();
    return value != null && element != null && value.getClass() == element.getClass() &&
           Comparing.equal(value.getQualifiedName(), ((JSQualifiedNamedElement)element).getQualifiedName());
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    return new ArrayList<AbstractTreeNode>();
  }

  @Override
  protected void update(PresentationData presentation) {
    presentation.setPresentableText(getValue().getName());
    presentation.setIcons(getValue().getIcon(Iconable.ICON_FLAG_VISIBILITY));
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
    getValue().navigate(true);
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
