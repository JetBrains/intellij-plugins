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

import com.intellij.compiler.server.BuildManager;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.framework.library.DownloadableLibraryService;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.libraries.AddCustomLibraryDialog;
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.components.ActionLink;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.facet.OsgiCoreLibraryType;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ProjectSettings;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.io.File;

/**
 * The facet editor tab which is used to set up general Osmorc facet settings.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsmorcFacetGeneralEditorTab extends FacetEditorTab {
  static final Key<Boolean> MANUAL_MANIFEST_EDITING_KEY = Key.create("MANUAL_MANIFEST_EDITING");
  static final Key<Boolean> EXT_TOOL_MANIFEST_CREATION_KEY = Key.create("EXT_TOOL_MANIFEST_CREATION");

  private JRadioButton myManuallyEditedRadioButton;
  private JRadioButton myControlledByOsmorcRadioButton;
  private TextFieldWithBrowseButton myManifestFileChooser;
  private JPanel myRoot;
  private JRadioButton myUseProjectDefaultManifestFileLocation;
  private JRadioButton myUseModuleSpecificManifestFileLocation;
  private JRadioButton myUseBndFileRadioButton;
  private JCheckBox myUseBndMavenPluginCheckBox;
  private JPanel myManifestPanel;
  private TextFieldWithBrowseButton myBndFile;
  private JPanel myBndPanel;
  private JRadioButton myUseBundlorFileRadioButton;
  private TextFieldWithBrowseButton myBundlorFile;
  private JPanel myBundlorPanel;
  private JCheckBox myDoNotSynchronizeFacetCheckBox;
  @SuppressWarnings("unused") private ActionLink mySetupCoreLibLink;

  private final FacetEditorContext myEditorContext;
  private final FacetValidatorsManager myValidatorsManager;
  private final Module myModule;
  private boolean myModified;

  public OsmorcFacetGeneralEditorTab(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    myEditorContext = editorContext;
    myValidatorsManager = validatorsManager;
    myModule = editorContext.getModule();

    myManifestFileChooser.addActionListener(e -> chooseFile(myManifestFileChooser));
    myBndFile.addActionListener(e -> chooseFile(myBndFile));
    myBundlorFile.addActionListener(e -> chooseFile(myBundlorFile));
    myUseProjectDefaultManifestFileLocation.addChangeListener(e -> manifestFileLocationSelectorChanged());

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(() -> { myModified = true; updateGui(); });
    watcher.register(myRoot);

    myValidatorsManager.registerValidator(new FacetEditorValidator() {
      @Override
      public @NotNull ValidationResult check() {
        if (myManuallyEditedRadioButton.isSelected()) {
          String location = myUseModuleSpecificManifestFileLocation.isSelected() ? myManifestFileChooser.getText() :
                            ProjectSettings.getInstance(myModule.getProject()).getDefaultManifestFileLocation();
          if (findFileInContentRoots(location, myModule) == null) {
            return new ValidationResult(OsmorcBundle.message("facet.editor.manifest.not.found", location));
          }
        }
        if (myUseBndFileRadioButton.isSelected()) {
          String location = myBndFile.getText();
          if (findFileInContentRoots(location, myModule) == null) {
            return new ValidationResult(OsmorcBundle.message("facet.editor.bnd.not.found", location));
          }
        }
        if (myUseBundlorFileRadioButton.isSelected()) {
          String location = myBundlorFile.getText();
          if (findFileInContentRoots(location, myModule) == null) {
            return new ValidationResult(OsmorcBundle.message("facet.editor.bundlor.not.found", location));
          }
        }
        return ValidationResult.OK;
      }
    });
  }

  private void createUIComponents() {
    mySetupCoreLibLink = new ActionLink("", e -> {
      CustomLibraryDescription description = DownloadableLibraryService.getInstance().createDescriptionForType(OsgiCoreLibraryType.class);
      AddCustomLibraryDialog.createDialog(description, myModule, null).show();
    });
  }

  private void updateGui() {
    boolean isBnd = myUseBndFileRadioButton.isSelected() && !myUseBndMavenPluginCheckBox.isSelected();
    boolean isBndMavenPlugin = myUseBndFileRadioButton.isSelected() && myUseBndMavenPluginCheckBox.isSelected();
    boolean isBundlor = myUseBundlorFileRadioButton.isSelected();
    boolean isManuallyEdited = myManuallyEditedRadioButton.isSelected();

    myEditorContext.putUserData(MANUAL_MANIFEST_EDITING_KEY, isManuallyEdited);
    myEditorContext.putUserData(EXT_TOOL_MANIFEST_CREATION_KEY, isBnd || isBndMavenPlugin || isBundlor);

    myBndPanel.setEnabled(isBnd || isBndMavenPlugin);
    myBundlorPanel.setEnabled(isBundlor);
    myManifestPanel.setEnabled(isManuallyEdited);
    myUseProjectDefaultManifestFileLocation.setEnabled(isManuallyEdited);
    myUseModuleSpecificManifestFileLocation.setEnabled(isManuallyEdited);
    myManifestFileChooser.setEnabled(isManuallyEdited && !myUseProjectDefaultManifestFileLocation.isSelected());
    myBndFile.setEnabled(isBnd);
    myUseBndMavenPluginCheckBox.setEnabled(isBnd || isBndMavenPlugin);
    myBundlorFile.setEnabled(isBundlor);

    myValidatorsManager.validate();
  }

  private void chooseFile(TextFieldWithBrowseButton field) {
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
    VirtualFile toSelect = findFileInContentRoots(field.getText(), myModule);
    VirtualFile file = FileChooser.chooseFile(descriptor, myModule.getProject(), toSelect);
    if (file != null) {
      for (VirtualFile root : getContentRoots(myModule)) {
        String relativePath = VfsUtilCore.getRelativePath(file, root, File.separatorChar);
        if (relativePath != null) {
          if (field == myManifestFileChooser && file.isDirectory()) {
            relativePath += "/MANIFEST.MF"; //NON-NLS
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

  @Override
  public @Nls String getDisplayName() {
    return OsmorcBundle.message("facet.tab.general");
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }

  @Override
  public @NotNull JComponent createComponent() {
    return myRoot;
  }

  @Override
  public boolean isModified() {
    return myModified;
  }

  @Override
  public void apply() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    configuration.setManifestGenerationMode(
      myControlledByOsmorcRadioButton.isSelected() ? ManifestGenerationMode.OsmorcControlled :
      myUseBndFileRadioButton.isSelected() ?
        (!myUseBndMavenPluginCheckBox.isSelected() ? ManifestGenerationMode.Bnd : ManifestGenerationMode.BndMavenPlugin) :
      myUseBundlorFileRadioButton.isSelected() ? ManifestGenerationMode.Bundlor :
      ManifestGenerationMode.Manually);
    configuration.setManifestLocation(FileUtil.toSystemIndependentName(myManifestFileChooser.getText()));
    configuration.setUseProjectDefaultManifestFileLocation(myUseProjectDefaultManifestFileLocation.isSelected());
    configuration.setBndFileLocation(FileUtil.toSystemIndependentName(myBndFile.getText()));
    configuration.setBundlorFileLocation(FileUtil.toSystemIndependentName(myBundlorFile.getText()));
    configuration.setDoNotSynchronizeWithMaven(myDoNotSynchronizeFacetCheckBox.isSelected());

    if (myModified) {
      BuildManager.getInstance().clearState(myModule.getProject());
    }
    myModified = false;
  }

  @Override
  public void reset() {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    if (configuration.isUseBndFile()) {
      myUseBndFileRadioButton.setSelected(true);
      myUseBndMavenPluginCheckBox.setSelected(false);
    }
    else if (configuration.isUseBndMavenPlugin()) {
      myUseBndFileRadioButton.setSelected(true);
      myUseBndMavenPluginCheckBox.setSelected(true);
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

  private static VirtualFile[] getContentRoots(Module module) {
    return ModuleRootManager.getInstance(module).getContentRoots();
  }

  private static VirtualFile findFileInContentRoots(String fileName, Module module) {
    for (VirtualFile root : getContentRoots(module)) {
      VirtualFile file = root.findFileByRelativePath(FileUtil.toSystemIndependentName(fileName));
      if (file != null) {
        return file;
      }
    }
    return null;
  }
}
