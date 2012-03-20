package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.RowIcon;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.navigation.Place;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.PopupListElementRenderer;
import com.intellij.util.ui.EmptyIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * User: ksafonov
 */
public class ChooseActiveBuildConfigurationAction extends DumbAwareAction {

  private static final Icon ICON_ACTIVE = IconLoader.getIcon("/actions/checked_16x16.png");
  private static final Icon ICON_ACTIVE_SELECTED = IconLoader.getIcon("/actions/checked_16x16_selected.png");
  private static final Icon ICON_EMPTY = new EmptyIcon(ICON_ACTIVE.getIconWidth(), ICON_ACTIVE.getIconHeight());

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
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(module);
    final FlexIdeBuildConfiguration activeBc = manager.getActiveConfiguration();
    FlexIdeBuildConfiguration[] bcs = manager.getBuildConfigurations();
    Arrays.sort(bcs, new Comparator<FlexIdeBuildConfiguration>() {
      @Override
      public int compare(final FlexIdeBuildConfiguration o1, final FlexIdeBuildConfiguration o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
    for (final FlexIdeBuildConfiguration bc : bcs) {
      actionGroup.add(new SelectBcAction(bc, manager));
    }
    actionGroup.addSeparator();
    actionGroup.add(new EditBcsAction(module));

    final DataContext dataContext = SimpleDataContext.getProjectContext(module.getProject());
    return new PopupFactoryImpl.ActionGroupPopup(FlexBundle.message("choose.build.configuration.popup.title", module.getName()),
                                                 actionGroup, dataContext, false, false, false, true, null, -1, new Condition<AnAction>() {
      @Override
      public boolean value(final AnAction anAction) {
        return anAction instanceof SelectBcAction && ((SelectBcAction)anAction).getBc() == activeBc;
      }
    }, null) {
      @Override
      protected ListCellRenderer getListElementRenderer() {
        return new PopupListElementRenderer(this) {
          {
            //myRendererComponent.setBorder(new EmptyBorder(5, 0, 5, 0));
          }
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
              FlexIdeBuildConfiguration bc = ((SelectBcAction)anAction).getBc();
              isActive = bc == activeBc;
              text = BCUtils.renderBuildConfiguration(bc, null, isActive);
              icon = bc.getIcon();
            }
            else {
              text = new SimpleColoredText(anAction.getTemplatePresentation().getText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
              icon = anAction.getTemplatePresentation().getIcon();
              isActive = false;
            }

            RowIcon rowIcon = new RowIcon(2);
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
      super(null);
      myModule = module;
      final AnAction a = ActionManager.getInstance().getAction("ShowProjectStructureSettings");
      getTemplatePresentation().copyFrom(a.getTemplatePresentation());
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

  private static class MyPanel extends JPanel {
    private final SimpleColoredComponent myComponent;

    public MyPanel() {
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
