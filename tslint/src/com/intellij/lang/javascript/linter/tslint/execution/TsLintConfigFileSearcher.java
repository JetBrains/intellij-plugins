package com.intellij.lang.javascript.linter.tslint.execution;

import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration.TSLINT_JSON;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public final class TsLintConfigFileSearcher {
  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);

  public VirtualFile getConfig(@NotNull TsLintState state, @NotNull VirtualFile virtualFile) {
    if (state.isCustomConfigFileUsed()) {
      final String configFilePath = state.getCustomConfigFilePath();
      if (StringUtil.isEmptyOrSpaces(configFilePath)) {
        return null;
      }
      final File configFile = new File(configFilePath);
      return VfsUtil.findFileByIoFile(configFile, false);
    }

    return lookupParentConfig(virtualFile);
  }

  @Nullable
  private static VirtualFile lookupParentConfig(@NotNull VirtualFile vf) {
    VirtualFile current = vf.getParent();
    while (current != null) {
      final VirtualFile child = current.findChild(TSLINT_JSON);
      if (child != null) return child;
      current = current.getParent();
    }
    final File file = new File(SystemProperties.getUserHome());
    if (file.exists()) {
      final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
      if (virtualFile == null) {
        LOG.debug("Could not find virtual file for config file, though config file exists: " + file.getAbsolutePath());
      }
    }
    return null;
  }
}
