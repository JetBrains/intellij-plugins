package com.intellij.tapestry.intellij.actions.createnew;

import com.intellij.CommonBundle;
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
import com.intellij.tapestry.intellij.view.nodes.ComponentsNode;
import com.intellij.tapestry.intellij.view.nodes.LibrariesNode;
import com.intellij.tapestry.intellij.view.nodes.PackageNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action that creates a new component.
 */
public class AddNewComponentAction extends AnAction {

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
                    PsiPackage componentsRootPackage;
                    try {
                        componentsRootPackage = JavaPsiFacade.getInstance(module.getProject()).findPackage(TapestryModuleSupportLoader.getTapestryProject(module).getComponentsRootPackage());
                    } catch (NotFoundException e) {
                        presentation.setEnabled(false);
                        presentation.setVisible(false);

                        return;
                    }
                    if (componentsRootPackage != null) {
                        if (eventPackage.getQualifiedName().startsWith(componentsRootPackage.getQualifiedName()) && TapestryUtils.isTapestryModule(module)) {
                            enabled = true;
                        }
                    }
                }
            }
        }
        // it's the Tapestry view
        else {
            // it's a folder
            if (element.getUserObject() instanceof PackageNode && (IdeaUtils.findFirstParent(element, ComponentsNode.class) != null || element.getUserObject() instanceof ComponentsNode)
                    && IdeaUtils.findFirstParent(element, LibrariesNode.class) == null) {
                enabled = true;
            }
        }

        presentation.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(AnActionEvent event) {
        final Module module = (Module) event.getDataContext().getData(DataKeys.MODULE.getName());
        final PsiElement eventPsiElement = (PsiElement) event.getDataContext().getData(DataKeys.PSI_ELEMENT.getName());

        String defaultComponentPath = "";
        if (eventPsiElement != null) {
            String eventPackage = IdeaUtils.getPackage((PsiDirectory) eventPsiElement).getQualifiedName();
            String baseComponentsPackage;
            try {
                baseComponentsPackage = TapestryModuleSupportLoader.getTapestryProject(module).getComponentsRootPackage();
            } catch (NotFoundException e) {
                Messages.showErrorDialog("Can't create component. Please check if this module is a valid Tapestry application!", CommonBundle.getErrorTitle());
                return;
            }

            try {
                defaultComponentPath = PathUtils.packageIntoPath(eventPackage.substring(baseComponentsPackage.length() + 1), true);
            } catch (StringIndexOutOfBoundsException ex) {
                //ignore
            }
        }

        final AddNewComponentDialog addNewComponentDialog = new AddNewComponentDialog((Module) event.getDataContext().getData(DataKeys.MODULE.getName()), defaultComponentPath, false);

        final DialogBuilder builder = new DialogBuilder(module.getProject());
        builder.setCenterPanel(addNewComponentDialog.getContentPane());
        builder.setTitle("New Tapestry Component");
        builder.setButtonsAlignment(SwingConstants.CENTER);

        builder.setOkOperation(
                new Runnable() {
                    public void run() {
                        final String componentName = addNewComponentDialog.getName();

                        if (!Validators.isValidComponentName(componentName)) {
                            Messages.showErrorDialog("Invalid component name!", CommonBundle.getErrorTitle());
                            return;
                        }

                        // Set default values.
                        String classSourceDir = addNewComponentDialog.getClassSourceDirectory().getPath();
                        String templateSourceDir = addNewComponentDialog.getTemplateSourceDirectory().getPath();

                        TapestryModuleSupportLoader.getInstance(module).getState().setNewComponentsClassesSourceDirectory(classSourceDir);
                        TapestryModuleSupportLoader.getInstance(module).getState().setNewComponentsTemplatesSourceDirectory(templateSourceDir);

                        ApplicationManager.getApplication().runWriteAction(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            PsiDirectory classSourceDirectory = PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getClassSourceDirectory());
                                            PsiDirectory templateSourceDirectory = PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getTemplateSourceDirectory());

                                            if (addNewComponentDialog.isNotCreatingTemplate()) {
                                                TapestryUtils.createComponent(module, classSourceDirectory, null, componentName, addNewComponentDialog.isReplaceExistingFiles());
                                            } else {
                                                TapestryUtils.createComponent(module, classSourceDirectory, templateSourceDirectory, componentName, addNewComponentDialog.isReplaceExistingFiles());
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