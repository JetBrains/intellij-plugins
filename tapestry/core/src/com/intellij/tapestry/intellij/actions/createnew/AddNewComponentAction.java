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
import com.intellij.tapestry.intellij.view.nodes.ComponentsNode;
import org.jetbrains.annotations.NotNull;

/**
 * Action that creates a new component.
 */
public class AddNewComponentAction extends AddNewElementAction<ComponentsNode> {
  public AddNewComponentAction() {
    super(ComponentsNode.class);
  }

  @Override
  protected String getElementsRootPackage(@NotNull TapestryProject tapestryProject) {
    return tapestryProject.getComponentsRootPackage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Module module = event.getData(PlatformCoreDataKeys.MODULE);
    if (module == null) return;

    String defaultComponentPath = getDefaultElementPath(event, module);
    if (defaultComponentPath == null) return;

    final AddNewComponentDialog addNewComponentDialog =
        new AddNewComponentDialog(module, defaultComponentPath, false);

    final DialogBuilder builder = new DialogBuilder(module.getProject());
    builder.setCenterPanel(addNewComponentDialog.getContentPane());
    builder.setTitle("New Tapestry Component");
    builder.setPreferredFocusComponent(addNewComponentDialog.getNameComponent());

    builder.setOkOperation(() -> {
      final String componentName = addNewComponentDialog.getNewComponentName();

      if (!Validators.isValidComponentName(componentName)) {
        Messages.showErrorDialog("Invalid component name!", CommonBundle.getErrorTitle());
        return;
      }

      // Set default values.
      String classSourceDir = addNewComponentDialog.getClassSourceDirectory().getPath();
      String templateSourceDir = addNewComponentDialog.getTemplateSourceDirectory().getPath();

      TapestryModuleSupportLoader.getInstance(module).getState().setNewComponentsClassesSourceDirectory(classSourceDir);
      TapestryModuleSupportLoader.getInstance(module).getState().setNewComponentsTemplatesSourceDirectory(templateSourceDir);

      ApplicationManager.getApplication().runWriteAction(() -> {
        try {
          PsiDirectory classSourceDirectory =
              PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getClassSourceDirectory());
          PsiDirectory templateSourceDirectory =
              PsiManager.getInstance(module.getProject()).findDirectory(addNewComponentDialog.getTemplateSourceDirectory());

          if (addNewComponentDialog.isNotCreatingTemplate()) {
            TapestryUtils
                .createComponent(module, classSourceDirectory, null, componentName, addNewComponentDialog.isReplaceExistingFiles());
          }
          else {
            TapestryUtils.createComponent(module, classSourceDirectory, templateSourceDirectory, componentName,
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