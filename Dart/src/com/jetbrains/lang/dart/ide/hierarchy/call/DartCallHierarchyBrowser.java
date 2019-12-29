// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.IdeActions;
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

  public DartCallHierarchyBrowser(Project project, PsiElement method) {
    super(project, method);
  }

  @Nullable
  @Override
  protected PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
    if (descriptor instanceof DartCallHierarchyNodeDescriptor) {
      DartCallHierarchyNodeDescriptor pyDescriptor = (DartCallHierarchyNodeDescriptor)descriptor;
      return pyDescriptor.getPsiElement();
    }
    return null;
  }

  @Override
  protected void createTrees(@NotNull Map<String, JTree> type2TreeMap) {
    ActionGroup group = (ActionGroup)ActionManager.getInstance().getAction(IdeActions.GROUP_CALL_HIERARCHY_POPUP);
    type2TreeMap.put(getCALLER_TYPE(), createHierarchyTree(group));
    type2TreeMap.put(getCALLEE_TYPE(), createHierarchyTree(group));
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
    if (getCALLER_TYPE().equals(typeName)) {
      return new DartCallerTreeStructure(myProject, psiElement, getCurrentScopeType());
    }
    else if (getCALLEE_TYPE().equals(typeName)) {
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
