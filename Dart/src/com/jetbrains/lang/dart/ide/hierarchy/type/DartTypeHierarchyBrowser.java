package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.hierarchy.TypeHierarchyBrowserBase;
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

  protected boolean isInterface(PsiElement psiElement) {
    return false;
  }

  protected void createTrees(@NotNull final Map<String, JTree> trees) {
    ActionGroup group = (ActionGroup)ActionManager.getInstance().getAction("DartClassHierarchyPopupMenu");
    final BaseOnThisTypeAction baseOnThisTypeAction = new BaseOnThisTypeAction();
    final JTree tree1 = createTree(true);
    PopupHandler.installPopupHandler(tree1, group, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP, ActionManager.getInstance());
    baseOnThisTypeAction
      .registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_TYPE_HIERARCHY).getShortcutSet(), tree1);
    trees.put(TYPE_HIERARCHY_TYPE, tree1);

    final JTree tree2 = createTree(true);
    PopupHandler.installPopupHandler(tree2, group, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP, ActionManager.getInstance());
    baseOnThisTypeAction
      .registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_TYPE_HIERARCHY).getShortcutSet(), tree2);
    trees.put(SUPERTYPES_HIERARCHY_TYPE, tree2);

    final JTree tree3 = createTree(true);
    PopupHandler.installPopupHandler(tree3, group, ActionPlaces.TYPE_HIERARCHY_VIEW_POPUP, ActionManager.getInstance());
    baseOnThisTypeAction
      .registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_TYPE_HIERARCHY).getShortcutSet(), tree3);
    trees.put(SUBTYPES_HIERARCHY_TYPE, tree3);
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

  protected PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
    if (!(descriptor instanceof DartTypeHierarchyNodeDescriptor)) return null;
    return ((DartTypeHierarchyNodeDescriptor)descriptor).getDartClass();
  }

  @Nullable
  protected JPanel createLegendPanel() {
    return null;
  }

  protected boolean isApplicableElement(@NotNull final PsiElement element) {
    return element instanceof DartClass;
  }

  protected Comparator<NodeDescriptor> getComparator() {
    return DartHierarchyUtil.getComparator(myProject);
  }

  protected HierarchyTreeStructure createHierarchyTreeStructure(@NotNull final String typeName, @NotNull final PsiElement psiElement) {
    if (SUPERTYPES_HIERARCHY_TYPE.equals(typeName)) {
      return new DartSupertypesHierarchyTreeStructure(myProject, (DartClass)psiElement);
    }
    else if (SUBTYPES_HIERARCHY_TYPE.equals(typeName)) {
      return new DartSubtypesHierarchyTreeStructure(myProject, (DartClass)psiElement, getCurrentScopeType());
    }
    else if (TYPE_HIERARCHY_TYPE.equals(typeName)) {
      return new DartTypeHierarchyTreeStructure(myProject, (DartClass)psiElement, getCurrentScopeType());
    }
    else {
      LOG.error("unexpected type: " + typeName);
      return null;
    }
  }

  protected boolean canBeDeleted(final PsiElement psiElement) {
    return psiElement instanceof DartClass;
  }

  protected String getQualifiedName(final PsiElement psiElement) {
    if (psiElement instanceof DartClass) {
      return ((DartClass)psiElement).getName();
    }
    return "";
  }

  public static class BaseOnThisTypeAction extends TypeHierarchyBrowserBase.BaseOnThisTypeAction {
    protected boolean isEnabled(@NotNull final HierarchyBrowserBaseEx browser, @NotNull final PsiElement psiElement) {
      return super.isEnabled(browser, psiElement) && !DartResolveUtil.OBJECT.equals(((DartClass)psiElement).getName());
    }
  }
}
