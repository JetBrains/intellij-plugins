package org.jetbrains.idea.perforce;

import com.intellij.execution.process.ProcessOutput;
import com.intellij.idea.Bombed;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.IoTestUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesCache;
import com.intellij.openapi.vcs.merge.MergeData;
import com.intellij.openapi.vfs.*;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.vcs.DuringChangeListManagerUpdateTestScheme;
import com.intellij.util.CollectConsumer;
import com.intellij.util.ExceptionUtil;
import org.jetbrains.idea.perforce.application.PerforceCommittedChangesProvider;
import org.jetbrains.idea.perforce.application.PerforceRepositoryLocation;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.changesBrowser.PerforceChangeBrowserSettings;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.AbstractP4Connection;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.intellij.testFramework.UsefulTestCase.assertEquals;
import static com.intellij.testFramework.UsefulTestCase.assertNull;
import static com.intellij.testFramework.UsefulTestCase.fail;
import static com.intellij.testFramework.UsefulTestCase.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PerforceChangeProviderTest extends PerforceTestCase {
  private DuringChangeListManagerUpdateTestScheme myScheme;

  @Override
  public void before() throws Exception {
    super.before();

    // similar to svn tests to be added
    myScheme = new DuringChangeListManagerUpdateTestScheme(myProject, myTempDirFixture.getTempDirPath());

    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
  }

  @Override
  public void after() throws Exception {
    myScheme = null;
    super.after();
  }

  private void doTestVariants(final Runnable runnable, final boolean insideUpdate) {
    if (insideUpdate) {
      myScheme.doTest(runnable);
    } else {
      runnable.run();
    }
  }

  @Test
  public void testAddedFile() {
    final VirtualFile file = createFileInCommand("a.txt", "");

    final ChangeListManagerImpl clManager = getChangeListManager();
    clManager.ensureUpToDate();

    assertOrderedEquals(clManager.getUnversionedFiles(), file);
    assertTrue(clManager.isUnversioned(file));
    assertEmpty(assertOneElement(clManager.getChangeLists()).getChanges());

    addFile("a.txt");

    refreshChanges();

    Assert.assertEquals(Change.Type.NEW, getSingleChange().getType());

    assertEmpty(clManager.getUnversionedFiles());
    assertFalse(clManager.isUnversioned(file));
  }

  @Test
  public void testListAddedExternally() throws Throwable {
    testListAddedExternallyImpl(false);
  }

  @Test
  public void testListAddedExternallyDuringUpdate() throws Throwable {
    testListAddedExternallyImpl(true);
  }

  private void testListAddedExternallyImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });
    assertNotNull(refA.get());
    assertNotNull(refB.get());

    addFile("a.txt");
    addFile("b.txt");

    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();

    final String listName = "test new list";
    final Ref<Long> numberRef = new Ref<>();
    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        numberRef.set(createChangeList(listName, Collections.singletonList("//depot/a.txt")));
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }
    assert !numberRef.isNull();
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(listName, clManager, refA.get());
  }

  @Test
  public void testPerforceCreatesList() throws Throwable {
    testPerforceCreatesListImpl(false);
  }

  @Test
  public void testPerforceCreatesListInUpdate() throws Throwable {
    testPerforceCreatesListImpl(true);
  }

  private void testPerforceCreatesListImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });
    assertNotNull(refA.get());
    assertNotNull(refB.get());

    addFile("a.txt");
    addFile("b.txt");

    final List<Long> before = getLists();

    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();

    final String name = "added in IDEA";
    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        LocalChangeList list = clManager.addChangeList(name, null);
        clManager.moveChangesTo(list, clManager.getChange(refA.get()));
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }

    getChangeListManager().waitUntilRefreshed();

    final List<Long> after = getLists();

    assert before.size() == 0;
    assert after.size() == 1;

    final List<String> files = getFilesInList(after.get(0));
    assert files.size() == 1;
    assert files.get(0).startsWith("//depot/a.txt") : files.get(0);
  }

  @Test
  public void testCommentFromIdeaGoesIntoNative() throws Throwable {
    testCommentFromIdeaGoesInfoNativeImpl(false);
  }

  @Test
  public void testCommentFromIdeaGoesIntoNativeInUpdate() throws Throwable {
    testCommentFromIdeaGoesInfoNativeImpl(true);
  }

  private void testCommentFromIdeaGoesInfoNativeImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });
    assertNotNull(refA.get());
    assertNotNull(refB.get());

    addFile("a.txt");
    addFile("b.txt");

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";

    long listNumber = createChangeList(nativeComment, Collections.singletonList("//depot/a.txt"));

    checkNativeList(listNumber, nativeComment);

    // sync
    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager, refA.get());
    final LocalChangeList list = clManager.findChangeList("idea name...");
    checkListComment(list, nativeComment);

    final String newName = "new name";
    final String newComment = "new name\n<--->\n>=***=<";

    // edit in idea
    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        clManager.editName("idea name...", newName);
        clManager.editComment(newName, newComment);
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }

    getChangeListManager().waitUntilRefreshed();

    // check native description
    checkNativeList(listNumber, newComment);
  }

  @Test
  public void testNativeCommentGoesIntoIdea() throws Throwable {
    testNativeCommentGoesInfoIdeaImpl(false);
  }

  @Test
  public void testNativeCommentGoesIntoIdeaInUpdate() throws Throwable {
    testNativeCommentGoesInfoIdeaImpl(true);
  }

  private void testNativeCommentGoesInfoIdeaImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });
    assertNotNull(refA.get());
    assertNotNull(refB.get());

    addFile("a.txt");
    addFile("b.txt");

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";

    final long listNumber = createChangeList(nativeComment, Collections.singletonList("//depot/a.txt"));

    checkNativeList(listNumber, nativeComment);

    // sync
    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager, refA.get());

    final String newComment = "new first line\nnew second line";
    final String newName = "new first line...";
    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        editListDescription(listNumber, newComment);
        checkNativeList(listNumber, newComment);
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }
    checkNativeList(listNumber, newComment);

    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(newName, clManager, refA.get());
    final LocalChangeList changeList = clManager.findChangeList(newName);
    checkListComment(changeList, newComment);
  }

  @Test
  public void testNativeRename() {
    final VirtualFile file = createFileInCommand("a.txt", "");
    addFile("a.txt");

    submitList(createChangeList("name", Collections.singletonList("//depot/a.txt")));

    verify(runP4WithClient("edit", new File(file.getPath()).getAbsolutePath()));
    verify(runP4WithClient("move", new File(file.getPath()).getAbsolutePath(),
                           new File(file.getParent().getPath(), "b.txt").getAbsolutePath()));

    refreshVfs();

    // sync
    final ChangeListManager clManager = getChangeListManager();
    refreshChanges();

    final VirtualFile renamed = myWorkingCopyDir.findFileByRelativePath("b.txt");
    assertNotNull(renamed);
    assertFalse(file.isValid());

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(clManager.getDefaultListName(), clManager, renamed);
    assertFalse(clManager.isUnversioned(renamed));

    final LocalChangeList list = assertOneElement(clManager.getChangeLists());
    Change change = assertOneElement(list.getChanges());
    Assert.assertEquals(Change.Type.MOVED, change.getType());
  }

  @Test
  public void testNativeMoveGoesIntoIdea() throws Throwable {
    testNativeMoveGoesIntoIdeaImpl(false);
  }

  @Test
  public void testNativeMoveGoesIntoIdeaInUpdate() throws Throwable {
    testNativeMoveGoesIntoIdeaImpl(true);
  }

  private void testNativeMoveGoesIntoIdeaImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";

    long listNumber = createChangeList(nativeComment, Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNativeList(listNumber, nativeComment);

    // sync
    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager,
                                                                refA.get(), refB.get());
    final LocalChangeList list = clManager.findChangeList("idea name...");
    checkListComment(list, nativeComment);

    final String anotherName = "anotherName...";
    final String anotherComment = "anotherName\nanotherComment";

    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        final long newListNumber = createChangeList(anotherComment, Collections.emptyList());
        checkNativeList(newListNumber, anotherComment);
        moveToChangelist(newListNumber, "//depot/a.txt");
        final List<String> filesInList = getFilesInList(newListNumber);
        assert filesInList.size() == 1;
        assert filesInList.get(0).startsWith("//depot/a.txt");
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }

    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(anotherName, clManager, refA.get());
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager, refB.get());
    final LocalChangeList listOld = clManager.findChangeList("idea name...");
    checkListComment(listOld, nativeComment);
    final LocalChangeList listNew = clManager.findChangeList(anotherName);
    checkListComment(listNew, anotherComment);
  }

  @Test
  public void testIdeaMoveGoesIntoNative() throws Throwable {
    testIdeaMoveGoesIntoNativeImpl(false);
  }

  @Test
  public void testIdeaMoveGoesIntoNativeInUpdate() throws Throwable {
    testIdeaMoveGoesIntoNativeImpl(true);
  }

  private void testIdeaMoveGoesIntoNativeImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";

    long listNumber = createChangeList(nativeComment, Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNativeList(listNumber, nativeComment);

    // sync
    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager,
                                                                refA.get(), refB.get());
    final LocalChangeList list = clManager.findChangeList("idea name...");
    checkListComment(list, nativeComment);

    final String newName = "newName";
    final String newComment = "newName\nnewComment";
    // idea move
    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        final LocalChangeList newChangeList = clManager.addChangeList(newName, newComment);
        clManager.moveChangesTo(newChangeList, clManager.getChange(refA.get()));
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }
    clManager.ensureUpToDate();
    final LocalChangeList newChangeList = clManager.findChangeList(newName);
    assert newChangeList != null;
    assert newChangeList.getChanges().size() == 1;

    final List<Long> listNumbers = getLists();
    listNumbers.remove(listNumber);
    assert !listNumbers.isEmpty();
    final Long newNumber = listNumbers.get(0);

    checkNativeList(newNumber, newComment);

    final List<String> files = getFilesInList(newNumber);
    assert files.size() == 1;
    assert files.get(0).startsWith("//depot/a.txt") : files.get(0);
  }

  @Test
  public void testIdeaDeleteGoesIntoNative() throws Throwable {
    testIdeaDeleteGoesIntoNativeImpl(false);
  }

  @Test
  public void testIdeaDeleteGoesIntoNativeInUpdate() throws Throwable {
    testIdeaDeleteGoesIntoNativeImpl(true);
  }

  private void testIdeaDeleteGoesIntoNativeImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";

    long listNumber = createChangeList(nativeComment, Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNativeList(listNumber, nativeComment);

    // sync
    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager,
                                                                refA.get(), refB.get());
    final LocalChangeList list = clManager.findChangeList("idea name...");
    checkListComment(list, nativeComment);

    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        clManager.removeChangeList(list.getName());
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }

    getChangeListManager().waitUntilRefreshed();

    final List<Long> listNumbers = getLists();
    assert listNumbers.isEmpty();

    final List<String> inDefault = getFilesInDefaultChangelist();
    assert inDefault.size() == 2;
    assert inDefault.get(0).startsWith("//depot/a.txt") || inDefault.get(1).startsWith("//depot/a.txt");
    assert inDefault.get(0).startsWith("//depot/b.txt") || inDefault.get(1).startsWith("//depot/b.txt");
  }

  @Test
  public void testNativeDeleteGoesIntoIdea() throws Throwable {
    testNativeDeleteGoesIntoIdeaImpl(false);
  }

  @Test
  public void testNativeDeleteGoesIntoIdeaInUpdate() throws Throwable {
    testNativeDeleteGoesIntoIdeaImpl(true);
  }

  private void testNativeDeleteGoesIntoIdeaImpl(final boolean inUpdate) throws Throwable {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");

    final String nativeComment = "idea name\nsecond comment line\nthird comment line";

    final long listNumber = createChangeList(nativeComment, Arrays.asList("//depot/a.txt", "//depot/b.txt"));

    checkNativeList(listNumber, nativeComment);

    // sync
    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("idea name...", clManager,
                                                                refA.get(), refB.get());
    final LocalChangeList list = clManager.findChangeList("idea name...");
    checkListComment(list, nativeComment);

    final Ref<Throwable> refThrowable = new Ref<>();
    doTestVariants(() -> {
      try {
        moveToDefaultChangelist("//depot/a.txt");
        moveToDefaultChangelist("//depot/b.txt");
        deleteList(listNumber);
      }
      catch (Throwable e) {
        refThrowable.set(e);
      }
    }, inUpdate);
    if (!refThrowable.isNull()) {
      throw refThrowable.get();
    }

    refreshChanges();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(clManager.getDefaultListName(), clManager,
                                                                refA.get(), refB.get());
  }

  private static final class SubTree {
    private VirtualFile myOuterDir;
    private VirtualFile myOuterFile;
    private VirtualFile myRootDir;
    private VirtualFile myInnerFile;
    private VirtualFile myNonVersionedUpper;

    private SubTree(final VirtualFile base) throws IOException {
      WriteAction.runAndWait(() -> {
        myOuterDir = base.createChildDirectory(this, "outer");
        myOuterFile = myOuterDir.createChildData(this, "outer.txt");
        myRootDir = myOuterDir.createChildDirectory(this, "root");
        myInnerFile = myRootDir.createChildData(this, "inner.txt");
        myNonVersionedUpper = base.createChildData(this, "nonVersioned.txt");
      });
    }

    public List<String> getForAdds() {
      return Arrays.asList("outer/outer.txt", "outer/root/inner.txt");
    }
  }

  @Test
  public void testPerforceVcsRootAbove() throws IOException {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);

    final SubTree subTree = new SubTree(myWorkingCopyDir);

    final List<String> paths = subTree.getForAdds();
    final String[] pathsForSubmit = new String[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      final String path = paths.get(i);
      addFile(path);
      pathsForSubmit[i] = "//depot/" + path;
    }

    submitFile(pathsForSubmit);

    runP4WithClient("edit", new File(subTree.myOuterFile.getPath()).getAbsolutePath());
    runP4WithClient("edit", new File(subTree.myInnerFile.getPath()).getAbsolutePath());

    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {subTree.myOuterFile, subTree.myInnerFile},
      clManager.getDefaultListName(), clManager);
  }

  @Test
  public void testFakeScopeDontBreakTheView() throws IOException {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    enableSilentOperation(VcsConfiguration.StandardConfirmation.REMOVE);

    final SubTree subTree = new SubTree(myWorkingCopyDir);

    final List<String> paths = subTree.getForAdds();
    final String[] pathsForSubmit = new String[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      final String path = paths.get(i);
      addFile(path);
      pathsForSubmit[i] = "//depot/" + path;
    }

    submitFile(pathsForSubmit);

    runP4WithClient("edit", new File(subTree.myOuterFile.getPath()).getAbsolutePath());
    runP4WithClient("edit", new File(subTree.myInnerFile.getPath()).getAbsolutePath());

    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {subTree.myOuterFile, subTree.myInnerFile},
      clManager.getDefaultListName(), clManager);

    VcsDirtyScopeManager.getInstance(myProject).fileDirty(subTree.myNonVersionedUpper);
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(new VirtualFile[] {subTree.myOuterFile, subTree.myInnerFile},
      clManager.getDefaultListName(), clManager);
  }

  @Test
  public void testNotSynchronizedInitially() throws IOException {
    final Ref<VirtualFile> refA = new Ref<>();
    final Ref<VirtualFile> refB = new Ref<>();
    final Ref<VirtualFile> refC = new Ref<>();
    final Ref<VirtualFile> refD = new Ref<>();

    WriteAction.runAndWait(() -> {
      refA.set(myWorkingCopyDir.createChildData(this, "a.txt"));
      refB.set(myWorkingCopyDir.createChildData(this, "b.txt"));
      refC.set(myWorkingCopyDir.createChildData(this, "c.txt"));
      refD.set(myWorkingCopyDir.createChildData(this, "d.txt"));
    });

    addFile("a.txt");
    addFile("b.txt");
    addFile("c.txt");
    addFile("d.txt");

    ChangeListManagerImpl clManager = ChangeListManagerImpl.getInstanceImpl(myProject);
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList(clManager.getDefaultListName(), clManager,
                                                                refA.get(), refB.get(), refC.get(), refD.get());

    final String name1 = "idea1";
    final String name2 = "idea2";
    // do synchronized:
    final LocalChangeList idea1list = clManager.addChangeList(name1, null);
    final LocalChangeList idea2list = clManager.addChangeList(name2, null);

    clManager.moveChangesTo(idea1list, clManager.getChange(refA.get()), clManager.getChange(refB.get()));
    clManager.moveChangesTo(idea2list, clManager.getChange(refC.get()), clManager.getChange(refD.get()));

    getChangeListManager().waitUntilRefreshed();

    final List<Long> lists = getLists();
    assert lists.size() == 2;
    final List<String> files1 = getFilesInList(lists.get(0));
    final List<String> files2 = getFilesInList(lists.get(1));

    assert files1.size() == 2;
    assert files2.size() == 2;

    if (files1.get(0).startsWith("//depot/a.txt") || files1.get(1).startsWith("//depot/a.txt")) {
      assertAandB(files1, "//depot/a.txt", "//depot/b.txt");
      assertAandB(files2, "//depot/c.txt", "//depot/d.txt");
    }
    else {
      assertAandB(files1, "//depot/c.txt", "//depot/d.txt");
      assertAandB(files2, "//depot/a.txt", "//depot/b.txt");
    }

    // do miss - synchronized

    final String p41 = "p41\ncomment";
    final String p42 = "p42\ncomment2";

    final long p41Number = createChangeList(p41, null);
    moveToChangelist(p41Number, "//depot/a.txt");
    moveToChangelist(p41Number, "//depot/c.txt");

    final long p42Number = createChangeList(p42, null);
    moveToChangelist(p42Number, "//depot/b.txt");
    moveToChangelist(p42Number, "//depot/d.txt");

    getChangeListManager().waitUntilRefreshed();

    // now synchronize
    VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
    clManager.ensureUpToDate();

    final LocalChangeList p41Idea = clManager.findChangeList("p41...");
    final LocalChangeList p42Idea = clManager.findChangeList("p42...");

    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("p41...", clManager, refA.get(), refC.get());
    DuringChangeListManagerUpdateTestScheme.checkFilesAreInList("p42...", clManager, refB.get(), refD.get());
    checkListComment(p41Idea, p41);
    checkListComment(p42Idea, p42);
  }

  private static void assertAandB(final List<String> files, final String stringA, final String stringB) {
    if (files.get(0).startsWith(stringA)) {
      assert files.get(1).startsWith(stringB);
    } else {
      assert files.get(0).startsWith(stringB);
      assert files.get(1).startsWith(stringA);
    }
  }

  private void deleteList(final long list) {
    final ProcessOutput result = runP4WithClient("change", "-d", String.valueOf(list));
    verify(result);
  }

  private static void checkListComment(final LocalChangeList list, final String etalon) {
    assert list != null && etalon.trim().equals(list.getComment().trim()) : (list == null ? null : list.getComment().trim());
  }

  @Test public void testDoubleIntegrate() throws Exception {
    VirtualFile file = createFileInCommand(createDirInCommand(myWorkingCopyDir, "main"), "a.txt", "original");
    String main = new File(myClientRoot, "main").getPath() + File.separator;
    String rel = new File(myClientRoot, "rel").getPath() + File.separator;

    verify(runP4WithClient("add", main + "a.txt"));
    submitDefaultList("comment1");

    verify(runP4WithClient("integrate", main + "...", rel + "..."));
    submitDefaultList("copied");

    verify(runP4WithClient("edit", main + "a.txt"));
    editFileInCommand(file, "changed 1");
    submitDefaultList("changed 1");
    verify(runP4WithClient("integrate", main + "...", rel + "..."));

    verify(runP4WithClient("edit", main + "a.txt"));
    editFileInCommand(file, "changed 2");
    verify(runP4WithClient("submit", "-d", "changed 2", main + "a.txt"));
    verify(runP4WithClient("integrate", main + "...", rel + "..."));

    refreshVfs();
    refreshChanges();

    VirtualFile relFile = myWorkingCopyDir.findFileByRelativePath("rel/a.txt");
    PerforceRunner runner = PerforceRunner.getInstance(myProject);
    assertOrderedEquals(runner.getResolvedWithConflicts(getConnection(), myWorkingCopyDir), relFile);

    MergeData data = getMergeData(relFile);
    assertEquals("original", new String(data.ORIGINAL, StandardCharsets.UTF_8));
    assertEquals("changed 2", new String(data.LAST, StandardCharsets.UTF_8));
    assertEquals("original", new String(data.CURRENT, StandardCharsets.UTF_8));
  }

  @Test
  public void testResolveMergeData() throws Exception {
    VirtualFile file = createFileInCommand(createDirInCommand(myWorkingCopyDir, "main"), "a.txt", "original");
    String main = new File(myClientRoot, "main").getPath() + File.separator;
    String rel = new File(myClientRoot, "rel").getPath() + File.separator;

    verify(runP4WithClient("add", main + "a.txt"));
    submitDefaultList("comment1");

    verify(runP4WithClient("integrate", main + "...", rel + "..."));
    submitDefaultList("copied");

    refreshVfs();
    refreshChanges();

    final VirtualFile relFile = myWorkingCopyDir.findFileByRelativePath("rel/a.txt");

    verify(runP4WithClient("edit", main + "a.txt"));
    editFileInCommand(file, "changed 1");
    submitDefaultList("changed 1");

    verify(runP4WithClient("edit", rel + "a.txt"));
    editFileInCommand(relFile, "changed 2");
    submitDefaultList("changed 2");
    verify(runP4WithClient("integrate", main + "...", rel + "..."));

    refreshVfs();
    refreshChanges();

    PerforceRunner runner = PerforceRunner.getInstance(myProject);
    assertOrderedEquals(runner.getResolvedWithConflicts(getConnection(), myWorkingCopyDir), relFile);
    assertEquals(FileStatus.MERGED_WITH_CONFLICTS, FileStatusManager.getInstance(myProject).getStatus(relFile));

    final MergeData data = getMergeData(relFile);
    assertEquals("original", new String(data.ORIGINAL, StandardCharsets.UTF_8));
    assertEquals("changed 1", new String(data.LAST, StandardCharsets.UTF_8));
    assertEquals("changed 2", new String(data.CURRENT, StandardCharsets.UTF_8));
  }

  @Test
  public void testResolveJar() throws Exception {
    byte[] original = new byte[239];
    byte[] changeOriginal = new byte[original.length + 1];
    byte[] changeCopy = new byte[original.length + 2];

    Random random = new Random();
    random.nextBytes(original);
    random.nextBytes(changeOriginal);
    random.nextBytes(changeCopy);

    VirtualFile file = createFileInCommand("a.jar", "");
    setBinaryContent(file, original);
    String main = myClientRoot.getPath() + File.separator + "a.jar";
    String copy = myClientRoot.getPath() + File.separator + "b.jar";

    verify(runP4WithClient("add", main));
    submitDefaultList("comment1");

    verify(runP4WithClient("integrate", main, copy));
    submitDefaultList("copied");

    refreshVfs();
    final VirtualFile relFile = myWorkingCopyDir.findFileByRelativePath("b.jar");
    assertNotNull(relFile);

    verify(runP4WithClient("edit", main));
    setBinaryContent(file, changeOriginal);
    verify(runP4WithClient("edit", copy));
    setBinaryContent(relFile, changeCopy);
    submitDefaultList("changed");

    verify(runP4WithClient("integrate", main, copy));

    refreshVfs();
    refreshChanges();

    PerforceRunner runner = PerforceRunner.getInstance(myProject);
    assertEquals(FileStatus.MERGED_WITH_CONFLICTS, FileStatusManager.getInstance(myProject).getStatus(relFile));
    assertOrderedEquals(runner.getResolvedWithConflicts(getConnection(), myWorkingCopyDir), relFile);

    final MergeData data = getMergeData(relFile);
    assertTrue(Arrays.equals(data.ORIGINAL, original));
    assertTrue(Arrays.equals(data.LAST, changeOriginal));
    assertTrue(Arrays.equals(data.CURRENT, changeCopy));
  }

  private MergeData getMergeData(final VirtualFile relFile) {
    final MergeData[] data = new MergeData[1];
    EdtTestUtil.runInEdtAndWait((() -> {
      try {
        data[0] = PerforceVcs.getInstance(myProject).getMergeProvider().loadRevisions(relFile);
      }
      catch (VcsException e) {
        fail(ExceptionUtil.getThrowableText(e));
      }
    }));
    return data[0];
  }

  @Test
  public void testCreateManyFilesExternally() {
    int n = 100;
    for (int i = 0; i < n; i++) {
      createFileInCommand("a" + i + ".txt", "content");
    }
    refreshVfs();
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());
    assertSize(n, getChangeListManager().getUnversionedFiles());
  }

  @Test
  @Bombed(year = 2021, month = Calendar.AUGUST, day = 20, user = "AMPivovarov")
  public void testTwoRootsWithSameConnection() {
    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1");
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2");
    VirtualFile file1 = createFileInCommand(dir1, "a.txt", "");
    VirtualFile file2 = createFileInCommand(dir2, "b.txt", "");
    createFileInCommand(dir1, TEST_P4CONFIG, createP4Config("test"));
    createFileInCommand(dir2, TEST_P4CONFIG, createP4Config("test"));
    addFile("dir1/a.txt");
    addFile("dir2/b.txt");
    addFile("dir1/" + TEST_P4CONFIG);
    addFile("dir2/" + TEST_P4CONFIG);
    submitDefaultList("initial");

    refreshVfs();
    openForEdit(file1);
    openForEdit(file2);
    refreshChanges();

    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertSize(2, getChangeListManager().getAllChanges());

    setUseP4Config();
    setVcsMappings(new VcsDirectoryMapping(dir1.getPath(), PerforceVcs.getInstance(myProject).getName()),
                   new VcsDirectoryMapping(dir2.getPath(), PerforceVcs.getInstance(myProject).getName()));

    refreshChanges();
    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertSize(2, getChangeListManager().getAllChanges());
  }

  @Test
  @Bombed(year = 2021, month = Calendar.AUGUST, day = 20, user = "AMPivovarov")
  public void testRefreshIsFastForManyRootsWithSameConnection() {
    int changeListCount = 10;
    int rootCount = 20;
    List<VirtualFile> roots = new ArrayList<>();
    for (int i = 0; i < rootCount; i++) {
      roots.add(createDirInCommand(myWorkingCopyDir, "dir" + i));
    }
    for (int i = 0; i < changeListCount; i++) {
      createChangeList("change" + i, Collections.emptyList());
    }

    setP4ConfigRoots(roots.toArray(VirtualFile.EMPTY_ARRAY));
    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertEmpty(getChangeListManager().getAllChanges());

    List<String> commands = new ArrayList<>();
    AbstractP4Connection.setCommandCallback(commands::add, myTestRootDisposable);

    refreshChanges();

    assertEquals(1, commands.stream().filter(s -> s.startsWith("changes ")).count());
  }

  @Test
  public void testIncomingChanges() throws VcsException {
    createFileInCommand("a.txt", "");
    addFile("a.txt");
    submitDefaultList("initial");
    verify(runP4WithClient("sync", "//depot/...@0"));

    PerforceRepositoryLocation location = PerforceRepositoryLocation.create(myWorkingCopyDir, myProject);
    assertOneElement(PerforceVcs.getInstance(myProject).getCommittedChangesProvider().getIncomingFiles(location));
  }

  @Test
  public void testRepositoryRefreshDoesNotQueryAboutLastCachedChangeList() throws VcsException {
    createFileInCommand("a.txt", "");
    addFile("a.txt");
    submitDefaultList("initial");

    PerforceVcs vcs = PerforceVcs.getInstance(myProject);
    PerforceRepositoryLocation location = PerforceRepositoryLocation.create(myWorkingCopyDir, myProject);
    CommittedChangesCache cache = CommittedChangesCache.getInstance(myProject);
    PerforceCommittedChangesProvider provider = vcs.getCommittedChangesProvider();
    PerforceChangeBrowserSettings settings = new PerforceChangeBrowserSettings();

    CollectConsumer<String> callback = new CollectConsumer<>();
    AbstractP4Connection.setCommandCallback(callback, myTestRootDisposable);

    assertOneElement(cache.getChanges(settings, myWorkingCopyDir, vcs, 100, false, provider, location));
    assertTrue(callback.getResult().contains("describe -s 1"));
    callback.getResult().clear();

    assertOneElement(cache.getChanges(settings, myWorkingCopyDir, vcs, 100, false, provider, location));
    assertFalse(callback.getResult().toString(), callback.getResult().contains("describe -s 1"));
  }

  @Test
  public void testChangesAfterUnshelveConflict() throws VcsException {
    VirtualFile file = createFileInCommand("a.txt", "");
    addFile("a.txt");
    submitDefaultList("initial");

    openForEdit(file);
    ChangeListManagerImpl clManager = getChangeListManager();
    clManager.waitUntilRefreshed();
    clManager.moveChangesTo(clManager.addChangeList("xxx", ""), getSingleChange());
    refreshChanges();

    verify(runP4WithClient("shelve", "-c", "2"));
    editFileInCommand(file, "new content");
    verify(runP4WithClient("unshelve", "-s", "2"));
    refreshChanges();

    assertOneElement(clManager.findChangeList("xxx").getChanges());
    assertOneElement(PerforceRunner.getInstance(myProject).getResolvedWithConflicts(getConnection(), myWorkingCopyDir));

    verify(runP4WithClient("resolve", "-ay", "//depot/a.txt"));
    refreshChanges();
    assertOneElement(clManager.findChangeList("xxx").getChanges());
    assertEmpty(PerforceRunner.getInstance(myProject).getResolvedWithConflicts(getConnection(), myWorkingCopyDir));

    verify(runP4WithClient("shelve", "-d", "-c", "2"));
    submitList(2);
  }

  @Test
  public void testFilesExcludedInClientSpec() {
    setupClient(buildTestClientSpec("test", myWorkingCopyDir.getPath(), "//test/..." + System.lineSeparator() +
                                                                        "\t-//depot/foo/.../*.txt //test/.../*.txt"));
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);

    VirtualFile foo = createDirInCommand(myWorkingCopyDir, "foo");
    VirtualFile bar = createDirInCommand(foo, "bar");
    VirtualFile atRoot = createFileInCommand("atRoot.txt", "");
    VirtualFile inFoo = createFileInCommand(foo, "inFoo.txt", "");
    VirtualFile inBar = createFileInCommand(bar, "inBar.txt", "");
    refreshChanges();

    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    VirtualFile inBarXml = createFileInCommand(bar, "inBarXml.xml", "");
    getChangeListManager().waitUntilRefreshed();

    List<String> cmds = AbstractP4Connection.dumpCommands(myTestRootDisposable);

    discardUnversionedCache();
    refreshChanges();

    assertEquals(atRoot, getSingleChange().getVirtualFile());
    assertOrderedEquals(getChangeListManager().getUnversionedFiles(), inBarXml);
    assertEquals(FileStatus.IGNORED, FileStatusManager.getInstance(myProject).getStatus(inFoo));
    assertEquals(FileStatus.IGNORED, FileStatusManager.getInstance(myProject).getStatus(inBar));

    cmds.forEach(cmd -> assertFalse(cmd, cmd.contains("inFoo.txt") || cmd.contains("inBar.txt")));
  }

  @Test
  public void testRevertFileWithSpecialCharactersInPath() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);

    VirtualFile dir = createDirInCommand(myWorkingCopyDir, "ivan_ivanov_dev@123@MSBDBMD2");
    VirtualFile file = createFileInCommand(dir, "bar.txt", "content");
    refreshChanges();
    getSingleChange();

    submitDefaultList("initial");
    refreshVfs();
    refreshChanges();

    openForEdit(file);
    refreshVfs();
    refreshChanges();

    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();

    assertEmpty(getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getUnversionedFiles());
  }

  @Test
  public void testAddedLocallyDeleted_1() {
    doTestAddedLocallyDeleted(true);
  }

  @Test
  public void testAddedLocallyDeleted_2() {
    doTestAddedLocallyDeleted(false);
  }

  private void doTestAddedLocallyDeleted(boolean rollbackLocallyDeleted) {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);

    File file = new File(VfsUtilCore.virtualToIoFile(myWorkingCopyDir), "a.txt");
    FileUtil.createIfDoesntExist(file);
    addFile("a.txt");
    FileUtil.delete(file);

    refreshVfs();
    refreshChanges();

    assertEquals(Change.Type.NEW, getSingleChange().getType());
    assertOneElement(getChangeListManager().getDeletedFiles());

    if (rollbackLocallyDeleted) {
      rollbackMissingFileDeletion(assertOneElement(getChangeListManager().getDeletedFiles()));
    } else {
      rollbackChange(getSingleChange());
    }
    getChangeListManager().waitUntilRefreshed();
    assertEmpty(getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getDeletedFiles());
    assertEmpty(getChangeListManager().getUnversionedFiles());
  }

  @Test
  public void testReportingFileTypeIgnoredFiles() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    createFileInCommand(createDirInCommand(myWorkingCopyDir, ".svn"), "a.txt", "");
    String path = ".svn/a.txt";
    addFile(path);
    submitDefaultList("initial");

    discardUnversionedCache();
    setVcsMappings(new VcsDirectoryMapping(myWorkingCopyDir.getPath(), "Perforce"));

    assertEmpty(getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getUnversionedFiles());
    assertEmpty(getChangeListManager().getDeletedFiles());
  }

  @Test
  @Bombed(year = 2021, month = Calendar.AUGUST, day = 20, user = "AMPivovarov")
  public void testTwoWorkspaces() {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);

    VirtualFile dir1 = createDirInCommand(myWorkingCopyDir, "dir1");
    VirtualFile dir2 = createDirInCommand(myWorkingCopyDir, "dir2");
    setupTwoClients(dir1, dir2);

    createFileInCommand(dir1, "a.txt", "");
    createFileInCommand(dir2, "b.txt", "");
    refreshChanges();

    assertSize(2, getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getUnversionedFiles());

    submitFileWithClient("test", "//depot/a.txt");
    submitFileWithClient("dir2", "//depot/b.txt");
    refreshVfs();
    refreshChanges();

    assertEmpty(getChangeListManager().getAllChanges());
    assertEmpty(getChangeListManager().getUnversionedFiles());
  }

  @Test
  public void testAlwaysWritableFileType() {
    VirtualFile file = createFileInCommand(myWorkingCopyDir, "a.log", "");

    verify(runP4WithClient("add", "-t", "text+w", new File(myClientRoot, file.getName()).toString()));
    submitFile("//depot/" + file.getName());

    refreshVfs();
    refreshChanges();

    assertTrue(file.isWritable());

    assertChangesViewEmpty();

    openForEdit(file);
    refreshChanges();

    assertEquals(file, getSingleChange().getVirtualFile());
  }

  @Test
  public void testNoModifiedWithoutCheckoutInAllWriteWorkspace() throws IOException {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    setupClient(buildTestClientSpec() + "Options:\tallwrite");

    VirtualFile file = createFileInCommand(myWorkingCopyDir, "a.txt", "");
    VirtualFile file2 = createFileInCommand(myWorkingCopyDir, "b.txt", "");
    submitFile("//depot/" + file.getName());
    submitFile("//depot/" + file2.getName());
    discardUnversionedCache();

    refreshVfs();
    refreshChanges();

    assertTrue(file.isWritable());
    assertTrue(file2.isWritable());

    assertChangesViewEmpty();

    openForEdit(file);
    FileUtil.writeToFile(VfsUtilCore.virtualToIoFile(file2), "new content");
    VfsUtil.markDirtyAndRefresh(false, false, false, file2);
    refreshChanges();

    assertEquals(file, getSingleChange().getVirtualFile());
    assertEmpty(getChangeListManager().getModifiedWithoutEditing()); // file2 changed status can't be determined
  }

  @Test
  public void testBrokenSymlink() {
    IoTestUtil.assumeSymLinkCreationIsSupported();

    final VirtualFile file = createFileInCommand("a.txt", "a");

    final VirtualFile link = LocalFileSystem.getInstance()
      .refreshAndFindFileByIoFile(IoTestUtil.createSymLink(file.getPath(), myWorkingCopyDir.getPath() + "/link.txt"));
    assertNotNull(link);

    addFile("a.txt");
    addFile("link.txt");

    renameFileInCommand(file, "b.txt");

    submitDefaultList("initial");

    VfsUtil.markDirtyAndRefresh(false, true, true, myWorkingCopyDir);
    assertTrue(link.is(VFileProperty.SYMLINK));
    assertNull(link.getCanonicalPath());

    refreshChanges();

    assertChangesViewEmpty();
  }

  @Test
  public void testModifyingSymlinkTarget() {
    IoTestUtil.assumeSymLinkCreationIsSupported();

    VirtualFile file = createFileInCommand("a.txt", "a");
    VirtualFile link = LocalFileSystem.getInstance()
      .refreshAndFindFileByIoFile(IoTestUtil.createSymLink(file.getPath(), myWorkingCopyDir.getPath() + "/link.txt"));
    assertNotNull(link);

    addFile("a.txt");

    refreshChanges();
    assertEquals(file, getSingleChange().getVirtualFile());
    assertOrderedEquals(getChangeListManager().getUnversionedFiles(), link);

    addFile("link.txt");
    submitDefaultList("initial");
    refreshChanges();
    assertChangesViewEmpty();

    openForEdit(file);
    refreshVfs();
    refreshChanges();
    assertEquals(file, getSingleChange().getVirtualFile());
    assertEmpty(getChangeListManager().getModifiedWithoutEditing());

    rollbackChange(getSingleChange());
    getChangeListManager().waitUntilRefreshed();
    assertChangesViewEmpty();
  }

  @Test
  public void everythingWorksWhenViewMappingUseClientNameInAnotherCase() {
    Assume.assumeFalse(SystemInfo.isFileSystemCaseSensitive);

    setupClient(buildTestClientSpec("test", myClientRoot.toString(), "//Test/..."));
    VirtualFile file = createFileInCommand("a.txt", "a");
    addFile("a.txt");

    refreshChanges();
    assertEquals(file, getSingleChange().getVirtualFile());
  }

}
