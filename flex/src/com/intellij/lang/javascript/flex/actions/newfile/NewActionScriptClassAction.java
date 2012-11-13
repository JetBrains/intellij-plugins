package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.IdeView;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.psi.PsiDirectory;

public class NewActionScriptClassAction extends AnAction {

  public void update(final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();

    final boolean enabled = isAvailable(dataContext);

    presentation.setVisible(enabled);
    presentation.setEnabled(enabled);
  }

  private boolean isAvailable(DataContext dataContext) {
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if (project == null || view == null) return false;

    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    for (PsiDirectory dir : view.getDirectories()) {
      if (projectFileIndex.isInSourceContent(dir.getVirtualFile()) &&
          DirectoryIndex.getInstance(dir.getProject()).getPackageName(dir.getVirtualFile()) != null) {
        Module module = ModuleUtilCore.findModuleForPsiElement(dir);
        if (module != null && isAvailableIn(module)) {
          return true;
        }
      }
    }

    return false;
  }

  protected boolean isAvailableIn(final Module module) {
    return ModuleType.get(module) == FlexModuleType.getInstance();
  }

  public void actionPerformed(final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();

    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if (view == null) {
      return;
    }

    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);

    final PsiDirectory dir = view.getOrChooseDirectory();
    if (dir == null || project == null) return;

    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
      @Override
      public void run() {
        createAction(dir).execute();
      }
    }, getCommandName(), null);

  }

  protected String getCommandName() {
    return JSBundle.message("new.actionscript.class.command.name");
  }

  protected CreateClassOrInterfaceAction createAction(final PsiDirectory dir) {
    return new CreateClassOrInterfaceAction(dir);
  }
}
