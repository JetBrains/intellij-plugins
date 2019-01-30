package com.jetbrains.lang.dart.ide.hierarchy.method;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.hierarchy.MethodHierarchyBrowserBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.PopupHandler;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.Map;

public class DartMethodHierarchyBrowser extends MethodHierarchyBrowserBase {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.hierarchy.method.DartMethodHierarchyBrowser");

  public DartMethodHierarchyBrowser(Project project, PsiElement target) {
    super(project, target);
  }

  @Nullable
  @Override
  protected PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
    if (descriptor instanceof DartMethodHierarchyNodeDescriptor) {
      return descriptor.getPsiElement();
    }
    return null;
  }

  @Override
  protected void createTrees(@NotNull Map<String, JTree> trees) {
    final JTree tree = createTree(false);
    ActionGroup group = (ActionGroup)ActionManager.getInstance().getAction(IdeActions.GROUP_METHOD_HIERARCHY_POPUP);
    PopupHandler.installPopupHandler(tree, group, ActionPlaces.METHOD_HIERARCHY_VIEW_POPUP, ActionManager.getInstance());
    trees.put(METHOD_TYPE, tree);
  }

  @Nullable
  @Override
  protected JPanel createLegendPanel() {
    return createStandardLegendPanel(IdeBundle.message("hierarchy.legend.method.is.defined.in.class"),
                                     IdeBundle.message("hierarchy.legend.method.defined.in.superclass"),
                                     IdeBundle.message("hierarchy.legend.method.should.be.defined"));
  }

  @Override
  protected boolean isApplicableElement(@NotNull PsiElement element) {
    return (element instanceof DartMethodDeclaration ||
            element instanceof DartGetterDeclaration ||
            element instanceof DartSetterDeclaration) &&
           PsiTreeUtil.getParentOfType(element, DartClass.class) != null;
  }

  @Nullable
  @Override
  protected HierarchyTreeStructure createHierarchyTreeStructure(@NotNull String type, @NotNull PsiElement psiElement) {
    if (!METHOD_TYPE.equals(type)) {
      LOG.error("unexpected type: " + type);
      return null;
    }
    return new DartMethodHierarchyTreeStructure(myProject, (DartComponent)psiElement);
  }

  @Nullable
  @Override
  protected Comparator<NodeDescriptor> getComparator() {
    return null;
  }
}
