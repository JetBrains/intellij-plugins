package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.PopupHandler;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.Map;

public class DartCallHierarchyBrowser extends CallHierarchyBrowserBase {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.hierarchy.call.DartCallHierarchyBrowser");
  private static final String GROUP_DART_CALL_HIERARCHY_POPUP = "DartCallHierarchyPopupMenu";

  public DartCallHierarchyBrowser(Project project, PsiElement method) {
    super(project, method);
  }

  @Nullable
  @Override
  protected PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
    if (descriptor instanceof DartHierarchyNodeDescriptor) {
      DartHierarchyNodeDescriptor pyDescriptor = (DartHierarchyNodeDescriptor)descriptor;
      return pyDescriptor.getPsiElement();
    }
    return null;
  }

  @Override
  protected void createTrees(@NotNull Map<String, JTree> type2TreeMap) {
    final ActionGroup group = (ActionGroup)ActionManager.getInstance().getAction(GROUP_DART_CALL_HIERARCHY_POPUP);
    type2TreeMap.put(CALLER_TYPE, createHierarchyTree(group));
    type2TreeMap.put(CALLEE_TYPE, createHierarchyTree(group));
  }

  private JTree createHierarchyTree(ActionGroup group) {
    final JTree tree = createTree(false);
    PopupHandler.installPopupHandler(tree, group, ActionPlaces.CALL_HIERARCHY_VIEW_POPUP, ActionManager.getInstance());
    return tree;
  }

  @Override
  protected boolean isApplicableElement(@NotNull PsiElement element) {
    return DartHierarchyUtil.isExecutable(element);
  }

  @Nullable
  @Override
  protected HierarchyTreeStructure createHierarchyTreeStructure(@NotNull String typeName, @NotNull PsiElement psiElement) {
    if (CALLER_TYPE.equals(typeName)) {
      return new DartCallerTreeStructure(myProject, psiElement, getCurrentScopeType());
    }
    else if (CALLEE_TYPE.equals(typeName)) {
      return new DartCalleeTreeStructure(myProject, psiElement, getCurrentScopeType());
    }
    else {
      LOG.error("unexpected type: " + typeName);
      return null;
    }
  }

  @Nullable
  @Override
  protected Comparator<NodeDescriptor> getComparator() {
    return DartHierarchyUtil.getComparator(myProject);
  }
}
