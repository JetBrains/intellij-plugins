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

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.Nls;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.settings.MyErrorText;
import org.osmorc.settings.ProjectSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;

/**
 * The facet editor tab which is used to set up general Osmorc facet settings.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetGeneralEditorTab extends FacetEditorTab {


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
  private JPanel myWarningPanel;
  private JButton myCreateButton;
  private MyErrorText myErrorText;
  private JRadioButton myUseBundlorFileRadioButton;
  private TextFieldWithBrowseButton myBundlorFile;
  private JPanel myBundlorPanel;
  private JCheckBox myDoNotSynchronizeFacetCheckBox;
  private boolean myModified;
  private final FacetEditorContext myEditorContext;
  private final Module myModule;
  static final Key<Boolean> MANUAL_MANIFEST_EDITING_KEY = Key.create("MANUAL_MANIFEST_EDITING");
  static final Key<Boolean> BND_CREATION_KEY = Key.create("BND_CREATION");
  static final Key<Boolean> BUNDLOR_CREATION_KEY = Key.create("BUNDLOR_CREATION");

  public OsmorcFacetGeneralEditorTab(FacetEditorContext editorContext) {
    myEditorContext = editorContext;
    myModule = editorContext.getModule();
    myManifestFileChooser.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onManifestFileSelect();
      }
    });
    myBndFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectBuildFile(myBndFile);
      }
    });
    myBundlorFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectBuildFile(myBundlorFile);
      }
    });

    ChangeListener listener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateGui();
      }
    };
    myManuallyEditedRadioButton.addChangeListener(listener);
    myUseBndFileRadioButton.addChangeListener(listener);
    myUseBundlorFileRadioButton.addChangeListener(listener);
    myControlledByOsmorcRadioButton.addChangeListener(listener);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener() {
      public void stateChanged() {
        myModified = true;
        checkFileExisting();
      }
    });

    watcher.register(myRoot);

    myUseProjectDefaultManifestFileLocation.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        onUseProjectDefaultManifestFileLocationChanged();
      }
    });
    myCreateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tryCreateBundleManifest();
        checkFileExisting();
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
    checkFileExisting();
  }

  private void onUseProjectDefaultManifestFileLocationChanged() {
    myManifestFileChooser.setEnabled(!myUseProjectDefaultManifestFileLocation.isSelected());
    myModified = true;
  }

  private void onManifestFileSelect() {
    VirtualFile[] roots = getContentRoots(myModule);
    VirtualFile currentFile = findFileInContentRoots(myManifestFileChooser.getText(), myModule);

    VirtualFile manifestFileLocation = FileChooser.chooseFile(myEditorContext.getProject(),
                                                              FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(),
                                                              currentFile);

    if (manifestFileLocation != null) {
      for (VirtualFile root : roots) {
        String relativePath = VfsUtilCore.getRelativePath(manifestFileLocation, root, File.separatorChar);
        if (relativePath != null) {
          // okay, it resides inside one of our content roots, so far so good.
          if (manifestFileLocation.isDirectory()) {
            // its a folder, so add "MANIFEST.MF" to it as a default.
            relativePath += "/MANIFEST.MF";
          }

          myManifestFileChooser.setText(relativePath);
          break;
        }
      }
    }
  }


  private static VirtualFile[] getContentRoots(Module module) {
    return ModuleRootManager.getInstance(module).getContentRoots();
  }

  @Nls
  public String getDisplayName() {
    return "General";
  }

  public JComponent createComponent() {
    return myRoot;
  }

  public boolean isModified() {
    return myModified;
  }

  private void selectBuildFile(TextFieldWithBrowseButton field) {
    VirtualFile[] roots = getContentRoots(myModule);
    VirtualFile currentFile = findFileInContentRoots(field.getText(), myModule);

    VirtualFile fileLocation = FileChooser.chooseFile(myEditorContext.getProject(),
                                                      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(), currentFile);


    if (fileLocation != null) {
      for (VirtualFile root : roots) {
        String relativePath = VfsUtilCore
          .getRelativePath(fileLocation, root, File.separatorChar);
        if (relativePath != null) {
          field.setText(relativePath);
          break;
        }
      }
    }
    updateGui();
  }

  public void apply() {
    OsmorcFacetConfiguration configuration =
      (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
    configuration.setManifestGenerationMode(
      myControlledByOsmorcRadioButton.isSelected() ? OsmorcFacetConfiguration.ManifestGenerationMode.OsmorcControlled :
      myUseBndFileRadioButton.isSelected() ? OsmorcFacetConfiguration.ManifestGenerationMode.Bnd :
      myUseBundlorFileRadioButton.isSelected() ? OsmorcFacetConfiguration.ManifestGenerationMode.Bundlor :
      OsmorcFacetConfiguration.ManifestGenerationMode.Manually);

    configuration.setManifestLocation(myManifestFileChooser.getText());
    configuration.setUseProjectDefaultManifestFileLocation(myUseProjectDefaultManifestFileLocation.isSelected());
    String bndFileLocation = myBndFile.getText();
    bndFileLocation = bndFileLocation.replace('\\', '/');
    configuration.setBndFileLocation(bndFileLocation);

    String bundlorFileLocation = myBundlorFile.getText();
    bundlorFileLocation = bundlorFileLocation.replace('\\', '/');
    configuration.setBundlorFileLocation(bundlorFileLocation);
    configuration.setDoNotSynchronizeWithMaven(myDoNotSynchronizeFacetCheckBox.isSelected());
  }

  public void reset() {
    OsmorcFacetConfiguration configuration =
      (OsmorcFacetConfiguration)myEditorContext.getFacet().getConfiguration();
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
    myManifestFileChooser.setText(configuration.getManifestLocation());

    if (configuration.isUseProjectDefaultManifestFileLocation()) {
      myUseProjectDefaultManifestFileLocation.setSelected(true);
    }
    else {
      myUseModuleSpecificManifestFileLocation.setSelected(true);
    }
    myBndFile.setText(configuration.getBndFileLocation());
    myBundlorFile.setText(configuration.getBundlorFileLocation());
    myDoNotSynchronizeFacetCheckBox.setSelected(configuration.isDoNotSynchronizeWithMaven());
    updateGui();
  }

  @Override
  public void onTabEntering() {
    super.onTabEntering();
    updateGui();
  }

  public void disposeUIResources() {

  }

  private String getManifestLocation() {
    if (myControlledByOsmorcRadioButton.isSelected() || myUseBndFileRadioButton.isSelected() || myUseBundlorFileRadioButton.isSelected()) {
      return null;
    }
    if (myUseModuleSpecificManifestFileLocation.isSelected()) {
      return myManifestFileChooser.getText();
    }
    if (myUseProjectDefaultManifestFileLocation.isSelected()) {
      final ProjectSettings projectSettings = ModuleServiceManager.getService(myModule, ProjectSettings.class);
      return projectSettings.getDefaultManifestFileLocation();
    }
    return null;
  }

  private void checkFileExisting() {
    boolean showWarning;
    if (myControlledByOsmorcRadioButton.isSelected() || myUseBndFileRadioButton.isSelected() || myUseBundlorFileRadioButton.isSelected()) {
      showWarning = false;
    }
    else {
      String location = getManifestLocation();
      if (location == null) {
        showWarning = false;
      }
      else {
        VirtualFile file = findFileInContentRoots(location, myModule);
        showWarning = file == null;
      }
    }

    myWarningPanel.setVisible(showWarning);
    myRoot.revalidate();
  }

  private void createUIComponents() {
    myErrorText = new MyErrorText();
    myErrorText.setError("The manifest file does not exist.");
  }

  private void tryCreateBundleManifest() {

    // check if a manifest path has been set up
    final String manifestPath = getManifestLocation();
    if (StringUtil.isEmpty(manifestPath)) {
      return;
    }

    final VirtualFile[] contentRoots = getContentRoots(myModule);
    if (contentRoots.length > 0) {

      Application application = ApplicationManager.getApplication();

      application.runWriteAction(new Runnable() {
        public void run() {
          try {

            VirtualFile contentRoot = contentRoots[0];
            String completePath = contentRoot.getPath() + File.separator + manifestPath;

            // unify file separators
            completePath = completePath.replace('\\', '/');

            // strip off the last part (its the filename)
            int lastPathSep = completePath.lastIndexOf('/');
            String path = completePath.substring(0, lastPathSep);
            String filename = completePath.substring(lastPathSep + 1);

            // make sure the folders exist
            VfsUtil.createDirectories(path);

            // and get the virtual file for it
            VirtualFile parentFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);

            // some heuristics for bundle name and version
            String bundleName = myModule.getName();
            Version bundleVersion = null;
            int nextDotPos = bundleName.indexOf('.');
            while (bundleVersion == null && nextDotPos >= 0) {
              try {
                bundleVersion = new Version(bundleName.substring(nextDotPos + 1));
                bundleName = bundleName.substring(0, nextDotPos);
              }
              catch (IllegalArgumentException e) {
                // Retry after next dot.
              }
              nextDotPos = bundleName.indexOf('.', nextDotPos + 1);
            }


            VirtualFile manifest = parentFolder.createChildData(this, filename);
            String text = Attributes.Name.MANIFEST_VERSION + ": 1.0.0\n" +
                          Constants.BUNDLE_MANIFESTVERSION + ": 2\n" +
                          Constants.BUNDLE_NAME + ": " + bundleName + "\n" +
                          Constants.BUNDLE_SYMBOLICNAME + ": " + bundleName + "\n" +
                          Constants.BUNDLE_VERSION + ": " +
                          (bundleVersion != null ? bundleVersion.toString() : "1.0.0") +
                          "\n";
            VfsUtil.saveText(manifest, text);
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      });
      VirtualFileManager.getInstance().refresh(false);
    }
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.module.facet.osgi";
  }

  private static VirtualFile findFileInContentRoots(String fileName, Module module) {
    VirtualFile[] roots = getContentRoots(module);
    VirtualFile currentFile = null;
    for (VirtualFile root : roots) {
      currentFile = VfsUtil.findRelativeFile(fileName, root);
      if (currentFile != null) {
        break;
      }
    }
    return currentFile;
  }


}

