package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.execution.KarmaRunSettings;
import com.intellij.javascript.testFramework.util.TestFullNameView;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class KarmaSuiteOrTestScopeView extends KarmaScopeView {
  private final TestFullNameView myTestNameView;
  private final JPanel myPanel;

  public KarmaSuiteOrTestScopeView(@NotNull String fullTestNamePopupTitle,
                                   @NotNull String fullTestNameLabel) {
    myTestNameView = new TestFullNameView(fullTestNamePopupTitle);
    myPanel = new FormBuilder()
      .setAlignLabelOnRight(false)
      .addLabeledComponent(fullTestNameLabel, myTestNameView.getComponent())
      .getPanel();
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void resetFrom(@NotNull KarmaRunSettings settings) {
    myTestNameView.setNames(settings.getTestNames());
  }

  @Override
  public void applyTo(@NotNull KarmaRunSettings.Builder builder) {
    builder.setTestNames(myTestNameView.getNames());
  }
}
