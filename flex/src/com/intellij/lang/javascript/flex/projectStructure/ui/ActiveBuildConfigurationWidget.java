package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ProjectTopics;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.HintHint;
import com.intellij.ui.LightweightHint;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
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

  private static class MyWidget extends EditorBasedWidget implements CustomStatusBarWidget, StatusBarWidget.Multiframe {

    private static final Icon ARROWS_ICON = AllIcons.Ide.Statusbar_arrows;
    private final JLabel myEnabledLabel = new JLabel();
    private final JLabel myDisabledLabel = new JLabel(FlexBundle.message("active.bc.widget.empty.text"));
    private final JPanel myPanel;
    private final JLabel myUpDownLabel = new JLabel(ARROWS_ICON);

    private MyWidget(@NotNull Project project) {
      super(project);
      Disposer.register(project, this);
      project.getMessageBus().connect(this).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
        public void rootsChanged(final ModuleRootEvent event) {
          update();
        }
      });

      myEnabledLabel.setFont(SystemInfo.isMac ? UIUtil.getLabelFont().deriveFont(11.0f) : UIUtil.getLabelFont());
      myEnabledLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      myDisabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
      myDisabledLabel.setToolTipText(FlexBundle.message("active.bc.widget.empty.tooltip"));
      myDisabledLabel.setForeground(UIUtil.getInactiveTextColor());
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
      myPanel.setBorder(WidgetBorder.INSTANCE);
      GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
      myPanel.add(myEnabledLabel, c);
      c.gridx++;
      c.anchor = GridBagConstraints.CENTER;
      myPanel.add(myDisabledLabel, c);
      c.gridx++;
      c.weightx = 0;
      myPanel.add(myUpDownLabel, c);

      MouseAdapter listener = new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
          Module module = findCurrentFlexModule();
          if (module != null) {
            ListPopup popup = ChooseActiveBuildConfigurationAction.createPopup(module);
            final Dimension dimension = popup.getContent().getPreferredSize();
            final Point at = new Point(0, -dimension.height);
            popup.show(new RelativePoint(e.getComponent(), at));
          }
          else {
            HintHint hintHint = new HintHint(e).setShowImmediately(true).setAwtTooltip(true);
            new LightweightHint(new JLabel(myDisabledLabel.getToolTipText())).show(myPanel, e.getX(), e.getY(), myPanel, hintHint);
          }
        }
      };
      myEnabledLabel.addMouseListener(listener);
      myDisabledLabel.addMouseListener(listener);
      myPanel.addMouseListener(listener);
      myUpDownLabel.addMouseListener(listener);
      update();
    }

    @Override
    public JComponent getComponent() {
      return myPanel;
    }

    @Override
    @Nullable
    public WidgetPresentation getPresentation(@NotNull final PlatformType type) {
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

      FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
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

    public StatusBarWidget copy() {
      return new MyWidget(getProject());
    }

    @NotNull
    public String ID() {
      return "ActiveFlexBuildConfiguration";
    }

    @Override
    public void selectionChanged(FileEditorManagerEvent event) {
      update();
    }

    @Nullable
    private Module findCurrentFlexModule() {
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

    public static String getAnchor() {
      return "after " + (SystemInfo.isMac ? "Encoding" : "InsertOverwrite");
    }
  }
}
