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
import com.intellij.ide.util.TreeClassChooserDialog;
import com.intellij.ide.util.TreeFileChooserDialog;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
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
 * The facet editor tab which is used to set up Osmorc facet settings concerning the generation of the manifest file by
 * Osmorc.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsmorcFacetManifestGenerationEditorTab extends FacetEditorTab
{
  public OsmorcFacetManifestGenerationEditorTab(FacetEditorContext editorContext)
  {
    _editorContext = editorContext;
    _bndFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onBndFileSelect();
      }
    });

    ChangeListener listener = new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        updateGui();
      }
    };
    _useExistingBndFile.addChangeListener(listener);

    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.addUserActivityListener(new UserActivityListener()
    {
      public void stateChanged()
      {
        _modified = true;
      }
    });

    watcher.register(_root);
    _bundleActivator.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onBundleActivatorSelect();
      }
    });

  }

  private void updateGui()
  {
    Boolean data = _editorContext.getUserData(OsmorcFacetGeneralEditorTab.MANUAL_MANIFEST_EDITING_KEY);
    boolean isManuallyEdited = data != null ? data : true;
    boolean useBndFile = _useExistingBndFile.isSelected();

    _useExistingBndFile.setEnabled(!isManuallyEdited);
    _generateWithTheseSettings.setEnabled(!isManuallyEdited);

    _bundleActivatorLabel.setEnabled(!isManuallyEdited && !useBndFile);
    _bundleActivator.setEnabled(!isManuallyEdited && !useBndFile);
    _bundleSymbolicName.setEnabled(!isManuallyEdited && !useBndFile);
    _bundleSymbolicNameLabel.setEnabled(!isManuallyEdited && !useBndFile);
    _bundleVersionLabel.setEnabled(!isManuallyEdited && !useBndFile);
    _bundleVersion.setEnabled(!isManuallyEdited && !useBndFile);
    _additionalProperties.setEnabled(!isManuallyEdited && !useBndFile);
    _additionalPropertiesLabel.setEnabled(!isManuallyEdited && !useBndFile);
    _additionalBndArgs.setEnabled(!isManuallyEdited && !useBndFile);
    _additionalBndArgsLabel.setEnabled(!isManuallyEdited && !useBndFile);

    _bndFile.setEnabled(!isManuallyEdited && useBndFile);
    _bndFileLabel.setEnabled(!isManuallyEdited && useBndFile);

  }

  private void onBundleActivatorSelect()
  {
    Project project = _editorContext.getProject();
    GlobalSearchScope searchScope = GlobalSearchScope.moduleWithDependenciesScope(_editorContext.getModule());
    // show a class selector for descendants of BundleActivator
    PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass("org.osgi.framework.BundleActivator", GlobalSearchScope.allScope(project));
    TreeClassChooserDialog dialog =
        new TreeClassChooserDialog(OsmorcBundle.getTranslation("faceteditor.select.bundleactivator"),
            project, searchScope, new TreeClassChooserDialog.InheritanceClassFilterImpl(
                psiClass, false, true,
                null), null);
    dialog.showDialog();
    PsiClass clazz = dialog.getSelectedClass();
    if (clazz != null)
    {
      _bundleActivator.setText(clazz.getQualifiedName());
    }
  }

  private void onBndFileSelect()
  {
    VirtualFile[] roots = ModuleRootManager.getInstance(_editorContext.getModule()).getContentRoots();
    TreeFileChooserDialog dialog =
        new TreeFileChooserDialog(_editorContext.getProject(),
            OsmorcBundle.getTranslation("faceteditor.select.bndfile"), null,
            null, new SubfolderFileFilter(roots), false,
            false);
    dialog.showDialog();
    PsiFile file = dialog.getSelectedFile();
    if (file != null)
    {
      VirtualFile bndFileLocation = file.getVirtualFile();
      for (VirtualFile root : roots)
      {
        String relativePath = VfsUtil
            .getRelativePath(bndFileLocation, root, File.separatorChar);
        if (relativePath != null)
        {
          _bndFile.setText(relativePath);
          break;
        }

      }
    }
  }

  @Nls
  public String getDisplayName()
  {
    return "Manifest Generation";
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
    configuration.setBundleActivator(_bundleActivator.getText());
    configuration.setBundleSymbolicName(_bundleSymbolicName.getText());
    configuration.setBundleVersion(_bundleVersion.getText());
    configuration.setAdditionalProperties(_additionalProperties.getText());
    configuration.setAdditionalBndArgs(_additionalBndArgs.getText());

    configuration.setUseBndFile(_useExistingBndFile.isSelected());
    String bndFileLocation = _bndFile.getText();
    bndFileLocation = bndFileLocation.replace('\\', '/');
    configuration.setBndFileLocation(bndFileLocation);
  }

  public void reset()
  {
    OsmorcFacetConfiguration configuration = (OsmorcFacetConfiguration) _editorContext.getFacet().getConfiguration();
    _bundleActivator.setText(configuration.getBundleActivator());
    _bundleSymbolicName.setText(configuration.getBundleSymbolicName());
    _bundleVersion.setText(configuration.getBundleVersion());
    _additionalProperties.setText(configuration.getAdditionalProperties());
    _bndFile.setText(configuration.getBndFileLocation());
    _additionalBndArgs.setText(configuration.getAdditionalBndArgs());
    if (configuration.isUseBndFile())
    {
      _useExistingBndFile.setSelected(true);
    }
    else
    {
      _generateWithTheseSettings.setSelected(true);
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

  private JPanel _root;
  private JTextField _bundleSymbolicName;
  private TextFieldWithBrowseButton _bundleActivator;
  private JLabel _bundleSymbolicNameLabel;
  private JLabel _bundleActivatorLabel;
  private JTextField _bundleVersion;
  private JLabel _bundleVersionLabel;
  private JTextArea _additionalProperties;
  private JLabel _additionalPropertiesLabel;
  private JTextField _additionalBndArgs;
  private JRadioButton _generateWithTheseSettings;
  private JRadioButton _useExistingBndFile;
  private JLabel _additionalBndArgsLabel;
  private JLabel _bndFileLabel;
  private TextFieldWithBrowseButton _bndFile;
  private boolean _modified;
  private final FacetEditorContext _editorContext;
}