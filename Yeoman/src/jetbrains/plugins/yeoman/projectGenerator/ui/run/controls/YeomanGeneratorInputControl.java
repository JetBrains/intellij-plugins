package jetbrains.plugins.yeoman.projectGenerator.ui.run.controls;


import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

@SuppressWarnings("unused")
public class YeomanGeneratorInputControl implements YeomanGeneratorControl {

  @NonNls
  private String type;
  @Nls
  private String name;
  @Nls
  private String message;
  @SerializedName("default") @Nls
  private String defaultValue;


  @Override
  public YeomanGeneratorControlUI createUI() {
    final FormBuilder builder = FormBuilder.createFormBuilder();

    final JBTextField textField = new JBTextField();
    if (!StringUtil.isEmpty(defaultValue)) {
      textField.setText(defaultValue);
    }

    builder.addComponent(YeomanGeneratorControlUtil.buildTitle(message));
    builder.addComponent(textField);
    return new YeomanGeneratorControlUI() {
      @Override
      public JComponent getComponent() {
        return builder.getPanel();
      }

      @Override
      public String getSelectedValue() {
        return textField.getText();
      }
    };
  }
}
