/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
