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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nls;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkInstanceUpdateNotifier;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ProjectSettingsEditor implements Configurable, ProjectSettingsAwareEditor, ApplicationSettingsAwareEditor,
    ApplicationSettingsUpdateNotifier.Listener
{
  public ProjectSettingsEditor(Project project, FrameworkInstanceUpdateNotifier updateNotifier,
                               ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier,
                               ProjectSettingsUpdateNotifier projectSettingsUpdateNotifier)
  {
    _project = project;
    _updateNotifier = updateNotifier;
    _projectSettingsUpdateNotifier = projectSettingsUpdateNotifier;

    _frameworkInstance.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (!_updatingFrameworkInstanceComboBox && _frameworkInstance.getSelectedItem() != null)
        {
          _projectSettingsWorkingCopy.setFrameworkInstanceName(
              ((FrameworkInstanceDefinition) _frameworkInstance.getSelectedItem()).getName());
          _projectSettingsUpdateNotifier.fireProjectSettingsChanged();
          updateChangedFlag();
          refreshFrameworkInstanceCombobox();
        }
      }
    });

    _createFrameworkInstanceModule.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        _projectSettingsWorkingCopy.setCreateFrameworkInstanceModule(_createFrameworkInstanceModule.isSelected());
        _projectSettingsUpdateNotifier.fireProjectSettingsChanged();
        updateChangedFlag();
      }
    });

    _defaultManifestFileLocation.setEditable(true);
    _defaultManifestFileLocation.addItem("META-INF");
    _defaultManifestFileLocation.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (_defaultManifestFileLocation.getSelectedItem() != null)
        {
          _projectSettingsWorkingCopy
              .setDefaultManifestFileLocation((String) _defaultManifestFileLocation.getSelectedItem());
          _projectSettingsUpdateNotifier.fireProjectSettingsChanged();
          updateChangedFlag();
        }
      }
    });

    _applicationSettingsUpdateNotifier = applicationSettingsUpdateNotifier;
  }

  public void applicationSettingsChanged()
  {
    if (_applicationSettingsWorkingCopy != null)
    {
      refreshFrameworkInstanceCombobox();
    }
  }


  private void updateChangedFlag()
  {
    _changed = !Comparing
        .strEqual(_projectSettingsWorkingCopy.getFrameworkInstanceName(), _projectSettings.getFrameworkInstanceName()) ||
        _projectSettingsWorkingCopy.isCreateFrameworkInstanceModule() !=
            _projectSettings.isCreateFrameworkInstanceModule() ||
        !Comparing.strEqual(_projectSettingsWorkingCopy.getDefaultManifestFileLocation(),
            _projectSettings.getDefaultManifestFileLocation());
  }


  @Nls
  public String getDisplayName()
  {
    return "Project Settings";
  }

  public Icon getIcon()
  {
    return null;
  }

  public String getHelpTopic()
  {
    return null;
  }

  public JComponent createComponent()
  {
    _applicationSettingsUpdateNotifier.addListener(this);
    return _mainPanel;
  }

  public void disposeUIResources()
  {
    _applicationSettingsUpdateNotifier.removeListener(this);
  }

  public boolean isModified()
  {
    return _changed;
  }

  public void apply() throws ConfigurationException
  {
    String oldFrameworkInstanceName = _projectSettings.getFrameworkInstanceName();
    final boolean oldCreateFrameworkInstanceModule = _projectSettings.isCreateFrameworkInstanceModule();

    copySettings(_projectSettingsWorkingCopy, _projectSettings);

    if (!Comparing.strEqual(_projectSettings.getFrameworkInstanceName(), oldFrameworkInstanceName))
    {
      _updateNotifier.fireUpdateFrameworkInstanceSelection(_project);
    }
    if (oldCreateFrameworkInstanceModule != _projectSettings.isCreateFrameworkInstanceModule())
    {
      _updateNotifier.fireFrameworkInstanceModuleHandlingChanged(_project);
    }
    _changed = false;
  }

  public void reset()
  {
    if (_projectSettingsWorkingCopy != null && _applicationSettingsWorkingCopy != null)
    {
      copySettings(_projectSettings, _projectSettingsWorkingCopy);

      refreshFrameworkInstanceCombobox();
      _defaultManifestFileLocation.setSelectedItem(_projectSettingsWorkingCopy.getDefaultManifestFileLocation());
      _createFrameworkInstanceModule.setSelected(_projectSettingsWorkingCopy.isCreateFrameworkInstanceModule());
      _changed = false;
    }
  }

  private void refreshFrameworkInstanceCombobox()
  {
    _updatingFrameworkInstanceComboBox = true;
    List<FrameworkInstanceDefinition> instanceDefinitions =
        _applicationSettingsWorkingCopy.getFrameworkInstanceDefinitions();

    String projectFrameworkInstanceName = _projectSettingsWorkingCopy.getFrameworkInstanceName();
    FrameworkInstanceDefinition projectFrameworkInstance = null;

    _frameworkInstance.removeAllItems();
    for (FrameworkInstanceDefinition instanceDefinition : instanceDefinitions)
    {
      _frameworkInstance.addItem(instanceDefinition);
      if (instanceDefinition.getName().equals(projectFrameworkInstanceName))
      {
        projectFrameworkInstance = instanceDefinition;
      }
    }
    if (projectFrameworkInstance == null && projectFrameworkInstanceName != null)
    {
      projectFrameworkInstance = new FrameworkInstanceDefinition();
      projectFrameworkInstance.setName(projectFrameworkInstanceName);
      projectFrameworkInstance.setDefined(false);
      _frameworkInstance.addItem(projectFrameworkInstance);
    }
    _frameworkInstance.setSelectedItem(projectFrameworkInstance);

    _updatingFrameworkInstanceComboBox = false;
  }

  private void copySettings(ProjectSettings from, ProjectSettings to)
  {
    to.setDefaultManifestFileLocation(from.getDefaultManifestFileLocation());
    to.setCreateFrameworkInstanceModule(from.isCreateFrameworkInstanceModule());
    to.setFrameworkInstanceName(from.getFrameworkInstanceName());

  }

  public void setProjectSettings(ProjectSettings projectSettings, ProjectSettings projectSettingsWorkingCopy)
  {
    _projectSettings = projectSettings;
    _projectSettingsWorkingCopy = projectSettingsWorkingCopy;

    reset();
  }

  public void setApplicationSettings(ApplicationSettings applicationSettings,
                                     ApplicationSettings applicationSettingsWorkingCopy)
  {
    _applicationSettingsWorkingCopy = applicationSettingsWorkingCopy;

    reset();
  }

  private JPanel _mainPanel;
  private JComboBox _frameworkInstance;
  private JCheckBox _createFrameworkInstanceModule;
  private JComboBox _defaultManifestFileLocation;
  private ProjectSettings _projectSettings;
  private ProjectSettings _projectSettingsWorkingCopy;
  private ApplicationSettings _applicationSettingsWorkingCopy;
  private Project _project;
  private FrameworkInstanceUpdateNotifier _updateNotifier;
  private ProjectSettingsUpdateNotifier _projectSettingsUpdateNotifier;
  private boolean _changed;
  private ApplicationSettingsUpdateNotifier _applicationSettingsUpdateNotifier;
  private boolean _updatingFrameworkInstanceComboBox = false;
}
