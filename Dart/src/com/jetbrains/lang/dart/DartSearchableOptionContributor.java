// Copyright 2000-2019 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart;

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
                         "Dart: " + DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), "editor.preferences.smartKeys", null,
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
