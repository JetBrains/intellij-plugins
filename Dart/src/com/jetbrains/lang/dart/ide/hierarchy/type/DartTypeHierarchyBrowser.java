package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.Map;

public class DartTypeHierarchyBrowser extends TypeHierarchyBrowserBase {
  private static final Logger LOG = Logger.getInstance(DartTypeHierarchyBrowser.class);

  public DartTypeHierarchyBrowser(final Project project, final DartClass dartClass) {
    super(project, dartClass);
  }

  @Override
  protected boolean isInterface(@NotNull PsiElement psiElement) {
    return false;
  }

  @Override
  protected void createTrees(@NotNull final Map<String, JTree> trees) {
    createTreeAndSetupCommonActions(trees, IdeActions.GROUP_TYPE_HIERARCHY_POPUP);
  }

  @Override
  protected PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
    if (!(descriptor instanceof DartTypeHierarchyNodeDescriptor)) return null;
    return ((DartTypeHierarchyNodeDescriptor)descriptor).getDartClass();
  }

  @Override
  @Nullable
  protected JPanel createLegendPanel() {
    return null;
  }

  @Override
  protected boolean isApplicableElement(@NotNull final PsiElement element) {
    return element instanceof DartClass;
  }

  @Override
  protected boolean isApplicableElementForBaseOn(@NotNull PsiElement element) {
    return element instanceof DartClass &&
           !DartResolveUtil.OBJECT.equals(((DartClass)element).getName());
  }

  @Override
  protected Comparator<NodeDescriptor> getComparator() {
    return DartHierarchyUtil.getComparator(myProject);
  }

  @Override
  protected HierarchyTreeStructure createHierarchyTreeStructure(@NotNull final String typeName, @NotNull final PsiElement psiElement) {
    if (SUPERTYPES_HIERARCHY_TYPE.equals(typeName)) {
      return new DartServerSupertypesHierarchyTreeStructure(myProject, (DartClass)psiElement);
    }
    else if (SUBTYPES_HIERARCHY_TYPE.equals(typeName)) {
      return new DartServerSubtypesHierarchyTreeStructure(myProject, (DartClass)psiElement, getCurrentScopeType());
    }
    else if (TYPE_HIERARCHY_TYPE.equals(typeName)) {
      return new DartServerTypeHierarchyTreeStructure(myProject, (DartClass)psiElement, getCurrentScopeType());
    }
    else {
      LOG.error("unexpected type: " + typeName);
      return null;
    }
  }

  @Override
  protected boolean canBeDeleted(final PsiElement psiElement) {
    return psiElement instanceof DartClass;
  }

  @Override
  protected String getQualifiedName(final PsiElement psiElement) {
    if (psiElement instanceof DartClass) {
      return ((DartClass)psiElement).getName();
    }
    return "";
  }
}
