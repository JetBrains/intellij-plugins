package org.jetbrains.plugins.ruby.motion.ui;

import org.jetbrains.plugins.ruby.RubyProjectSettings;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtilImpl;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionSettingsHolder extends RubyProjectSettings {
  private RubyMotionUtilImpl.ProjectType myProjectType;
  private boolean myUseCalabash;

  public RubyMotionUtilImpl.ProjectType getProjectType() {
    return myProjectType;
  }

  public void setProjectType(RubyMotionUtilImpl.ProjectType projectType) {
    myProjectType = projectType;
  }

  public boolean isUseCalabash() {
    return myUseCalabash;
  }

  public void setUseCalabash(boolean useCalabash) {
    myUseCalabash = useCalabash;
  }
}
