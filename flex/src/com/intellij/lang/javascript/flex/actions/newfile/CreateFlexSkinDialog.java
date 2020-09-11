package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.PublicInheritorFilter;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.util.ThreeState;

import javax.swing.*;

public class CreateFlexSkinDialog extends DialogWrapper {
  private static final String SKINNABLE_COMPONENT_CLASS = "spark.components.supportClasses.SkinnableComponent";
  private static final String DESTINATION_PACKAGE_RECENT_KEY = "CreateFlexSkinDialog.DESTINATION_PACKAGE_RECENT_KEY";
  private static final String HOST_COMPONENT_RECENT_KEY = "CreateFlexSkinDialog.HOST_COMPONENT_RECENT_KEY";

  private JPanel myMainPanel;
  private JSReferenceEditor myPackageCombo;
  private JSReferenceEditor myHostComponentCombo;

  private final Module myModule;
  private final String myPackageNameInitial;
  private final PsiFile myContextFile;
  private final String myHostComponentInitial;
  private PsiDirectory myTargetDirectory;

  protected CreateFlexSkinDialog(final Module module,
                                 final String skinName,
                                 final String packageName,
                                 final String hostComponent,
                                 PsiFile contextFile) {
    super(module.getProject());
    myModule = module;
    myPackageNameInitial = packageName;
    myContextFile = contextFile;
    myHostComponentInitial = hostComponent;

    setTitle(FlexBundle.message("create.skin", skinName));

    init();
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPackageCombo.getChildComponent();
  }

  public String getPackageName() {
    return myPackageCombo.getText().trim();
  }

  public String getHostComponent() {
    return myHostComponentCombo.getText().trim();
  }

  @Override
  protected void doOKAction() {
    final NamesValidator namesValidator = LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage());
    final String packageName = getPackageName();
    for (final String s : StringUtil.split(packageName, ".")) {
      if (!namesValidator.isIdentifier(s, null)) {
        setErrorText(FlexBundle.message("invalid.package", packageName), myPackageCombo);
        return;
      }
    }
    myHostComponentCombo.updateRecents();
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
    myPackageCombo = JSReferenceEditor.forPackageName(myPackageNameInitial, myModule.getProject(), DESTINATION_PACKAGE_RECENT_KEY,
                                                      getPackageScope(), RefactoringBundle.message("choose.destination.package"));
    myHostComponentCombo = createHostComponentCombo(myHostComponentInitial, myModule);
  }

  public static GlobalSearchScope getHostComponentScope(Module module) {
    return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
  }

  public static JSReferenceEditor createHostComponentCombo(String text, Module module) {
    final GlobalSearchScope scope = getHostComponentScope(module);
    Condition<JSClass> filter = new PublicInheritorFilter(module.getProject(), SKINNABLE_COMPONENT_CLASS, scope, true);
    return JSReferenceEditor.forClassName(text, module.getProject(), HOST_COMPONENT_RECENT_KEY, scope, null, filter,
                                          FlexBundle.message("choose.host.component"));
  }

  public PsiDirectory getTargetDirectory() {
    return myTargetDirectory;
  }
}
