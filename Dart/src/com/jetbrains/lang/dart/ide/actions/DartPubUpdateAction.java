package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class DartPubUpdateAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentableText() {
    return DartBundle.message("dart.pub.update");
  }

  protected boolean isEnabled(@NotNull final VirtualFile file) {
    return DartResolveUtil.findPackagesFolderByFile(file) != null;
  }

  @Override
  protected String getPubCommandArgument() {
    return "update";
  }

  @Override
  protected String getSuccessOutputMessage() {
    return "Dependencies updated!";
  }
}
