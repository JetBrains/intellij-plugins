package com.jetbrains.lang.dart.ide.editor;

import com.intellij.application.options.editor.AutoImportOptionsProvider;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.codeInsight.CodeInsightSettings.*;

public class DartAutoImportOptionsProvider implements AutoImportOptionsProvider {
  private JPanel myMainPanel;
  private JComboBox<String> mySmartPasteCombo;

  public DartAutoImportOptionsProvider() {
    mySmartPasteCombo.addItem(getINSERT_IMPORTS_ALWAYS());
    mySmartPasteCombo.addItem(getINSERT_IMPORTS_ASK());
    mySmartPasteCombo.addItem(getINSERT_IMPORTS_NONE());
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public void reset() {
    final int val = DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE;
    final String item = val == YES ? getINSERT_IMPORTS_ALWAYS() : val == ASK ? getINSERT_IMPORTS_ASK() : getINSERT_IMPORTS_NONE();
    mySmartPasteCombo.setSelectedItem(item);
  }

  @Override
  public void apply() throws ConfigurationException {
    DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE = getSmartPasteValue();
  }

  @Override
  public boolean isModified() {
    return DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE != getSmartPasteValue();
  }

  private int getSmartPasteValue() {
    final Object item = mySmartPasteCombo.getSelectedItem();
    return getINSERT_IMPORTS_ALWAYS().equals(item) ? YES : getINSERT_IMPORTS_NONE().equals(item) ? NO : ASK;
  }

  private static String getINSERT_IMPORTS_ALWAYS() {
    return ApplicationBundle.message("combobox.insert.imports.all");
  }

  private static String getINSERT_IMPORTS_ASK() {
    return ApplicationBundle.message("combobox.insert.imports.ask");
  }

  private static String getINSERT_IMPORTS_NONE() {
    return ApplicationBundle.message("combobox.insert.imports.none");
  }
}
