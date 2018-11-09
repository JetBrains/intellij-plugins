/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.facet.ui;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Struts2 facet tab "Features".
 *
 * @author Yann C&eacute;bron
 */
public class FeaturesConfigurationTab extends FacetEditorTab {

  private JPanel myPanel;
  private JCheckBox disablePropertiesKeys;

  private final StrutsFacetConfiguration originalConfiguration;

  public FeaturesConfigurationTab(final StrutsFacetConfiguration originalConfiguration) {
    this.originalConfiguration = originalConfiguration;

    disablePropertiesKeys.setSelected(originalConfiguration.isPropertiesKeysDisabled());
  }

  @Override
  @Nls
  public String getDisplayName() {
    return StrutsBundle.message("facet.features.title");
  }

  @Override
  @NotNull
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public boolean isModified() {
    return originalConfiguration.isPropertiesKeysDisabled() !=
           disablePropertiesKeys.isSelected();
  }

  @Override
  public void apply() {
    originalConfiguration.setPropertiesKeysDisabled(disablePropertiesKeys.isSelected());
    originalConfiguration.setModified();
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts2.facet";
  }
}
