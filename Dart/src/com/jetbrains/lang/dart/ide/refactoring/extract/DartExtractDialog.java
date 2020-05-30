package com.jetbrains.lang.dart.ide.refactoring.extract;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.DartControlFlow;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DartExtractDialog extends DialogWrapper {
  private final DartControlFlow myScope;
  private JLabel mySignatureLabel;
  private JPanel myMainPanel;
  private JTextField myFunctionNameField;

  protected DartExtractDialog(@Nullable Project project, String functionName, DartControlFlow scope) {
    super(project);
    myScope = scope;
    myFunctionNameField.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        mySignatureLabel.setText(myScope.getSignature(getFunctionName()));
      }

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          doOKAction();
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
      }
    });
    setTitle(DartBundle.message("dart.extract.method"));
    init();
    mySignatureLabel.setText(myScope.getSignature(functionName));
    myFunctionNameField.setText(functionName);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myFunctionNameField;
  }

  public String getFunctionName() {
    return myFunctionNameField.getText();
  }

  @Nullable
  @Override
  protected ValidationInfo doValidate() {
    if (!StringUtil.isJavaIdentifier(getFunctionName())) {
      return new ValidationInfo(DartBundle.message("validation.info.not.a.valid.name"), myFunctionNameField);
    }
    return super.doValidate();
  }
}
