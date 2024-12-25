// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangelistBuilder;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceAbstractChange;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.ResolvedFile;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.io.File;
import java.util.*;

public class OpenedResultProcessor {
  public static final Key<Boolean> BRANCHED_FILE = Key.create("Perforce.branched.file");

  private final ChangelistBuilder myBuilder;
  private final P4Connection myConnection;
  private final ChangeCreator myChangeCreator;
  private final LocalPathsSet myResolvedWithConflicts;
  private final ResolvedFilesWrapper myResolvedFiles;
  private final PerforceChangeListCalculator myChangelistCalculator;
  private final LocalFileSystem myLocalFileSystem;

  public OpenedResultProcessor(final @NotNull P4Connection connection, final ChangeCreator changeCreator, final ChangelistBuilder builder,
                               final LocalPathsSet resolvedWithConflicts, final ResolvedFilesWrapper resolvedFiles,
                               final PerforceChangeListCalculator changelistCalculator) {
    myConnection = connection;
    myChangeCreator = changeCreator;
    myBuilder = builder;
    myResolvedWithConflicts = resolvedWithConflicts;
    myResolvedFiles = resolvedFiles;
    myChangelistCalculator = changelistCalculator;
    myLocalFileSystem = LocalFileSystem.getInstance();
  }

  private abstract class MyAbstractProcessor {

    protected abstract boolean matches(final PerforceChange perforceChange);
    protected abstract void process(final PerforceChange perforceChange, final ChangeList changeList);

    public void processList(final Collection<PerforceChange> perforceChanges) {
      for (Iterator<PerforceChange> iterator = perforceChanges.iterator(); iterator.hasNext();) {
        final PerforceChange change = iterator.next();
        if (change.getFile() == null) continue; //?
        if (matches(change)) {
          ChangeList changeList = myChangelistCalculator.convert(change);
          iterator.remove();
          process(change, changeList);
        }
      }
    }
  }

  private class MyLocallyDeletedProcessor extends MyAbstractProcessor {
    @Override
    protected boolean matches(PerforceChange perforceChange) {
      final File file = perforceChange.getFile();
      final int type = perforceChange.getType();
      return (file != null) && (type != PerforceAbstractChange.DELETE) && (type != PerforceAbstractChange.MOVE_DELETE) &&
             myLocalFileSystem.findFileByIoFile(file) == null && (! file.exists());
    }

    @Override
    protected void process(PerforceChange change, final ChangeList changeList) {
      File file = change.getFile();
      if (file == null) return;
      if (change.getType() == PerforceAbstractChange.ADD || change.getType() == PerforceAbstractChange.MOVE_ADD) {
        myBuilder.processChangeInList(myChangeCreator.createAddedFileChange(toCanonicalFilePath(file), true),
                                      changeList, PerforceVcs.getKey());
      }
      myBuilder.processLocallyDeletedFile(VcsUtil.getFilePath(file, false));
    }
  }

  // post - applied
  private class MyDeletedProcessor extends MyAbstractProcessor {
    private final Map<String, PerforceChange> myDeleted = new HashMap<>();

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      final int type = perforceChange.getType();
      return (type == PerforceAbstractChange.DELETE) || (type == PerforceAbstractChange.MOVE_DELETE);
    }

    @Override
    protected void process(PerforceChange perforceChange, final ChangeList changeList) {
      if (myResolvedFiles.getDepotToFiles().containsKey(perforceChange.getDepotPath())) {
        myDeleted.put(perforceChange.getDepotPath(), perforceChange);
        return;
      }
      File file = perforceChange.getFile();
      if (file == null) return;
      myBuilder.processChangeInList(myChangeCreator.createDeletedFileChange(file, perforceChange.getRevision(), false),
                                    changeList, PerforceVcs.getKey());
    }

    public void postProcessAll() {
      for (PerforceChange perforceChange : myDeleted.values()) {
        final ChangeList changeList = myChangelistCalculator.convert(perforceChange);
        File file = perforceChange.getFile();
        if (file == null) return;
        myBuilder.processChangeInList(myChangeCreator.createDeletedFileChange(file, perforceChange.getRevision(), false),
                                      changeList, PerforceVcs.getKey());
      }
    }

