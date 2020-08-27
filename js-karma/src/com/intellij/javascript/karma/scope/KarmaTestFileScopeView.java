package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.execution.KarmaRunSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import com.intellij.webcore.ui.PathShortener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class KarmaTestFileScopeView extends KarmaScopeView {

  private final TextFieldWithBrowseButton myTestFileTextFieldWithBrowseButton;
  private final JPanel myPanel;

  public KarmaTestFileScopeView(@NotNull Project project) {
    myTestFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    PathShortener.enablePathShortening(myTestFileTextFieldWithBrowseButton.getTextField(), null);
    SwingHelper.installFileCompletionAndBrowseDialog(
      project,
      myTestFileTextFieldWithBrowseButton,
      KarmaBundle.message("run_configuration.select_test_file.browseTitle"),
      FileChooserDescriptorFactory.createSingleFileDescriptor()
    );
    myPanel = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(KarmaBundle.message("run_configuration.test_file.label"), myTestFileTextFieldWithBrowseButton)
      .getPanel();
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void resetFrom(@NotNull KarmaRunSettings settings) {
    myTestFileTextFieldWithBrowseButton.setText(settings.getTestFileSystemDependentPath());
  }

  @Override
  public void applyTo(@NotNull KarmaRunSettings.Builder builder) {
    builder.setTestFilePath(PathShortener.getAbsolutePath(myTestFileTextFieldWithBrowseButton.getTextField()));
  }
}
