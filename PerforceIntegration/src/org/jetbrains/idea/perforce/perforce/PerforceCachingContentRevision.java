package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.LastUnchangedContentTracker;
import com.intellij.openapi.vfs.InvalidVirtualFileAccessException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileAttribute;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceBinaryContentRevision;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public final class PerforceCachingContentRevision extends PerforceContentRevision {
  private final FilePath myCurrentPath;   // for renames - path after rename
  private final boolean myOffline;
  private static final Logger LOG = Logger.getInstance(PerforceCachingContentRevision.class);

  private static final FileAttribute PERFORCE_CONTENT_ATTRIBUTE = new FileAttribute("p4.content", 3, false);

  private PerforceCachingContentRevision(final Project project, final FilePath path, final FilePath currentPath, final long revision, boolean offline) {
    super(project, path, revision);
    myCurrentPath = currentPath;
    myOffline = offline;
  }

  @Override
  protected byte @NotNull [] loadContent() throws VcsException {
    assert myFilePath != null;
    if (LOG.isDebugEnabled()) {
      LOG.debug("loadContent: " + myCurrentPath);
    }
    VirtualFile vFile = myCurrentPath.getVirtualFile();
    if (vFile == null) return super.loadContent();
    byte[] content = null;
    try {
      content = loadCachedContent(vFile);
    }
    catch (InvalidVirtualFileAccessException e) {
      throw new VcsException(PerforceBundle.message("error.file.has.been.deleted", vFile.getPath()));
    }
    catch (IOException ignore) {
    }
    if (content == null) {
      if (!PerforceSettings.getSettings(myProject).ENABLED) throw new VcsException(
        PerforceBundle.message("error.can.t.load.content.perforce.is.offline"));

      content = super.loadContent();
      try {
        saveCachedContent(vFile, myRevision, content);
      }
      catch (InvalidVirtualFileAccessException e) {
        throw new VcsException(PerforceBundle.message("error.file.has.been.deleted", vFile.getPath()));
      }
      catch (IOException e) {
        LOG.error(e);
      }
    }
    return content;
  }

  private byte @Nullable [] loadCachedContent(final VirtualFile vFile) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("loadCachedContent: " + vFile + ", offline=" + myOffline);
    }
    if (myOffline) {
      final byte[] content = LastUnchangedContentTracker.getLastUnchangedContent(vFile);
      if (content != null) {
        return content;
      }
    }

    try (DataInputStream stream = PERFORCE_CONTENT_ATTRIBUTE.readAttribute(vFile)) {
      if (stream == null) return null;

      long cachedRevision = stream.readLong();
      if (cachedRevision != myRevision) return null;

      return FileUtil.loadBytes(stream, stream.readInt());
    }
  }

  private static void saveCachedContent(VirtualFile vFile, long revision, byte @NotNull [] content) throws IOException {
    try (DataOutputStream stream = PERFORCE_CONTENT_ATTRIBUTE.writeAttribute(vFile)) {
      stream.writeLong(revision);
      stream.writeInt(content.length);
      stream.write(content);
    }
  }

  public static void removeCachedContent(@NotNull VirtualFile vFile) {
    try {
      saveCachedContent(vFile, -1, ArrayUtilRt.EMPTY_BYTE_ARRAY);
    }
    catch (IOException e) {
      LOG.info(e);
    }
  }

  public static ContentRevision create(final Project project, final FilePath path, final long haveRevision) {
    if (path.getFileType().isBinary()) {
      return new PerforceBinaryContentRevision(project, path, haveRevision);
    }
    return new PerforceCachingContentRevision(project, path, path, haveRevision, false);
  }

  public static ContentRevision createOffline(final Project project,
                                              final FilePath path,
                                              final FilePath currentPath) {
    if (path.getFileType().isBinary()) {
      return new PerforceBinaryContentRevision(project, path, -1);
    }
    return new PerforceCachingContentRevision(project, path, currentPath, -1, true);
  }
}
