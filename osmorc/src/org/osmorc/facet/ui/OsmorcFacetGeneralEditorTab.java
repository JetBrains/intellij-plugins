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
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.jar.Attributes;

/**
 * The facet editor tab which is used to set up general Osmorc facet settings.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetGeneralEditorTab extends FacetEditorTab {

    public OsmorcFacetGeneralEditorTab(FacetEditorContext editorContext) {
        _editorContext = editorContext;
        _module = editorContext.getModule();
        _manifestFileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onManifestFileSelect();
            }
        });
        _bndFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBndFileSelect();
            }
        });

        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateGui();
            }
        };
        _manuallyEditedRadioButton.addChangeListener(listener);
        _useBndFileRadioButton.addChangeListener(listener);
        _controlledByOsmorcRadioButton.addChangeListener(listener);

        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                _modified = true;
                checkFileExisting();
            }
        });

        watcher.register(_root);

        _useProjectDefaultManifestFileLocation.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                onUseProjectDefaultManifestFileLocationChanged();
            }
        });
        _createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryCreateBundleManifest();
                checkFileExisting();
            }
        });
    }

    private void updateGui() {
        boolean isBnd = _useBndFileRadioButton.isSelected();
        boolean isManuallyEdited = _manuallyEditedRadioButton.isSelected();

        _editorContext.putUserData(MANUAL_MANIFEST_EDITING_KEY, isManuallyEdited);
        _editorContext.putUserData(BND_CREATION_KEY, isBnd);

        _bndPanel.setEnabled(isBnd);
        _manifestPanel.setEnabled(isManuallyEdited);
        _useProjectDefaultManifestFileLocation.setEnabled(isManuallyEdited);
        _useModuleSpecificManifestFileLocation.setEnabled(isManuallyEdited);
        _manifestFileChooser.setEnabled(isManuallyEdited && !_useProjectDefaultManifestFileLocation.isSelected());
        _bndFile.setEnabled(isBnd);
        checkFileExisting();
    }

    private void onUseProjectDefaultManifestFileLocationChanged() {
        _manifestFileChooser.setEnabled(!_useProjectDefaultManifestFileLocation.isSelected());
        _modified = true;
    }

    private void onManifestFileSelect() {
        VirtualFile[] roots = getContentRoots(_module);
        VirtualFile currentFile = findFileInContentRoots(_manifestFileChooser.getText(), _module);

        VirtualFile[] result = FileChooser.chooseFiles(_editorContext.getProject(),
                new FileChooserDescriptor(true, true, false, false, false, false), currentFile);

        if (result.length == 1) {
            VirtualFile manifestFileLocation = result[0]; //file.getVirtualFile();
            if (manifestFileLocation != null) {
                for (VirtualFile root : roots) {
                    String relativePath = VfsUtil
                            .getRelativePath(manifestFileLocation, root, File.separatorChar);
                    if (relativePath != null) {
                        // okay, it resides inside one of our content roots, so far so good.
                        if (manifestFileLocation.isDirectory()) {
                            // its a folder, so add "MANIFEST.MF" to it as a default.
                            relativePath += "/MANIFEST.MF";
                        }

                        _manifestFileChooser.setText(relativePath);
                        break;
                    }
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
        return _root;
    }

    public boolean isModified() {
        return _modified;
    }

    private void onBndFileSelect() {
        VirtualFile[] roots = getContentRoots(_module);
        VirtualFile currentFile = findFileInContentRoots(_bndFile.getText(), _module);

        VirtualFile[] result = FileChooser.chooseFiles(_editorContext.getProject(),
                new FileChooserDescriptor(true, false, false, false, false, false), currentFile);


        if (result.length == 1) {
            VirtualFile bndFileLocation = result[0];
            for (VirtualFile root : roots) {
                String relativePath = VfsUtil
                        .getRelativePath(bndFileLocation, root, File.separatorChar);
                if (relativePath != null) {
                    _bndFile.setText(relativePath);
                    break;
                }

            }
        }
        updateGui();
    }

    public void apply() {
        OsmorcFacetConfiguration configuration =
                (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
        configuration.setOsmorcControlsManifest(_controlledByOsmorcRadioButton.isSelected());
        configuration.setManifestLocation(_manifestFileChooser.getText());
        configuration.setUseProjectDefaultManifestFileLocation(_useProjectDefaultManifestFileLocation.isSelected());
        configuration.setUseBndFile(_useBndFileRadioButton.isSelected());
        String bndFileLocation = _bndFile.getText();
        bndFileLocation = bndFileLocation.replace('\\', '/');
        configuration.setBndFileLocation(bndFileLocation);

    }

    public void reset() {
        OsmorcFacetConfiguration configuration =
                (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
        if (configuration.isUseBndFile()) {
            _useBndFileRadioButton.setSelected(true);
        }
        else if (configuration.isOsmorcControlsManifest()) {
            _controlledByOsmorcRadioButton.setSelected(true);
        }
        else {
            _manuallyEditedRadioButton.setSelected(true);
        }
        _manifestFileChooser.setText(configuration.getManifestLocation());

        if (configuration.isUseProjectDefaultManifestFileLocation()) {
            _useProjectDefaultManifestFileLocation.setSelected(true);
        }
        else {
            _useModuleSpecificManifestFileLocation.setSelected(true);
        }
        _bndFile.setText(configuration.getBndFileLocation());
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
        if (_controlledByOsmorcRadioButton.isSelected() || _useBndFileRadioButton.isSelected()) {
            return null;
        }
        if (_useModuleSpecificManifestFileLocation.isSelected()) {
            return _manifestFileChooser.getText();
        }
        if (_useProjectDefaultManifestFileLocation.isSelected()) {
            final ProjectSettings projectSettings = ModuleServiceManager.getService(_module, ProjectSettings.class);
            return projectSettings.getDefaultManifestFileLocation();
        }
        return null;
    }

    private void checkFileExisting() {
        boolean showWarning;
        if (_controlledByOsmorcRadioButton.isSelected() || _useBndFileRadioButton.isSelected()) {
            showWarning = false;
        }
        else {
            String location = getManifestLocation();
            if (location == null) {
                showWarning = false;
            }
            else {
                VirtualFile file = findFileInContentRoots(location, _module);
                showWarning = file == null;
            }
        }

        _warningPanel.setVisible(showWarning);
        _root.revalidate();
    }

    private void createUIComponents() {
        _errorText = new MyErrorText();
        _errorText.setError("The manifest file does not exist.");
    }

    private void tryCreateBundleManifest() {

        // check if a manifest path has been set up
        final String manifestPath = getManifestLocation();
        if (StringUtil.isEmpty(manifestPath)) {
            return;
        }

        final VirtualFile[] contentRoots = getContentRoots(_module);
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
                        String bundleName = _module.getName();
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
                        OutputStream outputStream = manifest.getOutputStream(this);
                        PrintWriter writer = new PrintWriter(outputStream);
                        writer.write(Attributes.Name.MANIFEST_VERSION + ": 1.0.0\n" +
                                Constants.BUNDLE_MANIFESTVERSION + ": 2\n" +
                                Constants.BUNDLE_NAME + ": " + bundleName + "\n" +
                                Constants.BUNDLE_SYMBOLICNAME + ": " + bundleName + "\n" +
                                Constants.BUNDLE_VERSION + ": " +
                                (bundleVersion != null ? bundleVersion.toString() : "1.0.0") +
                                "\n");
                        writer.flush();
                        writer.close();
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

    private JRadioButton _manuallyEditedRadioButton;
    private JRadioButton _controlledByOsmorcRadioButton;
    private TextFieldWithBrowseButton _manifestFileChooser;
    private JPanel _root;
    private JRadioButton _useProjectDefaultManifestFileLocation;
    private JRadioButton _useModuleSpecificManifestFileLocation;
    private JRadioButton _useBndFileRadioButton;
    private JPanel _manifestPanel;
    private TextFieldWithBrowseButton _bndFile;
    private JPanel _bndPanel;
    private JPanel _warningPanel;
    private JButton _createButton;
    private MyErrorText _errorText;
    private boolean _modified;
    private final FacetEditorContext _editorContext;
    private final Module _module;
    static final Key<Boolean> MANUAL_MANIFEST_EDITING_KEY = Key.create("MANUAL_MANIFEST_EDITING");
    static final Key<Boolean> BND_CREATION_KEY = Key.create("BND_CREATION");


}

