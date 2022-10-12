package org.jetbrains.idea.perforce

import com.intellij.diff.contents.DocumentContent
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.*
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.history.VcsFileRevision
import com.intellij.openapi.vcs.history.VcsHistoryUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.encoding.EncodingProjectManager
import com.intellij.testFramework.PsiTestUtil
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction
import org.jetbrains.idea.perforce.application.PerforceAnnotationProvider
import org.jetbrains.idea.perforce.application.PerforceCheckinEnvironment
import org.jetbrains.idea.perforce.application.PerforceFileRevision
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.application.annotation.PerforceFileAnnotation
import org.jetbrains.idea.perforce.perforce.P4Revision
import org.jetbrains.idea.perforce.perforce.PerforceChangeList
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.junit.Test

import java.nio.charset.StandardCharsets

import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait
import static com.intellij.testFramework.UsefulTestCase.*
import static org.junit.Assert.assertEquals

class PerforceHistoryTest extends PerforceTestCase {
  @Override
  void before() throws Exception {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  void testCorrectSubmittedRevision() throws VcsException {
    createFileInCommand("a.txt", "aaa")

    getChangeListManager().setDefaultChangeList(getChangeListManager().addChangeList("another", null))
    VirtualFile file2 = createFileInCommand("b.txt", "aaa")
    refreshChanges()
    getSingleChange()

    submitDefaultList("initial")
    Collection<Change> changes = getChangeListManager().getAllChanges()
    assertEquals(file2, assertOneElement(changes).getVirtualFile())

    List<PerforceCheckinEnvironment.SubmitJob> jobs = PerforceVcs.getInstance(myProject).getCheckinEnvironment().getSubmitJobs(changes)
    long revision = assertOneElement(jobs).submit("comment", null)

    List<VcsFileRevision> history = getFileHistory(file2)
    assertEquals(history.get(0).getRevisionNumber().asString(), String.valueOf(revision))
  }

  @Test
  void testEmptyLineInSubmitComment() {
    VirtualFile file = createFileInCommand("a.txt", "aaa")
    refreshChanges()

    String comment = "aaa\n\nbbb"
    assertEmpty(PerforceVcs.getInstance(myProject).getCheckinEnvironment().commit(Arrays.asList(getSingleChange()), comment))

    List<VcsFileRevision> history = getFileHistory(file)
    assertEquals(comment, history.get(0).getCommitMessage())

  }

  @Test
  void "test history from another branch"() {
    VirtualFile file = createFileInCommand(createDirInCommand(workingCopyDir, "subdir"), "a.txt", "aaa")
    submitDefaultList("initial")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "bbb")
    submitDefaultList("added bbb")

    verify(runP4WithClient("integrate", workingCopyDir.path + "/subdir/...", workingCopyDir.path + "/subdir2/..."))
    submitDefaultList("copied")
    refreshVfs()

    def dir2 = workingCopyDir.findChild("subdir2")
    setP4ConfigRoots(dir2)
    def branchedFile = dir2.findChild("a.txt")

    def fileAnnotation = createTestAnnotation(branchedFile)
    def revision = fileAnnotation.findRevisionForLine(0)
    assert revision.submitMessage.contains("added bbb")

    def singleChange = assertOneElement(getSubmittedChanges(revision))

    assert singleChange.beforeRevision.content == "aaa"
    assert singleChange.afterRevision.content == "bbb"
  }

  private Collection<Change> getSubmittedChanges(P4Revision revision) {
    PerforceChangeList changeList
    runInEdtAndWait {
      changeList = ShowAllSubmittedFilesAction.
        getSubmittedChangeList(myProject, revision.changeNumber, revision.submitMessage, revision.date, revision.user, connection)
    }
    return changeList.changes
  }

  @Test
  void "test ignore whitespace"() {
    VirtualFile file = createFileInCommand(createDirInCommand(workingCopyDir, "subdir"), "a.txt", "aaa")
    submitDefaultList("initial")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "  aaa")
    submitDefaultList("added whitespace")

