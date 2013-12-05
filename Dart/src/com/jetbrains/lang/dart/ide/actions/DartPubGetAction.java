package com.jetbrains.lang.dart.ide.actions;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;

public class DartPubGetAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentableText() {
    return DartBundle.message("dart.pub.get");
  }

  @Override
  protected String getPubCommand() {
    return "get";
  }

  @Override
  protected String getSuccessMessage() {
    return DartBundle.message("dart.pub.get.success");
  }
}
