/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    return OsmorcBundle.message("configurable.LibraryBundlingEditor.display.name");
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
