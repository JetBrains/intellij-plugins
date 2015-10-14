package com.intellij.javascript.flex.copyright;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;
import com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider;
import com.maddyhome.idea.copyright.psi.UpdateJavaScriptFileCopyright;

public class UpdateActionScriptCopyrightsProvider extends UpdateCopyrightsProvider {

  public UpdateCopyright createInstance(final Project project,
                                        final Module module,
                                        final VirtualFile file,
                                        final FileType base,
                                        final CopyrightProfile options) {
    return new UpdateActionScriptFileCopyright(project, module, file, options);
  }

  private static class UpdateActionScriptFileCopyright extends UpdateJavaScriptFileCopyright {
    public UpdateActionScriptFileCopyright(final Project project,
                                           final Module module,
                                           final VirtualFile file,
                                           final CopyrightProfile options) {
      super(project, module, file, options);
    }

    protected boolean accept() {
      return DialectDetector.isActionScript(getFile());
    }
  }
}
