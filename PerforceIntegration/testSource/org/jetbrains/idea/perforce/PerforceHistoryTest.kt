package org.jetbrains.idea.perforce

import com.intellij.diff.contents.DocumentContent
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.history.VcsHistoryUtil
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.encoding.EncodingProjectManager
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.UsefulTestCase.assertOneElement
import com.intellij.testFramework.UsefulTestCase.assertSize
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction
import org.jetbrains.idea.perforce.application.PerforceAnnotationProvider
import org.jetbrains.idea.perforce.application.PerforceFileRevision
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.application.annotation.PerforceFileAnnotation
import org.jetbrains.idea.perforce.perforce.P4Revision
import org.jetbrains.idea.perforce.perforce.PerforceChangeList
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.util.*

class PerforceHistoryTest : PerforceTestCase() {

  override fun before() {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY)
  }

  @Test
  fun testCorrectSubmittedRevision() {
    createFileInCommand("a.txt", "aaa")
    changeListManager.defaultChangeList = changeListManager.addChangeList("another", null)
    val file2 = createFileInCommand("b.txt", "aaa")
    refreshChanges()
    getSingleChange()

    submitDefaultList("initial")
    val changes = changeListManager.allChanges
    assertEquals(file2, assertOneElement(changes).virtualFile)

    val jobs = PerforceVcs.getInstance(myProject).checkinEnvironment.getSubmitJobs(changes)
    val revision = assertOneElement(jobs!!).submit("comment", null)

    val history = getFileHistory(file2)
    assertEquals(history[0].revisionNumber.asString(), revision.toString())
  }

  @Test
  fun testEmptyLineInSubmitComment() {
    val file = createFileInCommand("a.txt", "aaa")
    refreshChanges()

    val comment = "aaa\n\nbbb"
    assertTrue(PerforceVcs.getInstance(myProject).checkinEnvironment.commit(listOf(getSingleChange()), comment)!!.isEmpty())

    val history = getFileHistory(file)
    assertEquals(comment, history[0].commitMessage)
  }

  @Test
  fun `test history from another branch`() {
    val file = createFileInCommand(createDirInCommand(workingCopyDir, "subdir"), "a.txt", "aaa")
    refreshChanges()

    submitDefaultList("initial")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "bbb")
    submitDefaultList("added bbb")

    verify(runP4WithClient("integrate", workingCopyDir.path + "/subdir/...", workingCopyDir.path + "/subdir2/..."))
    submitDefaultList("copied")
    refreshVfs()

    val dir2 = workingCopyDir.findChild("subdir2")
    setP4ConfigRoots(dir2!!)
    val branchedFile = dir2.findChild("a.txt")

    val fileAnnotation = createTestAnnotation(branchedFile!!)
    val revision = fileAnnotation.findRevisionForLine(0)
    assertTrue(revision!!.submitMessage.contains("added bbb"))

    val singleChange = assertOneElement(getSubmittedChanges(revision))

    assertEquals("aaa", singleChange.beforeRevision!!.content)
    assertEquals("bbb", singleChange.afterRevision!!.content)
  }

  private fun getSubmittedChanges(revision: P4Revision): Collection<Change> {
    var changeList: PerforceChangeList? = null
    runInEdtAndWait {
      changeList = ShowAllSubmittedFilesAction.
      getSubmittedChangeList(myProject, revision.changeNumber, revision.submitMessage,
          revision.date, revision.user, connection)!!
    }
    return changeList!!.changes
  }

  @Test
  fun `test ignore whitespace`() {
    val file = createFileInCommand(createDirInCommand(workingCopyDir, "subdir"), "a.txt", "aaa")
    refreshChanges()

    submitDefaultList("initial")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "  aaa")
    submitDefaultList("added whitespace")

    val fileAnnotation = createTestAnnotation(file)
    val revision = fileAnnotation.findRevisionForLine(0)
    assertTrue(revision!!.submitMessage.contains("initial"))
  }

  @Test
  fun `test annotate FilePath`() {
    var file = createFileInCommand(myWorkingCopyDir, "a.txt", "aaa")
    refreshChanges()

    submitDefaultList("initial")

    val filePath = VcsUtil.getFilePath(file)
    val newPath = file.parent.path + "/b.txt"

    verify(runP4WithClient("edit", file.path))
    verify(runP4WithClient("move", file.path, newPath))
    submitDefaultList("moved")

    VfsUtil.markDirty(true, true, myWorkingCopyDir)
    file = LocalFileSystem.getInstance().refreshAndFindFileByPath(newPath)

    val history = getFileHistory(file)
    assertEquals(2, history.size)

    val annotationProvider = PerforceVcs.getInstance(myProject).annotationProvider as PerforceAnnotationProvider
    val annotation = annotationProvider.annotate(filePath, (history[0] as PerforceFileRevision).revisionNumber) as PerforceFileAnnotation
    Disposer.register(myTestRootDisposable, Disposable { annotation.dispose() })
    assertEquals("initial", annotation.findRevisionForLine(0)!!.submitMessage)
  }

  @Test
  fun `test annotate after move`() {
    val tail = "penultimate\nlast"

    val dir = createDirInCommand(workingCopyDir, "subdir")
    var file = createFileInCommand(dir!!, "a.txt", tail)
    refreshChanges()

    submitDefaultList("initial")

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "line1\n$tail")
    submitDefaultList("add 1")

    verify(runP4WithClient("edit", file.path))
    verify(runP4WithClient("move", file.path, dir.path + "/b.txt"))
    submitDefaultList("moved")

    VfsUtil.markDirtyAndRefresh(false, true, true, dir)
    file = dir.findChild("b.txt")!!
    assertNotNull(file)

    verify(runP4WithClient("edit", file.path))
    setFileText(file, "line1\nline2\n" + tail)
    submitDefaultList("add 2")

    val history = getFileHistory(file)
    assertEquals(4, history.size)

    assertEquals("add 1", history[2].commitMessage)
    val fileAnnotation = PerforceVcs.getInstance(myProject).annotationProvider!!.annotate(file, history[2]) as PerforceFileAnnotation
    Disposer.register(myTestRootDisposable, Disposable { fileAnnotation.dispose() })
    assertEquals(3, fileAnnotation.lineCount)
    assertEquals(history[2].revisionNumber, fileAnnotation.getLineRevisionNumber(0))
    assertEquals(history[3].revisionNumber, fileAnnotation.getLineRevisionNumber(1))
    assertEquals(history[3].revisionNumber, fileAnnotation.getLineRevisionNumber(2))
  }

  @Test
  fun `test show affected changes after copy`() {
    val dir1 = createDirInCommand(workingCopyDir, "dir1")
    val file1 = createFileInCommand(dir1!!, "a.txt", "foo")
    refreshChanges()

    submitDefaultList("initial")

    // modify a.txt
    verify(runP4WithClient("edit", file1.path))
    setFileText(file1, "bbb")
    submitDefaultList("change")

    // check annotate works
    val fileAnnotation = createTestAnnotation(file1)
    val revision = fileAnnotation.findRevisionForLine(0)
    assertEquals("change", revision!!.submitMessage)

    var singleChange = assertOneElement(getSubmittedChanges(revision))
    assertEquals(file1.path, singleChange.beforeRevision!!.file.path)

    // p4 copy to dir2
    verify(runP4WithClient("copy", "//depot/dir1/...", "//depot/dir2/..."))
    VfsUtil.markDirtyAndRefresh(false, true, true, workingCopyDir)
    val file2 = LocalFileSystem.getInstance().findFileByPath(workingCopyDir.path + "/dir2/" + file1.name)
    assertNotNull(file2)

    // check copied file history is still there
    singleChange = assertOneElement(getSubmittedChanges(revision))
    assertEquals(file1.path, singleChange.beforeRevision!!.file.path)

    // narrow down project to dir2
    PsiTestUtil.removeAllRoots(myProjectFixture.module, null)
    PsiTestUtil.addContentRoot(myProjectFixture.module, file2!!.parent)
    setVcsMappings(listOf(VcsDirectoryMapping(file2.parent.path, PerforceVcs.getInstance(myProject).name)))
    refreshChanges()

    // check copied file history is still there, under depot path
    singleChange = assertOneElement(getSubmittedChanges(revision))
    assertEquals("//depot/dir1/a.txt", singleChange.beforeRevision!!.file.path)

    // narrow down workspace to dir2
    setupClient(buildTestClientSpecCore("test", workingCopyDir.path) + "\t//depot/dir2/...\t//test/dir2/...")
    refreshChanges()

    // check copied file history is still there, under depot path
    singleChange = assertOneElement(getSubmittedChanges(revision))
    assertEquals("//depot/dir1/a.txt", singleChange.beforeRevision!!.file.path)
  }