    public PerforceChange removePeer(final String depotPath) {
      return myDeleted.remove(depotPath);
    }
  }

  private final class MyAddedProcessor extends MyAbstractProcessor {
    private final MyDeletedProcessor myDeletedProcessor;

    private MyAddedProcessor(MyDeletedProcessor deletedProcessor) {
      myDeletedProcessor = deletedProcessor;
    }

    @Override
    protected boolean matches(PerforceChange perforceChange) {
      final int type = perforceChange.getType();
      // todo????
      return (type == PerforceAbstractChange.ADD) || (type == PerforceAbstractChange.MOVE_ADD);
    }

    @Override
    protected void process(PerforceChange perforceChange, ChangeList changeList) {
      final File file = perforceChange.getFile();
      final ResolvedFile resolvedPeer = file == null ? null : myResolvedFiles.getLocalToFiles().get(file);
      if (resolvedPeer != null) {
        final String operation = resolvedPeer.getOperation();
        if ((ResolvedFile.OPERATION_BRANCH.equals(operation)) || ResolvedFile.OPERATION_MOVE.equals(operation)) {
          final PerforceChange deletedChange = myDeletedProcessor.removePeer(resolvedPeer.getDepotPath());
          if (deletedChange != null) {
            // report move
            myBuilder.processChangeInList(myChangeCreator.createRenameChange(myConnection, resolvedPeer, toCanonicalFilePath(file)),
                                          changeList, PerforceVcs.getKey());
            return;
          }
        }
      }
      myBuilder.processChangeInList(myChangeCreator.createAddedFileChange(toCanonicalFilePath(file), myResolvedWithConflicts.contains(file)),
                                    changeList, PerforceVcs.getKey());
    }
  }

  /**
   * {@link PerforceReadOnlyFileStateManager#isKnownToPerforce} queries the current change list updater state,
   * which stores file paths case-sensitively ({@link com.intellij.openapi.vcs.changes.ChangeListsIndexes} internals).
   * So, to ensure a virtual file can be found in that structure later, we should use the VFS version of a (case-insensitive) path.
   */
  private static @NotNull FilePath toCanonicalFilePath(File file) {
    if (!SystemInfo.isFileSystemCaseSensitive) {
      VirtualFile vFile = VfsUtil.findFileByIoFile(file, false);
      if (vFile != null) return VcsUtil.getFilePath(vFile);
    }
    return VcsUtil.getFilePath(file);
  }

  // todo require additional work -> with integrated command
  private class MyBranchedProcessor extends MyAbstractProcessor {
    @Override
    protected boolean matches(PerforceChange perforceChange) {
      return perforceChange.getType() == PerforceAbstractChange.BRANCH;
    }

    @Override
    protected void process(PerforceChange perforceChange, ChangeList changeList) {
      final File file = perforceChange.getFile();
      if (file == null) return;

      FilePath filePath = toCanonicalFilePath(file);
      myBuilder.processChangeInList(myChangeCreator.createAddedFileChange(filePath, myResolvedWithConflicts.contains(file)),
                                    changeList, PerforceVcs.getKey());
      VirtualFile vf = filePath.getVirtualFile();
      if (vf == null) {
        vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath.getPath());
      }
      if (vf != null) {
        vf.putUserData(BRANCHED_FILE, Boolean.TRUE);
      }
    }
  }

  private class MyEditedProcessor extends MyAbstractProcessor {
    @Override
    protected boolean matches(PerforceChange perforceChange) {
      return (perforceChange.getRevision() > 0) &&
        ((perforceChange.getType() == PerforceAbstractChange.EDIT) || (perforceChange.getType() == PerforceAbstractChange.INTEGRATE));
    }

    @Override
    protected void process(PerforceChange perforceChange, ChangeList changeList) {
      final File file = perforceChange.getFile();
      if (file == null) return;
      myBuilder.processChangeInList(myChangeCreator.createEditedFileChange(toCanonicalFilePath(file), perforceChange.getRevision(),
                                                                           myResolvedWithConflicts.contains(file)), changeList, PerforceVcs.getKey());
    }
  }

  public void process(final Collection<PerforceChange> p4changes) {
    clearBranchedFlag(p4changes);

    // todo somehow static???
    final List<MyAbstractProcessor> processors = new LinkedList<>();
    processors.add(new MyLocallyDeletedProcessor());
    final MyDeletedProcessor deletedProcessor = new MyDeletedProcessor();
    processors.add(deletedProcessor);
    final MyAddedProcessor addedProcessor = new MyAddedProcessor(deletedProcessor);
    processors.add(addedProcessor);
    processors.add(new MyBranchedProcessor());
    processors.add(new MyEditedProcessor());

    for (MyAbstractProcessor processor : processors) {
      processor.processList(p4changes);
    }

    deletedProcessor.postProcessAll();
  }

  private void clearBranchedFlag(Collection<PerforceChange> p4changes) {
    for (PerforceChange change : p4changes) {
      final File ioFile = change.getFile();
      if (ioFile == null) continue;

      VirtualFile file = myLocalFileSystem.findFileByIoFile(ioFile);
      if (file == null && ioFile.exists()) {
        file = myLocalFileSystem.refreshAndFindFileByIoFile(ioFile);
      }
      if (file != null) {
        file.putUserData(BRANCHED_FILE, null);
      }
    }
  }
}
