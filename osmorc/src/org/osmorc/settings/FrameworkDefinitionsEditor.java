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
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.osmorc.frameworkintegration.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FrameworkDefinitionsEditor implements Configurable, ApplicationSettingsAwareEditor,
    ProjectSettingsAwareEditor
{
  public FrameworkDefinitionsEditor(FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                                    FrameworkInstanceUpdateNotifier updateNotifier,
                                    ApplicationSettingsUpdateNotifier applicationSettingsUpdateNotifier)
  {
    _frameworkIntegratorRegistry = frameworkIntegratorRegistry;
    _updateNotifier = updateNotifier;
    _applicationSettingsUpdateNotifier = applicationSettingsUpdateNotifier;

    _addedFrameworkInstances = new ArrayList<FrameworkInstanceDefinition>();
    _removedFrameworkInstances = new ArrayList<FrameworkInstanceDefinition>();
    _reloadedFrameworkInstances = new ArrayList<FrameworkInstanceDefinition>();

    _baseFolder.setEditable(false);
    _frameworkInstanceName.setEditable(false);
    _frameworkIntegrator.setEditable(false);

    _addFramework.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        addFrameworkInstance();
      }
    });
    _removeFramework.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        removeFrameworkInstance();
      }
    });

    _frameworkInstances.addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        _selectedFrameworkInstance = (FrameworkInstanceDefinition) _frameworkInstances.getSelectedValue();

        if (_selectedFrameworkInstance != null)
        {
          _frameworkIntegrator.setText(_selectedFrameworkInstance.getFrameworkIntegratorName());
          _baseFolder.setText(_selectedFrameworkInstance.getBaseFolder());
          _frameworkInstanceName.setText(_selectedFrameworkInstance.getName());
        }
      }
    });

  }

  private void addFrameworkInstance()
  {
    String selectedFrameworkInstanceForProject = _projectSettingsWorkingCopy.getFrameworkInstanceName();
    FrameworkInstanceDefinition frameworkInstanceDefinition =
        _applicationSettingsWorkingCopy.getFrameworkInstance(selectedFrameworkInstanceForProject);

    String frameworkInstanceNameForCreation = null;
    if (frameworkInstanceDefinition == null)
    {
      frameworkInstanceNameForCreation = selectedFrameworkInstanceForProject;
    }

    CreateFrameworkInstanceDialog dialog =
        new CreateFrameworkInstanceDialog(_frameworkIntegratorRegistry,
            frameworkInstanceNameForCreation);
    dialog.pack();
    dialog.show();

    if (dialog.isOK())
    {
      FrameworkInstanceDefinition instanceDefinition = new FrameworkInstanceDefinition();
      instanceDefinition.setName(dialog.getName());
      instanceDefinition.setFrameworkIntegratorName(dialog.getIntegratorName());
      instanceDefinition.setBaseFolder(dialog.getBaseFolder());

      getFrameworkInistanceManager(instanceDefinition).createLibraries(instanceDefinition);

      _applicationSettingsWorkingCopy.addFrameworkInstanceDefinition(instanceDefinition);
      _applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
      _changed = true;
      refreshFrameworkInstanceList();
      _frameworkInstances.setSelectedValue(instanceDefinition, true);

      _addedFrameworkInstances.add(instanceDefinition);
      _removedFrameworkInstances.remove(instanceDefinition);
      assert !_reloadedFrameworkInstances.contains(instanceDefinition);
    }
  }

  private FrameworkInstanceManager getFrameworkInistanceManager(FrameworkInstanceDefinition instanceDefinition)
  {
    FrameworkIntegrator frameworkIntegrator =
        _frameworkIntegratorRegistry.findIntegratorByInstanceDefinition(instanceDefinition);
    return frameworkIntegrator.getFrameworkInstanceManager();
  }

  private void removeFrameworkInstance()
  {
    FrameworkInstanceDefinition selectedFrameworkInstance = _selectedFrameworkInstance;
    if (selectedFrameworkInstance != null)
    {
      getFrameworkInistanceManager(selectedFrameworkInstance).removeLibraries(selectedFrameworkInstance);
      _applicationSettingsWorkingCopy.removeFrameworkInstanceDefinition(selectedFrameworkInstance);
      _applicationSettingsUpdateNotifier.fireApplicationSettingsChanged();
      _changed = true;
      refreshFrameworkInstanceList();
      _frameworkInstances.setSelectedIndex(0);

      if (!_addedFrameworkInstances.contains(selectedFrameworkInstance))
      {
        _removedFrameworkInstances.add(selectedFrameworkInstance);
      }
      _addedFrameworkInstances.remove(selectedFrameworkInstance);
      _reloadedFrameworkInstances.remove(selectedFrameworkInstance);
    }
  }

  private void refreshFrameworkInstanceList()
  {
    List<FrameworkInstanceDefinition> instanceDefinitions =
        _applicationSettingsWorkingCopy.getFrameworkInstanceDefinitions();
    _frameworkInstances.setListData(instanceDefinitions.toArray(new Object[instanceDefinitions.size()]));
  }

  private void copySettings(ApplicationSettings from, ApplicationSettings to)
  {
    List<FrameworkInstanceDefinition> copiedDefinitions = new ArrayList<FrameworkInstanceDefinition>();
    for (FrameworkInstanceDefinition definition : from.getFrameworkInstanceDefinitions())
    {
      FrameworkInstanceDefinition copiedDefinition = new FrameworkInstanceDefinition();
      XmlSerializerUtil.copyBean(definition, copiedDefinition);
      copiedDefinitions.add(copiedDefinition);
    }
    to.setFrameworkInstanceDefinitions(copiedDefinitions);
  }


  @Nls
  public String getDisplayName()
  {
    return "Framework Definitions";
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
    return _mainPanel;
  }

  public boolean isModified()
  {
    return _changed;
  }

  public void apply() throws ConfigurationException
  {
    copySettings(_applicationSettingsWorkingCopy, _applicationSettings);

    for (FrameworkInstanceDefinition addedFrameworkInstance : _addedFrameworkInstances)
    {
      _updateNotifier.fireUpdateFrameworkInstance(addedFrameworkInstance,
          FrameworkInstanceUpdateNotifier.UpdateKind.ADDITION);
    }
    for (FrameworkInstanceDefinition removedFrameworkInstance : _removedFrameworkInstances)
    {
      _updateNotifier.fireUpdateFrameworkInstance(removedFrameworkInstance,
          FrameworkInstanceUpdateNotifier.UpdateKind.REMOVAL);
    }
    for (FrameworkInstanceDefinition reloadedFrameworkInstance : _reloadedFrameworkInstances)
    {
      _updateNotifier.fireUpdateFrameworkInstance(reloadedFrameworkInstance,
          FrameworkInstanceUpdateNotifier.UpdateKind.RELOAD);
    }
    _changed = false;
  }

  public void reset()
  {
    if (_applicationSettings != null)
    {
      copySettings(_applicationSettings, _applicationSettingsWorkingCopy);
      refreshFrameworkInstanceList();
      _changed = false;
    }
  }

  public void disposeUIResources()
  {
    if (_changed)
    {
      cleanUpFrameworkInstances(_applicationSettings.getFrameworkInstanceDefinitions(),
          _applicationSettingsWorkingCopy.getFrameworkInstanceDefinitions());
    }
  }

  private void cleanUpFrameworkInstances(List<FrameworkInstanceDefinition> oldFrameworkInstances,
                                         List<FrameworkInstanceDefinition> newFrameworkInstances)
  {
    for (FrameworkInstanceDefinition newFrameworkInstance : newFrameworkInstances)
    {
      if (!oldFrameworkInstances.contains(newFrameworkInstance))
      {
        getFrameworkInistanceManager(newFrameworkInstance).removeLibraries(newFrameworkInstance);
      }
    }
  }

  public void setApplicationSettings(ApplicationSettings applicationSettings,
                                     ApplicationSettings applicationSettingsWorkingCopy)
  {
    _applicationSettings = applicationSettings;
    _applicationSettingsWorkingCopy = applicationSettingsWorkingCopy;
    reset();
  }

  public void setProjectSettings(ProjectSettings projectSettings, ProjectSettings projectSettingsWorkingCopy)
  {
    _projectSettingsWorkingCopy = projectSettingsWorkingCopy;
  }

  private JPanel _mainPanel;
  private JList _frameworkInstances;
  private JButton _addFramework;
  private JButton _removeFramework;
  private JTextField _frameworkIntegrator;
  private JTextField _baseFolder;
  private JTextField _frameworkInstanceName;
  private FrameworkIntegratorRegistry _frameworkIntegratorRegistry;
  private FrameworkInstanceUpdateNotifier _updateNotifier;
  private ApplicationSettingsUpdateNotifier _applicationSettingsUpdateNotifier;
  private List<FrameworkInstanceDefinition> _addedFrameworkInstances;
  private List<FrameworkInstanceDefinition> _removedFrameworkInstances;
  private List<FrameworkInstanceDefinition> _reloadedFrameworkInstances;
  private ApplicationSettings _applicationSettings;
  private ApplicationSettings _applicationSettingsWorkingCopy;
  private FrameworkInstanceDefinition _selectedFrameworkInstance;
  private boolean _changed;
  private ProjectSettings _projectSettingsWorkingCopy;
}
