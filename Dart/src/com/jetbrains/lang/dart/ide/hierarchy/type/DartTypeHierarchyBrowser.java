package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
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
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.hierarchy.type.DartTypeHierarchyBrowser");

  public DartTypeHierarchyBrowser(final Project project, final DartClass dartClass) {
    super(project, dartClass);
  }

  @Override
  protected boolean isInterface(@NotNull PsiElement psiElement) {
    return false;
  }

  @Override
  protected void createTrees(@NotNull final Map<String, JTree> trees) {
    createTreeAndSetupCommonActions(trees, "DartClassHierarchyPopupMenu");
  }

  @NotNull
  @Override
  protected TypeHierarchyBrowserBase.BaseOnThisTypeAction createBaseOnThisAction() {
    return new BaseOnThisTypeAction();
  }

  /*
  Commented out so far as it doesn't work yet. Probably here should be custom Dart specific ChangeScopeAction
  protected void prependActions(DefaultActionGroup actionGroup) {
    super.prependActions(actionGroup);
    actionGroup.add(new ChangeScopeAction() {
      protected boolean isEnabled() {
        return !Comparing.strEqual(myCurrentViewType, SUPERTYPES_HIERARCHY_TYPE);
      }
    });
  }
  */

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

  public static class BaseOnThisTypeAction extends TypeHierarchyBrowserBase.BaseOnThisTypeAction {
    @Override
    protected boolean isEnabled(@NotNull final HierarchyBrowserBaseEx browser, @NotNull final PsiElement psiElement) {
      return super.isEnabled(browser, psiElement) && !DartResolveUtil.OBJECT.equals(((DartClass)psiElement).getName());
    }
  }
}
