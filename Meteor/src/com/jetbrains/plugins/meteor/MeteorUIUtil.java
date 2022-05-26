package com.jetbrains.plugins.meteor;


import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.ui.SwingHelper;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MeteorUIUtil {

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createTextField(@Nullable final Project project) {
    TextFieldWithHistoryWithBrowseButton button = SwingHelper.createTextFieldWithHistoryWithBrowseButton(
      project,
      MeteorBundle.message("dialog.title.meteor.executable"),
      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
      () -> getMeteorExecutableList());

    setValue(MeteorSettings.getInstance().getExecutablePath(), button);

    return button;
  }

  public static void setValue(String value, TextFieldWithHistoryWithBrowseButton button) {
    TextFieldWithHistory textFieldWithHistory = button.getChildComponent();
    if (!StringUtil.isEmpty(value)) {
      textFieldWithHistory.setText(value);
      textFieldWithHistory.addCurrentTextToHistory();
    }
  }

  public static List<String> getMeteorExecutableList() {
    return MeteorSettings.detectMeteorExecutablePaths();
  }

  public static TextFieldWithBrowseButton createPackagesField(Project project) {
    TextFieldWithBrowseButton fieldWithBrowseButton = new TextFieldWithBrowseButton();
    SwingHelper.installFileCompletionAndBrowseDialog(project, fieldWithBrowseButton, MeteorBundle.message("global.meteor.root.folder"),
                                                     FileChooserDescriptorFactory.createSingleFolderDescriptor());
    return fieldWithBrowseButton;
  }
}
