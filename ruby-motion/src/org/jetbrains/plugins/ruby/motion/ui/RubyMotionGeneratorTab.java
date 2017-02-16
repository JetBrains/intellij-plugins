package org.jetbrains.plugins.ruby.motion.ui;

import org.jetbrains.plugins.ruby.wizard.RubySdkPanel;

public class RubyMotionGeneratorTab extends RubyMotionGeneratorTabBase {
  public RubyMotionGeneratorTab(RubyMotionSettingsHolder settingsHolder) {
    super(settingsHolder);
  }

  @Override
  protected RubySdkPanel getSdkComboPanel() {
    return null;
  }
}
