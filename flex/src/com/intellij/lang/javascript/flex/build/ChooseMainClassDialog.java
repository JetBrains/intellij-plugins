package com.intellij.lang.javascript.flex.build;

import com.intellij.execution.ExecutionBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.*;

public class ChooseMainClassDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JLabel myMainClassLabel;
  private JSReferenceEditor myMainClassTextWithBrowse;
  private final Module myModule;

  public ChooseMainClassDialog(final Module module,
                               final String presentableModuleOrFacetName,
                               final String mainClassName,
                               final String title) {
    super(module.getProject(), true);
    // initialize module before createUIComponents() call
    myModule = module;
    setTitle(title);
    myMainClassLabel.setLabelFor(myMainClassTextWithBrowse);
    myMainClassLabel.setText(FlexBundle.message("main.class.for", presentableModuleOrFacetName));
    myMainClassTextWithBrowse.setText(mainClassName);
    init();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myMainClassTextWithBrowse.getChildComponent();
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public String getMainClassName() {
    return myMainClassTextWithBrowse.getText().trim();
  }

  private void createUIComponents() {
    final Condition<JSClass> filter = FlexCompilerSettingsEditor.createMainClassFilter(myModule);
    myMainClassTextWithBrowse =
      JSReferenceEditor.forClassName("", myModule.getProject(), null, GlobalSearchScope.moduleScope(myModule), null,
                                     filter, ExecutionBundle.message("choose.main.class.dialog.title"));
  }
}
