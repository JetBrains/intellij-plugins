// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ProjectTopics;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.ClickListener;
import com.intellij.ui.HintHint;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.NamedColorUtil;
import com.intellij.util.ui.StartupUiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public final class ActiveBuildConfigurationWidget {
  private final Project myProject;

  @Nullable
  private MyWidget myWidget;

  public ActiveBuildConfigurationWidget(final Project project) {
    myProject = project;

    myProject.getMessageBus().connect(myProject).subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void modulesAdded(@NotNull Project project, @NotNull List<Module> modules) {
        showOrHideWidget(false);
      }

      @Override
      public void moduleRemoved(@NotNull final Project project, @NotNull final Module module) {
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

  private static final class MyWidget extends EditorBasedWidget implements CustomStatusBarWidget, StatusBarWidget.Multiframe {

    private final JLabel myEnabledLabel = new JLabel();
    private final JLabel myDisabledLabel = new JLabel(FlexBundle.message("active.bc.widget.empty.text"));
    private final JPanel myPanel;
    private final JLabel myUpDownLabel = new JLabel(AllIcons.Ide.Statusbar_arrows);

    private MyWidget(@NotNull Project project) {
      super(project);
      Disposer.register(project, this);
      project.getMessageBus().connect(this).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
        @Override
        public void rootsChanged(@NotNull final ModuleRootEvent event) {
          update();
        }
      });

      myEnabledLabel.setFont(SystemInfo.isMac ? StartupUiUtil.getLabelFont().deriveFont(11.0f) : StartupUiUtil.getLabelFont());
      myEnabledLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      myDisabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
      myDisabledLabel.setToolTipText(FlexBundle.message("active.bc.widget.empty.tooltip"));
      myDisabledLabel.setForeground(NamedColorUtil.getInactiveTextColor());
      myDisabledLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      myPanel = new JPanel(new GridBagLayout()) {
        @Override
        public Dimension getPreferredSize() {
          int max = getFontMetrics(getFont()).stringWidth(myDisabledLabel.getText());
          return new Dimension(20 + max, getMinimumSize().height);
        }
      };

      myPanel.setOpaque(false);
      myPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      myPanel.setBorder(JBUI.CurrentTheme.StatusBar.Widget.border());
      GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, JBInsets.emptyInsets(), 0, 0);
      myPanel.add(myEnabledLabel, c);
      c.gridx++;
      c.anchor = GridBagConstraints.CENTER;
      myPanel.add(myDisabledLabel, c);
      c.gridx++;
      c.weightx = 0;
      myPanel.add(myUpDownLabel, c);

      ClickListener listener = new ClickListener() {
        @Override
        public boolean onClick(@NotNull MouseEvent e, int clickCount) {
          Module module = findCurrentFlexModule();
          if (module != null) {
            DataContext dataContext = DataManager.getInstance().getDataContext(e.getComponent());
            ListPopup popup = ChooseActiveBuildConfigurationAction.createPopup(module, dataContext);
            final Dimension dimension = popup.getContent().getPreferredSize();
            final Point at = new Point(0, -dimension.height);
            popup.show(new RelativePoint(e.getComponent(), at));
          }
          else {
            HintHint hintHint = new HintHint(e).setShowImmediately(true).setAwtTooltip(true);
            new LightweightHint(new JLabel(myDisabledLabel.getToolTipText())).show(myPanel, e.getX(), e.getY(), myPanel, hintHint);
          }
          return true;
        }
      };

      listener.installOn(myEnabledLabel);
      listener.installOn(myDisabledLabel);
      listener.installOn(myPanel);
      listener.installOn(myUpDownLabel);

      update();
    }

    @Override
    public JComponent getComponent() {
      return myPanel;
    }

    @Override
    @Nullable
    public WidgetPresentation getPresentation() {
      return null;
    }

    private void update() {
      if (myStatusBar == null) {
        showDisabled();
        return;
      }

      final Module module = findCurrentFlexModule();
      if (module == null) {
        showDisabled();
        return;
      }

      FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
      myEnabledLabel.setText(BCUtils.renderBuildConfiguration(bc, null, false).toString());
      myEnabledLabel.setToolTipText(FlexBundle.message("active.bc.widget.tooltip", bc.getName(), module.getName()));
      myPanel.setToolTipText(myEnabledLabel.getToolTipText());
      myEnabledLabel.setIcon(bc.getIcon());
      myEnabledLabel.setVisible(true);
      myDisabledLabel.setVisible(false);
      myUpDownLabel.setVisible(true);
      myStatusBar.updateWidget(ID());
    }

    private void showDisabled() {
      myEnabledLabel.setVisible(false);
      myUpDownLabel.setVisible(false);
      myDisabledLabel.setVisible(true);
      myPanel.setToolTipText(myDisabledLabel.getToolTipText());
    }

    @Override
    public StatusBarWidget copy() {
      return new MyWidget(getProject());
    }

    @Override
    @NotNull
    public String ID() {
      return "ActiveFlexBuildConfiguration";
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
      update();
    }

    @Nullable
    private Module findCurrentFlexModule() {
      final VirtualFile selectedFile = getSelectedFile();
      if (selectedFile == null) {
        return null;
      }

      final Module module = ModuleUtilCore.findModuleForFile(selectedFile, getProject());
      if (module == null || FlexModuleType.getInstance() != ModuleType.get(module)) {
        return null;
      }
      return module;
    }

    public static String getAnchor() {
      return "after " + (SystemInfo.isMac ? "Encoding" : "InsertOverwrite");
    }
  }
}
