// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DefaultProjectFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thomä</a>
 */
public class LibraryBundlingEditor implements SearchableConfigurable, Configurable.NoScroll {
  private LibraryBundlingEditorComponent myComponent;

  @Nls
  @Override
  public String getDisplayName() {
    return OsmorcBundle.message("settings.application.bundling");
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
  public JComponent createComponent() {
    Project project = DefaultProjectFactory.getInstance().getDefaultProject();
    myComponent = new LibraryBundlingEditorComponent(project);
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
