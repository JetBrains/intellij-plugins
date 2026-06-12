package com.intellij.openRewrite.run.before

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.execution.ExecutionBundle
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.compiler.CompilationException
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.jps.model.java.JpsJavaSdkType
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

internal class OpenRewriteScratchClassCompilationSupport : CompileTask {
  override fun execute(context: CompileContext): Boolean {
    val configuration = CompileStepBeforeRun.getRunConfiguration(context)
    if (configuration !is OpenRewriteRunConfiguration) return true

    val beforeRunTask = configuration.getUserData(INSTALL_BEFORE_RUN_TASK_KEY) ?: return true
    val scratchUrl = beforeRunTask.scratchFileUrl
    if (scratchUrl == null) {
      context.addMessage(CompilerMessageCategory.ERROR,
                         ExecutionBundle.message("run.java.scratch.associated.file.not.specified"),
                         null, -1, -1)
      return false
    }

    val project = context.project
    val targetSdk = ProjectRootManager.getInstance(project).projectSdk
    if (targetSdk == null) {
      context.addMessage(CompilerMessageCategory.ERROR, ExecutionBundle.message("run.java.scratch.missing.jdk"), scratchUrl, -1, -1)
      return false
    }
    if (targetSdk.sdkType !is JavaSdkType) {
      context.addMessage(CompilerMessageCategory.ERROR,
                         ExecutionBundle.message("run.java.scratch.java.sdk.required.project", project.name),
                         scratchUrl, -1, -1)
      return false
    }

    val outputDir = getScratchOutputDirectory(project) ?: return true // should not happen for normal projects
    FileUtil.delete(outputDir) // perform cleanup

    try {
      val scratchFile = File(VirtualFileManager.extractPath(scratchUrl))
      val charset = ReadAction.compute<Charset, RuntimeException> {
        VirtualFileManager.getInstance().findFileByUrl(scratchUrl)?.charset
      }

      val cp = getClasspath(project)
      val platformCp = getPlatformClasspath(project)
      val options = getCompilerOptions(targetSdk, charset)

      val result = CompilerManager.getInstance(project).compileJavaCode(
        options, platformCp, cp, emptyList(), emptyList(), emptyList(), setOf(scratchFile), outputDir
      )
      for (classObject in result) {
        val bytes = classObject.content
        if (bytes != null) {
          FileUtil.writeToFile(File(classObject.path), bytes)
        }
      }
    }
    catch (e: CompilationException) {
      for (m in e.messages) {
        context.addMessage(m.category, m.text, scratchUrl, m.line, m.column)
      }
      return false
    }
    catch (e: IOException) {
      context.addMessage(CompilerMessageCategory.ERROR, e.message, scratchUrl, -1, -1)
      return false
    }
    return true
  }

  private fun getClasspath(project: Project): Collection<File> {
    val cp = LinkedHashSet<File>()
    runReadActionBlocking {
      for (library in OpenRewriteRecipeService.getInstance(project).getLibraries()) {
        for (root in library.binaryRoots) {
          val localVirtualFile = JarFileSystem.getInstance().getVirtualFileForJar(root)
          if (localVirtualFile != null) {
            cp.add(File(localVirtualFile.path))
          }
        }
      }
    }
    return cp
  }

  private fun getPlatformClasspath(project: Project): Collection<File> {
    val platformCp = LinkedHashSet<File>()
    runReadActionBlocking {
      val orderEnumerator = ProjectRootManager.getInstance(project).orderEntries()
      for (path in orderEnumerator.compileOnly().sdkOnly().pathsList.pathList) {
        platformCp.add(File(path))
      }
    }
    return platformCp
  }

  private fun getCompilerOptions(sdk: Sdk, charset: Charset?): List<String> {
    val options = ArrayList<String>()
    options.add("-g") // always compile with debug info
    val sdkVersion = JavaSdk.getInstance().getVersion(sdk)
    if (sdkVersion != null) {
      val level = sdkVersion.maxLanguageLevel
      val langLevel = JpsJavaSdkType.complianceOption(level.toJavaVersion())
      options.add("-source")
      options.add(langLevel)
      options.add("-target")
      options.add(langLevel)
      if (level.isPreview) {
        options.add("--enable-preview")
      }
    }
    options.add("-proc:none") // disable annotation processing
    if (charset != null) {
      options.add("-encoding")
      options.add(charset.name())
    }
    return options
  }
}