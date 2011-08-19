package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.options.Dependencies;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {

  private JPanel myMainPanel;

  private final Dependencies myDependencies;

  public DependenciesConfigurable(final Dependencies dependencies) {
    myDependencies = dependencies;
  }

  @Nls
  public String getDisplayName() {
    return "Dependencies";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "Dependencies";
  }

  public Icon getIcon() {
    return null;
  }

  public Dependencies getEditableObject() {
    return myDependencies;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);
  }

  public void applyTo(final Dependencies dependencies) {
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }
}
