package com.intellij.openRewrite.actions

import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openRewrite.OPEN_REWRITE_NOTIFICATION_GROUP_ID
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeDescriptor
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.recipe.OpenRewriteType
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openRewrite.run.openRewriteRunConfigurationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.NaturalComparator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.Callable
import javax.swing.Icon

private const val MIGRATION_ACTION_NOTIFICATION_DISPLAY_ID = "openRewrite.migration.recipe"

internal class OpenRewriteMigrationAction : OpenRewriteMigrationGroupAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return


    val runnable = Runnable {
      ReadAction.nonBlocking(Callable {
        OpenRewriteRecipeService.getInstance(project).getDescriptors(null, OpenRewriteType.RECIPE).filter { descriptor ->
          OpenRewriteRecipeLibraryContributor.EP_NAME.findFirstSafe { it.updateMatches(descriptor.name) } != null
        }
      })
        .coalesceBy(this, project)
        .expireWith(OpenRewriteRecipeService.getInstance(project))
        .inSmartMode(project)
        .finishOnUiThread(ModalityState.nonModal()) { descriptors ->
          if (descriptors.isEmpty()) {
            NotificationGroupManager.getInstance().getNotificationGroup(OPEN_REWRITE_NOTIFICATION_GROUP_ID)
              .createNotification(OpenRewriteBundle.message("open.rewrite.no.matching.migration"),
                                  NotificationType.WARNING)
              .setDisplayId(MIGRATION_ACTION_NOTIFICATION_DISPLAY_ID)
              .setIcon(OpenRewriteIcons.OpenRewrite)
              .notify(project)
            return@finishOnUiThread
          }
          val popup = JBPopupFactory.getInstance().createListPopup(createPopupStep(descriptors, virtualFile, project))
          popup.showCenteredInCurrentWindow(project)
        }
        .submit(AppExecutorUtil.getAppExecutorService())
    }

    val reloadJob = OpenRewriteRecipeService.getInstance(project).reload()
    if (reloadJob == null) {
      runnable.run()
    }
    else {
      reloadJob.invokeOnCompletion { runnable.run() }
    }
  }

  private fun createPopupStep(descriptors: List<OpenRewriteRecipeDescriptor>,
                              virtualFile: VirtualFile,
                              project: Project): ListPopupStep<OpenRewriteRecipeDescriptor> {
    val values = ArrayList(descriptors)
    values.sortWith(Comparator.comparing({ it.displayName ?: it.name }, NaturalComparator.INSTANCE))
    return object : BaseListPopupStep<OpenRewriteRecipeDescriptor>(
      OpenRewriteBundle.message("open.rewrite.migration.popup.title"),
      values) {

      override fun getTextFor(value: OpenRewriteRecipeDescriptor): String {
        @NlsSafe val text = value.displayName ?: value.name
        return text
      }

      override fun getIconFor(value: OpenRewriteRecipeDescriptor): Icon = OpenRewriteIcons.OpenRewrite

      override fun onChosen(selectedValue: OpenRewriteRecipeDescriptor, finalChoice: Boolean): PopupStep<*>? {
        runRecipe(selectedValue, virtualFile, project)
        return FINAL_CHOICE
      }

      override fun isSpeedSearchEnabled(): Boolean = true
    }
  }

  private fun runRecipe(descriptor: OpenRewriteRecipeDescriptor, virtualFile: VirtualFile, project: Project) {
    val path = virtualFile.parent.path
    val runManager = RunManager.getInstance(project)
    val allSettings = runManager.getConfigurationSettingsList(openRewriteRunConfigurationType())

    var settings = allSettings.find {
      val configuration = it.configuration as? OpenRewriteRunConfiguration ?: return@find false
      configuration.activeRecipes == descriptor.name && configuration.getExpandedWorkingDirectory() == path
    }
    if (settings == null) {
      settings = runManager.createConfiguration("", openRewriteRunConfigurationType().configurationFactories[0])
      val configuration = settings.configuration as OpenRewriteRunConfiguration
      configuration.activeRecipes = descriptor.name
      configuration.setGeneratedName()
      configuration.workingDirectory = virtualFile.parent.path
      runManager.setUniqueNameIfNeeded(settings)
      runManager.setTemporaryConfiguration(settings)
    }

    val builder = ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), settings)
    if (builder != null) {
      ExecutionManager.getInstance(project).restartRunProfile(builder.build())
    }
  }
}