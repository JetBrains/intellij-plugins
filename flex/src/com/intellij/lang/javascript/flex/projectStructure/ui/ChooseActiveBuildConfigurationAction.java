package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.*;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
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
public class ChooseActiveBuildConfigurationAction extends DumbAwareAction {

  public void update(final AnActionEvent e) {
    boolean enabled = isEnabled(e.getDataContext());
    if (ActionPlaces.isPopupPlace(e.getPlace())) {
      e.getPresentation().setVisible(enabled);
      e.getPresentation().setEnabled(enabled);
    }
    else {
      //e.getPresentation().setDescription(FlexBundle.message());
      e.getPresentation().setVisible(true);
      e.getPresentation().setEnabled(enabled);
    }
  }

  private static boolean isEnabled(final DataContext dataContext) {
    Module module = LangDataKeys.MODULE.getData(dataContext);
    return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
  }

  public void actionPerformed(final AnActionEvent e) {
    Module module = LangDataKeys.MODULE.getData(e.getDataContext());
    if (module != null) {
      createPopup(module).showInBestPositionFor(e.getDataContext());
    }
  }

  public static ListPopup createPopup(@NotNull Module module) {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.addSeparator("Build Configurations");
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);
    final FlexIdeBuildConfiguration active = manager.getActiveConfiguration();
    FlexIdeBuildConfiguration[] bcs = manager.getBuildConfigurations();
    for (final FlexIdeBuildConfiguration bc : bcs) {
      actionGroup.add(new SelectBcAction(bc, manager));
    }
    actionGroup.addSeparator();
    actionGroup.add(new EditBcsAction(module));

    final DataContext dataContext = SimpleDataContext.getProjectContext(module.getProject());
    return JBPopupFactory.getInstance()
      .createActionGroupPopup(FlexBundle.message("choose.build.configuration.popup.title", module.getName()), actionGroup, dataContext,
                              false, false, true, null, -1, new Condition<AnAction>() {
        public boolean value(final AnAction anAction) {
          return anAction instanceof SelectBcAction && ((SelectBcAction)anAction).getBc() == active;
        }
      });
  }

  private static class SelectBcAction extends DumbAwareAction {
    private final FlexIdeBuildConfiguration myBc;

    private final FlexBuildConfigurationManager myManager;

    public SelectBcAction(final FlexIdeBuildConfiguration bc, final FlexBuildConfigurationManager manager) {
      super(bc.getName(), getDescription(bc), bc.getIcon());
      myBc = bc;
      myManager = manager;
    }

    private static String getDescription(final FlexIdeBuildConfiguration bc) {
      return FlexBundle.message("bc.description", bc.getTargetPlatform().getPresentableText(),
                                StringUtil.decapitalize(bc.getOutputType().getPresentableText()), bc.getName(), bc.isPureAs() ? 1 : 2);
    }

    public void actionPerformed(final AnActionEvent e) {
      myManager.setActiveBuildConfiguration(myBc);
    }

    public FlexIdeBuildConfiguration getBc() {
      return myBc;
    }
  }


  private static class EditBcsAction extends DumbAwareAction {
    private final Module myModule;

    public EditBcsAction(Module module) {
      super("Configure project", "Edit Flex build configurations", IconLoader.getIcon("/actions/editSource.png"));
      myModule = module;
      final AnAction a = ActionManager.getInstance().getAction("ShowProjectStructureSettings");
      getTemplatePresentation().setText(a.getTemplatePresentation().getText());
      getTemplatePresentation().setIcon(a.getTemplatePresentation().getIcon());
      getTemplatePresentation().setDescription(a.getTemplatePresentation().getDescription());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      final FlexIdeBuildConfiguration activeConfiguration = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();
      final ProjectStructureConfigurable c = ProjectStructureConfigurable.getInstance(myModule.getProject());
      ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), c, new Runnable() {
        @Override
        public void run() {
          Place p = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(myModule, activeConfiguration);
          c.navigateTo(p, true);
        }
      });
    }
  }
}
