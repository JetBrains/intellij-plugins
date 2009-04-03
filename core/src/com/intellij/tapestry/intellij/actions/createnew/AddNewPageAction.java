package com.intellij.tapestry.intellij.actions.createnew;

import com.intellij.CommonBundle;
import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.intellij.TapestryApplicationSupportLoader;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.util.Validators;
import com.intellij.tapestry.intellij.view.TapestryProjectViewPane;
import com.intellij.tapestry.intellij.view.nodes.LibrariesNode;
import com.intellij.tapestry.intellij.view.nodes.PackageNode;
import com.intellij.tapestry.intellij.view.nodes.PagesNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/**
 * Action that creates a new page.
 */
public class AddNewPageAction extends AnAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(AnActionEvent event) {
        boolean enabled = false;
        Presentation presentation = event.getPresentation();

        if (!TapestryApplicationSupportLoader.getInstance().hasValidLicense()) {
            presentation.setEnabled(false);
            presentation.setVisible(false);

            return;
        }

        Module module = null;
        try {
            module = (Module) event.getDataContext().getData(DataKeys.MODULE.getName());
        } catch (Throwable ex) {
            // ignore
        }

        if (module == null) {
            presentation.setEnabled(false);
            presentation.setVisible(false);

            return;
        }

        final DefaultMutableTreeNode element = TapestryProjectViewPane.getInstance(module.getProject()).getSelectedNode();

        if (!TapestryUtils.isTapestryModule(module)) {
            presentation.setEnabled(false);
            presentation.setVisible(false);

            return;
        }

        // it's the project view
        if (element == null) {
            PsiElement eventPsiElement = (PsiElement) event.getDataContext().getData(DataKeys.PSI_ELEMENT.getName());
            if (eventPsiElement instanceof PsiDirectory) {
                PsiPackage eventPackage = IdeaUtils.getPackage((PsiDirectory) eventPsiElement);
                if (eventPackage != null) {
                    PsiPackage pagesRootPackage;

                    try {
                        pagesRootPackage = JavaPsiFacade.getInstance(module.getProject()).findPackage(TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage());
                    } catch (NotFoundException ex) {
                        presentation.setEnabled(false);
                        presentation.setVisible(false);

                        return;
                    }

                    if (pagesRootPackage != null) {
                        if (eventPackage.getQualifiedName().startsWith(pagesRootPackage.getQualifiedName()) && TapestryUtils.isTapestryModule(module)) {
                            enabled = true;
                        }
                    }
                } else {
                    try {
                        JavaPsiFacade.getInstance(module.getProject()).findPackage(TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage());
                    } catch (NotFoundException ex) {
                        presentation.setEnabled(false);

                        return;
                    }

                    WebFacet webFacet = IdeaUtils.getWebFacet(module);

                    if (webFacet != null && WebUtil.isInsideWebRoots(((PsiDirectory) eventPsiElement).getVirtualFile(), webFacet.getWebRoots(false))) {
                        enabled = true;
                    }
                }
            }
        }
        // it's the Tapestry view
        else {
            // it's a folder
            if (element.getUserObject() instanceof PackageNode && (IdeaUtils.findFirstParent(element, PagesNode.class) != null || element.getUserObject() instanceof PagesNode)
                    && IdeaUtils.findFirstParent(element, LibrariesNode.class) == null) {
                enabled = true;
            }
        }
        presentation.setVisible(true);
        presentation.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(AnActionEvent event) {
        final Module module = (Module) event.getDataContext().getData(DataKeys.MODULE.getName());
        final PsiElement eventPsiElement = (PsiElement) event.getDataContext().getData(DataKeys.PSI_ELEMENT.getName());

        String defaultPagePath = "";
        PsiPackage psiPackage = IdeaUtils.getPackage((PsiDirectory) eventPsiElement);
        if (eventPsiElement != null && psiPackage != null) {
            String eventPackage = psiPackage.getQualifiedName();
            String basePagesPackage;

            try {
                basePagesPackage = TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage();
            } catch (NotFoundException ex) {
                Messages.showErrorDialog("Can't create page. Please check if this module is a valid Tapestry application!", CommonBundle.getErrorTitle());
                return;
            }

            try {
                defaultPagePath = PathUtils.packageIntoPath(eventPackage.substring(basePagesPackage.length() + 1), true);
            } catch (StringIndexOutOfBoundsException ex) {
                //ignore
            }
        }

        if (eventPsiElement != null && psiPackage == null) {
            WebFacet webFacet = IdeaUtils.getWebFacet(module);

            WebRoot webRoot = WebUtil.findParentWebRoot(((PsiDirectory) eventPsiElement).getVirtualFile(), webFacet.getWebRoots(false));
            defaultPagePath = ((PsiDirectory) eventPsiElement).getVirtualFile().getPath().replaceFirst(webRoot.getFile().getPath(), "") + PathUtils.TAPESTRY_PATH_SEPARATOR;
            if (defaultPagePath.startsWith(File.separator)) {
                defaultPagePath = defaultPagePath.substring(1);
            }

            if (defaultPagePath.equals(PathUtils.TAPESTRY_PATH_SEPARATOR)) {
                defaultPagePath = "";
            }
        }

        final AddNewComponentDialog addNewComponentDialog = new AddNewComponentDialog((Module) event.getDataContext().getData(DataKeys.MODULE.getName()), defaultPagePath, true);

        final DialogBuilder builder = new DialogBuilder(module.getProject());
        builder.setCenterPanel(addNewComponentDialog.getContentPane());
        builder.setTitle("New Tapestry Page");
        builder.setButtonsAlignment(SwingConstants.CENTER);

        builder.setOkOperation(
                new Runnable() {
                    public void run() {
                        final String pageName = addNewComponentDialog.getName();

                        if (!Validators.isValidComponentName(pageName)) {
                            Messages.showErrorDialog("Invalid page name!", CommonBundle.getErrorTitle());
                            return;
                        }

                        // Set default values
                        String classSourceDir = addNewComponentDialog.getClassSourceDirectory().getPath();
                        String templateSourceDir = addNewComponentDialog.getTemplateSourceDirectory().getPath();

                        TapestryModuleSupportLoader.getInstance(module).getState().setNewPagesClassesSourceDirectory(classSourceDir);
                        TapestryModuleSupportLoader.getInstance(module).getState().setNewPagesTemplatesSourceDirectory(templateSourceDir);

                        ApplicationManager.getApplication().runWriteAction(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            PsiDirectory classSourceDirectory = PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getClassSourceDirectory());
                                            PsiDirectory templateSourceDirectory = PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getTemplateSourceDirectory());

                                            if (addNewComponentDialog.isNotCreatingTemplate()) {
                                                TapestryUtils.createPage(module, classSourceDirectory, null, pageName, addNewComponentDialog.isReplaceExistingFiles());
                                            } else {
                                                TapestryUtils.createPage(module, classSourceDirectory, templateSourceDirectory, pageName, addNewComponentDialog.isReplaceExistingFiles());
                                            }

                                        } catch (IllegalStateException ex) {
                                            Messages.showWarningDialog(module.getProject(), ex.getMessage(), "Error creating page");
                                        }
                                    }
                                }
                        );
                        builder.getWindow().dispose();
                    }
                }
        );

        builder.showModal(true);
    }
}