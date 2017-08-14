package com.jetbrains.lang.dart.ide.editor;

import com.intellij.application.options.editor.AutoImportOptionsProvider;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.codeInsight.CodeInsightSettings.*;

public class DartAutoImportOptionsProvider implements AutoImportOptionsProvider {
  private static final String INSERT_IMPORTS_ALWAYS = ApplicationBundle.message("combobox.insert.imports.all");
  private static final String INSERT_IMPORTS_ASK = ApplicationBundle.message("combobox.insert.imports.ask");
  private static final String INSERT_IMPORTS_NONE = ApplicationBundle.message("combobox.insert.imports.none");

  private JPanel myMainPanel;
  private JComboBox<String> mySmartPasteCombo;

  public DartAutoImportOptionsProvider() {
    mySmartPasteCombo.addItem(INSERT_IMPORTS_ALWAYS);
    mySmartPasteCombo.addItem(INSERT_IMPORTS_ASK);
    mySmartPasteCombo.addItem(INSERT_IMPORTS_NONE);
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public void reset() {
    final int val = DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE;
    final String item = val == YES ? INSERT_IMPORTS_ALWAYS : val == ASK ? INSERT_IMPORTS_ASK : INSERT_IMPORTS_NONE;
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
    return INSERT_IMPORTS_ALWAYS.equals(item) ? YES : INSERT_IMPORTS_NONE.equals(item) ? NO : ASK;
  }
}
