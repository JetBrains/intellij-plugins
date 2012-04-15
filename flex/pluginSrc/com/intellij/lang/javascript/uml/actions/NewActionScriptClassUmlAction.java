package com.intellij.lang.javascript.uml.actions;

import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSIconProvider;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;

public class NewActionScriptClassUmlAction extends NewJSClassUmlActionBase {

  public NewActionScriptClassUmlAction() {
    super(JSBundle.message("new.actionscript.class.uml.action.text"), JSBundle.message("new.actionscript.class.action.description"),
          JSIconProvider.DEFAULT_INSTANCE.getClassIcon());
  }

  @Override
  public String getActionName() {
    return JSBundle.message("new.actionscript.class.command.name");
  }

  @Nullable
  @Override
  protected PreparationData showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage) {
    final MyDialog dialog = new MyDialog(project, dirAndPackage.first, dirAndPackage.second);
    dialog.setTitle(JSBundle.message("new.actionscript.class.dialog.title"));

    for (FileTemplate fileTemplate : CreateClassOrInterfaceAction
      .getApplicableTemplates(CreateClassOrInterfaceAction.ACTIONSCRIPT_TEMPLATES_EXTENSIONS)) {
      String templateName = fileTemplate.getName();
      String shortName = CreateClassOrInterfaceAction.getTemplateShortName(templateName);
      Icon icon = CreateClassOrInterfaceAction.getTemplateIcon(templateName);
      dialog.addItem(shortName, icon, templateName);
    }

    dialog.show();
    if (!dialog.isOK()) return null;
    return new PreparationData(dialog.getQualifiedName(), dialog.getSelectedTemplate(), Collections.<String, String>emptyMap(),
                               dialog.myDirectory);
  }

  @Nullable
  @Override
  protected JSClass getClass(PsiFile file, PreparationData data) {
    return JSPsiImplUtils.findClass((JSFile)file);
  }

  private static class MyDialog extends CreateFileFromTemplateDialog {

    private final Project myProject;
    private PsiDirectory myDirectory;

    protected MyDialog(Project project, @Nullable PsiDirectory directory, String initialPackage) {
      super(project);
      myProject = project;
      myDirectory = directory;
      if (initialPackage.length() > 0) {
        getNameField().setText(initialPackage + ".");
      }
    }

    public void addItem(String shortName, Icon icon, String templateName) {
      getKindCombo().addItem(shortName, icon, templateName);
    }

    @Override
    protected void doOKAction() {
      String qName = getQualifiedName();
      if (CreateClassOrInterfaceAction.isClassifierTemplate(getSelectedTemplate()) && !JSUtils.isValidClassName(qName, true)) {
        Messages.showErrorDialog(getContentPane(), JSBundle.message("0.is.not.a.legal.name", qName), CommonBundle.getErrorTitle());
        return;
      }

      Module module = myDirectory != null ? ModuleUtil.findModuleForPsiElement(myDirectory) : null;
      GlobalSearchScope scope = GlobalSearchScope.projectScope(myProject);
      myDirectory =
        JSRefactoringUtil.chooseOrCreateDirectoryForClass(myProject, module, scope, StringUtil.getPackageName(qName),
                                                          StringUtil.getShortName(qName), myDirectory, ThreeState.UNSURE);
      if (myDirectory == null) {
        return;
      }
      close(DialogWrapper.OK_EXIT_CODE);
    }

    public String getQualifiedName() {
      return getNameField().getText();
    }

    public String getSelectedTemplate() {
      return getKindCombo().getSelectedName();
    }
  }
}
