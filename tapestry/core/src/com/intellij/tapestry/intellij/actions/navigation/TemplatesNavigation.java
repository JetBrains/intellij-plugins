package com.intellij.tapestry.intellij.actions.navigation;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
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
public class TemplatesNavigation extends ActionGroup implements DumbAware {

    private static final AnAction[] EMPTY_ACTION_ARRAY = AnAction.EMPTY_ARRAY;

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        final boolean isTapestryModule = TapestryUtils.isTapestryModule(event.getData(PlatformCoreDataKeys.MODULE));
        final Presentation presentation = event.getPresentation();
        presentation.setEnabledAndVisible(isTapestryModule);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        if (event == null)
            return EMPTY_ACTION_ARRAY;


        PsiFile psiFile = ClassTemplateNavigation.getEventPsiFile(event);

        if (psiFile == null)
            return EMPTY_ACTION_ARRAY;

        if (psiFile instanceof PsiClassOwner && event.getPresentation().getText()
                .equals("Tapestry Template")) {
            TemplateNavigate templateNavigate;

            DefaultActionGroup actions = DefaultActionGroup.createPopupGroup(() -> "TemplatesGroup");
            PresentationLibraryElement tapestryElement;
            try {
                PsiClass psiClass = IdeaUtils.findPublicClass(psiFile);

                // does current file contain a class ?
                if (psiClass == null) {
                    return EMPTY_ACTION_ARRAY;
                }

              Module module = event.getData(PlatformCoreDataKeys.MODULE);
              tapestryElement = PresentationLibraryElement.createProjectElementInstance(
                new IntellijJavaClassType(module, psiClass.getContainingFile()),
                TapestryModuleSupportLoader.getTapestryProject(module));
            } catch (NotTapestryElementException ex) {
                return EMPTY_ACTION_ARRAY;
            }

            if (tapestryElement != null && tapestryElement.allowsTemplate()) {
                for (int i = 0; i < tapestryElement.getTemplate().length; i++) {
                    templateNavigate = new TemplateNavigate(tapestryElement, tapestryElement.getTemplate()[i]);
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

    private static class TemplateNavigate extends AnAction {

      final PresentationLibraryElement tapestryElement;

      TemplateNavigate(PresentationLibraryElement tapestryElement, IResource template) {
            super(template.getName().replace("_", "__"), template.getName(), null);
        this.tapestryElement = tapestryElement;
      }

        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            Project project = event.getData(CommonDataKeys.PROJECT);
            if (project == null) return;

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