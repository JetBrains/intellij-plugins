package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.options.ConfigurableBuilder;
import com.jetbrains.lang.dart.DartBundle;

public class DartEditorAppearanceConfigurable extends ConfigurableBuilder {
  public DartEditorAppearanceConfigurable() {
    DartClosingLabelManager closingLabelManager = DartClosingLabelManager.getInstance();
    checkBox(DartBundle.message("dart.editor.showClosingLabels.text"),
             closingLabelManager::getShowClosingLabels,
             closingLabelManager::setShowClosingLabels);
  }
}
