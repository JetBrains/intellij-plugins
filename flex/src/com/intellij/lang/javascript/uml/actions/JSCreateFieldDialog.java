package com.intellij.lang.javascript.uml.actions;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.parsing.JavaScriptParserBase;
import com.intellij.lang.javascript.psi.JSElementFactory;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.refactoring.ui.JSEditorTextField;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.refactoring.ui.JSVisibilityPanel;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSCreateFieldDialog extends DialogWrapper {
  private JSReferenceEditor myTypeField;
  private JLabel myTypeLabel;
  private EditorTextField myNameField;
  private JCheckBox myDeclareStaticCb;
  private JPanel myContentPane;
  private JSVisibilityPanel myVisibilityPanel;
  private JLabel myNameLabel;
  private EditorTextField myInitializerField;
  private JLabel myInitializerLabel;
  private JCheckBox myDeclareConstantCb;
  private final JSClass myTargetClass;

  private static boolean ourDeclareConstant;
  private static boolean ourDeclareStatic;

  public JSCreateFieldDialog(JSClass clazz) {
    super(clazz.getProject(), true);
    myTargetClass = clazz;

    setTitle(JavaScriptBundle.message("create.field.dialog.title"));
    myVisibilityPanel.configureForClassMember(false, false, DialectDetector.dialectOfElement(clazz));
    myVisibilityPanel.setVisibility(JSAttributeList.AccessType.PRIVATE.name());
    myTypeLabel.setLabelFor(myTypeField.getChildComponent());
    myNameLabel.setLabelFor(myNameField);
    myInitializerLabel.setLabelFor(myInitializerField);

    myDeclareConstantCb.setSelected(ourDeclareConstant);
    myDeclareStaticCb.setSelected(ourDeclareStatic);
    init();
  }

  private void createUIComponents() {
    Module module = ModuleUtilCore.findModuleForPsiElement(myTargetClass);
    GlobalSearchScope scope = getTypeFieldScope(module, myTargetClass.getProject());
    myTypeField = createTypeField(myTargetClass.getProject(), scope);

    PsiCodeFragment initializerCodeFragment = createInitializerCodeFragment(myTargetClass);
    Document document = PsiDocumentManager.getInstance(myTargetClass.getProject()).getDocument(initializerCodeFragment);
    myInitializerField = new JSEditorTextField(myTargetClass.getProject(), document);
  }

  public static GlobalSearchScope getTypeFieldScope(@Nullable Module module, @NotNull Project project) {
    return module != null
           ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
           : GlobalSearchScope.allScope(project);
  }

  public static JSReferenceEditor createTypeField(Project project, GlobalSearchScope scope) {
    return JSReferenceEditor.forClassName("", project, "JSCreateFieldDialog", scope, JavaScriptParserBase.ForceContext.Type, null,
                                          JavaScriptBundle.message("choose.field.type"));
  }

  public static JSExpressionCodeFragment createInitializerCodeFragment(JSClass c) {
    return JSElementFactory.createExpressionCodeFragment(c.getProject(), "", c, JavaScriptSupportLoader.ECMA_SCRIPT_L4,
                                                         c.getResolveScope(), JSElementFactory.TopLevelCompletion.LITERAL_VALUES, null);
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContentPane;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTypeField.getChildComponent();
  }

  public String getFieldName() {
    return myNameField.getText();
  }

  public String getFieldType() {
    return myTypeField.getText();
  }

  public boolean isStatic() {
    return myDeclareStaticCb.isSelected();
  }

  public String getVisibility() {
    return myVisibilityPanel.getVisibility();
  }

  /**
   * @return pair(message, isFatal)
   */
  @Nullable
  private Pair<String, Boolean> validateData() {
    if (!JSRefactoringUtil.isValidIdentifier(getFieldName(), myTargetClass.getProject())) {
      return Pair.create(JavaScriptBundle.message("invalid.identifier.value.0", getFieldName()), true);
    }
    String type = getFieldType().trim();
    if ("void".equals(type) || type.contains(" ") || JSChangeUtil.tryCreateTypeElement(type, myTargetClass) == null) {
      return Pair.create(JavaScriptBundle.message("invalid.field.type.expression", type), true);
    }
    if (isConstant() && StringUtil.isEmpty(getInitializer())) {
      return Pair.create(JavaScriptBundle.message("field.initializer.is.not.specified"), true);
    }
    if (!JSRefactoringUtil.isResolvableType(type, myTargetClass, false, false)) {
      return Pair.create(JavaScriptBundle.message("type.is.not.resolved", type), false);
    }
    if (myTargetClass.findFieldByName(getFieldName()) != null) {
      return Pair.create(JavaScriptBundle.message("class.already.contains.field.warning", myTargetClass.getQualifiedName(), getFieldName()), false);
    }
    return null;
  }

  public boolean isConstant() {
    return myDeclareConstantCb.isSelected();
  }

  public String getInitializer() {
    return myInitializerField.getText();
  }

  @Override
  protected String getDimensionServiceKey() {
    return "JSCreateFieldDialog";
  }

  @Override
  protected void doOKAction() {
    Pair<String, Boolean> errorStatus = validateData();
    if (errorStatus != null) {
      if (errorStatus.second) {
        Messages.showErrorDialog(myContentPane, errorStatus.first, getTitle());
        return;
      }
      else {
        if (Messages.showYesNoDialog(myContentPane, errorStatus.first, getTitle(), Messages.getQuestionIcon()) != Messages.YES) {
          return;
        }
      }
    }
    myTypeField.updateRecents();
    //noinspection AssignmentToStaticFieldFromInstanceMethod
    ourDeclareConstant = myDeclareConstantCb.isSelected();
    //noinspection AssignmentToStaticFieldFromInstanceMethod
    ourDeclareStatic = myDeclareStaticCb.isSelected();
    super.doOKAction();
  }
}
