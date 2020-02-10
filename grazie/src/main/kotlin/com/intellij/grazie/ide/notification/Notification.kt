// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.notification

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.GraziePlugin
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.ide.ui.components.dsl.pluginOnlyMsg
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.remote.GrazieRemote
import com.intellij.notification.*
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.util.containers.ConcurrentMultiMap
import java.lang.ref.WeakReference

object Notification {
  private enum class Group { UPDATE, LANGUAGES }

  private val shownNotifications = ConcurrentMultiMap<Group, WeakReference<Notification>>()

  private val NOTIFICATION_GROUP_INSTALL = if (!GraziePlugin.isBundled) {
    NotificationGroup(pluginOnlyMsg("grazie.install.group"), NotificationDisplayType.STICKY_BALLOON, true)
  } else null

  private val NOTIFICATION_GROUP_UPDATE = if (!GraziePlugin.isBundled) {
    NotificationGroup(pluginOnlyMsg("grazie.update.group"), NotificationDisplayType.STICKY_BALLOON, true)
  } else null


  private val NOTIFICATION_GROUP_LANGUAGES = NotificationGroup(msg("grazie.languages.group"),
                                                               NotificationDisplayType.STICKY_BALLOON, true)

  fun showInstallationMessage(project: Project): Unit? {
    require(!GraziePlugin.isBundled) { "Trying to show installation message in bundled plugin!" }

    return NOTIFICATION_GROUP_INSTALL
      ?.createNotification(
        pluginOnlyMsg("grazie.install.title"), pluginOnlyMsg("grazie.install.body", ShowSettingsUtil.getSettingsMenuName()),
        NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
      ?.addAction(object : NotificationAction(pluginOnlyMsg("grazie.install.action.text", ShowSettingsUtil.getSettingsMenuName())) {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
          ShowSettingsUtil.getInstance().showSettingsDialog(project, msg("grazie.name"))
          notification.expire()
        }
      })
      ?.expireAll(Group.UPDATE)
      ?.notify(project)
  }

  fun showUpdateMessage(project: Project): Unit? {
    require(!GraziePlugin.isBundled) { "Trying to show update message in bundled plugin!" }

    return NOTIFICATION_GROUP_UPDATE
      ?.createNotification(
        pluginOnlyMsg("grazie.update.title", GraziePlugin.version), pluginOnlyMsg("grazie.update.body"),
        NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
      ?.expireAll(Group.UPDATE)
      ?.notify(project)
  }

  fun showLanguagesMessage(project: Project) {
    val langs = GrazieConfig.get().missedLanguages
    val s = if (langs.size > 1) "s" else ""
    NOTIFICATION_GROUP_LANGUAGES
      .createNotification(msg("grazie.languages.title", s),
                          msg("grazie.languages.body", langs.joinToString()),
                          NotificationType.WARNING, null)
      .addAction(object : NotificationAction(msg("grazie.languages.action.download", s)) {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
          GrazieRemote.downloadMissing(project)
          notification.expire()
        }
      })
      .addAction(object : NotificationAction(msg("grazie.languages.action.disable", s)) {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
          GrazieConfig.update { state ->
            state.copy(enabledLanguages = state.enabledLanguages - state.missedLanguages,
                         nativeLanguage = if (state.nativeLanguage.jLanguage == null) Lang.AMERICAN_ENGLISH else state.nativeLanguage)
          }
          notification.expire()
        }
      })
      .expireAll(Group.LANGUAGES)
      .notify(project)
  }

  private fun Notification.expireAll(group: Group): Notification {
    whenExpired { shownNotifications.remove(group)?.forEach { it.get()?.expire() } }
    shownNotifications.putValue(group, WeakReference(this))
    return this
  }
}
