// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.config;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.commands.ClearHistoryCommand;
import jetbrains.communicator.core.IDEtalkOptions;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.users.SettingsChanged;
import jetbrains.communicator.idea.actions.BaseAction;
import jetbrains.communicator.idea.monitor.UserActivityMonitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kir
 */
public class IDEtalkConfiguration implements Configurable {
  private JPanel myPanel;
  private JCheckBox myHideOffline;
  private JCheckBox myExpand;
  private JCheckBox myActivateWindow;
  private JCheckBox myHideMyFiles;
  private JCheckBox myUserEnterKeyToCheckBox;
  private JCheckBox myShowPopup;
  private JButton myClearHistory;
  private JSpinner myTimeoutAway;
  private JSpinner myTimeoutXA;
  private JCheckBox myPlaySoundCheckBox;

  private final Set<Pair<JCheckBox, OptionFlag>> myFlags = new HashSet<>();
  private final Project myProject;
  private final IDEtalkOptions myOptions;

  public IDEtalkConfiguration(final Project project) {

    myProject = project;

    myFlags.add(Pair.create(myHideOffline, OptionFlag.OPTION_HIDE_OFFLINE_USERS));
    myFlags.add(Pair.create(myPlaySoundCheckBox, IdeaFlags.SOUND_ON_MESSAGE));
    myFlags.add(Pair.create(myExpand, IdeaFlags.EXPAND_ON_MESSAGE));
    myFlags.add(Pair.create(myActivateWindow, IdeaFlags.ACTIVATE_WINDOW_ON_MESSAGE));
    myFlags.add(Pair.create(myHideMyFiles, OptionFlag.HIDE_ALL_KEY));
    myFlags.add(Pair.create(myUserEnterKeyToCheckBox, IdeaFlags.USE_ENTER_FOR_MESSAGES));
    myFlags.add(Pair.create(myShowPopup, IdeaFlags.POPUP_ON_MESSAGE));

    myOptions = Pico.getOptions();

    final UserCommand command = Pico.getCommandManager().getCommand(
        ClearHistoryCommand.class, BaseAction.getContainer(project));

    myClearHistory.setEnabled(command.isEnabled());
    myClearHistory.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        command.execute();
        myClearHistory.setEnabled(command.isEnabled());
      }
    });
  }

  private void setTimeoutModel(JSpinner spinner, String option, int defaultValue) {
    double value = myOptions.getNumber(option, defaultValue);
    spinner.setModel(new SpinnerNumberModel(value, 0.0, 9999, 1.0));
  }

  @Override
  public void apply() throws ConfigurationException {
    for (Pair<JCheckBox, OptionFlag> option : myFlags) {
      option.getSecond().change(option.getFirst().isSelected());
    }
    update(myTimeoutAway, IDEtalkOptions.TIMEOUT_AWAY_MIN);
    update(myTimeoutXA, IDEtalkOptions.TIMEOUT_XA_MIN);

    Pico.getEventBroadcaster().fireEvent(new SettingsChanged());
  }

  private void update(JSpinner spinner, String option) {
    Number value = (Number) spinner.getValue();
    myOptions.setNumber(option, value.doubleValue());
  }

  @Override
  public JComponent createComponent() {
    return myPanel;
  }


  @Override
  public boolean isModified() {
    for (Pair<JCheckBox, OptionFlag> option : myFlags) {
      if (option.getSecond().isSet() ^ option.getFirst().isSelected()) {
        return true;
      }
    }

    return timoutModified(myTimeoutAway, IDEtalkOptions.TIMEOUT_AWAY_MIN, UserActivityMonitor.AWAY_MINS) ||
        timoutModified(myTimeoutXA, IDEtalkOptions.TIMEOUT_XA_MIN, UserActivityMonitor.EXTENDED_AWAY_MINS);
  }

  private boolean timoutModified(JSpinner spinner, String optionName, int defaultValue) {
    Number value = (Number) spinner.getValue();
    return Math.abs(value.doubleValue() - myOptions.getNumber(optionName, defaultValue)) > 0.01;
  }


  @Override
  public void reset() {
    for (Pair<JCheckBox, OptionFlag> option : myFlags) {
      option.getFirst().setSelected(option.getSecond().isSet());
    }

    setTimeoutModel(myTimeoutAway, IDEtalkOptions.TIMEOUT_AWAY_MIN, UserActivityMonitor.AWAY_MINS);
    setTimeoutModel(myTimeoutXA, IDEtalkOptions.TIMEOUT_XA_MIN, UserActivityMonitor.EXTENDED_AWAY_MINS);
  }

  @Override
  public String getDisplayName() {
    return IdeBundle.message("configurable.IDEtalkConfiguration.display.name");
  }


  @Override
  @Nullable
  @NonNls
  public String getHelpTopic() {
    return "reference.dialogs.idetalk";
  }

  public void edit() {
    ShowSettingsUtil.getInstance().editConfigurable(myProject, this);
  }
}
