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
import com.intellij.tapestry.intellij.view.nodes.LibrariesNode;
import com.intellij.tapestry.intellij.view.nodes.MixinsNode;
import com.intellij.tapestry.intellij.view.nodes.PackageNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Action that creates a new mixin.
 */
public class AddNewMixinAction extends AnAction {

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
                    PsiPackage mixinsRootPackage;
                    try {
                        mixinsRootPackage = JavaPsiFacade.getInstance(module.getProject()).findPackage(TapestryModuleSupportLoader.getTapestryProject(module).getMixinsRootPackage());
                    } catch (NotFoundException e) {
                        presentation.setEnabled(false);
                        presentation.setVisible(false);

                        return;
                    }
                    if (mixinsRootPackage != null) {
                        if (eventPackage.getQualifiedName().startsWith(mixinsRootPackage.getQualifiedName()) && TapestryUtils.isTapestryModule(module)) {
                            enabled = true;
                        }
                    }
                }
            }
        }
        // it's the Tapestry view
        else {
            // it's a folder
            if (element.getUserObject() instanceof PackageNode && (IdeaUtils.findFirstParent(element, MixinsNode.class) != null || element.getUserObject() instanceof MixinsNode)
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

        String defaultMixinPath = "";
        PsiPackage aPackage = IdeaUtils.getPackage((PsiDirectory) eventPsiElement);
        if (eventPsiElement != null && aPackage != null) {
            String eventPackage = aPackage.getQualifiedName();
            String basePagesPackage;
            try {
                basePagesPackage = TapestryModuleSupportLoader.getTapestryProject(module).getMixinsRootPackage();
            } catch (NotFoundException e) {
                Messages.showErrorDialog("Can't create mixin. Please check if this module is a valid Tapestry application!", CommonBundle.getErrorTitle());
                return;
            }

            try {
                defaultMixinPath = PathUtils.packageIntoPath(eventPackage.substring(basePagesPackage.length() + 1), true);
            } catch (StringIndexOutOfBoundsException ex) {
                //ignore
            }
        }

        final AddNewMixinDialog addNewMixinDialog = new AddNewMixinDialog((Module) event.getDataContext().getData(DataKeys.MODULE.getName()), defaultMixinPath);

        final DialogBuilder builder = new DialogBuilder(module.getProject());
        builder.setCenterPanel(addNewMixinDialog.getContentPane());
        builder.setTitle("New Tapestry Mixin");
        builder.setButtonsAlignment(SwingConstants.CENTER);

        builder.setOkOperation(
                new Runnable() {
                    public void run() {
                        final String mixinName = addNewMixinDialog.getName();

                        if (!Validators.isValidComponentName(mixinName)) {
                            Messages.showErrorDialog("Invalid mixin name!", CommonBundle.getErrorTitle());
                            return;
                        }

                        // Set default values
                        String classSourceDir = addNewMixinDialog.getClassSourceDirectory().getPath();

                        TapestryModuleSupportLoader.getInstance(module).getState().setNewPagesClassesSourceDirectory(classSourceDir);

                        ApplicationManager.getApplication().runWriteAction(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            PsiDirectory classSourceDirectory = PsiManager.getInstance(module.getProject()).findDirectory(addNewMixinDialog.getClassSourceDirectory());
                                            TapestryUtils.createMixin(module, classSourceDirectory, mixinName, addNewMixinDialog.isReplaceExistingFiles());
                                        } catch (IllegalStateException ex) {
                                            Messages.showWarningDialog(module.getProject(), ex.getMessage(), "Error creating mixin");
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