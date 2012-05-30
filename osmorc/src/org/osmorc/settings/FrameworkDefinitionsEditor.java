package org.osmorc.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import javax.swing.*;


public class FrameworkDefinitionsEditor implements SearchableConfigurable {
  private FrameworkDefinitionsEditorComponent myComponent;
  private final FrameworkIntegratorRegistry myRegistry;

  public FrameworkDefinitionsEditor(FrameworkIntegratorRegistry registry) {
    myRegistry = registry;
  }

  @Nls
  public String getDisplayName() {
    return "Framework Definitions";
  }

  public String getHelpTopic() {
    return "reference.settings.project.osgi.framework.definitions";
  }

  @NotNull
  public String getId() {
    return getHelpTopic();
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  public JComponent createComponent() {
    myComponent = new FrameworkDefinitionsEditorComponent(myRegistry);
    return myComponent.getMainPanel();
  }

  public boolean isModified() {
    return myComponent.isModified();
  }

  public void apply() throws ConfigurationException {
    myComponent.applyTo(ApplicationSettings.getInstance());
  }

  public void reset() {
    myComponent.resetTo(ApplicationSettings.getInstance());
  }

  public void disposeUIResources() {
    myComponent.dispose();
    myComponent = null;
  }
}
