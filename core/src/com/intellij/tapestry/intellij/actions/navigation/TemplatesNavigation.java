package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows navigation to all templates of the class
 */
public class TemplatesNavigation extends ActionGroup {

    private static final AnAction[] EMPTY_ACTION_ARRAY = new AnAction[0];

    private PresentationLibraryElement tapestryElement;
    private final ClassTemplateNavigation classTemplateNavigation = new ClassTemplateNavigation();

    /**
     * {@inheritDoc}
     */
    public void update(AnActionEvent event) {
        final boolean isTapestryModule = TapestryUtils.isTapestryModule(event.getData(DataKeys.MODULE));
        final Presentation presentation = event.getPresentation();
        presentation.setEnabled(isTapestryModule);
        presentation.setVisible(isTapestryModule);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public AnAction[] getChildren(@Nullable AnActionEvent event) {
        if (event == null)
            return EMPTY_ACTION_ARRAY;


        PsiFile psiFile = classTemplateNavigation.getEventPsiFile(event);

        if (psiFile == null)
            return EMPTY_ACTION_ARRAY;

        if (psiFile instanceof PsiClassOwner && event.getPresentation().getText()
                .equals("Tapestry Template")) {
            TemplateNavigate templateNavigate;

            DefaultActionGroup actions = new DefaultActionGroup("TemplatesGroup", true);

            try {
                PsiClass psiClass = IdeaUtils.findPublicClass(psiFile);

                // does current file contain a class ?
                if (psiClass == null) {
                    return EMPTY_ACTION_ARRAY;
                }

                tapestryElement = PresentationLibraryElement.createProjectElementInstance(
                        new IntellijJavaClassType((Module) event.getDataContext().getData(DataKeys.MODULE.getName()),
                                psiClass.getContainingFile()),
                        TapestryModuleSupportLoader.getTapestryProject(
                                (Module) event.getDataContext().getData(DataKeys.MODULE.getName()))
                );
            } catch (NotTapestryElementException ex) {
                return EMPTY_ACTION_ARRAY;
            }

            if (tapestryElement.allowsTemplate()) {
                for (int i = 0; i < tapestryElement.getTemplate().length; i++) {
                    templateNavigate = new TemplateNavigate(tapestryElement.getTemplate()[i]);
                    actions.add(templateNavigate);
                }
            }

            if (actions.getChildrenCount() != 0) {
                return actions.getChildren(event);
            }

            return EMPTY_ACTION_ARRAY;
        }
        return EMPTY_ACTION_ARRAY;
    }

    private class TemplateNavigate extends AnAction {

        public TemplateNavigate(IResource template) {
            super(template.getName().replace("_", "__"), template.getName(), null);
        }

        public void actionPerformed(AnActionEvent event) {
            Project project = (Project) event.getDataContext().getData(DataKeys.PROJECT.getName());

            for (int i = 0; i < tapestryElement.getTemplate().length; i++) {
                IResource template = tapestryElement.getTemplate()[i];
                if (template.getName().equals(event.getPresentation().getDescription())) {
                    FileEditorManager.getInstance(project)
                            .openFile(((IntellijResource) template).getPsiFile().getVirtualFile(), true);
                    return;
                }
            }
        }
    }
}
