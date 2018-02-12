/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.settings;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.util.OsgiUiUtil;

import javax.swing.*;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class ProjectSettingsEditorComponent {
  private UserActivityWatcher myWatcher;
  private JPanel myMainPanel;
  private JComboBox<FrameworkInstanceDefinition> myFrameworkInstance;
  private ComboBox<String> myDefaultManifestFileLocation;
  private TextFieldWithBrowseButton myBundleOutputPath;
  private JButton myApplyToAllButton;
  private JBCheckBox myBndAutoImport;

  private final Project myProject;
  private ProjectSettings mySettings;
  private boolean myModified;

  public ProjectSettingsEditorComponent(Project project) {
    myProject = project;

    myFrameworkInstance.setRenderer(new OsgiUiUtil.FrameworkInstanceRenderer("[not specified]") {
      @Override
      protected boolean isInstanceDefined(@NotNull FrameworkInstanceDefinition instance) {
        return ApplicationSettings.getInstance().getActiveFrameworkInstanceDefinitions().contains(instance);
      }
    });

    myWatcher = new UserActivityWatcher();
    myWatcher.register(myMainPanel);
    myWatcher.addUserActivityListener(() -> myModified = true);

    myDefaultManifestFileLocation.setEditable(true);
    myDefaultManifestFileLocation.addItem("META-INF");

    myBundleOutputPath.addActionListener((e) -> {
      VirtualFile preselect = LocalFileSystem.getInstance().findFileByPath(myBundleOutputPath.getText());
      if (preselect == null) preselect = myProject.getBaseDir();
      VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), myProject, preselect);
      if (virtualFile != null) {
        myBundleOutputPath.setText(virtualFile.getPath());
      }
    });

    myApplyToAllButton.addActionListener((e) -> {
      WriteAction.run(() -> {
        for (Module module : ModuleManager.getInstance(myProject).getModules()) {
          OsmorcFacet facet = OsmorcFacet.getInstance(module);
          if (facet != null) {
            OsmorcFacetConfiguration configuration = facet.getConfiguration();
            configuration.setJarFileLocation(configuration.getJarFileName(), OutputPathType.OsgiOutputPath);
          }
        }
      });

      String message = "The output path has been applied to all OSGi facets in the current project.";
      Messages.showInfoMessage(myProject, message, "Output Path Applied");
    });
  }

  public void dispose() {
    myWatcher = null;
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void applyTo(ProjectSettings settings) {
    String fileLocation = (String)myDefaultManifestFileLocation.getSelectedItem();
    if (fileLocation != null) {
      settings.setDefaultManifestFileLocation(fileLocation);
    }

    FrameworkInstanceDefinition instance = (FrameworkInstanceDefinition)myFrameworkInstance.getSelectedItem();
    settings.setFrameworkInstanceName(instance != null ? instance.getName() : null);

    String outputPath = myBundleOutputPath.getText();
    settings.setBundlesOutputPath(!StringUtil.isEmptyOrSpaces(outputPath) ? outputPath : null);

    settings.setBndAutoImport(myBndAutoImport.isSelected());

    myModified = false;
  }

  public void resetTo(ProjectSettings settings) {
    mySettings = settings;

    refreshFrameworkInstanceCombobox();

    myDefaultManifestFileLocation.setSelectedItem(mySettings.getDefaultManifestFileLocation());

    String bundlesPath = mySettings.getBundlesOutputPath();
    if (bundlesPath != null) {
      myBundleOutputPath.setText(bundlesPath);
    }
    else {
      myBundleOutputPath.setText(ProjectSettings.getDefaultBundlesOutputPath(myProject));
    }

    myBndAutoImport.setSelected(settings.isBndAutoImport());

    myModified = false;
  }

  private void refreshFrameworkInstanceCombobox() {
    myFrameworkInstance.removeAllItems();
    myFrameworkInstance.addItem(null);

    List<FrameworkInstanceDefinition> instanceDefinitions = ApplicationSettings.getInstance().getActiveFrameworkInstanceDefinitions();
    String frameworkInstanceName = mySettings.getFrameworkInstanceName();

    FrameworkInstanceDefinition projectFrameworkInstance = null;
    for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions) {
      myFrameworkInstance.addItem(instanceDefinition);
      if (instanceDefinition.getName().equals(frameworkInstanceName)) {
        projectFrameworkInstance = instanceDefinition;
      }
    }

    // add it, but it will be marked red.
    if (projectFrameworkInstance == null && frameworkInstanceName != null) {
      projectFrameworkInstance = new FrameworkInstanceDefinition();
      projectFrameworkInstance.setName(frameworkInstanceName);
      myFrameworkInstance.addItem(projectFrameworkInstance);
    }
    myFrameworkInstance.setSelectedItem(projectFrameworkInstance);
  }

  public boolean isModified() {
    return myModified;
  }
}