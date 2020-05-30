package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.search.FileTypeIndex;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class CreateDartFileAction extends CreateFileFromTemplateAction {
  public CreateDartFileAction() {
    super(DartBundle.messagePointer("dart.file"), DartBundle.messagePointer("dart.file"), DartIcons.Dart_file);
  }

  @Override
  protected boolean isAvailable(DataContext dataContext) {
    final Module module = LangDataKeys.MODULE.getData(dataContext);
    return super.isAvailable(dataContext) &&
           module != null &&
           (FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, module.getModuleContentScope()) ||
            DartSdk.getDartSdk(module.getProject()) != null && ModuleTypeWithWebFeatures.isAvailable(module));
  }

  @Override
  protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
    return DartBundle.message("create.dart.file.0", newName);
  }

  @Override
  protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder
      .setTitle(DartBundle.message("new.dart.file.title"))
      .addKind(DartBundle.message("dart.file"), DartIcons.Dart_file, "Dart File");
  }
}
