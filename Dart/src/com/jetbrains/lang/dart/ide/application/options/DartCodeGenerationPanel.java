package com.jetbrains.lang.dart.ide.application.options;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartCodeGenerationPanel extends CodeStyleAbstractPanel {

  private JPanel myMainPanel;
  private JCheckBox myInsertOverrideAnnotationCheckBox;

  public DartCodeGenerationPanel(@NotNull final CodeStyleSettings settings) {
    super(settings);
  }

  @Override
  protected String getTabTitle() {
    return ApplicationBundle.message("title.code.generation");
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
    settings.INSERT_OVERRIDE_ANNOTATION = myInsertOverrideAnnotationCheckBox.isSelected();
  }

  @Override
  public boolean isModified(@NotNull final CodeStyleSettings settings) {
    return settings.INSERT_OVERRIDE_ANNOTATION != myInsertOverrideAnnotationCheckBox.isSelected();
  }

  @Override
  protected void resetImpl(@NotNull final CodeStyleSettings settings) {
    myInsertOverrideAnnotationCheckBox.setSelected(settings.INSERT_OVERRIDE_ANNOTATION);
  }
}
