package jetbrains.plugins.yeoman.projectGenerator.ui.run.controls;

import javax.swing.*;

public interface YeomanGeneratorControl {
  interface YeomanGeneratorControlUI {
    JComponent getComponent();
    String getSelectedValue();
  }

  YeomanGeneratorControlUI createUI();
}
