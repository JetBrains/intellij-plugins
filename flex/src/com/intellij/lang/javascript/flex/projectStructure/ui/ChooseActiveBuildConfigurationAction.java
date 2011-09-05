package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * User: ksafonov
 */
public class ChooseActiveBuildConfigurationAction extends ComboBoxAction implements DumbAware {

  private Module myLastModule;

  @Nullable
  private static Module findModule(DataContext dataContext) {
    Project project = PlatformDataKeys.PROJECT.getData(dataContext);
    if (project == null) {
      return null;
    }
    Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    if (editor == null) {
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
    if (!FlexIdeUtils.isNewUI()) {
      e.getPresentation().setVisible(false);
      return;
    }

    super.update(e);
    Module module = findModule(e.getDataContext());
    if (module == null || FlexModuleType.getInstance() != ModuleType.get(module)) {
      e.getPresentation().setEnabled(false);
      e.getPresentation().setText("(none)");
      e.getPresentation().setIcon(null);
      return;
    }

    myLastModule = module;
    e.getPresentation().setEnabled(true);
    FlexIdeBuildConfiguration activeConfiguration = FlexIdeBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    if (activeConfiguration != null) {
      e.getPresentation().setText(activeConfiguration.NAME);
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
    FlexIdeBuildConfigurationManager bcManager = FlexIdeBuildConfigurationManager.getInstance(myLastModule);
    FlexIdeBuildConfiguration[] buildConfigurations = bcManager.getBuildConfigurations();
    for (FlexIdeBuildConfiguration c : buildConfigurations) {
      if (c == bcManager.getActiveConfiguration()) {
        continue;
      }
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
      e.getPresentation().setText(myBuildConfiguration.NAME);
      e.getPresentation().setIcon(myBuildConfiguration.getIcon());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          FlexIdeBuildConfigurationManager.getInstance(myModule).setActiveBuildConfiguration(myBuildConfiguration);
        }
      });
    }
  }
}
