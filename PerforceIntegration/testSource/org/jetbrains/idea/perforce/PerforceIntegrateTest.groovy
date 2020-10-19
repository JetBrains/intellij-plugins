package org.jetbrains.idea.perforce
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsConfiguration
import com.intellij.openapi.vcs.VcsShowConfirmationOption
import com.intellij.openapi.vcs.update.UpdateEnvironment
import com.intellij.openapi.vcs.update.UpdateSession
import com.intellij.openapi.vcs.update.UpdatedFiles
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.UIUtil
import com.intellij.vcsUtil.VcsUtil
import org.jetbrains.idea.perforce.application.ParticularConnectionPerforceIntegratePanel
import org.jetbrains.idea.perforce.application.PerforceIntegrateEnvironment
import org.jetbrains.idea.perforce.application.PerforceVcs
import org.jetbrains.idea.perforce.perforce.ParticularConnectionSettings
import org.jetbrains.idea.perforce.perforce.PerforceSettings
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
/**
 * @author peter
 */
@RunWith(Parameterized)
class PerforceIntegrateTest extends PerforceTestCase {
  private VirtualFile myMainDir
  private VirtualFile myBranchDir
  
  private boolean myInMain
  private boolean myReverse
  private boolean myIntegrateChangeList

  PerforceIntegrateTest(boolean inMain, boolean reverse, boolean integrateChangeList) {
    myInMain = inMain
    myReverse = reverse
    myIntegrateChangeList = integrateChangeList
  }

  @Parameterized.Parameters(name = "inMain={0} reverse={1} changeList={2}")
  static Collection data() {
    [[false, true], [false, true], [false, true]].combinations { it as Object[] }
  }
  
  @Override
  @Before
  void before() throws Exception {
    super.before()
    setStandardConfirmation("Perforce", VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY)
    myMainDir = createDirInCommand(myWorkingCopyDir, "main")
    myBranchDir = createDirInCommand(myWorkingCopyDir, "branch")
    verify(runP4(["-c", "test", "branch", "-i"] as String[], """\
Branch: branch
Owner: test
Options: unlocked
View: //depot/main/...\t//depot/branch/..."""))
  }

  @Test
  void checkIntegrate() {
    def projectDir = myInMain ? myMainDir : myBranchDir
    setVcsMappings(createMapping(projectDir))

    def srcDir = myReverse ? myBranchDir : myMainDir
    createFileInCommand(srcDir, "a.txt", "foo")
    addFile(srcDir.name + "/a.txt")
    submitDefaultList("create file")

    def targetDir = myReverse ? myMainDir : myBranchDir
    UpdatedFiles files = integrate()
    def useDepotPath = targetDir != projectDir
    def pathPrefix = (useDepotPath ? "//depot/" + targetDir.name : targetDir.path)
    def expectedFile = pathPrefix + "/a.txt" + (useDepotPath ? "#1" : "")
    assert files.getGroupById(PerforceIntegrateEnvironment.BRANCHED).files == [useDepotPath ? expectedFile : FileUtil.toSystemDependentName(expectedFile)]
  }

  private UpdatedFiles integrate() {
    ParticularConnectionSettings s = PerforceSettings.getSettings(myProject).getSettings(getConnection())
    s.INTEGRATE_BRANCH_NAME = "branch"
    s.INTEGRATE_REVERSE = myReverse
    s.INTEGRATE_CHANGE_LIST = myIntegrateChangeList
    if (myIntegrateChangeList) {
      s.INTEGRATED_CHANGE_LIST_NUMBER = "1"
    }

    UIUtil.invokeAndWaitIfNeeded {
      assert new ParticularConnectionPerforceIntegratePanel(myProject, connection).getChangesToIntegrate('branch', myReverse).size() == 1
    }

    UpdatedFiles files = UpdatedFiles.create()
    UpdateEnvironment environment = PerforceVcs.getInstance(myProject).getIntegrateEnvironment()
    assert environment
    
    environment.fillGroups(files)
    FilePath root = VcsUtil.getFilePath(ProjectLevelVcsManager.getInstance(myProject).allVcsRoots[0].path)
    
    UpdateSession session = environment.updateDirectories([root] as FilePath[], files, new EmptyProgressIndicator(), Ref.create(null))
    assert !session.exceptions
    
    return files
  }
}
