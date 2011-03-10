package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateTemplateInPackageAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Map;

public abstract class NewJSClassActionBase extends CreateTemplateInPackageAction<PsiFile> {
  private final Collection<String> myTemplatesExtensions;

  protected NewJSClassActionBase(String text, String description, Icon icon, Collection<String> templatesExtensions) {
    super(text, description, icon, true);
    myTemplatesExtensions = templatesExtensions;
  }

  @Override
  protected boolean isAvailable(DataContext dataContext) {
    final Module module = LangDataKeys.MODULE.getData(dataContext);
    return super.isAvailable(dataContext) && module != null && FlexUtils.isFlexModuleOrContainsFlexFacet(module);
  }

  @Override
  protected PsiElement getNavigationElement(@NotNull PsiFile createdElement) {
    return createdElement;
  }

  @Override
  protected boolean checkPackageExists(PsiDirectory directory) {
    return DirectoryIndex.getInstance(directory.getProject()).getPackageName(directory.getVirtualFile()) != null;
  }

  @Override
  protected void doCheckCreate(PsiDirectory dir, String className, String templateName)
    throws IncorrectOperationException {
    if (CreateClassOrInterfaceAction.isClassifierTemplate(templateName) && !JSUtils.isValidClassName(className, false)) {
      throw new IncorrectOperationException(JSBundle.message("0.is.not.a.legal.name", className));
    }

    for (String extension : myTemplatesExtensions) {
      dir.checkCreateFile(className + "." + extension);
    }
  }

  @Nullable
  @Override
  protected PsiFile doCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
    return doCreate(dir, className, templateName, null);
  }

  @Nullable
  protected PsiFile doCreate(PsiDirectory dir, String className, String templateName, Map<String, String> customProperties)
    throws IncorrectOperationException {
    String packageName = DirectoryIndex.getInstance(dir.getProject()).getPackageName(dir.getVirtualFile());
    try {
      PsiFile file = (PsiFile)CreateClassOrInterfaceAction.createClass(className, packageName, dir, templateName);
      return file;
    }
    catch (Exception e) {
      throw new IncorrectOperationException(e.getMessage(), e);
    }
  }


  @Override
  protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle(getDialogTitle());
    for (FileTemplate fileTemplate : CreateClassOrInterfaceAction.getApplicableTemplates(myTemplatesExtensions)) {
      String templateName = fileTemplate.getName();
      String shortName = CreateClassOrInterfaceAction.getTemplateShortName(templateName);
      Icon icon = CreateClassOrInterfaceAction.getTemplateIcon(templateName);
      builder.addKind(shortName, icon, templateName);
    }
  }

  protected abstract String getDialogTitle();
}
