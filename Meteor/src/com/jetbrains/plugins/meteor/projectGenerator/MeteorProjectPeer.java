package com.jetbrains.plugins.meteor.projectGenerator;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorUIUtil;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeteorProjectPeer implements WebProjectGenerator.GeneratorPeer<MeteorProjectTemplateGenerator.MeteorProjectSettings> {
  public static final String EMPTY_PROJECT_TYPE = "default";

  public static class RepoInfo {
    public final String myRepo;
    public final String myBranch;
    public final String myUser;

    public RepoInfo(@NotNull String user, @NotNull String repo, @NotNull String branch) {
      myUser = user;
      myRepo = repo;
      myBranch = branch;
    }

    public RepoInfo(@NotNull String repo, @NotNull String branch) {
      this(OWNER_NAME, repo, branch);
    }
  }

  public static final Map<String, RepoInfo> PROJECT_TYPES = new LinkedHashMap<>();

  static final String OWNER_NAME = "meteor";

  static {
    PROJECT_TYPES.put("default", null);
    PROJECT_TYPES.put("clock", null);
    PROJECT_TYPES.put("leaderboard", null);
    PROJECT_TYPES.put("localmarket", null);
    PROJECT_TYPES.put("simple-todos", null);
    PROJECT_TYPES.put("simple-todos-react", null);
    PROJECT_TYPES.put("simple-todos-angular", null);
    PROJECT_TYPES.put("todos", null);
    PROJECT_TYPES.put("todos-react", new RepoInfo("todos", "react"));
    PROJECT_TYPES.put("angular2-boilerplate", new RepoInfo("bsliran", "angular2-meteor-base", "master"));
  }


  private final List<WebProjectGenerator.SettingsStateListener> myStateListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  private TextFieldWithHistoryWithBrowseButton myExecutablePathField;
  private final JComboBox myProjectType;
  private final JTextField myName;

  MeteorProjectPeer() {
    myProjectType = new ComboBox();
    myName = new JBTextField();
    myName.setText(MeteorProjectTemplateGenerator.DEFAULT_TEMPLATE_NAME);

    myProjectType.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final Container parent = myName.getParent();
        final boolean enabled = EMPTY_PROJECT_TYPE.equals(myProjectType.getSelectedItem());

        if (parent instanceof LabeledComponent) {
          parent.setVisible(enabled);
        }
        else {
          myName.setEnabled(enabled);
        }
      }
    });
  }

  private void createAndInit() {
    myExecutablePathField = MeteorUIUtil.createTextField(null);
    SwingHelper.updateItems(myProjectType, getProjectTypes(), null);
  }

  @Override
  public @NotNull JComponent getComponent(@NotNull TextFieldWithBrowseButton myLocationField, @NotNull Runnable checkValid) {
    createAndInit();
    return FormBuilder.createFormBuilder()
      .addLabeledComponent(MeteorBundle.message("settings.meteor.configurable.executable.generator"), myExecutablePathField)
      .addLabeledComponent(MeteorBundle.message("settings.meteor.project.generator.type"), myProjectType)
      .addLabeledComponent(MeteorBundle.message("settings.meteor.project.generator.app.name"), myName)
      .getPanel();
  }

  protected List<String> getProjectTypes() {
    return new ArrayList<>(PROJECT_TYPES.keySet());
  }

  @Override
  public void buildUI(@NotNull SettingsStep settingsStep) {
    createAndInit();
    settingsStep.addSettingsField(MeteorBundle.message("settings.meteor.configurable.executable.generator"),
                                  SwingHelper.wrapWithHorizontalStretch(myExecutablePathField));
    settingsStep.addSettingsField(MeteorBundle.message("settings.meteor.project.generator.type"), myProjectType);
    settingsStep.addSettingsField(MeteorBundle.message("settings.meteor.project.generator.app.name"), myName);
  }


  @Override
  public @NotNull MeteorProjectTemplateGenerator.MeteorProjectSettings getSettings() {
    MeteorProjectTemplateGenerator.MeteorProjectSettings settings = new MeteorProjectTemplateGenerator.MeteorProjectSettings();
    settings.setMeteorExecutablePath(myExecutablePathField.getText());
    settings.setType(((String)myProjectType.getSelectedItem()));
    settings.setName(myName.getText());
    return settings;
  }

  @Override
  public @Nullable ValidationInfo validate() {
    String path = myExecutablePathField.getText();

    boolean error;
    try {
      File file = new File(path);
      error = !file.exists() ||
              !StringUtil.toLowerCase(file.getName()).contains(MeteorSettings.METEOR_SIMPLE_NAME) ||
              !file.isFile() ||
              !file.canExecute();
    }
    catch (Exception e) {
      error = true;
    }

    return error ? new ValidationInfo(MeteorBundle.message("dialog.message.incorrect.meteor.executable")) : null;
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(@NotNull WebProjectGenerator.SettingsStateListener listener) {
    myStateListeners.add(listener);
  }
}
