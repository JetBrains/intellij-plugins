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
import com.intellij.ide.util.TreeFileChooserDialog;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import org.jetbrains.annotations.Nls;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * The facet editor tab which is used to set up general Osmorc facet settings.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetGeneralEditorTab extends FacetEditorTab
{
  public OsmorcFacetGeneralEditorTab(FacetEditorContext editorContext)
  {
    _editorContext = editorContext;
    _manifestFileChooser.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onManifestFileSelect();
      }
    });

    ChangeListener listener = new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        updateGui();
      }
    };
    _manuallyEditedRadioButton.addChangeListener(listener);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener()
    {
      public void stateChanged()
      {
        _modified = true;
      }
    });

    watcher.register(_root);

    _useProjectDefaultManifestFileLocation.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        onUseProjectDefaultManifestFileLocationChanged();
      }
    });
  }

  private void updateGui()
  {
    boolean isManuallyEdited = _manuallyEditedRadioButton.isSelected();

    _editorContext.putUserData(MANUAL_MANIFEST_EDITING_KEY, isManuallyEdited);

    _useProjectDefaultManifestFileLocation.setEnabled(isManuallyEdited);
    _useModuleSpecificManifestFileLocation.setEnabled(isManuallyEdited);
    _manifestFileChooser.setEnabled(isManuallyEdited && !_useProjectDefaultManifestFileLocation.isSelected());
  }

  private void onUseProjectDefaultManifestFileLocationChanged()
  {
    _manifestFileChooser.setEnabled(!_useProjectDefaultManifestFileLocation.isSelected());
    _modified = true;
  }

  private void onManifestFileSelect()
  {
    VirtualFile[] roots = ModuleRootManager.getInstance(_editorContext.getModule()).getContentRoots();
    TreeFileChooserDialog dialog =
        new TreeFileChooserDialog(_editorContext.getProject(),
            OsmorcBundle.getTranslation("faceteditor.select.manifest"), null,
            FileTypeManager.getInstance().getFileTypeByFileName("MANIFEST.MF"), new SubfolderFileFilter(roots), false,
            false);
    dialog.showDialog();
    PsiFile file = dialog.getSelectedFile();
    if (file != null)
    {
      VirtualFile manifestFileLocation = file.getVirtualFile();
      if (manifestFileLocation != null && !manifestFileLocation.isDirectory())
      {
        manifestFileLocation = manifestFileLocation.getParent();
      }
      for (VirtualFile root : roots)
      {
        String relativePath = VfsUtil
            .getRelativePath(manifestFileLocation, root, File.separatorChar);
        if (relativePath != null)
        {
          _manifestFileChooser.setText(relativePath);
          break;
        }

      }
    }
  }

  @Nls
  public String getDisplayName()
  {
    return "General";
  }

  public JComponent createComponent()
  {
    return _root;
  }

  public boolean isModified()
  {
    return _modified;
  }

  public void apply() throws ConfigurationException
  {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
    configuration.setOsmorcControlsManifest(_controlledByOsmorcRadioButton.isSelected());
    configuration.setManifestLocation(_manifestFileChooser.getText());
    configuration.setUseProjectDefaultManifestFileLocation(_useProjectDefaultManifestFileLocation.isSelected());
  }

  public void reset()
  {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
    if (configuration.isOsmorcControlsManifest())
    {
      _controlledByOsmorcRadioButton.setSelected(true);
    }
    else
    {
      _manuallyEditedRadioButton.setSelected(true);
    }
    _manifestFileChooser.setText(configuration.getManifestLocation());

    if (configuration.isUseProjectDefaultManifestFileLocation())
    {
      _useProjectDefaultManifestFileLocation.setSelected(true);
    }
    else
    {
      _useModuleSpecificManifestFileLocation.setSelected(true);
    }
    updateGui();
  }

  @Override
  public void onTabEntering()
  {
    super.onTabEntering();
    updateGui();
  }

  public void disposeUIResources()
  {

  }

  private JRadioButton _manuallyEditedRadioButton;
  private JRadioButton _controlledByOsmorcRadioButton;
  private TextFieldWithBrowseButton _manifestFileChooser;
  private JPanel _root;
  private JRadioButton _useProjectDefaultManifestFileLocation;
  private JRadioButton _useModuleSpecificManifestFileLocation;
  private boolean _modified;
  private final FacetEditorContext _editorContext;
  static final Key<Boolean> MANUAL_MANIFEST_EDITING_KEY = Key.create("MANUAL_MANIFEST_EDITING");
}

