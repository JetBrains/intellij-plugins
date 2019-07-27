package com.intellij.aws.cloudformation

import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.json.JsonFileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryAdapter
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.util.io.HttpRequests
import org.jdom.JDOMException
import org.jetbrains.yaml.YAMLFileType
import java.io.IOException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Based on org.rust.ide.update.UpdateComponent
 */
class CloudFormationUpdateComponent : ApplicationComponent.Adapter(), Disposable {

  init {
    Disposer.register(ApplicationManager.getApplication(), this)
    val pluginId = PluginManager.getPluginByClassName(this.javaClass.name)
    val descriptor = PluginManager.getPlugin(pluginId)
    ourCurrentPluginVersion = descriptor?.version
  }

  override fun initComponent() {
    val application = ApplicationManager.getApplication()
    if (!application.isUnitTestMode) {
      EditorFactory.getInstance().addEditorFactoryListener(EditorListener(this), this)
    }
  }

  override fun dispose() {
  }

  private class EditorListener(val parentDisposable: Disposable) : EditorFactoryAdapter() {
    private fun checkPsi(project: Project, file: VirtualFile): Boolean {
      if (file.fileType === YamlCloudFormationFileType.INSTANCE ||
          file.fileType === JsonCloudFormationFileType.INSTANCE)
        return true

      try {
        val psiFile = PsiManager.getInstance(project).findFile(file)
        return psiFile != null && CloudFormationPsiUtils.isCloudFormationFile(psiFile)
      } catch (t: Throwable) {
        LOG.debug("Unable to detect whether file ${file.path} is CloudFormation file")
        return false
      }
    }

    override fun editorCreated(event: EditorFactoryEvent) {
      val document = event.editor.document
      val file = FileDocumentManager.getInstance().getFile(document)
      val project = event.editor.project
      if (project != null && file != null && file.fileType in FILE_TYPES && checkPsi(project, file)) {
        updateOnPooledThread()

        (event.editor as? EditorEx)?.addFocusListener(object : FocusChangeListener {
          override fun focusGained(editor: Editor) {
            updateOnPooledThread()
          }

          override fun focusLost(editor: Editor) { }
        }, parentDisposable)
      }
    }
  }

  companion object {
    private val lock: Any = Any()
    private val PLUGIN_ID: String = "AWSCloudFormation"
    private val LAST_UPDATE: String = "$PLUGIN_ID.LAST_UPDATE"
    private val LAST_VERSION: String = "$PLUGIN_ID.LAST_VERSION"

    private val FILE_TYPES: Set<FileType> = setOf(
        YAMLFileType.YML,
        JsonFileType.INSTANCE,
        JsonCloudFormationFileType.INSTANCE,
        YamlCloudFormationFileType.INSTANCE
    )

    private val LOG = Logger.getInstance(CloudFormationUpdateComponent::class.java)

    private var ourCurrentPluginVersion: String? = null

    fun updateOnPooledThread() = ApplicationManager.getApplication().executeOnPooledThread { update() }

    fun update() {
      val properties = PropertiesComponent.getInstance()
      synchronized(lock) {
        val lastPluginVersion = properties.getValue(LAST_VERSION)
        val lastUpdate = properties.getOrInitLong(LAST_UPDATE, 0L)
        val shouldUpdate = lastUpdate == 0L
            || System.currentTimeMillis() - lastUpdate > TimeUnit.DAYS.toMillis(1)
            || lastPluginVersion == null
            || lastPluginVersion != ourCurrentPluginVersion
        if (shouldUpdate) {
          properties.setValue(LAST_UPDATE, System.currentTimeMillis().toString())
          properties.setValue(LAST_VERSION, ourCurrentPluginVersion)
        } else {
          return
        }
      }

      val url = updateUrl
      try {
        HttpRequests.request(url).connect {
          try {
            JDOMUtil.load(it.reader)
          } catch (e: JDOMException) {
            LOG.warn(e)
          }
          LOG.info("updated: $url")
        }
      } catch (ignored: UnknownHostException) {
        // No internet connections, no need to log anything
      } catch (e: IOException) {
        LOG.warn(e)
      }
    }

    private val updateUrl: String get() {
      val applicationInfo = ApplicationInfoEx.getInstanceEx()
      val buildNumber = applicationInfo.build.asString()
      val plugin = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID))!!
      val pluginId = plugin.pluginId.idString
      val os = URLEncoder.encode("${SystemInfo.OS_NAME} ${SystemInfo.OS_VERSION}", Charsets.UTF_8.name())
      val uid = PermanentInstallationID.get()
      val baseUrl = "https://plugins.jetbrains.com/plugins/list"
      return "$baseUrl?pluginId=$pluginId&build=$buildNumber&pluginVersion=${plugin.version}&os=$os&uuid=$uid"
    }
  }
}