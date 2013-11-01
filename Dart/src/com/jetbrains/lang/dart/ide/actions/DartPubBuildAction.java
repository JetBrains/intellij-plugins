package com.jetbrains.lang.dart.ide.actions;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;

public class DartPubBuildAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentableText() {
    return DartBundle.message("dart.pub.build");
  }

  @Override
  protected String getPubCommand() {
    return "build";
  }

  @Override
  protected String getSuccessMessage() {
    return DartBundle.message("dart.pub.build.success");
  }
}
