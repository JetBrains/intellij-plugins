// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.application.options.CodeCompletionConfigurable;
import com.intellij.ide.ui.search.SearchableOptionContributor;
import com.intellij.ide.ui.search.SearchableOptionProcessor;
import org.jetbrains.annotations.NotNull;

final class DartSearchableOptionContributor extends SearchableOptionContributor {
  @Override
  public void processOptions(@NotNull SearchableOptionProcessor processor) {
    processor.addOptions("dart " + DartBundle.message("dialog.paste.on.import.title"), null,
                         "Dart: " + DartBundle.message("dialog.paste.on.import.title"), "editor.preferences.import", null, false);
    processor.addOptions(DartBundle.message("checkbox.collapse.parts"), null,
                         "Code Folding: collapse Dart parts by default", "editor.preferences.folding", null, false);
    processor.addOptions(DartBundle.message("checkbox.collapse.generic.parameters"), null,
                         "Code Folding: collapse Dart generic parameters by default", "editor.preferences.folding", null, false);
    processor.addOptions("dart " + DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), null,
                         "Dart: " + DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), CodeCompletionConfigurable.ID, null,
                         false);

    processor.addOptions("Dart SDK path", null,
                         "Dart SDK path", "dart.settings", null, false);
    processor.addOptions("Dart pub webdev server port", null,
                         "Dart webdev server port", "dart.settings", null, false);
    processor.addOptions(DartBundle.message("code.style.settings.label.line.length"), null,
                         DartBundle.message("code.style.settings.label.line.length"), "preferences.sourceCode.Dart", null, false);
  }
}
