package org.osmorc.settings;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class LibraryBundlingEditor implements SearchableConfigurable {
  private LibraryBundlingEditorComponent myComponent;

  public LibraryBundlingEditor() {
  }

  @Nls
  public String getDisplayName() {
    return "Library Bundling";
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return "reference.settings.project.osgi.library.bundling";
  }

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
    return myComponent.isModified();
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
