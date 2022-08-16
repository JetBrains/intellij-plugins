// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.IconManager;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.icons.RowIcon;
import com.intellij.ui.navigation.Place;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.PopupListElementRenderer;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public final class ChooseActiveBuildConfigurationAction extends DumbAwareAction {
  private static final Icon ICON_ACTIVE = PlatformIcons.CHECK_ICON;
  private static final Icon ICON_ACTIVE_SELECTED = PlatformIcons.CHECK_ICON_SELECTED;
  private static final Icon ICON_EMPTY = IconManager.getInstance().createEmptyIcon(ICON_ACTIVE);

  @Override
  public void update(@NotNull final AnActionEvent e) {
    boolean enabled = isEnabled(e.getDataContext());
    if (ActionPlaces.isPopupPlace(e.getPlace())) {
      e.getPresentation().setEnabledAndVisible(enabled);
    }
    else {
      //e.getPresentation().setDescription(FlexBundle.message());
      e.getPresentation().setVisible(true);
      e.getPresentation().setEnabled(enabled);
    }
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private static boolean isEnabled(final DataContext dataContext) {
    Module module = PlatformCoreDataKeys.MODULE.getData(dataContext);
    return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    Module module = e.getData(PlatformCoreDataKeys.MODULE);
    if (module != null) {
      createPopup(module, e.getDataContext()).showInBestPositionFor(e.getDataContext());
    }
  }

  public static ListPopup createPopup(@NotNull Module module, @NotNull DataContext dataContext) {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);
    final FlexBuildConfiguration activeBc = manager.getActiveConfiguration();
    final FlexBuildConfiguration[] bcs = manager.getBuildConfigurations();
    Arrays.sort(bcs, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    for (final FlexBuildConfiguration bc : bcs) {
      actionGroup.add(new SelectBcAction(bc, manager));
    }
    actionGroup.addSeparator();
    actionGroup.add(new EditBcsAction(module));

    return new PopupFactoryImpl.ActionGroupPopup(FlexBundle.message("choose.build.configuration.popup.title", module.getName()),
                                                 actionGroup, dataContext, false, false, false, true, null, -1,
                                                 anAction -> anAction instanceof SelectBcAction && ((SelectBcAction)anAction).getBC() == activeBc, null) {
      @Override
      protected ListCellRenderer getListElementRenderer() {
        return new PopupListElementRenderer(this) {

          @Override
          protected JComponent createItemComponent() {
            return new MyPanel();
          }

          @Override
          public Component getListCellRendererComponent(final JList list,
                                                        final Object value,
                                                        final int index,
                                                        final boolean isSelected,
                                                        final boolean cellHasFocus) {
            MyPanel p = (MyPanel)myComponent;
            p.clear();

            PopupFactoryImpl.ActionItem actionItem = (PopupFactoryImpl.ActionItem)value;
            AnAction anAction = actionItem.getAction();
            SimpleColoredText text;
            Icon icon;
            boolean isActive;
            if (anAction instanceof SelectBcAction) {
              FlexBuildConfiguration bc = ((SelectBcAction)anAction).getBC();
              isActive = bc == activeBc;
              text = BCUtils.renderBuildConfiguration(bc, null, isActive);
              icon = bc.getIcon();
            }
            else {
              text = new SimpleColoredText(anAction.getTemplatePresentation().getText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
              icon = anAction.getTemplatePresentation().getIcon();
              isActive = false;
            }

            RowIcon rowIcon = IconManager.getInstance().createRowIcon(2);
            rowIcon.setIcon(isActive ? (isSelected ? ICON_ACTIVE_SELECTED : ICON_ACTIVE) : ICON_EMPTY, 0);
            rowIcon.setIcon(icon, 1);
            p.setIcon(rowIcon);

            if (isSelected) {
              text = text.derive(SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES, true);
              setSelected(p);
            }
            else {
              setDeselected(p);
            }

            p.setText(text);
            mySeparatorComponent.setVisible(actionItem.isPrependWithSeparator());
            return myRendererComponent;
          }
        };
      }
    };
  }

  private static class SelectBcAction extends DumbAwareAction {
    private final FlexBuildConfiguration myBc;

    private final FlexBuildConfigurationManager myManager;

    SelectBcAction(final FlexBuildConfiguration bc, final FlexBuildConfigurationManager manager) {
      super(bc.getName(), getDescription(bc), bc.getIcon());
      myBc = bc;
      myManager = manager;
    }

    private static String getDescription(final FlexBuildConfiguration bc) {
      return bc.getNature().getPresentableText();
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      myManager.setActiveBuildConfiguration(myBc);
    }

    public FlexBuildConfiguration getBC() {
      return myBc;
    }
  }


  private static class EditBcsAction extends DumbAwareAction {
    private final Module myModule;

    EditBcsAction(Module module) {
      myModule = module;
      final AnAction a = ActionManager.getInstance().getAction("ShowProjectStructureSettings");
      getTemplatePresentation().copyFrom(a.getTemplatePresentation());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      final FlexBuildConfiguration activeConfiguration = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();
      final ProjectStructureConfigurable c = ProjectStructureConfigurable.getInstance(myModule.getProject());
      ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), c, () -> {
        Place p = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(myModule, activeConfiguration.getName());
        c.navigateTo(p, true);
      });
    }
  }

  private static class MyPanel extends JPanel {
    private final SimpleColoredComponent myComponent;

    MyPanel() {
      super(new BorderLayout());
      setBorder(new EmptyBorder(2, 0, 2, 0));
      setOpaque(true);

      myComponent = new SimpleColoredComponent();
      myComponent.setIconOpaque(false);
      myComponent.setOpaque(false);
      add(myComponent, BorderLayout.CENTER);
    }

    public void setText(final SimpleColoredText text) {
      text.appendToComponent(myComponent);
    }

    public void setIcon(final Icon icon) {
      myComponent.setIcon(icon);
    }

    public void clear() {
      myComponent.clear();
    }
  }
}