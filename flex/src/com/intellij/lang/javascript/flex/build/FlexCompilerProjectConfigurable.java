package com.intellij.lang.javascript.flex.build;

import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.newEditor.OptionsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexCompilerProjectConfigurable implements SearchableConfigurable, Configurable.NoScroll {

  private JPanel myMainPanel;
  private JRadioButton myBuiltInCompilerRadioButton;
  private JRadioButton myMxmlcCompcRadioButton;
  private JCheckBox myPreferASC20CheckBox;

  private JSpinner myParallelCompilationsAmountSpinner;
  private JTextField myHeapSizeTextField;
  private RawCommandLineEditor myVMOptionsEditor;
  private JLabel myParallelCompilationLabel;
  private JLabel myThreadsOrProcessesLabel;
  private HoverHyperlinkLabel myCompilerPageLink;

  private final Project myProject;
  private final FlexCompilerProjectConfiguration myConfig;

  public FlexCompilerProjectConfigurable(final Project project) {
    myProject = project;
    myConfig = FlexCompilerProjectConfiguration.getInstance(project);

    final ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();
      }
    };
    myBuiltInCompilerRadioButton.addActionListener(actionListener);
    myMxmlcCompcRadioButton.addActionListener(actionListener);

    myParallelCompilationsAmountSpinner
      .setModel(new SpinnerNumberModel(1, 1, FlexCompilerProjectConfiguration.PARALLEL_COMPILATIONS_LIMIT, 1));

    myVMOptionsEditor.setDialogCaption(FlexBundle.message("flex.compiler.vm.options.title"));
  }

  private void updateControls() {
    final boolean parallelEnabled = myBuiltInCompilerRadioButton.isSelected() || myMxmlcCompcRadioButton.isSelected();
    myParallelCompilationLabel.setEnabled(parallelEnabled);
    myParallelCompilationsAmountSpinner.setEnabled(parallelEnabled);
    myThreadsOrProcessesLabel.setEnabled(parallelEnabled);
    if (myMxmlcCompcRadioButton.isSelected()) {
      myThreadsOrProcessesLabel.setText(" " + FlexBundle.message("processes"));
    }
    else if (myBuiltInCompilerRadioButton.isSelected()) {
      myThreadsOrProcessesLabel.setText(" " + FlexBundle.message("threads"));
    }
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  @NotNull
  public String getId() {
    return "flex.compiler";
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  public String getDisplayName() {
    return "Flex Compiler";
  }

  public String getHelpTopic() {
    return "reference.projectsettings.compiler.flex";
  }

  public boolean isModified() {
    return isImportantModified() ||
           myConfig.MAX_PARALLEL_COMPILATIONS != (Integer)myParallelCompilationsAmountSpinner.getValue();
  }

  private boolean isImportantModified() {
    return myConfig.USE_MXMLC_COMPC != myMxmlcCompcRadioButton.isSelected() ||
           myConfig.USE_BUILT_IN_COMPILER != myBuiltInCompilerRadioButton.isSelected() ||
           myConfig.PREFER_ASC_20 != myPreferASC20CheckBox.isSelected() ||
           !myHeapSizeTextField.getText().trim().equals(String.valueOf(myConfig.HEAP_SIZE_MB)) ||
           !myVMOptionsEditor.getText().trim().equals(myConfig.VM_OPTIONS);
  }

  public void apply() throws ConfigurationException {
    if (!myProject.isDefault() && isImportantModified()) {
      final FlexCompilerHandler flexCompilerHandler = FlexCompilerHandler.getInstance(myProject);
      flexCompilerHandler.getCompilerDependenciesCache().clear();
      flexCompilerHandler.quitCompilerShell();
      flexCompilerHandler.getBuiltInFlexCompilerHandler().stopCompilerProcess();
    }

    myConfig.USE_BUILT_IN_COMPILER = myBuiltInCompilerRadioButton.isSelected();
    myConfig.USE_MXMLC_COMPC = myMxmlcCompcRadioButton.isSelected();
    myConfig.PREFER_ASC_20 = myPreferASC20CheckBox.isSelected();
    myConfig.MAX_PARALLEL_COMPILATIONS = (Integer)myParallelCompilationsAmountSpinner.getValue();

    try {
      final int heapSizeMb = Integer.parseInt(myHeapSizeTextField.getText().trim());
      if (heapSizeMb > 0) {
        myConfig.HEAP_SIZE_MB = heapSizeMb;
      }
      else {
        throw new ConfigurationException(FlexBundle.message("invalid.flex.compiler.heap.size"));
      }
    }
    catch (NumberFormatException e) {
      throw new ConfigurationException(FlexBundle.message("invalid.flex.compiler.heap.size"));
    }

    myConfig.VM_OPTIONS = myVMOptionsEditor.getText().trim();
  }

  public void reset() {
    myBuiltInCompilerRadioButton.setSelected(myConfig.USE_BUILT_IN_COMPILER);
    myMxmlcCompcRadioButton.setSelected(myConfig.USE_MXMLC_COMPC);
    myPreferASC20CheckBox.setSelected(myConfig.PREFER_ASC_20);
    myParallelCompilationsAmountSpinner.setValue(myConfig.MAX_PARALLEL_COMPILATIONS);
    myHeapSizeTextField.setText(String.valueOf(myConfig.HEAP_SIZE_MB));
    myVMOptionsEditor.setText(myConfig.VM_OPTIONS);
    updateControls();
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    myCompilerPageLink = new HoverHyperlinkLabel("parallel compilation");

    final HyperlinkAdapter listener = new HyperlinkAdapter() {
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        final DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        final OptionsEditor optionsEditor = OptionsEditor.KEY.getData(dataContext);
        if (optionsEditor != null) {
          final Configurable configurable = optionsEditor.findConfigurableById("project.propCompiler");
          if (configurable != null) {
            optionsEditor.clearSearchAndSelect(configurable);
          }
        }
      }
    };

    myCompilerPageLink.addHyperlinkListener(listener);
  }
}
