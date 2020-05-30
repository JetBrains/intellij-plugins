// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartCodeStylePanel extends CodeStyleAbstractPanel {
  private JPanel myMainPanel;
  private JBLabel myDartfmtLabel;
  private JBTextField myLineLengthField;

  public DartCodeStylePanel(@NotNull CodeStyleSettings currentSettings, @NotNull CodeStyleSettings settings) {
    super(DartLanguage.INSTANCE, currentSettings, settings);
    myDartfmtLabel.setCopyable(true); // This is needed to make the FAQ link clickable.
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
    //return FormBuilder.createFormBuilder()
    //  .addLabeledComponent(DartBundle.message("line.length"), myLineLengthField)
    //  .addTooltip(DartBundle.message("dartfmt.tooltip"))
    //  .getPanel();

    return myMainPanel;
  }

  @Override
  public void apply(@NotNull final CodeStyleSettings settings) {
    CommonCodeStyleSettings dartSettings = settings.getCommonSettings(getDefaultLanguage());
    try {
      int lineLength = Integer.parseInt(myLineLengthField.getText().trim());
      if (lineLength > 0) {
        dartSettings.RIGHT_MARGIN = lineLength;
      }
    }
    catch (NumberFormatException ignore) {/*unlucky*/}
  }

  @Override
  public boolean isModified(@NotNull final CodeStyleSettings settings) {
    CommonCodeStyleSettings dartSettings = settings.getCommonSettings(getDefaultLanguage());
    try {
      int lineLength = Integer.parseInt(myLineLengthField.getText().trim());
      return lineLength != dartSettings.RIGHT_MARGIN;
    }
    catch (NumberFormatException ignore) {/*unlucky*/}
    return false;
  }

  @Override
  protected void resetImpl(@NotNull final CodeStyleSettings settings) {
    CommonCodeStyleSettings dartSettings = settings.getCommonSettings(getDefaultLanguage());
    myLineLengthField.setText(String.valueOf(dartSettings.RIGHT_MARGIN));
  }
}