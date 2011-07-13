package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.ui.JSMemberSelectionPanel;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CreateFlexUnitTestDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JTextField myTestClassNameTextField;
  private JSReferenceEditor myPackageCombo;
  private JSReferenceEditor mySuperClassField;
  private JCheckBox mySetUpCheckBox;
  private JCheckBox myTearDownCheckBox;
  private JSMemberSelectionPanel myMemberSelectionPanel;

  private final Project myProject;
  private final JSClass myContextClass;
  private PsiDirectory myTargetDirectory;
  private JSClass mySuperClass;

  public CreateFlexUnitTestDialog(final Project project, final JSClass contextClass) {
    super(project);
    myProject = project;
    myContextClass = contextClass;
    myTestClassNameTextField.setText(myContextClass.getName() + "Test");
    setTitle(CodeInsightBundle.message("intention.create.test"));
    init();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myTestClassNameTextField;
  }

  private void createUIComponents() {
    final Module module = ModuleUtil.findModuleForPsiElement(myContextClass);
    assert module != null;

    myPackageCombo = JSReferenceEditor.forPackageName(StringUtil.getPackageName(myContextClass.getQualifiedName()), myProject, null,
                                                      getTestClassPackageScope(module),
                                                      RefactoringBundle.message("choose.destination.package"));

    final Condition<JSClass> filter = new Condition<JSClass>() {
      public boolean value(final JSClass jsClass) {
        final JSAttributeList attributeList = jsClass.getAttributeList();
        return !jsClass.isInterface() && attributeList != null && !attributeList.hasModifier(JSAttributeList.ModifierType.FINAL);
      }
    };

    mySuperClassField = JSReferenceEditor.forClassName("", myProject, null, getSuperClassScope(module), null, filter,
                                                       JSBundle.message("choose.super.class.title"));


    final List<JSMemberInfo> memberInfos = new ArrayList<JSMemberInfo>();
    JSMemberInfo.extractClassMembers(myContextClass, memberInfos, new MemberInfoBase.Filter<JSAttributeListOwner>() {
      public boolean includeMember(final JSAttributeListOwner member) {
        final JSAttributeList attributeList = member.getAttributeList();
        return member instanceof JSFunction &&
               ((JSFunction)member).getKind() == JSFunction.FunctionKind.SIMPLE &&
               attributeList != null &&
               attributeList.getAccessType() == JSAttributeList.AccessType.PUBLIC;
      }
    });
    myMemberSelectionPanel = new JSMemberSelectionPanel("Generate test methods for:", memberInfos, null);
  }

  protected void doOKAction() {
    final Module module = ModuleUtil.findModuleForPsiElement(myContextClass);
    assert module != null;
    final String superClassFqn = mySuperClassField.getText().trim();
    final PsiElement element = JSResolveUtil.findClassByQName(superClassFqn, getSuperClassScope(module));
    mySuperClass = element instanceof JSClass ? (JSClass)element : null;

    myTargetDirectory = JSRefactoringUtil.chooseOrCreateDirectoryForClass(myProject, module, getTestClassPackageScope(module),
                                                                          getPackageName(), getTestClassName(), null, ThreeState.YES);
    if (myTargetDirectory != null) {
      super.doOKAction();
    }
  }

  private static GlobalSearchScope getSuperClassScope(final Module module) {
    return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
  }

  private static GlobalSearchScope getTestClassPackageScope(final Module module) {
    return GlobalSearchScope.moduleWithDependentsScope(module);
  }

  public String getTestClassName() {
    return myTestClassNameTextField.getText().trim();
  }

  public String getPackageName() {
    return myPackageCombo.getText().trim();
  }

  public PsiDirectory getTargetDirectory() {
    return myTargetDirectory;
  }

  @Nullable
  public JSClass getSuperClass() {
    return mySuperClass;
  }

  public boolean isGenerateSetUp() {
    return mySetUpCheckBox.isSelected();
  }

  public boolean isGenerateTearDown() {
    return myTearDownCheckBox.isSelected();
  }

  public JSMemberInfo[] getSelectedMemberInfos() {
    return JSMemberInfo.getSelected(myMemberSelectionPanel.getTable().getSelectedMemberInfos(), myContextClass, Condition.TRUE);
  }
}