/*
  override fun getPerforceVersion(): String {
    return "2015.1"
  }
*/

  //@Test todo make it work for all p4 versions
  fun test_annotate_after_integrate() {
    val file = createFileInCommand(createDirInCommand(workingCopyDir, "subdir"), "a.txt", "aaa")
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

    val branchedFile = workingCopyDir.findChild("subdir2")!!.findChild("a.txt")

    val fileAnnotation = createTestAnnotation(PerforceVcs.getInstance(myProject).annotationProvider!!,
                                              branchedFile!!) as PerforceFileAnnotation
    assertTrue(fileAnnotation.findRevisionForLine(0)?.submitMessage?.contains("initial") == true)
    assertTrue(fileAnnotation.findRevisionForLine(1)?.submitMessage?.contains("added bbb") == true)
    assertTrue(fileAnnotation.findRevisionForLine(2)?.submitMessage?.contains("added ccc") == true)
  }

  @Test
  fun `get annotated content using changelist number with follow integrations enabled`() {
    checkAnnotationHistoryContent(true)
  }

  @Test
  fun `get annotated content using changelist number with follow integrations disabled`() {
    checkAnnotationHistoryContent(false)
  }

  private fun checkAnnotationHistoryContent(followIntegrations: Boolean) {
    val settings = PerforceSettings.getSettings(myProject)
    val old = settings.SHOW_BRANCHES_HISTORY
    settings.SHOW_BRANCHES_HISTORY = followIntegrations
    Disposer.register(myTestRootDisposable, Disposable { settings.SHOW_BRANCHES_HISTORY = old })

    val file1 = createFileInCommand("a.txt", "aaa")
    refreshChanges()

    submitDefaultList("initial")

    createFileInCommand("b.txt", "bbb")
    refreshChanges()

    submitDefaultList("add file2")

    verify(runP4WithClient("edit", file1.path))
    setFileText(file1, "xxx\n")
    submitDefaultList("first edit")

    verify(runP4WithClient("edit", file1.path))
    setFileText(file1, "xxx\nyyy")
    submitDefaultList("second edit")

    val fileAnnotation = createTestAnnotation(PerforceVcs.getInstance(myProject).annotationProvider!!, file1) as PerforceFileAnnotation
    assertTrue(fileAnnotation.findRevisionForLine(0)?.submitMessage?.contains("first edit") == true)
    assertTrue(fileAnnotation.findRevisionForLine(1)?.submitMessage?.contains("second edit") == true)
    assertEquals("4", fileAnnotation.getLineRevisionNumber(1)!!.asString())

    val revisions = fileAnnotation.revisions!!
    assertEquals(3, revisions.size)
    assertEquals("xxx\nyyy", StringUtil.convertLineSeparators(String(revisions[0].loadContent()!!)))
    assertEquals("xxx\n", StringUtil.convertLineSeparators(String(revisions[1].loadContent()!!)))
    assertEquals("aaa", String(revisions[2].loadContent()!!))
  }

  @Test
  fun `correct revision content after rename and change`() {
    val file = createFileInCommand("a.txt", "aaa")
    refreshChanges()

    submitDefaultList("initial")

    openForEdit(file)
    editFileInCommand(file, "aaa bbb")

    refreshChanges()

    assertEquals("aaa", singleChange.beforeRevision!!.content)

    renameFileInCommand(file, "b.txt")

    refreshChanges()

    assertEquals("aaa", singleChange.beforeRevision!!.content)

    submitDefaultList("renamed")
    openForEdit(file)
    editFileInCommand(file, "aaa bbb ccc")

    refreshChanges()

    assertEquals("aaa bbb", singleChange.beforeRevision!!.content)
  }

  @Test
  fun `test diff for an utf-16 file`() {
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)

    val file = createFileInCommand("a.txt", "")
    file.charset = StandardCharsets.UTF_16LE
    setFileText(file, "first")

    val firstContent = file.contentsToByteArray()
    assertTrue(CharsetToolkit.hasUTF16LEBom(firstContent))

    verify(runP4WithClient("add", "-t", "utf16", file.path))
    submitDefaultList("first")
    verify(runP4WithClient("edit", file.path))
    setFileText(file, "second")
    submitDefaultList("second")

    assertEquals(StandardCharsets.UTF_16LE, file.charset)

    val history = getFileHistory(file)
    assertSize(2, history)
    assertTrue(Arrays.equals(history[0].loadContent(), file.contentsToByteArray()))
    assertTrue(Arrays.equals(history[1].loadContent(), firstContent))
  }

  @Test
  fun `test diff for older revisions on file which now is utf16`() {
    val file = createFileInCommand("a.txt", "")
    setFileText(file, "first")
    refreshChanges()

    submitDefaultList("first")

    openForEdit(file)
    setFileText(file, "second")
    submitDefaultList("second")

    verify(runP4WithClient("edit", "-t", "utf16", file.path))
    file.charset = StandardCharsets.UTF_16LE
    setFileText(file, "third")
    submitDefaultList("third")

    val path = VcsUtil.getFilePath(file)

    // set some Chinese charset to ensure it doesn't affect the diff content
    runInEdtAndWait { EncodingProjectManager.getInstance(myProject).defaultCharsetName = "GB2312" }

    val history = getFileHistory(file)
    val diffs = history.map { VcsHistoryUtil.loadContentForDiff(myProject, path, it) as DocumentContent }
    assertEquals(listOf("third", "second", "first"), diffs.map { it.document.text })
    assertEquals(listOf(StandardCharsets.UTF_16LE, StandardCharsets.UTF_8, StandardCharsets.UTF_8), diffs.map { it.charset })
  }
}
