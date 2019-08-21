package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.options.BeanConfigurable;
import com.jetbrains.lang.dart.DartBundle;

public class DartEditorAppearanceConfigurable extends BeanConfigurable<DartClosingLabelManager> {
  public DartEditorAppearanceConfigurable() {
    super(DartClosingLabelManager.getInstance());

    DartClosingLabelManager closingLabelManager = getInstance();
    checkBox(DartBundle.message("dart.editor.showClosingLabels.text"),
             closingLabelManager::getShowClosingLabels,
             closingLabelManager::setShowClosingLabels);
  }
}
