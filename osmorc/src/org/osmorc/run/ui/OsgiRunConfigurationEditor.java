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

import com.intellij.execution.ui.AlternativeJREPanel;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.BundleSelectionAction;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.OsgiRunConfigurationChecker;
import org.osmorc.run.OsgiRunConfigurationCheckerProvider;
import org.osmorc.settings.ApplicationSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Editor for a bundle run configuration.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class OsgiRunConfigurationEditor extends SettingsEditor<OsgiRunConfiguration> implements BundleSelectionAction.Context {
  private final DefaultActionGroup frameworkSpecificBundleSelectionActions;

  public OsgiRunConfigurationEditor(final Project project) {
    ApplicationSettings registry = ServiceManager.getService(ApplicationSettings.class);
    DefaultComboBoxModel cbmodel = new DefaultComboBoxModel(registry.getFrameworkInstanceDefinitions().toArray());
    frameworkInstances.setModel(cbmodel);
    frameworkInstances.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onFrameworkChange();
      }
    });

    this.project = project;
    RunConfigurationTableModel model = new RunConfigurationTableModel();

    modulesList.setModel(model);
    modulesList.setRowSelectionAllowed(true);
    modulesList.setColumnSelectionAllowed(false);
    modulesList.setDefaultEditor(Integer.class, new JSpinnerCellEditor());
    modulesList.setDefaultRenderer(Integer.class, new JSpinnerCellEditor());

    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onAddClick();
      }
    });
    removeButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onRemoveClick();
      }
    });
    osmorcControlledRadioButton.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        boolean isUserDefined = !osmorcControlledRadioButton.isSelected();
        workingDirField.setEnabled(isUserDefined);
      }
    });

    workingDirField
      .addBrowseFolderListener("Choose a working directory", "The working directory is the directory from which the framework is started",
                               null, FileChooserDescriptorFactory.createSingleFolderDescriptor());
    workingDirField.getTextField().setColumns(30);
    frameworkSpecificBundleSelectionActions = new DefaultActionGroup("frameworkSpecificBundleSelectionActions", true);

    myAutomaticStartLevel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myFrameworkStartLevel.setEnabled(!myAutomaticStartLevel.isSelected());
      }
    });

    frameworkSpecificButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JBPopupFactory.getInstance().createActionGroupPopup(null, frameworkSpecificBundleSelectionActions,
                                                            DataManager.getInstance().getDataContext(frameworkSpecificButton),
                                                            JBPopupFactory.ActionSelectionAid.NUMBERING, true)
          .showUnderneathOf(frameworkSpecificButton);
      }
    });
    modulesList.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        int width = modulesList.getWidth();
        int bundleNameWidth = 2 * width / 3;
        int otherWidth = width / 3 / 2;

        TableColumn bundleColumn = modulesList.getColumnModel().getColumn(0);
        bundleColumn.setPreferredWidth(bundleNameWidth);

        TableColumn startLevelColumn = modulesList.getColumnModel().getColumn(1);
        startLevelColumn.setPreferredWidth(otherWidth);

        TableColumn startColumn = modulesList.getColumnModel().getColumn(2);
        startColumn.setPreferredWidth(otherWidth);
      }
    });
    // avoid text fields growing the dialog when much text is entered.
    vmParams.getTextField().setPreferredSize(new Dimension(100,20));
    programParameters.getTextField().setPreferredSize(new Dimension(100,20));  
  }

  /**
   * Called when the framework is changed. This will create a new editor for framework properties and will also remove
   * any framework bundles from the list, as they are no longer in classpath.
   */
  private void onFrameworkChange() {
    frameworkSpecificBundleSelectionActions.removeAll();
    if (frameworkInstances.getSelectedItem() != null) {
      FrameworkInstanceDefinition frameworkInstanceDefinition = (FrameworkInstanceDefinition)frameworkInstances.getSelectedItem();

      // we got a framework instance, get the integrator
      FrameworkIntegratorRegistry registry = ServiceManager.getService(FrameworkIntegratorRegistry.class);
      FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(frameworkInstanceDefinition);

      // clear the panel
      additionalFrameworkPropertiesPanel.removeAll();

      // create and install a new editor (if present)
      currentFrameworkRunPropertiesEditor = integrator.createRunPropertiesEditor();
      if (currentFrameworkRunPropertiesEditor != null) {
        additionalFrameworkPropertiesPanel.removeAll();
        additionalFrameworkPropertiesPanel.add(currentFrameworkRunPropertiesEditor.getUI(), BorderLayout.CENTER);
        if (osgiRunConfiguration != null) {
          currentFrameworkRunPropertiesEditor.resetEditorFrom(osgiRunConfiguration);
          OsgiRunConfigurationChecker checker = null;
          if (integrator instanceof OsgiRunConfigurationCheckerProvider) {
            checker = ((OsgiRunConfigurationCheckerProvider)integrator).getOsgiRunConfigurationChecker();
          }
          osgiRunConfiguration.setAdditionalChecker(checker);
        }
      }

      // remove all framework bundles from the list
      RunConfigurationTableModel model = getTableModel();
      model.removeAllOfType(SelectedBundle.BundleType.FrameworkBundle);

      for (BundleSelectionAction bundleSelectionAction : integrator.getBundleSelectionActions()) {
        bundleSelectionAction.setContext(this);
        frameworkSpecificBundleSelectionActions.add(bundleSelectionAction);
      }
    }
  }

  private void onRemoveClick() {
    int[] indices = modulesList.getSelectedRows();
    RunConfigurationTableModel model = getTableModel();
    for (int i = indices.length - 1; i >= 0; i--) {
      model.removeBundleAt(indices[i]);
    }
  }

  private RunConfigurationTableModel getTableModel() {
    return (RunConfigurationTableModel)modulesList.getModel();
  }

  private void onAddClick() {

    BundleSelector selector = new BundleSelector(project);
    selector.setUp((FrameworkInstanceDefinition)frameworkInstances.getSelectedItem(), getBundlesToRun());
    selector.show(root);
    List<SelectedBundle> selectedModules = selector.getSelectedBundles();
    if (selectedModules != null) {
      RunConfigurationTableModel model = getTableModel();
      for (SelectedBundle aModule : selectedModules) {
        model.addBundle(aModule);
      }
    }
  }

  @NotNull
  public List<SelectedBundle> getCurrentlySelectedBundles() {
    return getBundlesToRun();
  }

  public void addBundle(@NotNull SelectedBundle bundle) {
    getTableModel().addBundle(bundle);
  }

  public void removeBundle(@NotNull SelectedBundle bundle) {
    getTableModel().removeBundle(bundle);
  }

  @Nullable
  public FrameworkInstanceDefinition getUsedFrameworkInstance() {
    return (FrameworkInstanceDefinition)frameworkInstances.getSelectedItem();
  }

  protected void resetEditorFrom(OsgiRunConfiguration osgiRunConfiguration) {
    this.osgiRunConfiguration = osgiRunConfiguration;
    vmParams.setText(osgiRunConfiguration.getVmParameters());
    programParameters.setText(osgiRunConfiguration.getProgramParameters());
    frameworkInstances.setSelectedItem(osgiRunConfiguration.getInstanceToUse());
    includeAllBundlesinClassPath.setSelected(osgiRunConfiguration.isIncludeAllBundlesInClassPath());

    if (currentFrameworkRunPropertiesEditor != null) {
      currentFrameworkRunPropertiesEditor.resetEditorFrom(osgiRunConfiguration);
    }

    // I deliberately set the list of modules as the last step here as
    // the framework specific modules are cleaned out when you change the framework instance
    // so the framework instance should be changed first
    List<SelectedBundle> modules = osgiRunConfiguration.getBundlesToDeploy();
    RunConfigurationTableModel model = getTableModel();
    while (model.getRowCount() > 0) {
      model.removeBundleAt(0);
    }
    for (SelectedBundle module : modules) {
      model.addBundle(module);
    }
    modulesList.getColumnModel().getColumn(1).setPreferredWidth(200);

    myAutomaticStartLevel.setSelected(osgiRunConfiguration.isAutoStartLevel());
    myFrameworkStartLevel.setValue(osgiRunConfiguration.getFrameworkStartLevel());
    myFrameworkStartLevel.setEnabled(!myAutomaticStartLevel.isSelected());
    myDefaultStartLevel.setValue(osgiRunConfiguration.getDefaultStartLevel());

    boolean useUserDefinedFields = !osgiRunConfiguration.isGenerateWorkingDir();
    workingDirField.setText(osgiRunConfiguration.getWorkingDir());
    if (workingDirField.getText().length() == 0) {
      final CompilerProjectExtension extension = CompilerProjectExtension.getInstance(project);
      if (extension != null) {
        final VirtualFilePointer outputDirPointer = extension.getCompilerOutputPointer();
        if (outputDirPointer != null) {
          workingDirField.setText(VfsUtil.urlToPath(outputDirPointer.getUrl() + "/run.osgi/"));
        }
      }
    }

    workingDirField.setEnabled(useUserDefinedFields);
    userDefinedRadioButton.setSelected(useUserDefinedFields);
    osmorcControlledRadioButton.setSelected(!useUserDefinedFields);
    alternativeJREPanel.init(osgiRunConfiguration.getAlternativeJrePath(), osgiRunConfiguration.isUseAlternativeJre());
  }

  protected void applyEditorTo(OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException {
    List<SelectedBundle> modules = getBundlesToRun();
    osgiRunConfiguration.setBundlesToDeploy(modules);
    osgiRunConfiguration.setVmParameters(vmParams.getText());
    osgiRunConfiguration.setProgramParameters(programParameters.getText());
    osgiRunConfiguration.setIncludeAllBundlesInClassPath(includeAllBundlesinClassPath.isSelected());
    osgiRunConfiguration.setWorkingDir(workingDirField.getText().replace('\\', '/'));
    osgiRunConfiguration.setUseAlternativeJre(alternativeJREPanel.isPathEnabled());
    osgiRunConfiguration.setAlternativeJrePath(alternativeJREPanel.getPath());
    osgiRunConfiguration.setFrameworkStartLevel((Integer)myFrameworkStartLevel.getValue());
    osgiRunConfiguration.setDefaultStartLevel((Integer)myDefaultStartLevel.getValue());
    osgiRunConfiguration.setAutoStartLevel(myAutomaticStartLevel.isSelected());
    osgiRunConfiguration.setGenerateWorkingDir(osmorcControlledRadioButton.isSelected());
    FrameworkInstanceDefinition frameworkInstanceDefinition = (FrameworkInstanceDefinition)frameworkInstances.getSelectedItem();
    if (frameworkInstanceDefinition != null) {
      osgiRunConfiguration.setInstanceToUse(frameworkInstanceDefinition);
    }

    if (currentFrameworkRunPropertiesEditor != null) {
      currentFrameworkRunPropertiesEditor.applyEditorTo(osgiRunConfiguration);
    }

  }

  private List<SelectedBundle> getBundlesToRun() {
    return getTableModel().getBundles();
  }


  @NotNull
  protected JComponent createEditor() {
    return root;
  }

  protected void disposeEditor() {
  }

  private OsgiRunConfiguration osgiRunConfiguration;
  private RawCommandLineEditor vmParams;
  private JButton addButton;
  private JButton removeButton;
  private JComboBox frameworkInstances;
  private JPanel additionalFrameworkPropertiesPanel;
  private JTable modulesList;
  private JTabbedPane root;
  private JCheckBox includeAllBundlesinClassPath;
  private JRadioButton osmorcControlledRadioButton;
  private JRadioButton userDefinedRadioButton;
  private TextFieldWithBrowseButton workingDirField;
  private RawCommandLineEditor programParameters;
  private JButton frameworkSpecificButton;
  private AlternativeJREPanel alternativeJREPanel;
  private JSpinner myFrameworkStartLevel;
  private JCheckBox myAutomaticStartLevel;
  private JSpinner myDefaultStartLevel;
  private final Project project;
  private FrameworkRunPropertiesEditor currentFrameworkRunPropertiesEditor;


  private static class RunConfigurationTableModel extends AbstractTableModel {
    private final List<SelectedBundle> selectedBundles;

    public RunConfigurationTableModel() {
      selectedBundles = new ArrayList<SelectedBundle>();
    }

    public SelectedBundle getBundleAt(final int index) {
      return selectedBundles.get(index);
    }

    public List<SelectedBundle> getBundles() {
      return selectedBundles;
    }

    public void removeBundle(SelectedBundle bundle) {
      removeBundleAt(selectedBundles.indexOf(bundle));
    }

    public void removeBundleAt(final int index) {
      selectedBundles.remove(index);
      fireTableRowsDeleted(index, index);
    }

    public void addBundle(final SelectedBundle bundle) {
      selectedBundles.add(bundle);
      fireTableRowsInserted(selectedBundles.size() - 1, selectedBundles.size() - 1);
    }

    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return String.class;
        case 1:
          return Integer.class;
        case 2:
          return Boolean.class;
        default:
          return Object.class;
      }
    }

    @Override
    public String getColumnName(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return "Bundle Name";
        case 1:
          return "Start Level";
        case 2:
          return "Start After Install";
        default:
          return "";
      }
    }

    public int getColumnCount() {
      return 3;
    }

    public int getRowCount() {
      return selectedBundles.size();
    }

    public boolean isCellEditable(int row, int column) {
      return column != 0;
    }

    @Override
    public void setValueAt(Object o, int row, int column) {
      SelectedBundle bundle = getBundleAt(row);
      switch (column) {
        case 1:
          bundle.setStartLevel((Integer)o);
          break;
        case 2:
          bundle.setStartAfterInstallation((Boolean)o);
          break;
        default:
          throw new RuntimeException("Cannot edit column " + column);
      }
    }

    public Object getValueAt(int row, int column) {
      SelectedBundle bundle = getBundleAt(row);
      switch (column) {
        case 0:
          return bundle.toString();
        case 1:
          return bundle.getStartLevel();
        case 2:
          return bundle.isStartAfterInstallation();
        default:
          throw new RuntimeException("Don't know column " + column);
      }
    }

    public void removeAllOfType(SelectedBundle.BundleType type) {
      for (Iterator<SelectedBundle> selectedBundleIterator = selectedBundles.iterator(); selectedBundleIterator.hasNext();) {
        SelectedBundle selectedBundle = selectedBundleIterator.next();
        if (selectedBundle.getBundleType() == type) {
          selectedBundleIterator.remove();
        }
      }
      fireTableDataChanged();
    }
  }
}


