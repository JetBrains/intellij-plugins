package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSFunctionImpl;
import com.intellij.lang.javascript.psi.impl.JSVariableImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class SwfQualifiedNamedElementNode extends ProjectViewNode<JSQualifiedNamedElement> {

  public SwfQualifiedNamedElementNode(Project project, JSQualifiedNamedElement element) {
    super(project, element, ViewSettings.DEFAULT);
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return false;
  }

  @Override
  public boolean canRepresent(Object element) {
    return getValue().getClass() == element.getClass() &&
           Comparing.equal(getValue().getQualifiedName(), ((JSQualifiedNamedElement)element).getQualifiedName());
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    return new ArrayList<AbstractTreeNode>();
  }

  @Override
  protected void update(PresentationData presentation) {
    presentation.setPresentableText(getValue().getName());
    presentation.setIcons(getIcon(getValue(), Iconable.ICON_FLAG_VISIBILITY));
  }

  private static Icon getIcon(JSQualifiedNamedElement element, int flags) {
    if (element instanceof JSClass) {
      return element.getIcon(flags);
    }
    else if (element instanceof JSFunction) {
      return JSFunctionImpl.getIcon(JSFunction.FunctionKind.SIMPLE, flags, ((JSFunction)element).getAttributeList());
    }
    else if (element instanceof JSVariable) {
      return JSVariableImpl.getIcon(((JSVariable)element).getAttributeList(), flags, false);
    }
    assert false : "unexpected element: " + element;
    return element.getIcon(flags);
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

  @Override
  public Comparable getTypeSortKey() {
    return null;
  }

}
