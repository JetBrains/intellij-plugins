package com.intellij.javascript.flex.mxml.copyright;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;
import com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider;
import com.maddyhome.idea.copyright.psi.UpdateXmlCopyrightsProvider;

public class UpdateMxmlCopyrightsProvider extends UpdateCopyrightsProvider {

  public UpdateCopyright createInstance(final Project project,
                                        final Module module,
                                        final VirtualFile file,
                                        final FileType base,
                                        final CopyrightProfile options) {
    return new UpdateMxmlFileCopyright(project, module, file, options);
  }

  private static class UpdateMxmlFileCopyright extends UpdateXmlCopyrightsProvider.UpdateXmlFileCopyright {
    public UpdateMxmlFileCopyright(final Project project, final Module module, final VirtualFile file, final CopyrightProfile options) {
      super(project, module, file, options);
    }

    protected boolean accept() {
      return getFile().getFileType() == FlexApplicationComponent.MXML;
    }
  }
}
