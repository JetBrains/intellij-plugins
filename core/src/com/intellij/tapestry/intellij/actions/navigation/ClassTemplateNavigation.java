package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;

/**
 * Allows navigation from a class to it's corresponding template and vice-versa.
 */
public class ClassTemplateNavigation extends AnAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(AnActionEvent event) {
        Presentation presentation = event.getPresentation();

        Module module;
        try {
            module = (Module) event.getDataContext().getData(DataKeys.MODULE.getName());
        } catch (Throwable ex) {
            presentation.setEnabled(false);
            presentation.setVisible(false);

            return;
        }

        if (!TapestryUtils.isTapestryModule(module)) {
            presentation.setEnabled(false);
            presentation.setVisible(false);

            return;
        }

        PsiFile psiFile = getEventPsiFile(event);

        if (psiFile == null || !psiFile.getFileType().equals(StdFileTypes.HTML) && event.getPresentation().getText()
                .equals("Tapestry Class")) {
            presentation.setEnabled(false);

            return;
        }

        presentation.setEnabled(true);
        presentation.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(AnActionEvent event) {
        Project project = (Project) event.getDataContext().getData(DataKeys.PROJECT.getName());

        PsiFile psiFile = getEventPsiFile(event);
        if (psiFile == null)
            return;

        if (psiFile.getFileType().equals(StdFileTypes.JAVA) && event.getPresentation().getText()
                .equals("Class <-> Template Navigation")
                && event.getDataContext().getData(DataKeys.MODULE.getName()) != null) {

            PresentationLibraryElement tapestryElement;

            try {
                PsiClass psiClass = IdeaUtils.findPublicClass(((PsiJavaFile) psiFile).getClasses());

                if (psiClass == null) {
                    showCantNavigateMessage();

                    return;
                }

                tapestryElement = PresentationLibraryElement.createProjectElementInstance(
                        new IntellijJavaClassType((Module) event.getDataContext().getData(DataKeys.MODULE.getName()),
                                psiClass.getContainingFile()),
                        TapestryModuleSupportLoader.getTapestryProject(
                                (Module) event.getDataContext().getData(DataKeys.MODULE.getName()))
                );
            } catch (NotTapestryElementException e) {
                showCantNavigateMessage();

                return;
            }

            if (tapestryElement.allowsTemplate() && tapestryElement.getTemplate().length != 0) {
                IResource template = tapestryElement.getTemplate()[0];
                if (template != null) {
                    FileEditorManager.getInstance(project)
                            .openFile(((IntellijResource) template).getPsiFile().getVirtualFile(), true);
                }
            } else {
                showCantNavigateMessage();
                return;
            }
        }

        if (psiFile.getFileType().equals(StdFileTypes.HTML) && event.getPresentation().getText()
                .equals("Class <-> Template Navigation")
                || psiFile.getFileType().equals(StdFileTypes.HTML) && event.getPresentation().getText()
                .equals("Tapestry Class")) {

            IJavaClassType elementClass;
            try {
                elementClass = ComponentUtils
                        .findClassFromTemplate(new IntellijResource(psiFile), TapestryModuleSupportLoader.getTapestryProject(
                                (Module) event.getDataContext().getData(DataKeys.MODULE.getName())));
            } catch (NotFoundException e) {
                elementClass = null;
            }

            if (elementClass != null) {
                FileEditorManager.getInstance(project).openFile(
                        ((IntellijJavaClassType) elementClass).getPsiClass().getContainingFile().getVirtualFile(),
                        true);
            } else {
                showCantNavigateMessage();
            }
        }
    }

    /**
     * Finds the PsiFile on which the event occured.
     *
     * @param event the event.
     * @return the PsiFile on which the event occured, or <code>null</code> if the file couldn't be determined.
     */
    public PsiFile getEventPsiFile(AnActionEvent event) {
        FileEditorManager fileEditorManager = FileEditorManager
                .getInstance((Project) event.getDataContext().getData(DataKeys.PROJECT.getName()));

        if (fileEditorManager == null)
            return null;

        Editor editor = fileEditorManager.getSelectedTextEditor();

        if (editor == null)
            return null;

        return PsiManager.getInstance((Project) event.getDataContext().getData(DataKeys.PROJECT.getName()))
                .findFile(FileDocumentManager.getInstance().getFile(editor.getDocument()));
    }

    public void showCantNavigateMessage() {
        Messages.showInfoMessage("Couldn't find a file to navigate to.", "Not Tapestry file");
    }
}
