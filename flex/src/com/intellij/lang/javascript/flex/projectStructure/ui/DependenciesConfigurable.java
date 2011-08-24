package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.options.Dependencies;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {

  private JPanel myMainPanel;
  private FlexSdkChooserPanel mySdkPanel;

  private final Dependencies myDependencies;
  private final Project myProject;
  private final ModifiableRootModel myRootModel;

  public DependenciesConfigurable(final Dependencies dependencies, Project project, ModifiableRootModel rootModel) {
    myDependencies = dependencies;
    myProject = project;
    myRootModel = rootModel;
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
    return mySdkPanel.isModified();
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);
    mySdkPanel.apply();
  }

  public void applyTo(final Dependencies dependencies) {
  }

  public void reset() {
    mySdkPanel.reset();
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    mySdkPanel = new FlexSdkChooserPanel(myProject, myRootModel);
  }

  public FlexSdkChooserPanel getSdkChooserPanel() {
    return mySdkPanel;
  }

  public void addFlexSdkListener(ChangeListener listener) {
    mySdkPanel.addListener(listener);
  }

  public void removeFlexSdkListener(ChangeListener listener) {
    mySdkPanel.removeListener(listener);
  }
}
