package com.intellij.lang.javascript.flex;

import com.intellij.application.options.editor.AutoImportOptionsProvider;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ActionScriptAutoImportOptionsProvider implements AutoImportOptionsProvider {

  private static final String ADD_IMPORTS_ON_THE_FLY_PROPERTY = "ActionScript.add.unambiguous.imports.on.the.fly";
  private static final boolean ADD_IMPORTS_ON_THE_FLY_DEFAULT = false;

  private JPanel myMainPanel;
  private JBCheckBox myOnTheFlyCheckBox;

  static boolean isAddUnambiguousImportsOnTheFly() {
    return PropertiesComponent.getInstance().getBoolean(ADD_IMPORTS_ON_THE_FLY_PROPERTY, ADD_IMPORTS_ON_THE_FLY_DEFAULT);
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public boolean isModified() {
    return myOnTheFlyCheckBox.isSelected() != isAddUnambiguousImportsOnTheFly();
  }

  @Override
  public void apply() {
    final boolean onTheFly = myOnTheFlyCheckBox.isSelected();
    PropertiesComponent.getInstance().setValue(ADD_IMPORTS_ON_THE_FLY_PROPERTY, onTheFly, ADD_IMPORTS_ON_THE_FLY_DEFAULT);
  }

  @Override
  public void reset() {
    myOnTheFlyCheckBox.setSelected(isAddUnambiguousImportsOnTheFly());
  }
}
