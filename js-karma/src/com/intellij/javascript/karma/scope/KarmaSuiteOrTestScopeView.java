// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.execution.KarmaRunSettings;
import com.intellij.javascript.testFramework.util.TestFullNameView;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import com.intellij.webcore.ui.PathShortener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class KarmaSuiteOrTestScopeView extends KarmaScopeView {
  private final TestFullNameView myTestNameView;
  private final TextFieldWithBrowseButton myTestFileTextFieldWithBrowseButton;
  private final JPanel myPanel;

  public KarmaSuiteOrTestScopeView(@NotNull Project project,
                                   @NotNull @NlsContexts.PopupTitle String fullTestNamePopupTitle,
                                   @NotNull @NlsContexts.Label String fullTestNameLabel) {
    myTestNameView = new TestFullNameView(fullTestNamePopupTitle);
    myTestFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    PathShortener.enablePathShortening(myTestFileTextFieldWithBrowseButton.getTextField(), null);
    var descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().withTitle(JavaScriptBundle.message("rc.testRunScope.testFile.browseTitle"));
    SwingHelper.installFileCompletionAndBrowseDialog(project, myTestFileTextFieldWithBrowseButton, descriptor);
    myPanel = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(fullTestNameLabel, myTestNameView.getComponent())
      .addLabeledComponent(JavaScriptBundle.message("rc.testRunScope.testFile.label"), myTestFileTextFieldWithBrowseButton)
      .getPanel();
  }

  @Override
  public @NotNull JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void resetFrom(@NotNull KarmaRunSettings settings) {
    myTestNameView.setNames(settings.getTestNames());
    myTestFileTextFieldWithBrowseButton.setText(settings.getTestFileSystemDependentPath());
  }

  @Override
  public void applyTo(@NotNull KarmaRunSettings.Builder builder) {
    builder.setTestNames(myTestNameView.getNames());
    builder.setTestFilePath(PathShortener.getAbsolutePath(myTestFileTextFieldWithBrowseButton.getTextField()));
  }
}
