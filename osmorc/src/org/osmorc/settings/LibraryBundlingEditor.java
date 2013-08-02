package org.osmorc.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class LibraryBundlingEditor implements SearchableConfigurable, Configurable.NoScroll {
  private LibraryBundlingEditorComponent myComponent;

  @Nls
  @Override
  public String getDisplayName() {
    return "Library Bundling";
  }

  @NotNull
  @Override
  public String getHelpTopic() {
    return "reference.settings.project.osgi.library.bundling";
  }

  @NotNull
  @Override
  public String getId() {
    return getHelpTopic();
  }

  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Override
  public JComponent createComponent() {
    myComponent = new LibraryBundlingEditorComponent();
    return myComponent.getMainPanel();
  }

  @Override
  public boolean isModified() {
    return myComponent != null && myComponent.isModified(ApplicationSettings.getInstance());
  }

  @Override
  public void apply() throws ConfigurationException {
    myComponent.applyTo(ApplicationSettings.getInstance());
  }

  @Override
  public void reset() {
    myComponent.resetTo(ApplicationSettings.getInstance());
  }

  @Override
  public void disposeUIResources() {
    if (myComponent != null) {
      myComponent.dispose();
      myComponent = null;
    }
  }
}