    def fileAnnotation = createTestAnnotation(file)
    def revision = fileAnnotation.findRevisionForLine(0)
    assert revision.submitMessage.contains("initial")
  }

  @Test
  void "test annotate FilePath"() {
    VirtualFile file = createFileInCommand(myWorkingCopyDir, "a.txt", "aaa")
    submitDefaultList("initial")

    FilePath filePath = VcsUtil.getFilePath(file)
    def newPath = file.parent.path + "/b.txt"

    verify(runP4WithClient("edit", file.path))
    verify(runP4WithClient("move", file.path, newPath))
    submitDefaultList("moved")

    VfsUtil.markDirty(true, true, myWorkingCopyDir)
    file = LocalFileSystem.instance.refreshAndFindFileByPath(newPath)

    List<VcsFileRevision> history = getFileHistory(file)
    assert history.size() == 2

    PerforceAnnotationProvider annotationProvider = (PerforceAnnotationProvider) PerforceVcs.getInstance(myProject).annotationProvider
    def annotation = (PerforceFileAnnotation)annotationProvider.annotate(filePath, ((PerforceFileRevision) history[0]).revisionNumber)
    Disposer.register(myTestRootDisposable, { annotation.dispose() } as Disposable)
    assert annotation.findRevisionForLine(0).submitMessage == 'initial'
  }

  @Test
  void "test annotate after move"() {
    def tail = "penultimate\nlast"

    def dir = createDirInCommand(workingCopyDir, "subdir")
    VirtualFile file = createFileInCommand(dir, "a.txt", tail)
    submitDefaultList("initial")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "line1\n" + tail)
    submitDefaultList("add 1")

    verify(runP4WithClient("edit", file.path))
    verify(runP4WithClient("move", file.path, dir.path + "/b.txt"))
    submitDefaultList("moved")

    VfsUtil.markDirtyAndRefresh(false, true, true, dir)
    file = dir.findChild("b.txt")
    assert file

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "line1\nline2\n" + tail)
    submitDefaultList("add 2")

    List<VcsFileRevision> history = getFileHistory(file)
    assert history.size() == 4

    assert "add 1" == history[2].commitMessage
    def fileAnnotation = (PerforceFileAnnotation) PerforceVcs.getInstance(myProject).annotationProvider.annotate(file, history[2])
    Disposer.register(myTestRootDisposable, { fileAnnotation.dispose() } as Disposable)
    assert fileAnnotation.lineCount == 3
    assert history[2].revisionNumber == fileAnnotation.getLineRevisionNumber(0)
    assert history[3].revisionNumber == fileAnnotation.getLineRevisionNumber(1)
    assert history[3].revisionNumber == fileAnnotation.getLineRevisionNumber(2)
  }

  @Test
  void "test show affected changes after copy"() {
    // create dir2/a.txt
    def dir1 = createDirInCommand(workingCopyDir, "dir1")
    def file1 = createFileInCommand(dir1, "a.txt", "foo")
    submitDefaultList("initial")

    // modify a.txt
    verify(runP4WithClient("edit", file1.path))
    setFileText(file1, "bbb")
    submitDefaultList("change")

    // check annotate works
    def fileAnnotation = createTestAnnotation(file1)
    def revision = fileAnnotation.findRevisionForLine(0)
    assert revision.submitMessage == "change"

    Change singleChange = assertOneElement(getSubmittedChanges(revision))
    assert singleChange.beforeRevision.file.path == file1.path

    // p4 copy to dir2
    verify(runP4WithClient("copy", '//depot/dir1/...', '//depot/dir2/...'))
    VfsUtil.markDirtyAndRefresh(false, true, true, workingCopyDir)
    def file2 = LocalFileSystem.instance.findFileByPath(workingCopyDir.path + "/dir2/" + file1.name)
    assert file2

    // check copied file history is still there
    singleChange = assertOneElement(getSubmittedChanges(revision))
    assert singleChange.beforeRevision.file.path == file1.path

    // narrow down project to dir2
    PsiTestUtil.removeAllRoots(myProjectFixture.module, null)
    PsiTestUtil.addContentRoot(myProjectFixture.module, file2.parent)
    setVcsMappings(new VcsDirectoryMapping(file2.parent.path, PerforceVcs.getInstance(myProject).getName()))
    refreshChanges()

    // check copied file history is still there, under depot path
    singleChange = assertOneElement(getSubmittedChanges(revision))
    assert singleChange.beforeRevision.file.path == '//depot/dir1/a.txt'

    // narrow down workspace to dir2
    setupClient(buildTestClientSpecCore("test", workingCopyDir.path) + "\t//depot/dir2/...\t//test/dir2/...")
    refreshChanges()

    // check copied file history is still there, under depot path
    singleChange = assertOneElement(getSubmittedChanges(revision))
    assert singleChange.beforeRevision.file.path == '//depot/dir1/a.txt'
  }

