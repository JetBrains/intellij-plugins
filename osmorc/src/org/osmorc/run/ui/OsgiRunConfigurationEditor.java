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

package org.osmorc.run.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.settings.ApplicationSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor for a bundle run configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class OsgiRunConfigurationEditor extends SettingsEditor<OsgiRunConfiguration>
{

  public OsgiRunConfigurationEditor(final Project project)
  {
    ApplicationSettings registry = ServiceManager.getService(ApplicationSettings.class);
    DefaultComboBoxModel cbmodel = new DefaultComboBoxModel(registry.getFrameworkInstanceDefinitions().toArray());
    _frameworkInstances.setModel(cbmodel);
    _frameworkInstances.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onFrameworkChange();
      }
    });

    this._project = project;
    DefaultTableModel model = new RunConfigurationTableModel();
    model.addColumn(OsmorcBundle.getTranslation("runconfiguration.bundlename"));
    model.addColumn(OsmorcBundle.getTranslation("runconfiguration.bundlestartlevel"));

    _modulesList.setModel(model);
    _modulesList.setRowSelectionAllowed(true);
    _modulesList.setColumnSelectionAllowed(false);

    _addButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onAddClick();
      }
    });
    _removeButton.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        onRemoveClick();
      }
    });
  }

  /**
   * Called when the framework is changed. This will create a new editor for framework properties and will also remove
   * any framework bundles from the list, as they are no longer in classpath.
   */
  private void onFrameworkChange()
  {
    if (_frameworkInstances.getSelectedItem() != null)
    {
      FrameworkInstanceDefinition frameworkInstanceDefinition =
          (FrameworkInstanceDefinition) _frameworkInstances.getSelectedItem();

      // we got a framework instance, get the integrator
      FrameworkIntegratorRegistry registry = ServiceManager.getService(FrameworkIntegratorRegistry.class);
      FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(frameworkInstanceDefinition);

      // clear the panel
      _additionalFrameworkPropertiesPanel.removeAll();

      // create and instal a new editor (if present)
      _currentFrameworkRunPropertiesEditor = integrator.createRunPropertiesEditor();
      if (_currentFrameworkRunPropertiesEditor != null)
      {
        _additionalFrameworkPropertiesPanel.removeAll();
        _additionalFrameworkPropertiesPanel
            .add(_currentFrameworkRunPropertiesEditor.getUI(), BorderLayout.CENTER);
        if (_osgiRunConfiguration != null)
        {
          _currentFrameworkRunPropertiesEditor.resetEditorFrom(_osgiRunConfiguration);
        }
      }

      // remove all framework bundles from the list
      RunConfigurationTableModel model = getTableModel();
      model.removeAllOfType(SelectedBundle.BundleType.FrameworkBundle);
    }
  }

  private void onRemoveClick()
  {
    int[] indices = _modulesList.getSelectedRows();
    DefaultTableModel model = getTableModel();
    for (int i = indices.length - 1; i >= 0; i--)
    {
      model.removeRow(indices[i]);
    }
  }

  private RunConfigurationTableModel getTableModel()
  {
    return (RunConfigurationTableModel) _modulesList.getModel();
  }

  private void onAddClick()
  {

    BundleSelector selector = new BundleSelector(_project);
    selector.setUp((FrameworkInstanceDefinition) _frameworkInstances.getSelectedItem(), getBundlesToRun());
    selector.show(_root);
    List<SelectedBundle> selectedModules = selector.getSelectedBundles();
    if (selectedModules != null)
    {
      RunConfigurationTableModel model = getTableModel();
      for (SelectedBundle aModule : selectedModules)
      {
        model.addModule(aModule);
      }
    }
  }

  protected void resetEditorFrom(OsgiRunConfiguration osgiRunConfiguration)
  {
    _osgiRunConfiguration = osgiRunConfiguration;
    _vmParams.setText(osgiRunConfiguration.getVmParameters());
    _frameworkInstances.setSelectedItem(osgiRunConfiguration.getInstanceToUse());
    _includeAllBundlesinClassPath.setSelected(osgiRunConfiguration.isIncludeAllBundlesInClassPath());

    if (_currentFrameworkRunPropertiesEditor != null)
    {
      _currentFrameworkRunPropertiesEditor.resetEditorFrom(osgiRunConfiguration);
    }

    // I deliberately set the list of modules as the last step here as
    // the framework specific modules are cleaned out when you change the framework instance
    // so the framework instance should be changed first
    List<SelectedBundle> modules = osgiRunConfiguration.getBundlesToDeploy();
    RunConfigurationTableModel model = getTableModel();
    while (model.getRowCount() > 0)
    {
      model.removeRow(0);
    }
    for (SelectedBundle module : modules)
    {
      model.addModule(module);
    }
    _modulesList.getColumnModel().getColumn(1).setPreferredWidth(100);

  }

  protected void applyEditorTo(OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException
  {
    List<SelectedBundle> modules = getBundlesToRun();
    osgiRunConfiguration.setBundlesToDeploy(modules);
    osgiRunConfiguration.setVmParameters(_vmParams.getText());
    osgiRunConfiguration.setIncludeAllBundlesInClassPath(_includeAllBundlesinClassPath.isSelected());
    FrameworkInstanceDefinition frameworkInstanceDefinition =
        (FrameworkInstanceDefinition) _frameworkInstances.getSelectedItem();
    if (frameworkInstanceDefinition != null)
    {
      osgiRunConfiguration.setInstanceToUse(frameworkInstanceDefinition);
    }

    if (_currentFrameworkRunPropertiesEditor != null)
    {
      _currentFrameworkRunPropertiesEditor.applyEditorTo(osgiRunConfiguration);
    }
  }

  private List<SelectedBundle> getBundlesToRun()
  {
    return getTableModel().getBundles();
  }


  @NotNull
  protected JComponent createEditor()
  {
    return _root;
  }

  protected void disposeEditor()
  {
  }

  private OsgiRunConfiguration _osgiRunConfiguration;
  private RawCommandLineEditor _vmParams;
  private JButton _addButton;
  private JButton _removeButton;
  private JComboBox _frameworkInstances;
  private JPanel _additionalFrameworkPropertiesPanel;
  private JTable _modulesList;
  private JTabbedPane _root;
  private JCheckBox _includeAllBundlesinClassPath;
  private final Project _project;
  private FrameworkRunPropertiesEditor _currentFrameworkRunPropertiesEditor;

  private static class RunConfigurationTableModel extends DefaultTableModel
  {

    public Class<?> getColumnClass(int columnIndex)
    {
      if (columnIndex == 1)
      {
        return Integer.class;
      }
      return super
          .getColumnClass(
              columnIndex);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public boolean isCellEditable(int row, int column)
    {
      return column != 0 && super.isCellEditable(row, column);
    }

    @Override
    public void setValueAt(Object o, int i, int i1)
    {
      SelectedBundle bundle = getBundleAt(i);
      if (i1 == 1)
      {
        bundle.setStartLevel((Integer) o);
      }
    }

    public SelectedBundle getBundleAt(int i)
    {
      return (SelectedBundle) super.getValueAt(i, 0);
    }

    public List<SelectedBundle> getBundles()
    {
      List<SelectedBundle> bundles = new ArrayList<SelectedBundle>();
      for (int i = 0; i < getRowCount(); i++)
      {
        bundles.add(getBundleAt(i));
      }
      return bundles;
    }

    @Override
    public Object getValueAt(int i, int i1)
    {
      SelectedBundle bundle = getBundleAt(i);
      if (i1 == 0)
      {
        return bundle.toString();
      }
      if (i1 == 1)
      {
        return bundle.getStartLevel();
      }
      return null;
    }

    public void addModule(SelectedBundle aModule)
    {
      addRow(new Object[]{aModule});
    }

    public void removeAllOfType(SelectedBundle.BundleType type)
    {
      for (int i = 0; i < getRowCount(); i++)
      {
        if (getBundleAt(i).getBundleType() == type)
        {
          removeRow(i);
          i--;
        }
      }
    }
  }
}


