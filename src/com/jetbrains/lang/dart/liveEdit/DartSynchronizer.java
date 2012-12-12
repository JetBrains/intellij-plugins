package com.jetbrains.lang.dart.liveEdit;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.jetbrains.browserConnection.BrowserConnection;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.liveEdit.Dom;
import com.jetbrains.liveEdit.FileBasedSynchronizer;

public class DartSynchronizer extends FileBasedSynchronizer<DartFile> {
  public DartSynchronizer(BrowserConnection browserConnection) {
    super(browserConnection);
  }

  @Override
  public boolean canHandle(FileType fileType) {
    return fileType == DartFileType.INSTANCE;
  }

  @Override
  protected void sync(DartFile file) {
    Dom.getInstance().setScriptSource(file.getProject().getLocationHash(), file.getName(), file.getViewProvider().getContents());
  }

  @Override
  public void reload(PsiFile file) {
    final String filename = file.getName();
    final String projectId = file.getProject().getLocationHash();
    saveFile(file, new Runnable() {
      @Override
      public void run() {
        Dom.getInstance().reloadPagesContainingScript(projectId, filename);
      }
    });
  }
}
