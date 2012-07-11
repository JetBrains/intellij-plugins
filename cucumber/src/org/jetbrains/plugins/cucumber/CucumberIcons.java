package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Roman.Chernyatchik
 * @date May 19, 2009
 */
public interface CucumberIcons {

  Icon FILE_ICON = IconLoader.getIcon("/org/jetbrains/plugins/cucumber/icons/cucumber.png");
  Icon STRUCTURE_STEPS_GROUP_OPEN_ICON = IconLoader.getIcon("/org/jetbrains/plugins/cucumber/icons/steps_group_opened.png");
  Icon STRUCTURE_STEPS_GROUP_CLOSED_ICON = IconLoader.getIcon("/org/jetbrains/plugins/cucumber/icons/steps_group_closed.png");

  Icon STRUCTURE_STEP_ICON = FILE_ICON;
}