/*
  @Override
  protected String getPerforceVersion() {
    return "2015.1"
  }
*/

  //@Test todo make it work for all p4 versions
  void "annotate after integrate"() {
    VirtualFile file = createFileInCommand(createDirInCommand(workingCopyDir, "subdir"), "a.txt", "aaa")
    submitDefaultList("initial")

    verify(runP4WithClient("integrate", workingCopyDir.path + "/subdir/...", workingCopyDir.path + "/subdir2/..."))
    submitDefaultList("copied")
    refreshVfs()

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "aaa\nbbb")
    submitDefaultList("added bbb")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "aaa\nbbb\nccc")
    submitDefaultList("added ccc")

    verify(runP4WithClient("integrate", workingCopyDir.path + "/subdir/...", workingCopyDir.path + "/subdir2/..."))
    verify(runP4WithClient("resolve", "-a"))
    submitDefaultList("copied again")
    refreshVfs()

    def branchedFile = workingCopyDir.findChild("subdir2").findChild("a.txt")

    def fileAnnotation = (PerforceFileAnnotation)createTestAnnotation(PerforceVcs.getInstance(myProject).annotationProvider, branchedFile)
    assert fileAnnotation.findRevisionForLine(0)?.submitMessage?.contains("initial")
    assert fileAnnotation.findRevisionForLine(1)?.submitMessage?.contains("added bbb")
    assert fileAnnotation.findRevisionForLine(2)?.submitMessage?.contains("added ccc")
  }

  @Test
  void "get annotated content using changelist number with follow integrations enabled"() {
    checkAnnotationHistoryContent(true)
  }

  @Test
  void "get annotated content using changelist number with follow integrations disabled"() {
    checkAnnotationHistoryContent(false)
  }

  private void checkAnnotationHistoryContent(boolean followIntegrations) {
    def settings = PerforceSettings.getSettings(myProject)
    def old = settings.SHOW_BRANCHES_HISTORY
    settings.SHOW_BRANCHES_HISTORY = followIntegrations
    Disposer.register(myTestRootDisposable, { settings.SHOW_BRANCHES_HISTORY = old } as Disposable)

    VirtualFile file1 = createFileInCommand("a.txt", "aaa")
    submitDefaultList("initial")

    createFileInCommand("b.txt", "bbb")
    submitDefaultList("add file2")

    verify(runP4WithClient("edit", file1.path))
    setFileText(file1, "xxx\n")
    submitDefaultList("first edit")

    verify(runP4WithClient("edit", file1.path))
    setFileText(file1, "xxx\nyyy")
    submitDefaultList("second edit")

    def fileAnnotation = (PerforceFileAnnotation)createTestAnnotation(PerforceVcs.getInstance(myProject).annotationProvider, file1)
    assert fileAnnotation.findRevisionForLine(0)?.submitMessage?.contains("first edit")
    assert fileAnnotation.findRevisionForLine(1)?.submitMessage?.contains("second edit")
    assert fileAnnotation.getLineRevisionNumber(1).asString() == '4'

    def revisions = fileAnnotation.revisions
    assert revisions.size() == 3
    assert 'xxx\nyyy' == StringUtil.convertLineSeparators(new String(revisions[0].loadContent()))
    assert 'xxx\n' == StringUtil.convertLineSeparators(new String(revisions[1].loadContent()))
    assert 'aaa' == new String(revisions[2].loadContent())
  }

  @Test
  void "correct revision content after rename and change"() {
    VirtualFile file = createFileInCommand("a.txt", "aaa")
    submitDefaultList("initial")

    openForEdit(file)
    editFileInCommand(file, "aaa bbb")
    changeListManager.waitUntilRefreshed()

    assert singleChange.beforeRevision.content == 'aaa'

    renameFileInCommand(file, "b.txt")
    changeListManager.waitUntilRefreshed()

    assert singleChange.beforeRevision.content == 'aaa'

    submitDefaultList("renamed")
    openForEdit(file)
    editFileInCommand(file, "aaa bbb ccc")
    changeListManager.waitUntilRefreshed()

    assert singleChange.beforeRevision.content == 'aaa bbb'
  }

  @Test
  void "test diff for an utf-16 file"() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    VirtualFile file = createFileInCommand("a.txt", "")
    file.charset = StandardCharsets.UTF_16LE
    setFileText(file, "first")

    def firstContent = file.contentsToByteArray()
    assert CharsetToolkit.hasUTF16LEBom(firstContent)

    verify(runP4WithClient("add", "-t", "utf16", file.path))
    submitDefaultList("first")
    verify(runP4WithClient("edit", file.path))
    setFileText(file, "second")
    submitDefaultList("second")

    assert file.charset == StandardCharsets.UTF_16LE

    List<VcsFileRevision> history = getFileHistory(file)
    assertSize(2, history)
    assert Arrays.equals(history[0].loadContent(), file.contentsToByteArray())
    assert Arrays.equals(history[1].loadContent(), firstContent)
  }

  @Test
  void "test diff for older revisions on file which now is utf16"() {
    VirtualFile file = createFileInCommand("a.txt", "")
    setFileText(file, "first")
    submitDefaultList("first")

    openForEdit(file)
    setFileText(file, "second")
    submitDefaultList("second")

    verify(runP4WithClient("edit", "-t", "utf16", file.path))
    file.charset = StandardCharsets.UTF_16LE
    setFileText(file, "third")
    submitDefaultList("third")

    def path = VcsUtil.getFilePath(file)

    // set some Chinese charset to ensure it doesn't affect the diff content
    runInEdtAndWait { EncodingProjectManager.getInstance(myProject).defaultCharsetName = "GB2312" }

    List<VcsFileRevision> history = getFileHistory(file)
    List<DocumentContent> diffs = history.collect { VcsHistoryUtil.loadContentForDiff(myProject, path, it) as DocumentContent }
    assert diffs.collect { it.document.text } == ["third", "second", "first"]
    assert diffs.collect { it.charset } == [StandardCharsets.UTF_16LE, StandardCharsets.UTF_8, StandardCharsets.UTF_8]
  }
}
