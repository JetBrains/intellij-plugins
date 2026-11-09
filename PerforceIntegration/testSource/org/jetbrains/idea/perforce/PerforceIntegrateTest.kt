package org.jetbrains.idea.perforce

import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.update.UpdatedFiles
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.UIUtil
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.application.ParticularConnectionPerforceIntegratePanel
import org.jetbrains.idea.perforce.application.PerforceIntegrateEnvironment
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PerforceIntegrateTest : PerforceTestCase() {

  private lateinit var myMainDir: VirtualFile
  private lateinit var myBranchDir: VirtualFile

  companion object {
    @JvmStatic
    fun data(): Collection<Array<Any?>> {
      return arrayListOf(
        arrayOf(false, false, false),
        arrayOf(false, false, true),
        arrayOf(false, true, false),
        arrayOf(false, true, true),
        arrayOf(true, false, false),
        arrayOf(true, false, true),
        arrayOf(true, true, false),
        arrayOf(true, true, true)
      )
    }
  }

  override fun before() {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    myMainDir = createDirInCommand(myWorkingCopyDir, "main")
    myBranchDir = createDirInCommand(myWorkingCopyDir, "branch")
    verify(runP4(arrayOf("-c", "test", "branch", "-i"),
           "\nBranch: branch\nOwner: test\nOptions: unlocked\nView: //depot/main/...\t//depot/branch/..."))
  }

  @ParameterizedTest(name = "inMain={0} reverse={1} changeList={2}")
  @MethodSource("data")
  fun checkIntegrate(myInMain: Boolean, myReverse: Boolean, myIntegrateChangeList: Boolean) {
    val projectDir = if (myInMain) myMainDir else myBranchDir
    setVcsMappings(createMapping(projectDir))

    val srcDir = if (myReverse) myBranchDir else myMainDir
    createFileInCommand(srcDir, "a.txt", "foo")
    addFile(srcDir.name + "/a.txt")
    submitDefaultList("create file")

    val targetDir = if (myReverse) myMainDir else myBranchDir
    val files = integrate(myReverse, myIntegrateChangeList)
    val useDepotPath = targetDir != projectDir
    val pathPrefix = if (useDepotPath) "//depot/" + targetDir.name else targetDir.path
    val expectedFile = pathPrefix + "/a.txt" + if (useDepotPath) "#1" else ""
    assertEquals(files.getGroupById(PerforceIntegrateEnvironment.BRANCHED)!!.files,
                 if (useDepotPath) listOf(expectedFile) else listOf(FileUtil.toSystemDependentName(expectedFile)))
  }

  private fun integrate(myReverse: Boolean, myIntegrateChangeList: Boolean): UpdatedFiles {
    val s = PerforceSettings.getSettings(myProject).getSettings(connection)
    s.INTEGRATE_BRANCH_NAME = "branch"
    s.INTEGRATE_REVERSE = myReverse
    s.INTEGRATE_CHANGE_LIST = myIntegrateChangeList
    if (myIntegrateChangeList) {
      s.INTEGRATED_CHANGE_LIST_NUMBER = "1"
    }

    UIUtil.invokeAndWaitIfNeeded {
      assertEquals(1, ParticularConnectionPerforceIntegratePanel(myProject, connection).getChangesToIntegrate("branch", myReverse).size)
    }

    val files = UpdatedFiles.create()
    val environment = PerforceVcs.getInstance(myProject).integrateEnvironment
    assertNotNull(environment)

    environment!!.fillGroups(files)
    val root = VcsUtil.getFilePath(ProjectLevelVcsManager.getInstance(myProject).getAllVcsRoots()[0].path)

    val session = environment.updateDirectories(arrayOf(root), files, EmptyProgressIndicator(), Ref.create(null))
    assertTrue(session.exceptions.isEmpty())

    return files
  }
}
