// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.application.options.editor.AutoImportOptionsProvider;
import com.intellij.openapi.application.ApplicationBundle;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.codeInsight.CodeInsightSettings.*;

public class DartAutoImportOptionsProvider implements AutoImportOptionsProvider {
  private JPanel myMainPanel;
  private JComboBox<String> mySmartPasteCombo;

  public DartAutoImportOptionsProvider() {
    mySmartPasteCombo.addItem(getInsertImportsAlways());
    mySmartPasteCombo.addItem(getInsertImportsAsk());
    mySmartPasteCombo.addItem(getInsertImportsNone());
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  public void reset() {
    final int val = DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE;
    final String item = val == YES ? getInsertImportsAlways() : val == ASK ? getInsertImportsAsk() : getInsertImportsNone();
    mySmartPasteCombo.setSelectedItem(item);
  }

  @Override
  public void apply() {
    DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE = getSmartPasteValue();
  }

  @Override
  public boolean isModified() {
    return DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE != getSmartPasteValue();
  }

  private int getSmartPasteValue() {
    final Object item = mySmartPasteCombo.getSelectedItem();
    return getInsertImportsAlways().equals(item) ? YES : getInsertImportsNone().equals(item) ? NO : ASK;
  }

  private static @Nls String getInsertImportsAlways() {
    return ApplicationBundle.message("combobox.insert.imports.all");
  }

  private static @Nls String getInsertImportsAsk() {
    return ApplicationBundle.message("combobox.insert.imports.ask");
  }

  private static @Nls String getInsertImportsNone() {
    return ApplicationBundle.message("combobox.insert.imports.none");
  }
}
