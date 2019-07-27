// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.application.options.CodeCompletionOptions;
import com.intellij.application.options.CodeStyleConfigurableWrapper;
import com.intellij.ide.ui.search.SearchableOptionContributor;
import com.intellij.ide.ui.search.SearchableOptionProcessor;
import com.jetbrains.lang.dart.ide.application.options.DartfmtCodeStylePanel;
import org.jetbrains.annotations.NotNull;

public class DartSearchableOptionContributor extends SearchableOptionContributor {
  @Override
  public void processOptions(@NotNull SearchableOptionProcessor processor) {
    processor.addOptions(DartBundle.message("dart.editor.showClosingLabels.text"), null,
                         DartBundle.message("dart.editor.showClosingLabels.text"), "editor.preferences.appearance", null, false);
    processor.addOptions("dart " + DartBundle.message("dialog.paste.on.import.title"), null,
                         "Dart: " + DartBundle.message("dialog.paste.on.import.title"), "editor.preferences.import", null, false);
    processor.addOptions(DartBundle.message("checkbox.collapse.parts"), null,
                         "Code Folding: collapse Dart parts by default", "editor.preferences.folding", null, false);
    processor.addOptions(DartBundle.message("checkbox.collapse.generic.parameters"), null,
                         "Code Folding: collapse Dart generic parameters by default", "editor.preferences.folding", null, false);
    processor.addOptions("dart " + DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), null,
                         "Dart: " + DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), CodeCompletionOptions.ID, null,
                         false);

    processor.addOptions("Dart format style dartfmt", DartfmtCodeStylePanel.TAB_TITLE,
                         "Use the dartfmt tool when formatting the whole file", CodeStyleConfigurableWrapper.getConfigurableId("Dart"),
                         "Code Style > Dart", false);

    processor.addOptions("Dart SDK path", null,
                         "Dart SDK path", "dart.settings", null, false);
    processor.addOptions("Dart pub webdev server port", null,
                         "Dart webdev server port", "dart.settings", null, false);
  }
}
