package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.module.DartModuleType;
import com.jetbrains.lang.dart.util.DartFileTemplateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Properties;

/**
 * @author: Fedor.Korotkov
 */
public class CreateDartFileAction extends CreateFromTemplateAction<PsiFile> {
  public CreateDartFileAction() {
    super(DartBundle.message("action.create.new.file"), DartBundle.message("action.create.new.file"), icons.DartIcons.Dart_16);
  }

  @Override
  protected boolean isAvailable(DataContext dataContext) {
    final Module module = LangDataKeys.MODULE.getData(dataContext);
    final ModuleType moduleType = module == null ? null : ModuleType.get(module);
    final boolean isWebOrDartModule = moduleType instanceof WebModuleTypeBase || moduleType instanceof DartModuleType;
    return super.isAvailable(dataContext) && isWebOrDartModule;
  }

  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return DartBundle.message("progress.creating.file", newName);
  }

  @Override
  protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle(IdeBundle.message("action.create.new.class"));
    for (FileTemplate fileTemplate : DartFileTemplateUtil.getApplicableTemplates()) {
      final String templateName = fileTemplate.getName();
      final String shortName = DartFileTemplateUtil.getTemplateShortName(templateName);
      final Icon icon = DartFileTemplateUtil.getTemplateIcon(templateName);
      builder.addKind(shortName, icon, templateName);
    }
  }

  @Nullable
  @Override
  protected PsiFile createFile(String className, String templateName, PsiDirectory dir) {
    try {
      return createFile(className, dir, templateName).getContainingFile();
    }
    catch (Exception e) {
      throw new IncorrectOperationException(e.getMessage(), e);
    }
  }

  private static PsiElement createFile(String className, @NotNull PsiDirectory directory, final String templateName)
    throws Exception {
    final Properties props = new Properties(FileTemplateManager.getInstance().getDefaultProperties(directory.getProject()));
    props.setProperty(FileTemplate.ATTRIBUTE_NAME, className);

    final FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate(templateName);

    return FileTemplateUtil.createFromTemplate(template, className, props, directory, CreateDartFileAction.class.getClassLoader());
  }
}
