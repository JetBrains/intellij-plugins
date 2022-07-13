package com.intellij.tapestry.intellij.actions.createnew;

import com.intellij.CommonBundle;
import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.Utils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.view.nodes.LibrariesNode;
import com.intellij.tapestry.intellij.view.nodes.PackageNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

import static com.intellij.openapi.actionSystem.PlatformCoreDataKeys.SELECTED_ITEM;

public abstract class AddNewElementAction<T extends PackageNode> extends AnAction {

  private final Class<T> nodeClass;

  protected AddNewElementAction(@NotNull Class<T> nodeClass) {
    this.nodeClass = nodeClass;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(@NotNull AnActionEvent event) {
    boolean enabled = false;
    Presentation presentation = event.getPresentation();

    Module module = event.getData(PlatformCoreDataKeys.MODULE);

    if (!TapestryUtils.isTapestryModule(module)) {
      presentation.setEnabledAndVisible(false);
      return;
    }

    Object data = event.getData(SELECTED_ITEM);
    final DefaultMutableTreeNode element = data instanceof DefaultMutableTreeNode ? (DefaultMutableTreeNode)data : null;
    // it's the project view
    if (element == null) {
      PsiElement eventPsiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
      final TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
      if (tapestryProject == null) {
        presentation.setEnabledAndVisible(false);
        return;
      }
      final String aPackage = getElementsRootPackage(tapestryProject);
      if (aPackage == null) {
        presentation.setEnabledAndVisible(false);
        return;
      }

      PsiPackage eventPackage = IdeaUtils.getPackage(eventPsiElement);

      if (eventPackage != null) {
        PsiPackage elementsRootPackage = JavaPsiFacade.getInstance(module.getProject()).findPackage(aPackage);

        if (elementsRootPackage != null) {
          if (eventPackage.getQualifiedName().startsWith(elementsRootPackage.getQualifiedName()) &&
              TapestryUtils.isTapestryModule(module)) {
            enabled = true;
          }
        }
      }
      else {
        if (JavaPsiFacade.getInstance(module.getProject()).findPackage(aPackage) == null) {
          presentation.setEnabled(false);
          return;
        }
        WebFacet webFacet = IdeaUtils.getWebFacet(module);

        if (eventPsiElement instanceof PsiDirectory &&
            webFacet != null &&
            WebUtil.isInsideWebRoots(((PsiDirectory)eventPsiElement).getVirtualFile(), webFacet.getWebRoots())) {
          enabled = true;
        }
      }

    }
    // it's the Tapestry view | folder
    else if (element.getUserObject() instanceof PackageNode) {
      UpdateSession session = Utils.getOrCreateUpdateSession(event);
      if (session.compute(this, "findParent", ActionUpdateThread.EDT, () ->
        (IdeaUtils.findFirstParent(element, nodeClass) != null || nodeClass.isInstance(element.getUserObject())) &&
        IdeaUtils.findFirstParent(element, LibrariesNode.class) == null)) {
        enabled = true;
      }
    }
    presentation.setVisible(true);
    presentation.setEnabled(enabled);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Nullable
  protected abstract String getElementsRootPackage(@NotNull TapestryProject tapestryProject);

  /**
   * {@inheritDoc}
   */

  @Nullable
  protected String getDefaultElementPath(AnActionEvent event, Module module) {
    PsiElement eventPsiElement = event.getData(CommonDataKeys.PSI_ELEMENT);

    PsiPackage psiPackage = IdeaUtils.getPackage(eventPsiElement);
    String defaultPagePath = "";
    if (psiPackage != null) {
      String eventPackage = psiPackage.getQualifiedName();
      final TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
      if (tapestryProject == null) {
        showError();
        return null;
      }
      String basePagesPackage = getElementsRootPackage(TapestryModuleSupportLoader.getTapestryProject(module));
      if (basePagesPackage == null) {
        showError();
        return null;
      }

      try {
        defaultPagePath = PathUtils.packageIntoPath(eventPackage.substring(basePagesPackage.length() + 1), true);
      }
      catch (StringIndexOutOfBoundsException ex) {
        //ignore
      }
    }

    if (eventPsiElement != null && psiPackage == null) {
      WebFacet webFacet = IdeaUtils.getWebFacet(module);

      WebRoot webRoot = WebUtil.findParentWebRoot(((PsiDirectory)eventPsiElement).getVirtualFile(), webFacet.getWebRoots());
      defaultPagePath = ((PsiDirectory)eventPsiElement).getVirtualFile().getPath().replaceFirst(webRoot.getFile().getPath(), "") +
                        PathUtils.TAPESTRY_PATH_SEPARATOR;
      if (defaultPagePath.startsWith(File.separator)) {
        defaultPagePath = defaultPagePath.substring(1);
      }

      if (defaultPagePath.equals(PathUtils.TAPESTRY_PATH_SEPARATOR)) {
        defaultPagePath = "";
      }
    }
    return defaultPagePath;
  }

  private static void showError() {
    Messages
      .showErrorDialog("Can't create element. Please check if this module is a valid Tapestry application!", CommonBundle.getErrorTitle());
  }
}