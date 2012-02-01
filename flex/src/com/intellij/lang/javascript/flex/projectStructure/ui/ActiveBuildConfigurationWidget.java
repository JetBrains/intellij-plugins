package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ProjectTopics;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

/**
 * User: ksafonov
 */
public class ActiveBuildConfigurationWidget {

  private final Project myProject;

  @Nullable
  private MyWidget myWidget;

  public ActiveBuildConfigurationWidget(final Project project) {
    myProject = project;

    myProject.getMessageBus().connect(myProject).subscribe(ProjectTopics.MODULES, new ModuleAdapter() {
      @Override
      public void moduleAdded(final Project project, final Module module) {
        showOrHideWidget(false);
      }

      @Override
      public void moduleRemoved(final Project project, final Module module) {
        showOrHideWidget(false);
      }
    });

    showOrHideWidget(false);
  }

  public void destroy() {
    showOrHideWidget(true);
  }

  private void showOrHideWidget(boolean forceRemove) {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
    if (statusBar == null) {
      return;
    }

    boolean showWidget = !forceRemove && shouldShowWidget();
    if (showWidget) {
      if (myWidget == null) {
        myWidget = new MyWidget(myProject);
        statusBar.addWidget(myWidget, MyWidget.getAnchor());
      }
    }
    else {
      if (myWidget != null) {
        statusBar.removeWidget(myWidget.ID());
        myWidget = null;
      }
    }
  }

  private boolean shouldShowWidget() {
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        return true;
      }
    }
    return false;
  }

  private static class MyWidget extends EditorBasedWidget
    implements StatusBarWidget.MultipleTextValuesPresentation, StatusBarWidget.Multiframe {

    private MyWidget(@NotNull Project project) {
      super(project);
      Disposer.register(project, this);
      project.getMessageBus().connect(this).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
        public void beforeRootsChange(final ModuleRootEvent event) {
        }

        public void rootsChanged(final ModuleRootEvent event) {
          if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
          }
        }
      });
    }

    public StatusBarWidget copy() {
      return new MyWidget(getProject());
    }

    @NotNull
    public String ID() {
      return "ActiveFlexBuildConfiguration";
    }

    public WidgetPresentation getPresentation(@NotNull final PlatformType type) {
      return this;
    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
      if (myStatusBar != null) {
        myStatusBar.updateWidget(ID());
      }
    }

    @Nullable
    private Module getModule() {
      final VirtualFile selectedFile = getSelectedFile();
      if (selectedFile == null) {
        return null;
      }

      final Module module = ModuleUtil.findModuleForFile(selectedFile, getProject());
      if (module == null || FlexModuleType.getInstance() != ModuleType.get(module)) {
        return null;
      }
      return module;
    }

    public ListPopup getPopupStep() {
      Module module = getModule();
      return module != null ? ChooseActiveBuildConfigurationAction.createPopup(module) : null;
    }

    public String getSelectedValue() {
      final Module module = getModule();
      if (module == null) {
        return null;
      }

      FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
      if (bc == null) {
        return "(none)";
      }
      return bc.getName();
    }

    @NotNull
    public String getMaxValue() {
      return StringUtil.repeat("a", 15);
    }

    public String getTooltipText() {
      final Module module = getModule();
      if (module == null) {
        return null;
      }

      FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
      if (bc != null) {
        return FlexBundle.message("choose.build.configuration.action.tooltip", bc.getName(), module.getName());
      }
      else {
        return FlexBundle.message("choose.build.configuration.action.tooltip.none", module.getName());
      }
    }

    public Consumer<MouseEvent> getClickConsumer() {
      return null;
    }

    public static String getAnchor() {
      return "after " + (SystemInfo.isMac ? "Encoding" : "InsertOverwrite");
    }
  }
}
