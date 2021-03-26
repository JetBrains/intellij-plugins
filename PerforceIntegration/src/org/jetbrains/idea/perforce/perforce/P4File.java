/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceBaseInfoWorker;
import org.jetbrains.idea.perforce.application.PerforceClientRootsChecker;
import org.jetbrains.idea.perforce.application.PerforceVcs;

import java.io.File;

public final class P4File {

  //
  // constructor data
  //
  private final VirtualFile myVFile;
  private final VirtualFile myParentDirOfDeleted;
  private final String myName;

  private P4File(String name, VirtualFile vFile, VirtualFile parentDirOfDeleted) {
    myParentDirOfDeleted = parentDirOfDeleted;
    myVFile = vFile;
    myName = name;
  }

  //
  // cached values
  //
  private FStat myFstat = null;

  private String myLocalPath = null;
  @NonNls private static final String VF_CACHE = "P4_VF_CACHE";
  public static final Key<P4File> KEY = new Key<>(VF_CACHE);

  @NotNull
  public static P4File createInefficientFromLocalPath(final String localPath) {
    final P4File p4File = new P4File(null, null, null);
    p4File.myLocalPath = localPath;
    return p4File;
  }
  @NotNull
  public static P4File createInefficientFromLocalPath(final String localPath,
                                                      String name,
                                                      VirtualFile vFile,
                                                      VirtualFile parentDirOfDeleted) {
    final P4File p4File = new P4File(name, vFile, parentDirOfDeleted);
    p4File.myLocalPath = localPath;
    return p4File;
  }

  @NotNull
  public static P4File create(@NotNull final VirtualFile vFile) {
    final P4File userData = vFile.getUserData(KEY);
    final P4File p4File;
    if (userData != null) {
      p4File = userData;
    }
    else {
      p4File = new P4File(null, vFile, null);
      p4File.myVFile.putUserData(KEY, p4File);
    }
    return p4File;
  }
  @NotNull
  public static P4File create(@NotNull final FilePath filePath) {
    VirtualFile virtualFile = filePath.getVirtualFile();
    if (virtualFile != null) {
      return create(virtualFile);
    }
    else {
      return createInefficientFromLocalPath(filePath.getPath(), filePath.getName(), null, filePath.getVirtualFileParent());
    }
  }
  @NotNull
  public static P4File create(@NotNull final File file) {
    VirtualFile virtualFile = findVirtualFile(file);
    if (virtualFile != null) {
      return create(virtualFile);
    }
    else {
      return createInefficientFromLocalPath(file.getAbsolutePath(), file.getName(), null, findVirtualFile(file.getParentFile()));
    }
  }

  private static VirtualFile findVirtualFile(@NotNull final File file) {
    return ReadAction.compute(() -> LocalFileSystem.getInstance().findFileByIoFile(file));
  }

  public String getEscapedPath() {
    return escapeWildcards(getLocalPath());
  }

  public String getRecursivePath() {
    String filePath = getEscapedPath();
    if (isDirectory()) {
      return filePath + "/...";
    }
    return filePath;
  }

  @Contract(pure = true)
  public static String escapeWildcards(String path) {
    path = path.replace("%", "%25");
    path = path.replace("@", "%40");
    path = path.replace("#", "%23");
    return path;
  }

  @Contract(pure = true)
  public static String unescapeWildcards(String path) {
    path = path.replace("%23", "#");
    path = path.replace("%40", "@");
    path = path.replace("%25", "%");
    return path;
  }

  public void invalidateFstat() {
    myFstat = null;

    if (myVFile != null && myVFile.isValid()) {
      invalidateFstat(myVFile);
    }
  }

  public static void invalidateFstat(final Project project) {
    ApplicationManager.getApplication().runReadAction(() -> {
      final PerforceVcs perforceVcs = PerforceVcs.getInstance(project);
      for (VirtualFile contentRoot : ProjectLevelVcsManager.getInstance(project).getRootsUnderVcsWithoutFiltering(perforceVcs)) {
        invalidateFstat(contentRoot);
      }
    });
  }

  public static void invalidateFstat(final VirtualFile file) {
    ApplicationManager.getApplication().runReadAction(() -> invalidateFStatImpl(file));
  }

  private static void invalidateFStatImpl(final VirtualFile file) {
    if (file.getFileSystem() == LocalFileSystem.getInstance()) {
      VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor<Void>(VirtualFileVisitor.NO_FOLLOW_SYMLINKS) {
        @Override
        public Iterable<VirtualFile> getChildrenIterable(@NotNull VirtualFile file) {
          return ((NewVirtualFile)file).getCachedChildren();
        }

        @Override
        public boolean visitFile(@NotNull VirtualFile file) {
          file.putUserData(KEY, null);
          return true;
        }
      });
    }
  }

  public void clearCache() {
    myFstat = null;
    if (myVFile != null) {
      myLocalPath = null;
    }
  }

  public FStat getFstat(final Project project, final boolean forceNew) throws VcsException {
    final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
    final PerforceRunner perforceRunner = PerforceRunner.getInstance(project);
    return getFstat(project.getService(PerforceBaseInfoWorker.class), changeListManager, perforceRunner, forceNew);
  }

  public FStat getFstat(final PerforceBaseInfoWorker baseInfoWorker, final ChangeListManager changeListManager, final PerforceRunner perforceRunner,
                        final boolean forceNew) throws VcsException {
    final long lastValidTime = baseInfoWorker.getLastValidTime();
    if (myFstat == null || forceNew ||
        myFstat.statTime < lastValidTime
        || lastValidTime == -1) {
      if (myVFile != null && changeListManager.isUnversioned(myVFile) && !forceNew) {
        myFstat = new FStat();
        myFstat.status = FStat.STATUS_NOT_ADDED;
      }
      else {
        // 7-8
        myFstat = perforceRunner.getProxy().fstat(this);
      }
    }
    return myFstat;
  }

  public File getLocalFile() {
    return new File(getLocalPath());
  }

  @NotNull
  public String getLocalPath() {
    if (myLocalPath == null) {
      ApplicationManager.getApplication().runReadAction(() -> {
        if (myVFile != null) {
          myLocalPath = myVFile.getPath();
        }
        else if (myParentDirOfDeleted != null) {
          myLocalPath = myParentDirOfDeleted.getPath() + System.lineSeparator() + myName;
        }
        else {
          throw new RuntimeException(PerforceBundle.message("exception.text.cannot.figure.out.local.path"));
        }
      });
    }
    return myLocalPath;
  }

  @NonNls
  public String toString() {
    return "org.jetbrains.idea.perforce.perforce.P4File{'" + getLocalPath() + "'}";
  }

  public boolean isDirectory() {
    if (myVFile != null) {
      return myVFile.isDirectory();
    }
    return PerforceClientRootsChecker.isDirectory(getLocalFile());
  }

  public boolean isCaseSensitive() {
    return myVFile == null ? myParentDirOfDeleted == null ? SystemInfo.isFileSystemCaseSensitive : myParentDirOfDeleted.isCaseSensitive() : myVFile.isCaseSensitive();
  }
}
