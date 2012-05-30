package org.osmorc.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class LibraryBundlingEditor implements SearchableConfigurable, Configurable.NoScroll {
  private LibraryBundlingEditorComponent myComponent;

  public LibraryBundlingEditor() {
  }

  @Nls
  public String getDisplayName() {
    return "Library Bundling";
  }

  public String getHelpTopic() {
    return "reference.settings.project.osgi.library.bundling";
  }

  @NotNull
  public String getId() {
    return getHelpTopic();
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  public JComponent createComponent() {
    myComponent = new LibraryBundlingEditorComponent();
    return myComponent.getMainPanel();
  }

  public boolean isModified() {
    // Fixes:    EA-23199. This probably occurs when isModified is called after disposing the UI. should not happen but does.. :(
    return myComponent != null ? myComponent.isModified() : false;
  }

  public void apply() throws ConfigurationException {
    myComponent.applyTo(ApplicationSettings.getInstance());
  }

  public void reset() {
    myComponent.resetTo(ApplicationSettings.getInstance());
  }

  public void disposeUIResources() {
    if (myComponent != null) {
      myComponent.dispose();
    }
    myComponent = null;
  }
}
