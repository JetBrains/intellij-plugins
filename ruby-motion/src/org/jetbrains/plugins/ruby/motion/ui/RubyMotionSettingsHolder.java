package org.jetbrains.plugins.ruby.motion.ui;

import org.jetbrains.plugins.ruby.wizard.RubyProjectSettings;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtilImpl;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionSettingsHolder extends RubyProjectSettings {
  private RubyMotionUtilImpl.ProjectType myProjectType;

  public RubyMotionUtilImpl.ProjectType getProjectType() {
    return myProjectType;
  }

  public void setProjectType(RubyMotionUtilImpl.ProjectType projectType) {
    myProjectType = projectType;
  }
}
