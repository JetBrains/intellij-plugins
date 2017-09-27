package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.options.BeanConfigurable;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.ui.IdeBorderFactory;
import com.jetbrains.lang.dart.DartBundle;

import javax.swing.*;

public class DartEditorAppearanceConfigurable extends BeanConfigurable<ClosingLabelManager> implements UnnamedConfigurable {
  public DartEditorAppearanceConfigurable() {
    super(ClosingLabelManager.getInstance());

    ClosingLabelManager closingLabelManager = getInstance();
    checkBox(
      DartBundle.message("dart.editor.showClosingLabels.text"),
      () -> closingLabelManager.getShowClosingLabels(),
      v -> closingLabelManager.setShowClosingLabels(v));
  }

  @Override
  public JComponent createComponent() {
    JComponent result = super.createComponent();
    assert result != null;
    result.setBorder(IdeBorderFactory.createTitledBorder("Dart")); //$NON-NLS-1$
    return result;
  }
}
