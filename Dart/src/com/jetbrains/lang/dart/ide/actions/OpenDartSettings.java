package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import icons.DartIcons;

public class OpenDartSettings extends DumbAwareAction {
  public OpenDartSettings() {
    super(DartBundle.message("open.dart.settings"), DartBundle.message("open.dart.settings"), DartIcons.Dart_16);
  }

  @Override
  public void actionPerformed(final AnActionEvent e) {
    ShowSettingsUtilImpl.showSettingsDialog(e.getProject(), DartConfigurable.DART_SETTINGS_PAGE_ID, "");
  }
}
