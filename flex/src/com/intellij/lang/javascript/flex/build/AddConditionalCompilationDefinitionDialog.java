package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class AddConditionalCompilationDefinitionDialog
  extends AddRemoveTableRowsDialog.AddObjectDialog<FlexBuildConfiguration.ConditionalCompilationDefinition> {

  private JPanel myMainPanel;
  private JTextField myNameTextField;
  private JTextField myValueTextField;

  private FlexBuildConfiguration.ConditionalCompilationDefinition myConditionalCompilationDefinition =
    new FlexBuildConfiguration.ConditionalCompilationDefinition();

  public AddConditionalCompilationDefinitionDialog(final Project project) {
    super(project);
    setTitle(FlexBundle.message("add.conditional.compilation.definition.title"));

    myNameTextField.setText(myConditionalCompilationDefinition.NAME);
    myValueTextField.setText(myConditionalCompilationDefinition.VALUE);

    init();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameTextField;
  }

  protected void doOKAction() {
    final String name = myNameTextField.getText().trim();
    if (name.matches(FlexCompiler.CONDITIONAL_COMPILATION_VARIABLE_PATTERN)) {
      super.doOKAction();
    }
    else {
      setErrorText(FlexBundle.message("bad.conditional.compilation.var.name"));
    }
  }

  public FlexBuildConfiguration.ConditionalCompilationDefinition getAddedObject() {
    myConditionalCompilationDefinition.NAME = myNameTextField.getText().trim();
    myConditionalCompilationDefinition.VALUE = myValueTextField.getText().trim();
    return myConditionalCompilationDefinition;
  }


}
