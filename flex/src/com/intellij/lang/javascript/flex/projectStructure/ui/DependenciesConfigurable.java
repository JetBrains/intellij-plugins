package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeModuleStructureExtension;
import com.intellij.lang.javascript.flex.projectStructure.options.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class DependenciesConfigurable extends NamedConfigurable<Dependencies> {

  private static final Logger LOG = Logger.getInstance(DependenciesConfigurable.class.getName());

  private JPanel myMainPanel;
  private FlexSdkChooserPanel mySdkPanel;
  private JComboBox myComponentSetCombo;
  private JComboBox myFrameworkLinkageCombo;
  private JLabel myComponentSetLabel;
  private JPanel myTablePanel;
  private final EditableTreeTable<MyTableItem> myTable;

  private final Dependencies myDependencies;
  private final Project myProject;
  private final ModifiableRootModel myRootModel;
  private AddItemPopupAction[] myPopupActions;

  private abstract static class MyTableItem {
    public abstract String getText();

    public abstract Icon getIcon();
  }

  private static class BCItem extends MyTableItem {
    public final FlexIdeBCConfigurable configurable;

    public BCItem(FlexIdeBCConfigurable configurable) {
      this.configurable = configurable;
    }

    @Override
    public String getText() {
      return MessageFormat.format("{0} ({1})", configurable.getTreeNodeText(), configurable.getModuleName());
    }

    @Override
    public Icon getIcon() {
      return configurable.getIcon();
    }
  }

  public DependenciesConfigurable(final FlexIdeBuildConfiguration bc, Project project, ModifiableRootModel rootModel) {
    myDependencies = bc.DEPENDENCIES;
    myProject = project;
    myRootModel = rootModel;

    final boolean mobilePlatform = bc.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile;

    myComponentSetLabel.setVisible(!mobilePlatform && !bc.PURE_ACTION_SCRIPT);
    myComponentSetCombo.setVisible(!mobilePlatform && !bc.PURE_ACTION_SCRIPT);

    myComponentSetCombo.setModel(new DefaultComboBoxModel(FlexIdeBuildConfiguration.ComponentSet.values()));
    myComponentSetCombo.setRenderer(new ListCellRendererWrapper<FlexIdeBuildConfiguration.ComponentSet>(myComponentSetCombo.getRenderer()) {
      public void customize(JList list, FlexIdeBuildConfiguration.ComponentSet value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });

    final FlexIdeBuildConfiguration.FrameworkLinkage defaultLinkage =
      BCUtils.getDefaultFrameworkLinkage(bc.TARGET_PLATFORM, bc.PURE_ACTION_SCRIPT, bc.OUTPUT_TYPE);
    myFrameworkLinkageCombo
      .setRenderer(new ListCellRendererWrapper<FlexIdeBuildConfiguration.FrameworkLinkage>(myFrameworkLinkageCombo.getRenderer()) {
        public void customize(JList list, FlexIdeBuildConfiguration.FrameworkLinkage value, int index, boolean selected, boolean hasFocus) {
          if (value == FlexIdeBuildConfiguration.FrameworkLinkage.Default) {
            setText(MessageFormat.format("Default ({0})", defaultLinkage.PRESENTABLE_TEXT));
          }
          else {
            setText(value.PRESENTABLE_TEXT);
          }
        }
      });

    myFrameworkLinkageCombo.setModel(new DefaultComboBoxModel(BCUtils.getSuitableFrameworkLinkages(bc.TARGET_PLATFORM,
                                                                                                   bc.PURE_ACTION_SCRIPT, bc.OUTPUT_TYPE)));
    myTable = new EditableTreeTable<MyTableItem>("") {
      @Override
      protected void render(SimpleColoredComponent c, MyTableItem item) {
        if (item != null) {
          c.append(item.getText());
          c.setIcon(item.getIcon());
        }
      }
    };
    myTable.setRootVisible(false);
    myTable.getTree().setLineStyleAngled();

    ToolbarDecorator d = ToolbarDecorator.createDecorator(myTable);
    d.setAddAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton button) {
        addItem(button);
      }
    });
    d.setRemoveAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton anActionButton) {
        removeSelection();
      }
    });
    d.setUpAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton anActionButton) {
        moveSelection(-1);
      }
    });
    d.setDownAction(new AnActionButtonRunnable() {
      @Override
      public void run(AnActionButton anActionButton) {
        moveSelection(1);
      }
    });
    myTablePanel.add(d.createPanel(), BorderLayout.CENTER);
  }

  private void addItem(AnActionButton button) {
    initPopupActions();
    final JBPopup popup = JBPopupFactory.getInstance().createListPopup(
      new BaseListPopupStep<AddItemPopupAction>(null, myPopupActions) {
        @Override
        public Icon getIconFor(AddItemPopupAction aValue) {
          return aValue.getIcon();
        }

        @Override
        public boolean hasSubstep(AddItemPopupAction selectedValue) {
          return selectedValue.hasSubStep();
        }

        public boolean isMnemonicsNavigationEnabled() {
          return true;
        }

        public PopupStep onChosen(final AddItemPopupAction selectedValue, final boolean finalChoice) {
          if (selectedValue.hasSubStep()) {
            return selectedValue.createSubStep();
          }
          return doFinalStep(new Runnable() {
            public void run() {
              selectedValue.run();
            }
          });
        }

        @NotNull
        public String getTextFor(AddItemPopupAction value) {
          return "&" + value.getIndex() + "  " + value.getTitle();
        }
      });
    popup.show(button.getPreferredPopupPoint());
  }

  private void removeSelection() {
    int[] selectedRows = myTable.getSelectedRows();
    Arrays.sort(selectedRows);
    for (int i = 0; i < selectedRows.length; i++) {
      DefaultMutableTreeNode root = myTable.getRoot();
      root.remove(selectedRows[i] - i);
    }
    myTable.refresh();
    if (myTable.getRowCount() > 0) {
      int toSelect = Math.min(myTable.getRowCount() - 1, selectedRows[0]);
      myTable.clearSelection();
      myTable.getSelectionModel().addSelectionInterval(toSelect, toSelect);
    }
  }

  private void moveSelection(int delta) {
    int[] selectedRows = myTable.getSelectedRows();
    Arrays.sort(selectedRows);
    DefaultMutableTreeNode root = myTable.getRoot();

    if (delta < 0) {
      for (int i = 0; i < selectedRows.length; i++) {
        int row = selectedRows[i];
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(row);
        root.remove(row);
        root.insert(child, row + delta);
      }
    }
    else {
      for (int i = selectedRows.length - 1; i >= 0; i--) {
        int row = selectedRows[i];
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(row);
        root.remove(row);
        root.insert(child, row + delta);
      }
    }
    myTable.refresh();
    myTable.clearSelection();
    for (int selectedRow : selectedRows) {
      myTable.getSelectionModel().addSelectionInterval(selectedRow + delta, selectedRow + delta);
    }
  }

  @Nls
  public String getDisplayName() {
    return "Dependencies";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "Dependencies";
  }

  public Icon getIcon() {
    return null;
  }

  public Dependencies getEditableObject() {
    return myDependencies;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (mySdkPanel.isModified()) return true;
    if (myDependencies.COMPONENT_SET != myComponentSetCombo.getSelectedItem()) return true;
    if (myDependencies.FRAMEWORK_LINKAGE != myFrameworkLinkageCombo.getSelectedItem()) return true;

    List<MyTableItem> items = myTable.getItems();
    List<DependencyEntry> entries = myDependencies.getEntries();
    if (items.size() != entries.size()) return true;
    for (int i = 0; i < items.size(); i++) {
      BCItem item = (BCItem)items.get(i);
      BuildConfigurationEntry entry = (BuildConfigurationEntry)entries.get(i);
      if (item.configurable.getModifiableRootModel().getModule() != entry.getModule()) return true;
      if (!item.configurable.getDisplayName().equals(entry.getBcName())) return true;
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myDependencies);
    mySdkPanel.apply();
  }

  public void applyTo(final Dependencies dependencies) {
    dependencies.COMPONENT_SET = (FlexIdeBuildConfiguration.ComponentSet)myComponentSetCombo.getSelectedItem();
    dependencies.FRAMEWORK_LINKAGE = (FlexIdeBuildConfiguration.FrameworkLinkage)myFrameworkLinkageCombo.getSelectedItem();
    dependencies.getEntries().clear();
    List<MyTableItem> items = myTable.getItems();
    for (MyTableItem item : items) {
      FlexIdeBCConfigurable configurable = ((BCItem)item).configurable;
      dependencies.getEntries()
        .add(new BuildConfigurationEntry(configurable.getModifiableRootModel().getModule(), configurable.getDisplayName()));
    }
  }

  public void reset() {
    mySdkPanel.reset();
    myComponentSetCombo.setSelectedItem(myDependencies.COMPONENT_SET);
    myFrameworkLinkageCombo.setSelectedItem(myDependencies.FRAMEWORK_LINKAGE);

    DefaultMutableTreeNode root = myTable.getRoot();
    root.removeAllChildren();
    FlexIdeBCConfigurator configurator = FlexIdeModuleStructureExtension.getInstance().getConfigurator();
    for (DependencyEntry entry : myDependencies.getEntries()) {
      final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;
      FlexIdeBCConfigurable configurable =
        ContainerUtil.find(configurator.getBCConfigurables(bcEntry.getModule()), new Condition<FlexIdeBCConfigurable>() {
          @Override
          public boolean value(FlexIdeBCConfigurable configurable) {
            return configurable.getEditableObject().NAME.equals(bcEntry.getBcName());
          }
        });
      if (configurable == null) {
        // broken project file?
        LOG.error("configurable not found for " + bcEntry.getBcName());
      }
      else {
        root.add(new DefaultMutableTreeNode(new BCItem(configurable), false));
      }
    }
    myTable.refresh();
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    mySdkPanel = new FlexSdkChooserPanel(myProject, myRootModel);
  }

  public FlexSdkChooserPanel getSdkChooserPanel() {
    return mySdkPanel;
  }

  public void addFlexSdkListener(ChangeListener listener) {
    mySdkPanel.addListener(listener);
  }

  public void removeFlexSdkListener(ChangeListener listener) {
    mySdkPanel.removeListener(listener);
  }

  private void initPopupActions() {
    if (myPopupActions == null) {
      int actionIndex = 1;
      final List<AddItemPopupAction> actions = new ArrayList<AddItemPopupAction>();
      actions.add(new AddBuildConfigurationDependencyAction(actionIndex++));
      //actions.add(new AddLibraryAction(this, actionIndex++, ProjectBundle.message("classpath.add.library.action"), context));
      //actions.add(new AddItemPopupAction<Module>(this, actionIndex, ProjectBundle.message("classpath.add.module.dependency.action"),
      //                                           StdModuleTypes.JAVA.getNodeIcon(false)) {
      //  protected ClasspathTableItem<?> createTableItem(final Module item) {
      //    return ClasspathTableItem.createItem(getRootModel().addModuleOrderEntry(item), context);
      //  }
      //
      //  protected ClasspathElementChooser<Module> createChooser() {
      //    final java.util.List<Module> chooseItems = getDependencyModules();
      //    if (chooseItems.isEmpty()) {
      //      Messages
      //        .showMessageDialog(ClasspathPanelImpl.this, ProjectBundle.message("message.no.module.dependency.candidates"), getTitle(),
      //                           Messages.getInformationIcon());
      //      return null;
      //    }
      //    return new ModuleChooser(chooseItems, ProjectBundle.message("classpath.chooser.title.add.module.dependency"),
      //                             ProjectBundle.message("classpath.chooser.description.add.module.dependency"));
      //  }
      //}
      //);

      myPopupActions = actions.toArray(new AddItemPopupAction[actions.size()]);
    }
  }

  private class AddBuildConfigurationDependencyAction extends AddItemPopupAction {
    public AddBuildConfigurationDependencyAction(int index) {
      super(index, "Build Configuration...", null);
    }

    @Override
    public void run() {
      Collection<FlexIdeBCConfigurable> dependencies = new ArrayList<FlexIdeBCConfigurable>();
      List<MyTableItem> items = myTable.getItems();
      for (MyTableItem item : items) {
        dependencies.add(((BCItem)item).configurable);
      }

      Map<Module, List<FlexIdeBCConfigurable>> treeItems = new HashMap<Module, List<FlexIdeBCConfigurable>>();
      FlexIdeBCConfigurator configurator = FlexIdeModuleStructureExtension.getInstance().getConfigurator();
      for (Module module : ModuleStructureConfigurable.getInstance(myProject).getModules()) {
        for (FlexIdeBCConfigurable configurable : configurator.getBCConfigurables(module)) {
          if (dependencies.contains(configurable) || configurable.getDependenciesConfigurable() == DependenciesConfigurable.this) {
            continue;
          }
          List<FlexIdeBCConfigurable> list = treeItems.get(module);
          if (list == null) {
            list = new ArrayList<FlexIdeBCConfigurable>();
            treeItems.put(module, list);
          }
          list.add(configurable);
        }
      }

      if (treeItems.isEmpty()) {
        Messages.showInfoMessage(myProject, "No applicable build configurations found", "Add Dependency");
        return;
      }

      ChooseBuildConfigurationDialog d = new ChooseBuildConfigurationDialog(myProject, treeItems);
      d.show();
      if (!d.isOK()) {
        return;
      }

      FlexIdeBCConfigurable[] configurables = d.getSelectedConfigurables();
      DefaultMutableTreeNode root = myTable.getRoot();
      for (FlexIdeBCConfigurable configurable : configurables) {
        root.add(new DefaultMutableTreeNode(new BCItem(configurable), false));
      }
      myTable.refresh();
      myTable.getSelectionModel().clearSelection();
      int rowCount = myTable.getRowCount();
      myTable.getSelectionModel().addSelectionInterval(rowCount - configurables.length, rowCount - 1);
    }
  }
}
