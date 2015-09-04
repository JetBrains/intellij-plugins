package org.jetbrains.plugins.ruby.motion.ui;

import org.jetbrains.plugins.ruby.RubyProjectSettings;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionSettingsHolder extends RubyProjectSettings {
  private RubyMotionUtil.ProjectType myProjectType;
  private boolean myUseCalabash;

  public RubyMotionUtil.ProjectType getProjectType() {
    return myProjectType;
  }

  public void setProjectType(RubyMotionUtil.ProjectType projectType) {
    myProjectType = projectType;
  }

  public boolean isUseCalabash() {
    return myUseCalabash;
  }

  public void setUseCalabash(boolean useCalabash) {
    myUseCalabash = useCalabash;
  }
}
