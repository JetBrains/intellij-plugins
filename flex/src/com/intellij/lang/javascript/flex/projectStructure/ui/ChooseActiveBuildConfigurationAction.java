package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.newEditor.OptionsEditorDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * User: ksafonov
 */
public class ChooseActiveBuildConfigurationAction extends ComboBoxAction implements DumbAware {

  private Module myLastModule;

  @Override
  public final JComponent createCustomComponent(Presentation presentation) {
    JPanel p = new JPanel(new GridBagLayout());
    p.add(new JLabel("Flex build configuration:"),
          new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                                 new Insets(0, 5, 0, 0), 0, 0));
    p.add(super.createCustomComponent(presentation),
          new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                                 new Insets(0, 0, 0, 0), 0, 0));
    return p;
  }

  @Nullable
  private static Module findModule(DataContext dataContext) {
    Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return null;
    }
    VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (file == null) {
      return null;
    }
    return ModuleUtil.findModuleForFile(file, project);
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    Module module = findModule(e.getDataContext());
    if (module == null) {
      if (myLastModule != null && myLastModule.isDisposed()) {
        myLastModule = null;
      }
      module = myLastModule;
    }
    if (module == null || FlexModuleType.getInstance() != ModuleType.get(module)) {
      e.getPresentation().setEnabled(false);
      e.getPresentation().setText("(none)");
      e.getPresentation().setIcon(null);
      return;
    }

    myLastModule = module;
    e.getPresentation().setEnabled(true);
    FlexIdeBuildConfiguration activeConfiguration = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    if (activeConfiguration != null) {
      e.getPresentation().setText(activeConfiguration.getName());
      e.getPresentation().setIcon(activeConfiguration.getIcon());
    }
    else {
      e.getPresentation().setText("(none)");
    }
  }

  @NotNull
  @Override
  protected DefaultActionGroup createPopupActionGroup(JComponent button) {
    DefaultActionGroup group = new DefaultActionGroup();
    FlexBuildConfigurationManager bcManager = FlexBuildConfigurationManager.getInstance(myLastModule);
    FlexIdeBuildConfiguration[] buildConfigurations = bcManager.getBuildConfigurations();
    group.add(new EditBcsAction());
    group.addSeparator();
    for (FlexIdeBuildConfiguration c : buildConfigurations) {
      group.add(new BCAction(myLastModule, c));
    }
    return group;
  }

  @Override
  protected ComboBoxButton createComboBoxButton(Presentation presentation) {
    return new ComboBoxButton(presentation) {
      @Override
      protected ListPopup createPopup(Runnable onDispose) {
        final DataContext popupCreationDataContext = getDataContext();

        ListPopup popup = super.createPopup(onDispose);
        popup.setDataProvider(new DataProvider() {
          @Override
          public Object getData(@NonNls String dataId) {
            return popupCreationDataContext.getData(dataId);
          }
        });
        return popup;
      }
    };
  }

  private static class BCAction extends DumbAwareAction {
    private final Module myModule;
    private final FlexIdeBuildConfiguration myBuildConfiguration;

    public BCAction(Module module, FlexIdeBuildConfiguration buildConfiguration) {
      myModule = module;
      myBuildConfiguration = buildConfiguration;
    }

    @Override
    public void update(AnActionEvent e) {
      e.getPresentation().setText(myBuildConfiguration.getName());
      e.getPresentation().setIcon(myBuildConfiguration.getIcon());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      FlexBuildConfigurationManager.getInstance(myModule).setActiveBuildConfiguration(myBuildConfiguration);
    }
  }

  private static class EditBcsAction extends DumbAwareAction {

    public EditBcsAction() {
      super("Configure project", "Edit Flex build configurations", IconLoader.getIcon("/actions/editSource.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      Project project = getEventProject(e);
      ShowSettingsUtil.getInstance()
        .editConfigurable(project, OptionsEditorDialog.DIMENSION_KEY, ProjectStructureConfigurable.getInstance(project));
    }
  }
}
