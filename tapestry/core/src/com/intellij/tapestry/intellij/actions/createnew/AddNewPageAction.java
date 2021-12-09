package com.intellij.tapestry.intellij.actions.createnew;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.util.Validators;
import com.intellij.tapestry.intellij.view.nodes.PagesNode;
import org.jetbrains.annotations.NotNull;

/**
 * Action that creates a new page.
 */
public class AddNewPageAction extends AddNewElementAction<PagesNode> {

  public AddNewPageAction() {
    super(PagesNode.class);
  }

  @Override
  protected String getElementsRootPackage(@NotNull TapestryProject tapestryProject) {
    return tapestryProject.getPagesRootPackage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Module module = event.getData(PlatformCoreDataKeys.MODULE);
    if (module == null) return;

    String defaultPagePath = getDefaultElementPath(event, module);
    if (defaultPagePath == null) return;

    final AddNewComponentDialog addNewComponentDialog =
        new AddNewComponentDialog(module, defaultPagePath, true);

    final DialogBuilder builder = new DialogBuilder(module.getProject());
    builder.setCenterPanel(addNewComponentDialog.getContentPane());
    builder.setTitle("New Tapestry Page");
    builder.setPreferredFocusComponent(addNewComponentDialog.getNameComponent());

    builder.setOkOperation(() -> {
      final String pageName = addNewComponentDialog.getNewComponentName();

      if (!Validators.isValidComponentName(pageName)) {
        Messages.showErrorDialog("Invalid page name!", CommonBundle.getErrorTitle());
        return;
      }

      // Set default values
      String classSourceDir = addNewComponentDialog.getClassSourceDirectory().getPath();
      String templateSourceDir = addNewComponentDialog.getTemplateSourceDirectory().getPath();

      TapestryModuleSupportLoader.getInstance(module).getState().setNewPagesClassesSourceDirectory(classSourceDir);
      TapestryModuleSupportLoader.getInstance(module).getState().setNewPagesTemplatesSourceDirectory(templateSourceDir);

      ApplicationManager.getApplication().runWriteAction(() -> {
        try {
          PsiDirectory classSourceDirectory =
              PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getClassSourceDirectory());
          PsiDirectory templateSourceDirectory =
              PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getTemplateSourceDirectory());

          if (addNewComponentDialog.isNotCreatingTemplate()) {
            TapestryUtils.createPage(module, classSourceDirectory, null, pageName, addNewComponentDialog.isReplaceExistingFiles());
          }
          else {
            TapestryUtils.createPage(module, classSourceDirectory, templateSourceDirectory, pageName,
                                     addNewComponentDialog.isReplaceExistingFiles());
          }

        }
        catch (IllegalStateException ex) {
          Messages.showWarningDialog(module.getProject(), ex.getMessage(), "Error creating page");
        }
      });
      builder.getWindow().dispose();
    });

    builder.showModal(true);
  }
}