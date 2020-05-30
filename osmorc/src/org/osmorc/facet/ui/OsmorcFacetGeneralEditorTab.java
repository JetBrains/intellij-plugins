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
package org.osmorc.facet.ui;

import com.intellij.CommonBundle;
import com.intellij.facet.ui.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.settings.ProjectSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * The facet editor tab which is used to set up general Osmorc facet settings.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsmorcFacetGeneralEditorTab extends FacetEditorTab {
  static final Key<Boolean> MANUAL_MANIFEST_EDITING_KEY = Key.create("MANUAL_MANIFEST_EDITING");
  static final Key<Boolean> BND_CREATION_KEY = Key.create("BND_CREATION");
  static final Key<Boolean> BUNDLOR_CREATION_KEY = Key.create("BUNDLOR_CREATION");

  private JRadioButton myManuallyEditedRadioButton;
  private JRadioButton myControlledByOsmorcRadioButton;
  private TextFieldWithBrowseButton myManifestFileChooser;
  private JPanel myRoot;
  private JRadioButton myUseProjectDefaultManifestFileLocation;
  private JRadioButton myUseModuleSpecificManifestFileLocation;
  private JRadioButton myUseBndFileRadioButton;
  private JPanel myManifestPanel;
  private TextFieldWithBrowseButton myBndFile;
  private JPanel myBndPanel;
  private JRadioButton myUseBundlorFileRadioButton;
  private TextFieldWithBrowseButton myBundlorFile;
  private JPanel myBundlorPanel;
  private JCheckBox myDoNotSynchronizeFacetCheckBox;

  private final FacetEditorContext myEditorContext;
  private final FacetValidatorsManager myValidatorsManager;
  private final Module myModule;
  private boolean myModified;

  public OsmorcFacetGeneralEditorTab(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    myEditorContext = editorContext;
    myValidatorsManager = validatorsManager;
    myModule = editorContext.getModule();

    myManifestFileChooser.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        chooseFile(myManifestFileChooser);
      }
    });
    myBndFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        chooseFile(myBndFile);
      }
    });
    myBundlorFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        chooseFile(myBundlorFile);
      }
    });
    myUseProjectDefaultManifestFileLocation.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        manifestFileLocationSelectorChanged();
      }
    });

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener() {
      @Override
      public void stateChanged() {
        myModified = true;
        updateGui();
      }
    });
    watcher.register(myRoot);

    myValidatorsManager.registerValidator(new FacetEditorValidator() {
      @NotNull
      @Override
      public ValidationResult check() {
        if (myManuallyEditedRadioButton.isSelected()) {
          String location = myUseModuleSpecificManifestFileLocation.isSelected() ? myManifestFileChooser.getText() :
                            ProjectSettings.getInstance(myModule.getProject()).getDefaultManifestFileLocation();
          if (findFileInContentRoots(location, myModule) == null) {
            return new ValidationResult("No manifest file '" + location + "' found in module");
          }
        }
        if (myUseBndFileRadioButton.isSelected()) {
          String location = myBndFile.getText();
          if (findFileInContentRoots(location, myModule) == null) {
            return new ValidationResult("No Bnd file '" + location + "' found in module");
          }
        }
        if (myUseBundlorFileRadioButton.isSelected()) {
          String location = myBundlorFile.getText();
          if (findFileInContentRoots(location, myModule) == null) {
            return new ValidationResult("No Bundlor file '" + location + "' found in module");
          }
        }
        return ValidationResult.OK;
      }
    });
  }

  private void updateGui() {
    boolean isBnd = myUseBndFileRadioButton.isSelected();
    boolean isBundlor = myUseBundlorFileRadioButton.isSelected();
    boolean isManuallyEdited = myManuallyEditedRadioButton.isSelected();

    myEditorContext.putUserData(MANUAL_MANIFEST_EDITING_KEY, isManuallyEdited);
    myEditorContext.putUserData(BND_CREATION_KEY, isBnd);
    myEditorContext.putUserData(BUNDLOR_CREATION_KEY, isBundlor);

    myBndPanel.setEnabled(isBnd);
    myBundlorPanel.setEnabled(isBundlor);
    myManifestPanel.setEnabled(isManuallyEdited);
    myUseProjectDefaultManifestFileLocation.setEnabled(isManuallyEdited);
    myUseModuleSpecificManifestFileLocation.setEnabled(isManuallyEdited);
    myManifestFileChooser.setEnabled(isManuallyEdited && !myUseProjectDefaultManifestFileLocation.isSelected());
    myBndFile.setEnabled(isBnd);
    myBundlorFile.setEnabled(isBundlor);

    myValidatorsManager.validate();
  }

  private void chooseFile(TextFieldWithBrowseButton field) {
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
    VirtualFile toSelect = findFileInContentRoots(field.getText(), myModule);
    VirtualFile file = FileChooser.chooseFile(descriptor, myEditorContext.getProject(), toSelect);
    if (file != null) {
      for (VirtualFile root : getContentRoots(myModule)) {
        String relativePath = VfsUtilCore.getRelativePath(file, root, File.separatorChar);
        if (relativePath != null) {
          if (field == myManifestFileChooser && file.isDirectory()) {
            relativePath += "/MANIFEST.MF";
          }
          field.setText(relativePath);
          break;
        }
      }
    }
  }

  private void manifestFileLocationSelectorChanged() {
    myManifestFileChooser.setEnabled(!myUseProjectDefaultManifestFileLocation.isSelected());
    myModified = true;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return CommonBundle.message("tab.title.general");
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }

  @NotNull
  @Override
  public JComponent createComponent() {
    return myRoot;
  }

  @Override
  public boolean isModified() {
    return myModified;
  }

  @Override
  public void apply() throws ConfigurationException {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    configuration.setManifestGenerationMode(
      myControlledByOsmorcRadioButton.isSelected() ? ManifestGenerationMode.OsmorcControlled :
      myUseBndFileRadioButton.isSelected() ? ManifestGenerationMode.Bnd :
      myUseBundlorFileRadioButton.isSelected() ? ManifestGenerationMode.Bundlor :
      ManifestGenerationMode.Manually);
    configuration.setManifestLocation(FileUtil.toSystemIndependentName(myManifestFileChooser.getText()));
    configuration.setUseProjectDefaultManifestFileLocation(myUseProjectDefaultManifestFileLocation.isSelected());
    configuration.setBndFileLocation(FileUtil.toSystemIndependentName(myBndFile.getText()));
    configuration.setBundlorFileLocation(FileUtil.toSystemIndependentName(myBundlorFile.getText()));
    configuration.setDoNotSynchronizeWithMaven(myDoNotSynchronizeFacetCheckBox.isSelected());

    myModified = false;
  }

  @Override
  public void reset() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();

    if (configuration.isUseBndFile()) {
      myUseBndFileRadioButton.setSelected(true);
    }
    else if (configuration.isUseBundlorFile()) {
      myUseBundlorFileRadioButton.setSelected(true);
    }
    else if (configuration.isOsmorcControlsManifest()) {
      myControlledByOsmorcRadioButton.setSelected(true);
    }
    else {
      myManuallyEditedRadioButton.setSelected(true);
    }
    myManifestFileChooser.setText(FileUtil.toSystemDependentName(configuration.getManifestLocation()));
    if (configuration.isUseProjectDefaultManifestFileLocation()) {
      myUseProjectDefaultManifestFileLocation.setSelected(true);
    }
    else {
      myUseModuleSpecificManifestFileLocation.setSelected(true);
    }
    myBndFile.setText(FileUtil.toSystemDependentName(configuration.getBndFileLocation()));
    myBundlorFile.setText(FileUtil.toSystemDependentName(configuration.getBundlorFileLocation()));
    myDoNotSynchronizeFacetCheckBox.setSelected(configuration.isDoNotSynchronizeWithMaven());

    updateGui();
    myModified = false;
  }

  @Override
  public void onTabEntering() {
    updateGui();
  }

  @Override
  public void disposeUIResources() { }

  private static VirtualFile[] getContentRoots(Module module) {
    return ModuleRootManager.getInstance(module).getContentRoots();
  }

  private static VirtualFile findFileInContentRoots(String fileName, Module module) {
    for (VirtualFile root : getContentRoots(module)) {
      VirtualFile file = root.findFileByRelativePath(fileName);
      if (file != null) {
        return file;
      }
    }
    return null;
  }
}
