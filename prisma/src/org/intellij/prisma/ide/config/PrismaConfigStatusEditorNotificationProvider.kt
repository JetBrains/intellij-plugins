// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.config

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.InstallNodeModuleAddToDevDependenciesQuickFix
import com.intellij.lang.javascript.modules.InstallNodeModuleQuickFix
import com.intellij.lang.javascript.modules.InstallNodeModuleQuickFix.ModuleElement
import com.intellij.lang.javascript.modules.RunNpmUpdateQuickFix
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import com.intellij.util.CommonProcessors
import com.intellij.util.ExceptionUtil
import com.intellij.util.ui.JBUI
import icons.PrismaIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.lang.PrismaCoroutineScope
import org.jetbrains.annotations.Nls
import java.util.function.Function
import javax.swing.JComponent
import kotlin.time.Duration.Companion.seconds

class PrismaConfigStatusEditorNotificationProvider : EditorNotificationProvider {
  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
    if (!PrismaConfig.isPrismaConfig(file)) return Function { null }

    val processor = CommonProcessors.CollectProcessor<VirtualFile>()
    PackageJsonUtil.processUpPackageJsonFiles(project, file, processor)
    val packageJsonFiles = processor.results
    val isDeclared = packageJsonFiles.any { PackageJsonData.getOrCreate(it).isDependencyOfAnyType("tsx") }

    return Function { fileEditor ->
      val configManager = PrismaConfigManager.getInstance(project)
      val throwable = configManager.getEvaluationError(file) ?: return@Function null

      EditorNotificationPanel(fileEditor, JBUI.CurrentTheme.Notification.Error.BACKGROUND).apply {
        val errorType = PrismaConfigErrorType.match(throwable)

        text = getDisplayMessage(throwable)
        icon(PrismaIcons.Prisma)
        val cause = throwable.cause
        if (cause != null) {
          createActionLabel(
            PrismaBundle.message("prisma.config.error.show.stack.trace.action"), {
              val stackTrace = ExceptionUtil.getThrowableText(cause)
              val stackTraceFile = PsiFileFactory.getInstance(project)
                .createFileFromText("config-error.txt", PlainTextLanguage.INSTANCE, stackTrace)
              OpenFileDescriptor(project, stackTraceFile.virtualFile).navigate(true)
            }, false
          )
        }

        if (errorType == PrismaConfigErrorType.TSX_PACKAGE_MISSING) {
          val editor = (fileEditor as? TextEditor)?.editor
          val psiFile = PsiManager.getInstance(project).findFile(file)

          if (editor != null && psiFile != null) {
            val actions = getFixesForNotInstalledModule(object : ModuleElement {
              override fun getModuleName() = "tsx"
              override fun getProject() = project
            }, packageJsonFiles, isDeclared)

            actions.forEach {
              createActionLabel(it.text) {
                it.invoke(project, editor, psiFile)
                PrismaCoroutineScope.get(project).launch {
                  configManager.invalidate(file)
                  EditorNotifications.getInstance(project).updateNotifications(file)
                  delay(5.seconds) // wait for installation to complete
                  PrismaConfigManager.getInstanceAsync(project).getConfigForFile(file)
                }
              }
            }
          }
        }

        createActionLabel(PrismaBundle.message("prisma.config.reload"), {
          PrismaCoroutineScope.get(project).launch {
            configManager.invalidateAndReload(file)
            EditorNotifications.getInstance(project).updateNotifications(file)
          }
        }, false)
      }
    }
  }

  private fun getDisplayMessage(throwable: Throwable): @Nls String {
    val errorType = PrismaConfigErrorType.match(throwable)
    return when (errorType) {
      PrismaConfigErrorType.MISSING_ENVIRONMENT -> {
        val envVariable = PrismaConfigErrorType.MISSING_ENVIRONMENT.extractVariable(throwable)
        if (!envVariable.isNullOrBlank()) {
          PrismaBundle.message("prisma.config.missing.environment.variable", envVariable)
        }
        else {
          PrismaBundle.message("prisma.config.missing.environment.variables")
        }
      }

      PrismaConfigErrorType.TSX_PACKAGE_MISSING -> PrismaBundle.message("prisma.config.missing.tsx.package")

      else -> PrismaBundle.message("prisma.config.evaluation.error")
    }.let {
      PrismaBundle.message("prisma.config.error.editor.title", it)
    }
  }

  private fun getFixesForNotInstalledModule(
    moduleElement: ModuleElement,
    packageJsonFiles: Collection<VirtualFile>,
    inPackageJson: Boolean,
  ): List<IntentionAction> {
    val fixes = mutableListOf<IntentionAction>()
    if (inPackageJson) {
      if (!packageJsonFiles.isEmpty()) {
        fixes.add(RunNpmUpdateQuickFix(moduleElement, packageJsonFiles))
      }
    }
    else {
      fixes.add(InstallNodeModuleQuickFix(moduleElement, packageJsonFiles))
      fixes.add(InstallNodeModuleAddToDevDependenciesQuickFix(moduleElement, packageJsonFiles))
    }
    return fixes
  }
}

