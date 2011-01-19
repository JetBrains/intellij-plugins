package org.osmorc.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class ProjectSettingsEditor implements SearchableConfigurable {

  private ProjectSettingsEditorComponent component;
  private final Project myProject;

  public ProjectSettingsEditor(Project project) {
    myProject = project;
  }

  @Nls
  public String getDisplayName() {
    return "OSGi";
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return "reference.settings.project.osgi.project.settings";
  }

  @NotNull
  public String getId() {
    return getHelpTopic();
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  public JComponent createComponent() {
    component = new ProjectSettingsEditorComponent(myProject);
    return component.getMainPanel();
  }

  public void disposeUIResources() {
    component.dispose();
    component = null;
  }

  public boolean isModified() {
    // Fixes:    EA-23200. This probably occurs when isModified is called after disposing the UI. should not happen but does.. :(
    return component != null ? component.isModified() : false;
  }

  public void apply() throws ConfigurationException {
    component.applyTo(ProjectSettings.getInstance(myProject));
  }

  public void reset() {
    component.resetTo(ProjectSettings.getInstance(myProject));
  }
}
