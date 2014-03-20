package com.jetbrains.lang.dart.analyzer;

import com.google.dart.engine.internal.context.TimestampedData;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.source.UriKind;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

public class DartFileBasedSource implements Source {

  private final @NotNull Project myProject;
  private final @NotNull VirtualFile myFile;
  private final @NotNull UriKind myUriKind;
  private long myModificationStampWhenFileContentWasRead = -1;

  private DartFileBasedSource(final @NotNull Project project, final @NotNull VirtualFile file, final @NotNull UriKind uriKind) {
    myProject = project;
    myFile = file;
    myUriKind = uriKind;
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

  public TimestampedData<CharSequence> getContents() throws Exception {
    final Pair<CharSequence, Long> contentsAndTimestamp = loadFile(myFile);
    myModificationStampWhenFileContentWasRead = contentsAndTimestamp.second;
    return new TimestampedData<CharSequence>(contentsAndTimestamp.second, contentsAndTimestamp.first);
  }

  @Override
  public void getContentsToReceiver(final ContentReceiver receiver) throws Exception {
    final Pair<CharSequence, Long> contentsAndTimestamp = loadFile(myFile);
    myModificationStampWhenFileContentWasRead = contentsAndTimestamp.second;
    receiver.accept(contentsAndTimestamp.first, contentsAndTimestamp.second);
  }

  @Override
  public String getEncoding() {
    return myUriKind.getEncoding() + myFile.getUrl();
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

  @NotNull
  @Override
  public UriKind getUriKind() {
    return myUriKind;
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

  private static Pair<CharSequence, Long> loadFile(final VirtualFile file) throws Exception {
    final Ref<CharSequence> contentsRef = Ref.create();
    final Ref<Long> timestampRef = Ref.create();
    final Exception exception = ApplicationManager.getApplication().runReadAction(new NullableComputable<Exception>() {
      @Nullable
      public Exception compute() {
        final Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(file);
        if (cachedDocument != null) {
          contentsRef.set(cachedDocument.getCharsSequence());
          timestampRef.set(cachedDocument.getModificationStamp());
        }
        else {
          try {
            contentsRef.set(VfsUtilCore.loadText(file));
            timestampRef.set(file.getModificationStamp());
          }
          catch (IOException e) {
            return e;
          }
        }
        return null;
      }
    });

    if (exception != null) throw exception;

    return Pair.create(contentsRef.get(), timestampRef.get());
  }

  public static DartFileBasedSource getSource(final @NotNull Project project, final @NotNull VirtualFile file) {
    return DartAnalyzerService.getInstance(project).getOrCreateSource(file, new Function<VirtualFile, DartFileBasedSource>() {
      public DartFileBasedSource fun(final VirtualFile file) {
        return new DartFileBasedSource(project, file, getUriKind(project, file));
      }
    });
  }

  private static UriKind getUriKind(final Project project, final VirtualFile file) {
    final VirtualFile packagesFolder = PubspecYamlUtil.getDartPackagesFolder(project, file);

    if (packagesFolder != null && VfsUtilCore.isAncestor(packagesFolder, file, true)) {
      return UriKind.PACKAGE_URI;
    }
    else {
      final VirtualFile pubspecYamlFile = PubspecYamlUtil.getPubspecYamlFile(project, file);
      final VirtualFile lib = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild("lib");
      final VirtualFile libFolder = lib != null && lib.isDirectory() ? lib : null;
      return libFolder != null && VfsUtilCore.isAncestor(libFolder, file, true) ? UriKind.PACKAGE_URI : UriKind.FILE_URI;
    }
  }
}
