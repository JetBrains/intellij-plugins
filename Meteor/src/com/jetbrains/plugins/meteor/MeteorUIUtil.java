package com.jetbrains.plugins.meteor;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.ui.SwingHelper;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MeteorUIUtil {
  public static @NotNull TextFieldWithHistoryWithBrowseButton createTextField(final @Nullable Project project) {
    var descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withTitle(MeteorBundle.message("dialog.title.meteor.executable"));
    var button = SwingHelper.createTextFieldWithHistoryWithBrowseButton(project, descriptor, () -> getMeteorExecutableList());
    setValue(MeteorSettings.getInstance().getExecutablePath(), button);
    return button;
  }

  public static void setValue(String value, TextFieldWithHistoryWithBrowseButton button) {
    var textFieldWithHistory = button.getChildComponent();
    if (!StringUtil.isEmpty(value)) {
      textFieldWithHistory.setText(value);
      textFieldWithHistory.addCurrentTextToHistory();
    }
  }

  public static List<String> getMeteorExecutableList() {
    return MeteorSettings.detectMeteorExecutablePaths();
  }

  public static TextFieldWithBrowseButton createPackagesField(Project project) {
    var fieldWithBrowseButton = new TextFieldWithBrowseButton();
    var descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(MeteorBundle.message("global.meteor.root.folder"));
    SwingHelper.installFileCompletionAndBrowseDialog(project, fieldWithBrowseButton, descriptor);
    return fieldWithBrowseButton;
  }
}
