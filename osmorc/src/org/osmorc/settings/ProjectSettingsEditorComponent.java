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

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.NotNull;
import org.osmorc.ModuleDependencySynchronizer;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class ProjectSettingsEditorComponent implements ApplicationSettings.ApplicationSettingsListener {
  private boolean myModified;
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private ProjectSettings mySettings;
  private UserActivityWatcher myWatcher;
  private JPanel myMainPanel;
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private JComboBox myFrameworkInstance;
  private JCheckBox myCreateFrameworkInstanceModule;
  private JComboBox myDefaultManifestFileLocation;
  private TextFieldWithBrowseButton myBundleOutputPath;
  private JButton myApplyToAllButton;
  private JComboBox mySynchronizationType;
  private JButton mySynchronizeNowButton;
  private Project myProject;

  public ProjectSettingsEditorComponent(Project project) {
    myProject = project;
    myFrameworkInstance.setRenderer(new FrameworkInstanceCellRenderer(myFrameworkInstance.getRenderer()) {
      @Override
      protected boolean isInstanceDefined(FrameworkInstanceDefinition instance) {
        List<FrameworkInstanceDefinition> instanceDefinitions = ApplicationSettings.getInstance().getFrameworkInstanceDefinitions();
        for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions) {
          if (instance.equals(instanceDefinition)) {
            return true;
          }
        }
        return false;
      }
    });
    myWatcher = new UserActivityWatcher();
    myWatcher.register(myMainPanel);
    myWatcher.addUserActivityListener(new UserActivityListener() {
      public void stateChanged() {
        myModified = true;
      }
    });

    myDefaultManifestFileLocation.setEditable(true);
    myDefaultManifestFileLocation.addItem("META-INF");

    myBundleOutputPath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOutputPathSelect();
      }
    });

    myApplyToAllButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onApplyToAllClick();
      }
    });

    mySynchronizeNowButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ModuleDependencySynchronizer.resynchronizeAll(myProject);
      }
    });
    ApplicationSettings.getInstance().addApplicationSettingsListener(this);
  }

  private void onOutputPathSelect() {
    VirtualFile preselect = LocalFileSystem.getInstance().findFileByPath(myBundleOutputPath.getText());
    if (preselect == null) {
      preselect = myProject.getBaseDir();
    }
    VirtualFile virtualFile =
      FileChooser.chooseFile(myProject, FileChooserDescriptorFactory.createSingleFolderDescriptor(), preselect);
    if (virtualFile != null) {
      myBundleOutputPath.setText(virtualFile.getPath());
    }
  }


  private void onApplyToAllClick() {
    final Application application = ApplicationManager.getApplication();
    application.runWriteAction(new Runnable() {
      @Override
      public void run() {
        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        for (Module module : modules) {
          OsmorcFacet facet = OsmorcFacet.getInstance(module);
          if (facet != null) {
            OsmorcFacetConfiguration facetConfiguration = facet.getConfiguration();
            facetConfiguration
              .setJarFileLocation(facetConfiguration.getJarFileName(), OsmorcFacetConfiguration.OutputPathType.OsgiOutputPath);
          }
        }
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            Messages.showMessageDialog(myProject, "The output path has been applied to all OSGi facets in the current project.",
                                       "Output path applied",
                                       Messages.getInformationIcon());

          }
        });
      }
    });
  }


  public void applyTo(ProjectSettings settings) {
    settings.setCreateFrameworkInstanceModule(myCreateFrameworkInstanceModule.isSelected());
    final String fileLocation = (String)myDefaultManifestFileLocation.getSelectedItem();
    if (fileLocation != null) {
      settings.setDefaultManifestFileLocation(fileLocation);
    }
    final FrameworkInstanceDefinition instanceDefinition = (FrameworkInstanceDefinition)this.myFrameworkInstance.getSelectedItem();
    if (instanceDefinition != null) {
      settings.setFrameworkInstanceName(instanceDefinition.getName());
    }

    if (myBundleOutputPath.getText() != null && !"".equals(myBundleOutputPath.getText().trim())) {
      settings.setBundlesOutputPath(myBundleOutputPath.getText());
    }
    else {
      settings.setBundlesOutputPath(null);
    }

    SynchronizationItem selectedItem = (SynchronizationItem)mySynchronizationType.getSelectedItem();
    settings.setManifestSynchronizationType(selectedItem.getType());
    myModified = false;
  }

  public void dispose() {
    myWatcher = null;
    ApplicationSettings.getInstance().removeApplicationSettingsListener(this);
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void resetTo(ProjectSettings settings) {
    mySettings = settings;
    refreshFrameworkInstanceCombobox();
    refreshSynchronizationCombobox();
    myDefaultManifestFileLocation.setSelectedItem(mySettings.getDefaultManifestFileLocation());
    myCreateFrameworkInstanceModule.setSelected(mySettings.isCreateFrameworkInstanceModule());
    String bundlesPath = mySettings.getBundlesOutputPath();
    if (bundlesPath != null) {
      myBundleOutputPath.setText(bundlesPath);
    }
    else {
      myBundleOutputPath.setText(ProjectSettings.getDefaultBundlesOutputPath(myProject));
    }
    myModified = false;
  }

  private synchronized void refreshFrameworkInstanceCombobox() {
    if (mySettings == null) return;

    myFrameworkInstance.removeAllItems();

    List<FrameworkInstanceDefinition> instanceDefinitions = ApplicationSettings.getInstance().getFrameworkInstanceDefinitions();
    final String frameworkInstanceName = mySettings.getFrameworkInstanceName();

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
      projectFrameworkInstance.setDefined(false);
      myFrameworkInstance.addItem(projectFrameworkInstance);
    }
    myFrameworkInstance.setSelectedItem(projectFrameworkInstance);

  }

  private synchronized void refreshSynchronizationCombobox() {
    if ( mySettings == null) return;
    mySynchronizationType.removeAllItems();

    for (ProjectSettings.ManifestSynchronizationType type : ProjectSettings.ManifestSynchronizationType.values()) {
      SynchronizationItem item =  new SynchronizationItem(type);
      mySynchronizationType.addItem(item);
      if ( type == mySettings.getManifestSynchronizationType() ) {
        mySynchronizationType.setSelectedItem(item);
      }
    }
  }

  public boolean isModified() {
    return myModified;
  }

  public void frameworkInstancesChanged() {
    boolean modified = myModified;
    refreshFrameworkInstanceCombobox();
    myModified = modified;
  }

  private void createUIComponents() {
    myDefaultManifestFileLocation = new ComboBox();
  }

  private static class SynchronizationItem {
    private ProjectSettings.ManifestSynchronizationType myType;

    public SynchronizationItem(@NotNull ProjectSettings.ManifestSynchronizationType type) {
      myType = type;
    }

    public ProjectSettings.ManifestSynchronizationType getType() {
      return myType;
    }


    @Override
    public String toString() {
      switch (myType) {
        case AutomaticallySynchronize:
          return "Automatically synchronize dependencies";
        case ManuallySynchronize:
          return "Show notification bar and manually synchronize dependencies";
        case DoNotSynchronize:
          return "Do not synchronize dependencies";
      }

      // should not happen
      return myType.toString();
    }
  }
}
