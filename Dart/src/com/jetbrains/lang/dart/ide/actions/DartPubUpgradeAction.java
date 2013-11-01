package com.jetbrains.lang.dart.ide.actions;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;

public class DartPubUpgradeAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentableText() {
    return DartBundle.message("dart.pub.upgrade");
  }

  @Override
  protected String getPubCommand() {
    return "upgrade";
  }

  @Override
  protected String getSuccessMessage() {
    return DartBundle.message("dart.pub.upgrade.success");
  }
}
