package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.UriKind;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

public class DartFileBasedSource implements Source {

  private final @NotNull Project myProject;
  private final @NotNull VirtualFile myFile;
  private long myModificationStampWhenFileContentWasRead = -1;

  private DartFileBasedSource(final @NotNull Project project, final @NotNull VirtualFile file) {
    myProject = project;
    myFile = file;
  }

  @NotNull
  public VirtualFile getFile() {
    return myFile;
  }

  public boolean isOutOfDate() {
    return myModificationStampWhenFileContentWasRead == -1 || myModificationStampWhenFileContentWasRead != getModificationStamp();
  }

  @Override
  public boolean equals(final Object object) {
    return object != null && this.getClass() == object.getClass()
           && myFile.equals(((DartFileBasedSource)object).myFile);
  }

  @Override
  public boolean exists() {
    return myFile.exists() && !myFile.isDirectory();
  }

  @Override
  public void getContents(final ContentReceiver receiver) throws Exception {
    final Exception exception = ApplicationManager.getApplication().runReadAction(new NullableComputable<Exception>() {
      @Nullable
      public Exception compute() {
        final Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(myFile);
        if (cachedDocument != null) {
          myModificationStampWhenFileContentWasRead = cachedDocument.getModificationStamp();
          receiver.accept(cachedDocument.getText(), myModificationStampWhenFileContentWasRead);
        }
        else {
          myModificationStampWhenFileContentWasRead = myFile.getModificationStamp();
          try {
            receiver.accept(VfsUtilCore.loadText(myFile), myModificationStampWhenFileContentWasRead);
          }
          catch (IOException e) {
            return e;
          }
        }
        return null;
      }
    });

    if (exception != null) {
      throw exception;
    }
  }

  @Override
  public String getEncoding() {
    return UriKind.FILE_URI.getEncoding() + myFile.getUrl();
  }

  @Override
  public String getFullName() {
    return myFile.getPath();
  }

  @Override
  public long getModificationStamp() {
    final Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(myFile);
    if (cachedDocument != null) {
      return cachedDocument.getModificationStamp();
    }
    else {
      return myFile.getModificationStamp();
    }
  }

  @Override
  public String getShortName() {
    return myFile.getName();
  }

  @Override
  public UriKind getUriKind() {
    return UriKind.FILE_URI;
  }

  @Override
  public int hashCode() {
    return myFile.hashCode();
  }

  @Override
  public boolean isInSystemLibrary() {
    return false;
  }

  @Override
  public Source resolveRelative(final URI containedUri) {
    final VirtualFile file = containedUri.getScheme() == null
                             ? VfsUtilCore.findRelativeFile(containedUri.toString(), myFile.getParent())
                             : LocalFileSystem.getInstance().findFileByPath(containedUri.getPath());

    return file == null ? null : getSource(myProject, file);
  }

  @Override
  public String toString() {
    return myFile.getPath();
  }

  public static DartFileBasedSource getSource(final @NotNull Project project, final @NotNull VirtualFile file) {
    return DartAnalyzerService.getInstance(project).getOrCreateSource(file, new Function<VirtualFile, DartFileBasedSource>() {
      public DartFileBasedSource fun(final VirtualFile file) {
        return new DartFileBasedSource(project, file);
      }
    });
  }
}
