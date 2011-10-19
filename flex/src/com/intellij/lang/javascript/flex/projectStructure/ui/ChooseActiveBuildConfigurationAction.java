package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeModuleStructureExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.*;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.ui.navigation.Place;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * User: ksafonov
 */
public class ChooseActiveBuildConfigurationAction extends ComboBoxAction implements DumbAware {

  private final Map<Project, Module> myLastModules = new HashMap<Project, Module>();

  public ChooseActiveBuildConfigurationAction() {
    Application application = ApplicationManager.getApplication();
    application.getMessageBus().connect(application).subscribe(ProjectManager.TOPIC, new ProjectManagerAdapter() {
      @Override
      public void projectClosed(Project project) {
        myLastModules.remove(project);
      }
    });
  }

  @Override
  public final JComponent createCustomComponent(Presentation presentation) {
    JPanel p = new JPanel(new GridBagLayout());
    final JLabel label = new JLabel("Build configuration:");
    //action description in plugin.xml provides tooltip for combobox, let's set the same tooltip for label
    label.setToolTipText("The selected configuration is used for source files highlighting");
    p.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 5, 0, 0), 0, 0));
    p.add(super.createCustomComponent(presentation),
          new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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
    Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) {
      disable(e);
      return;
    }

    Module module = findModule(e.getDataContext());
    if (module == null) {
      if (myLastModules.get(project) != null && myLastModules.get(project).isDisposed()) {
        myLastModules.remove(project);
      }
      module = myLastModules.get(project);
    }
    if (module == null || FlexModuleType.getInstance() != ModuleType.get(module)) {
      disable(e);
      return;
    }

    myLastModules.put(project, module);
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

  private static void disable(AnActionEvent e) {
    e.getPresentation().setEnabled(false);
    e.getPresentation().setText("(none)");
    e.getPresentation().setIcon(null);
  }

  @NotNull
  @Override
  protected DefaultActionGroup createPopupActionGroup(JComponent button) {
    DefaultActionGroup group = new DefaultActionGroup();
    Project project = findProject(button);
    if (project == null) {
      return group;
    }
    Module module = myLastModules.get(project);
    if (module == null) {
      return group;
    }

    FlexBuildConfigurationManager bcManager = FlexBuildConfigurationManager.getInstance(module);
    FlexIdeBuildConfiguration[] buildConfigurations = bcManager.getBuildConfigurations();
    group.add(new EditBcsAction(module));
    group.addSeparator();
    for (FlexIdeBuildConfiguration c : buildConfigurations) {
      group.add(new BCAction(module, c));
    }
    return group;
  }

  @Nullable
  private static Project findProject(JComponent button) {
    Component parent = UIUtil.findUltimateParent(button);
    return parent instanceof IdeFrame ? ((IdeFrame)parent).getProject() : null;
  }

  @Override
  protected ComboBoxButton createComboBoxButton(Presentation presentation) {
    return new ComboBoxButton(presentation) {
      @Override
      protected ListPopup createPopup(Runnable onDispose) {
        final DataContext popupCreationDataContext = DataManager.getInstance().getDataContext(this);

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
    private final Module myModule;

    public EditBcsAction(Module module) {
      super("Configure project", "Edit Flex build configurations", IconLoader.getIcon("/actions/editSource.png"));
      myModule = module;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      final FlexIdeBuildConfiguration activeConfiguration = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();
      final ProjectStructureConfigurable c = ProjectStructureConfigurable.getInstance(myModule.getProject());
      ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), c, new Runnable() {
        @Override
        public void run() {
          Place p = FlexIdeModuleStructureExtension.getInstance().getConfigurator().getPlaceFor(myModule, activeConfiguration);
          c.navigateTo(p, true);
        }
      });
    }
  }
}
