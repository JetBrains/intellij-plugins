package com.jetbrains.lang.dart.ide.application.options;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.codeStyle.CodeStyleSchemesModel;
import com.intellij.application.options.codeStyle.CodeStyleSchemesModelListener;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.ui.components.JBLabel;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartfmtCodeStylePanel extends CodeStyleAbstractPanel {

  private JPanel myMainPanel;
  private JCheckBox myDelegateToDartfmtCheckBox;
  private JBLabel myRightMarginLabel;

  public DartfmtCodeStylePanel(@NotNull final CodeStyleSettings settings) {
    super(settings);
  }

  @Override
  public void setModel(@Nullable CodeStyleSchemesModel model) {
    super.setModel(model);
    if (model != null) {
      model.addListener(new CodeStyleSchemesModelListener() {
        @Override
        public void afterCurrentSettingsChanged() {
          updateRightMarginLabel();
        }
      });
    }
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
    myRightMarginLabel.setText(DartBundle.message("dartfmt.right.margin", settings.getRightMargin(DartLanguage.INSTANCE)));
  }

  public void updateRightMarginLabel() {
    myRightMarginLabel.setText(DartBundle.message("dartfmt.right.margin", getSettings().getRightMargin(DartLanguage.INSTANCE)));
  }
}