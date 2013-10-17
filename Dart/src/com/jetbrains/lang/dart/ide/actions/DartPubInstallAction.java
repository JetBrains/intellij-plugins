package com.jetbrains.lang.dart.ide.actions;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;

public class DartPubInstallAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentableText() {
    return DartBundle.message("dart.pub.install");
  }

  @Override
  protected String getPubCommandArgument() {
    return "install";
  }

  @Override
  protected String getSuccessOutputMessage() {
    return "Dependencies installed!";
  }
}
