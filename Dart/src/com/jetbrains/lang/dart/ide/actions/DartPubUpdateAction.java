package com.jetbrains.lang.dart.ide.actions;

import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

/**
 * Created by fedorkorotkov.
 */
public class DartPubUpdateAction extends DartPubActionBase {
  @Nls
  @Override
  protected String getPresentationText() {
    return DartBundle.message("dart.pub.update");
  }

  @Override
  protected boolean isEnabled(YAMLFile file) {
    return DartResolveUtil.findPackagesFolderByFile(file.getVirtualFile()) != null;
  }

  @Override
  protected String getPubCommandArgument() {
    return "update";
  }

  @Override
  protected boolean isOK(@NotNull String output) {
    return output.contains("Dependencies updated!");
  }
}
