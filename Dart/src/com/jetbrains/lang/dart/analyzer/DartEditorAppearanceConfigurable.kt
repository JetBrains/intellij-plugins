package com.jetbrains.lang.dart.analyzer

import com.intellij.openapi.options.BeanConfigurable
import com.jetbrains.lang.dart.DartBundle

class DartEditorAppearanceConfigurable : BeanConfigurable<DartClosingLabelManager>(DartClosingLabelManager.getInstance()) {

  init {
    checkBox(DartBundle.message("dart.editor.showClosingLabels.text"), instance::getShowClosingLabels, instance::setShowClosingLabels)
  }
}
