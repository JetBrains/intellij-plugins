package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.NotNullProducer;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhoneGapUIUtil {

  @NotNull
  public static TextFieldWithHistoryWithBrowseButton createPhoneGapExecutableTextField(@Nullable Project project) {
    TextFieldWithHistoryWithBrowseButton textFieldWithHistoryWithBrowseButton = new TextFieldWithHistoryWithBrowseButton();
    final TextFieldWithHistory textFieldWithHistory = textFieldWithHistoryWithBrowseButton.getChildComponent();
    textFieldWithHistory.setHistorySize(-1);
    textFieldWithHistory.setMinimumAndPreferredWidth(0);
    SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
      @NotNull
      @Override
      public List<String> produce() {
        return PhoneGapSettings.getDefaultPaths();
      }
    });
    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      textFieldWithHistoryWithBrowseButton,
      "Phonegap/Cordova executable path:",
      FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
    );

    String executablePath = PhoneGapSettings.getInstance().getExecutablePath();
    if (StringUtil.isNotEmpty(executablePath)) {
      textFieldWithHistory.setText(executablePath);
      textFieldWithHistory.addCurrentTextToHistory();
    }

    return textFieldWithHistoryWithBrowseButton;
  }

  public static void setExecutablePath(TextFieldWithHistoryWithBrowseButton field, String executablePath) {
    TextFieldWithHistory component = field.getChildComponent();
    component.setText(executablePath);
    if (!StringUtil.isEmpty(executablePath)) {
      component.addCurrentTextToHistory();
    }
  }
}
