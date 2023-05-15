package com.intellij.lang.javascript.validation.fixes;

import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.javascript.ui.ActionScriptPackageChooserDialog;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.util.ThreeState;

import javax.swing.*;

public class ChoosePackageDialog extends DialogWrapper{
  private static final String DESTINATION_PACKAGE_RECENT_KEY = "ChoosePackageDialog.DESTINATION_PACKAGE_RECENT_KEY";

  private JPanel myMainPanel;
  private JSReferenceEditor myPackageCombo;

  private final Module myModule;
  private final String myPackageNameInitial;
  private final PsiFile myContextFile;
  private PsiDirectory myTargetDirectory;

  protected ChoosePackageDialog(final Module module, final String title, final String packageNameInitial, final PsiFile contextFile) {
    super(module.getProject());
    myModule = module;
    myPackageNameInitial = packageNameInitial;
    myContextFile = contextFile;
    setTitle(title);
    init();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPackageCombo.getChildComponent();
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected void doOKAction() {
    final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavascriptLanguage.INSTANCE);
    final String packageName = getPackageName();
    for (final String s : StringUtil.split(packageName, ".")) {
      if (!namesValidator.isIdentifier(s, null)) {
        setErrorText(FlexBundle.message("invalid.package", packageName), myPackageCombo);
        return;
      }
    }
    myPackageCombo.updateRecents();

    myTargetDirectory = JSRefactoringUtil.chooseOrCreateDirectoryForClass(myModule.getProject(), myModule, getPackageScope(), packageName,
                                                                          null, myContextFile.getParent(), ThreeState.UNSURE);
    if (myTargetDirectory != null) {
      super.doOKAction();
    }
  }

  private GlobalSearchScope getPackageScope() {
    return PlatformPackageUtil.adjustScope(myContextFile.getParent(), GlobalSearchScope.moduleWithDependenciesScope(myModule), false, true);
  }

  private void createUIComponents() {
    myPackageCombo = ActionScriptPackageChooserDialog.createPackageReferenceEditor(myPackageNameInitial, myModule.getProject(), DESTINATION_PACKAGE_RECENT_KEY,
                                                                                   getPackageScope(), RefactoringBundle.message("choose.destination.package"));
  }

  public String getPackageName() {
    return myPackageCombo.getText().trim();
  }

  public PsiDirectory getTargetDirectory() {
    return myTargetDirectory;
  }
}
