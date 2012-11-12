package com.intellij.lang.javascript.uml.actions;

import com.intellij.CommonBundle;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.actions.newfile.NewFlexComponentAction;
import com.intellij.lang.javascript.flex.actions.newfile.NewFlexComponentDialog;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
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
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ThreeState;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class NewFlexComponentUmlAction extends NewJSClassUmlActionBase {

  public NewFlexComponentUmlAction() {
    super(JSBundle.message("new.flex.component.uml.action.text"), JSBundle.message("new.flex.component.action.description"),
          JavaScriptLanguageIcons.Flex.XmlBackedClass);
  }

  @Override
  public String getActionName() {
    return JSBundle.message("new.flex.component.command.name");
  }

  @Override
  protected PreparationData showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage) {
    MyDialog dialog = new MyDialog(project, dirAndPackage.first, dirAndPackage.second);
    dialog.setTitle(JSBundle.message("new.flex.component.dialog.title"));
    dialog.show();
    if (!dialog.isOK()) return null;
    return new PreparationData(dialog.getQualifiedName(), dialog.getSelectedTemplate(), dialog.getCustomProperties(), dialog.myDirectory);
  }

  @Nullable
  @Override
  protected JSClass getClass(PsiFile file, PreparationData data) {
    return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)file);
  }


  private static class MyDialog extends NewFlexComponentDialog {

    private PsiDirectory myDirectory;

    public MyDialog(Project project, @Nullable PsiDirectory directory, String initialPackage) {
      super(project, directory);
      myDirectory = directory;

      if (initialPackage.length() > 0) {
        getNameField().setText(initialPackage + ".");
      }
    }

    public String getQualifiedName() {
      return getNameField().getText();
    }

    public String getSelectedTemplate() {
      return getKindCombo().getSelectedName();
    }

    public Map<String, String> getCustomProperties() {
      return Collections.singletonMap(PARENT_COMPONENT, myParentComponentField.getText());
    }

    @Override
    protected void doOKAction() {
      String qName = getQualifiedName();
      if (NewFlexComponentAction.isClassifierTemplate(getSelectedTemplate()) && !JSUtils.isValidClassName(qName, true)) {
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

  }
}
