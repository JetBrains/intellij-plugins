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
import com.intellij.tapestry.intellij.view.nodes.MixinsNode;
import org.jetbrains.annotations.NotNull;

/**
 * Action that creates a new mixin.
 */
public class AddNewMixinAction extends AddNewElementAction<MixinsNode> {
  public AddNewMixinAction() {
    super(MixinsNode.class);
  }


  @Override
  protected String getElementsRootPackage(@NotNull TapestryProject tapestryProject) {
    return tapestryProject.getMixinsRootPackage();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Module module = event.getData(PlatformCoreDataKeys.MODULE);
    if (module == null) return;

    String defaultMixinPath = getDefaultElementPath(event, module);
    if (defaultMixinPath == null) return;

    final AddNewMixinDialog addNewMixinDialog =
        new AddNewMixinDialog(module, defaultMixinPath);

    final DialogBuilder builder = new DialogBuilder(module.getProject());
    builder.setCenterPanel(addNewMixinDialog.getContentPane());
    builder.setTitle("New Tapestry Mixin");
    builder.setPreferredFocusComponent(addNewMixinDialog.getNameComponent());

    builder.setOkOperation(() -> {
      final String mixinName = addNewMixinDialog.getNewMixinName();

      if (!Validators.isValidComponentName(mixinName)) {
        Messages.showErrorDialog("Invalid mixin name!", CommonBundle.getErrorTitle());
        return;
      }

      // Set default values
      String classSourceDir = addNewMixinDialog.getClassSourceDirectory().getPath();

      TapestryModuleSupportLoader.getInstance(module).getState().setNewPagesClassesSourceDirectory(classSourceDir);

      ApplicationManager.getApplication().runWriteAction(() -> {
        try {
          PsiDirectory classSourceDirectory =
              PsiManager.getInstance(module.getProject()).findDirectory(addNewMixinDialog.getClassSourceDirectory());
          TapestryUtils.createMixin(module, classSourceDirectory, mixinName, addNewMixinDialog.isReplaceExistingFiles());
        }
        catch (IllegalStateException ex) {
          Messages.showWarningDialog(module.getProject(), ex.getMessage(), "Error creating mixin");
        }
      });
      builder.getWindow().dispose();
    });

    builder.showModal(true);
  }
}