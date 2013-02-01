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

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.facet.Struts2LibraryType;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.struts2.facet.StrutsFacetLibrariesValidatorDescription;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ArrayUtil;
import com.intellij.util.download.DownloadableFileSetDescription;
import com.intellij.util.download.DownloadableFileSetVersions;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Struts2 facet tab "Features".
 *
 * @author Yann C&eacute;bron
 */
public class FeaturesConfigurationTab extends FacetEditorTab {

  private JPanel myPanel;
  private JComboBox versionComboBox;
  private JCheckBox disablePropertiesKeys;

  private final StrutsFacetConfiguration originalConfiguration;
  private final FacetLibrariesValidator validator;

  public FeaturesConfigurationTab(final StrutsFacetConfiguration originalConfiguration,
                                  final FacetEditorContext editorContext,
                                  final FacetLibrariesValidator validator) {
    this.originalConfiguration = originalConfiguration;
    this.validator = validator;

    disablePropertiesKeys.setSelected(originalConfiguration.isPropertiesKeysDisabled());

    final Module module = editorContext.getModule();
    final String version = StrutsVersionDetector.detectStrutsVersion(module);
    if (version != null) {
      versionComboBox.setModel(new DefaultComboBoxModel(new String[]{version}));
      versionComboBox.getModel().setSelectedItem(version);
      versionComboBox.setEnabled(false);
      return;
    }

    setupVersionComboBox();
  }

  private void setupVersionComboBox() {
    versionComboBox.setRenderer(new ListCellRendererWrapper<DownloadableFileSetDescription>() {
      @Override
      public void customize(final JList list,
                            final DownloadableFileSetDescription value,
                            final int index,
                            final boolean selected,
                            final boolean hasFocus) {
        setText(value.getVersionString());
      }
    });
    versionComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final DownloadableFileSetDescription version = getSelectedVersion();
        assert version != null;
        validator.setRequiredLibraries(getRequiredLibraries());
        validator.setDescription(new StrutsFacetLibrariesValidatorDescription(version.getVersionString()));
      }
    });

    final ModalityState state = ModalityState.current();

    final DownloadableFileSetVersions<DownloadableFileSetDescription> fileSetVersions = Struts2LibraryType.getVersions();
    fileSetVersions.fetchVersions(new DownloadableFileSetVersions.FileSetVersionsCallback<DownloadableFileSetDescription>() {
      @Override
      public void onSuccess(@NotNull final List<? extends DownloadableFileSetDescription> versions) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            versionComboBox.setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(versions)));
            versionComboBox.setSelectedIndex(0);

            validator.setRequiredLibraries(getRequiredLibraries());
            validator.setDescription(new StrutsFacetLibrariesValidatorDescription(versions.get(0).getVersionString()));
          }
        }, state);
      }
    });
  }

  @Nullable
  private DownloadableFileSetDescription getSelectedVersion() {
    final Object version = versionComboBox.getModel().getSelectedItem();
    return version instanceof DownloadableFileSetDescription ? (DownloadableFileSetDescription)version : null;
  }

  @Nullable
  private LibraryInfo[] getRequiredLibraries() {
    final DownloadableFileSetDescription version = getSelectedVersion();
    if (version == null) {
      return null;
    }

    return Struts2LibraryType.getLibraryInfo(version);
  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    validator.onFacetInitialized(facet);
  }

  @Nls
  public String getDisplayName() {
    return StrutsBundle.message("facet.features.title");
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public boolean isModified() {
    return originalConfiguration.isPropertiesKeysDisabled() !=
           disablePropertiesKeys.isSelected();
  }

  public void apply() {
    originalConfiguration.setPropertiesKeysDisabled(disablePropertiesKeys.isSelected());
    originalConfiguration.setModified();
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts2.facet";
  }
}
