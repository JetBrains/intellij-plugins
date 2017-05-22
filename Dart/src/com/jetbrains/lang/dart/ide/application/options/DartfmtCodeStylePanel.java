package com.jetbrains.lang.dart.ide.application.options;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartfmtCodeStylePanel extends CodeStyleAbstractPanel {

  private JPanel myMainPanel;
  private JCheckBox myDelegateToDartfmtCheckBox;

  public DartfmtCodeStylePanel(@NotNull final CodeStyleSettings settings) {
    super(settings);
  }

  private static DartCodeStyleSettings getDartCodeStyleSettings(@NotNull final CodeStyleSettings settings) {
    return settings.getCustomSettings(DartCodeStyleSettings.class);
  }

  @Override
  protected String getTabTitle() {
    return "Dartfmt";
  }

  @Override
  protected int getRightMargin() {
    return 0;
  }

  @Nullable
  @Override
  protected EditorHighlighter createHighlighter(EditorColorsScheme scheme) {
    return null;
  }

  @NotNull
  @Override
  protected FileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @Nullable
  @Override
  protected String getPreviewText() {
    return null;
  }

  @Nullable
  @Override
  public JComponent getPanel() {
    return myMainPanel;
  }

  @Override
  public void apply(@NotNull final CodeStyleSettings settings) throws ConfigurationException {
    getDartCodeStyleSettings(settings).DELEGATE_TO_DARTFMT = myDelegateToDartfmtCheckBox.isSelected();
  }

  @Override
  public boolean isModified(@NotNull final CodeStyleSettings settings) {
    return getDartCodeStyleSettings(settings).DELEGATE_TO_DARTFMT != myDelegateToDartfmtCheckBox.isSelected();
  }

  @Override
  protected void resetImpl(@NotNull final CodeStyleSettings settings) {
    myDelegateToDartfmtCheckBox.setSelected(getDartCodeStyleSettings(settings).DELEGATE_TO_DARTFMT);
  }
}