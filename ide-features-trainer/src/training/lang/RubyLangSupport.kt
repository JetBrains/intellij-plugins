package training.lang

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.text.VersionComparatorUtil
import org.jetbrains.plugins.ruby.gem.GemDependency
import org.jetbrains.plugins.ruby.gem.GemInstallUtil
import org.jetbrains.plugins.ruby.gem.gem.GemRunner
import org.jetbrains.plugins.ruby.ruby.RModuleUtil
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType
import org.jetbrains.plugins.ruby.ruby.sdk.RubyVersionUtil
import org.jetbrains.plugins.ruby.version.management.SdkRefresher
import training.learn.LearnBundle
import training.learn.exceptons.InvalidSdkException
import training.learn.exceptons.NoSdkException
import training.project.ProjectUtils
import java.io.File

class RubyLangSupport : AbstractLangSupport() {
  private val rubyProjectName: String
    get() = "RubyLearnProject"

  override fun checkSdk(sdk: Sdk?, project: Project) {
    if (project.name != rubyProjectName) return

    checkSdkPresence(sdk)
    if (sdk!!.sdkType !is RubySdkType) {
      throw InvalidSdkException("Selected SDK should be Ruby SDK")
    }
    val rubyVersion = RubyVersionUtil.getShortVersion(
            sdk.versionString ?: throw InvalidSdkException("SDK should have a version"))
            ?: throw InvalidSdkException("Invalid version: " + sdk.versionString)
    if (VersionComparatorUtil.compare(rubyVersion, "2.3.0") < 0) {
      throw InvalidSdkException("Ruby version should be at least 2.3")
    }
  }

  override fun getSdkForProject(project: Project): Sdk? {
    return try {
      super.getSdkForProject(project)
    } catch (e: NoSdkException) {
      SdkRefresher.refreshAll()
      super.getSdkForProject(project)
    }
  }

  override fun applyProjectSdk(sdk: Sdk, project: Project) {
    super.applyProjectSdk(sdk, project)
    RModuleUtil.getInstance().changeModuleSdk(sdk, project.module)
  }

  override fun importLearnProject(): Project? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override val defaultProjectName:String
    get() = rubyProjectName

  override val primaryLanguage: String
    get() = "ruby"

  override fun applyToProjectAfterConfigure(): (Project) -> Unit = { project ->
    val tempDirectory = FileUtil.createTempDirectory("bundler_gem", null, true)
    val bundlerGem = File(tempDirectory, "bundler-2.0.1.gem")
    FileUtil.writeToFile(bundlerGem, FileUtil.loadBytes(RubyLangSupport::class.java.getResourceAsStream(
            "/learnProjects/ruby/gems/bundler-2.0.1.gem")))

    val sdk = ProjectRootManager.getInstance(project).projectSdk!!
    val module = project.module

    GemInstallUtil.installGemsRequirements(sdk,
            module,
            listOf(GemDependency.any(bundlerGem.absolutePath)),
            false, false, false, false, true, null, HashMap())

    GemRunner.bundle(module, sdk, "install", null, null, null,
            "--local",
            false)
  }

  override fun createProject(projectName: String, projectToClose: Project?): Project? {
    return ProjectUtils.importOrOpenProject("/learnProjects/ruby/RubyLearnProject", projectName)
  }

  override fun setProjectListeners(project: Project) {
    project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileListener)
  }

  private val Project.module: Module
    get() {
      val modules = ModuleManager.getInstance(this).modules
      assert(modules.size == 1)
      return modules[0]
    }

  companion object {
    const val sandboxFile = "app/sandbox.rb"

    private val fileListener = object : FileEditorManagerListener {
      override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        val project = source.project
        if (ScratchFileService.isInScratchRoot(file)) {
          return
        }
        if (file.path == project.basePath + VfsUtilCore.VFS_SEPARATOR_CHAR + sandboxFile) {
          return
        }
        source.getAllEditors(file).forEach {
          ((it as? TextEditor)?.editor as? EditorEx)?.let { editorEx ->
            //TODO: replace with EditorModificationUtil#setReadOnlyHint in 2019.2 API or
            // implement WritingAccessProvider (it has no getReadOnlyMessage in 2019.1)
            // also it will provide lock icon
            EditorModificationUtil.READ_ONLY_VIEW_MESSAGE_KEY.set(editorEx, LearnBundle.message("learn.project.read.only.hint"))
            editorEx.isViewer = true
          }
        }
      }
    }
  }
}