package org.jetbrains.plugins.ruby.motion;

import com.intellij.facet.ui.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionGeneratorTabBase;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionSettingsHolder;
import org.jetbrains.plugins.ruby.wizard.RubyGeneratorPeer;
import org.jetbrains.plugins.ruby.wizard.RubyProjectSharedSettings;

import javax.swing.*;

public class RubyMotionGeneratorPeer extends RubyGeneratorPeer<RubyMotionSettingsHolder> {
  private RubyMotionGeneratorTabBase myGeneratorTab;

  public RubyMotionGeneratorPeer(@NotNull final RubyProjectSharedSettings sharedSettings) {
    super(sharedSettings);
  }

  @NotNull
  @Override
  public RubyMotionSettingsHolder createSettings() {
    return new RubyMotionSettingsHolder();
  }

  @NotNull
  @Override
  public JPanel getSettingsPanel(@NotNull RubyMotionSettingsHolder settings, @NotNull Runnable checkValid) {
    if (myGeneratorTab == null) {
      myGeneratorTab = new RubyMotionGeneratorTabBase(settings);
    }

    mySettings = settings;
    final JComponent generatorTabComponent = myGeneratorTab.createComponent();
    return (JPanel)generatorTabComponent;
  }
  @NotNull
  @Override
  public ValidationResult doValidate() {
    return RubyMotionUtil.getInstance().rubyMotionPresent() ? ValidationResult.OK : new ValidationResult("RubyMotion is not installed");
  }
}
