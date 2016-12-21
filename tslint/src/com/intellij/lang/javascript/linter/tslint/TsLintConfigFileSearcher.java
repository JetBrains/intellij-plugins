package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;

import java.io.File;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintConfigFileSearcher {
  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);
  public final static String CONFIG_FILE_NAME = "tslint.json";

  public VirtualFile lookup(VirtualFile vf) {
    VirtualFile current = vf.getParent();
    while (current != null) {
      final VirtualFile child = current.findChild(CONFIG_FILE_NAME);
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
