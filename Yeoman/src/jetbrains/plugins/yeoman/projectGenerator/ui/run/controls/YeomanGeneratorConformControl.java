package jetbrains.plugins.yeoman.projectGenerator.ui.run.controls;


import com.google.gson.annotations.SerializedName;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class YeomanGeneratorConformControl implements YeomanGeneratorControl {
  @SerializedName("type")
  public String type;
  @SerializedName("name")
  public String name;
  @SerializedName("message") @Nls
  public String message;
  @SerializedName("default")
  public boolean defaultValue;


  @Override
  public YeomanGeneratorControlUI createUI() {
    return new YeomanGeneratorControlUI() {
      private final JCheckBox myBox;
      private final JPanel myPanel;

      {
        FormBuilder builder = FormBuilder.createFormBuilder();
        myBox = new JCheckBox();
        builder.addLabeledComponent(myBox, new JBLabel(message));
        myBox.setSelected(defaultValue);

        myPanel = builder.getPanel();
      }

      @Override
      public JComponent getComponent() {
        return myPanel;
      }

      @Override
      public String getSelectedValue() {
        return myBox.isSelected() ? "1" : "0";
      }
    };
  }
}
