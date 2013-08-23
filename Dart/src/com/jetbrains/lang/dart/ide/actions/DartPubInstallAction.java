package com.jetbrains.lang.dart.ide.actions;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

/**
 * Created by fedorkorotkov.
 */
public class DartPubInstallAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentationText() {
    return DartBundle.message("dart.pub.install");
  }

  @Override
  protected boolean isEnabled(YAMLFile file) {
    return true;
  }

  @Override
  protected String getPubCommandArgument() {
    return "update";
  }

  @Override
  protected boolean isOK(@NotNull String output) {
    return output.contains("Dependencies installed!");
  }
}
