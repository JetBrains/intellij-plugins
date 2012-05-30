/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.idea.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.HashSet;
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

  private final Set<Pair<JCheckBox, OptionFlag>> myFlags = new HashSet<Pair<JCheckBox, OptionFlag>>();
  private final Project myProject;
  private final IDEtalkOptions myOptions;

  public IDEtalkConfiguration(final Project project) {

    myProject = project;

    myFlags.add(new Pair<JCheckBox, OptionFlag>(myHideOffline, OptionFlag.OPTION_HIDE_OFFLINE_USERS));
    myFlags.add(new Pair<JCheckBox, OptionFlag>(myPlaySoundCheckBox, IdeaFlags.SOUND_ON_MESSAGE));
    myFlags.add(new Pair<JCheckBox, OptionFlag>(myExpand, IdeaFlags.EXPAND_ON_MESSAGE));
    myFlags.add(new Pair<JCheckBox, OptionFlag>(myActivateWindow, IdeaFlags.ACTIVATE_WINDOW_ON_MESSAGE));
    myFlags.add(new Pair<JCheckBox, OptionFlag>(myHideMyFiles, OptionFlag.HIDE_ALL_KEY));
    myFlags.add(new Pair<JCheckBox, OptionFlag>(myUserEnterKeyToCheckBox, IdeaFlags.USE_ENTER_FOR_MESSAGES));
    myFlags.add(new Pair<JCheckBox, OptionFlag>(myShowPopup, IdeaFlags.POPUP_ON_MESSAGE));

    myOptions = Pico.getOptions();

    final UserCommand command = Pico.getCommandManager().getCommand(
        ClearHistoryCommand.class, BaseAction.getContainer(project));

    myClearHistory.setEnabled(command.isEnabled());
    myClearHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        command.execute();
        myClearHistory.setEnabled(command.isEnabled());
      }
    });
  }

  private void setTimoutModel(JSpinner spinner, String option, int defaultValue) {
    double value = myOptions.getNumber(option, defaultValue);
    spinner.setModel(new SpinnerNumberModel(value, 0.0, 9999, 1.0));
  }

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

  public JComponent createComponent() {
    return myPanel;
  }


  public void disposeUIResources() {
  }


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


  public void reset() {
    for (Pair<JCheckBox, OptionFlag> option : myFlags) {
      option.getFirst().setSelected(option.getSecond().isSet());
    }

    setTimoutModel(myTimeoutAway, IDEtalkOptions.TIMEOUT_AWAY_MIN, UserActivityMonitor.AWAY_MINS);
    setTimoutModel(myTimeoutXA, IDEtalkOptions.TIMEOUT_XA_MIN, UserActivityMonitor.EXTENDED_AWAY_MINS);
  }

  public String getDisplayName() {
    //noinspection HardCodedStringLiteral
    return "IDEtalk Options";
  }


  @Nullable
  @NonNls
  public String getHelpTopic() {
    return "reference.dialogs.idetalk";
  }

  public void edit() {
    ShowSettingsUtil.getInstance().editConfigurable(myProject, this);
  }
}
