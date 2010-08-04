package org.osmorc.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;

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

  public String getId() {
    return getHelpTopic();
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  public JComponent createComponent() {
    component = new ProjectSettingsEditorComponent();
    return component.getMainPanel();
  }

  public void disposeUIResources() {
    component.dispose();
    component = null;
  }

  public boolean isModified() {
    return component.isModified();
  }

  public void apply() throws ConfigurationException {
    component.applyTo(ProjectSettings.getInstance(myProject));
  }

  public void reset() {
    component.resetTo(ProjectSettings.getInstance(myProject));
  }
}
