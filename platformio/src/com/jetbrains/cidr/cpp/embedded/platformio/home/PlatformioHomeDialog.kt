package com.jetbrains.cidr.cpp.embedded.platformio.home

import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.icons.AllIcons
import com.intellij.ide.impl.TrustedPathsSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.io.BaseOutputReader
import com.intellij.util.io.HttpRequests
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioUsagesCollector
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatfromioCliBuilder
import com.jetbrains.cidr.cpp.embedded.platformio.ui.notifyPlatformioNotFound
import java.io.IOException
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.SwingUtilities

private const val MIN_PRIVATE_IP_PORT: Int = 49152

class PlatformioHomeDialog(private val project: Project?, parentComponent: JComponent?) : DialogWrapper(project, parentComponent, true,
                                                                                                        IdeModalityType.PROJECT) {
  private val portNumber: Int = MIN_PRIVATE_IP_PORT + (Math.random() * (65535 - MIN_PRIVATE_IP_PORT)).toInt()
  private val browser: JBCefBrowser = JBCefBrowser().also {
    it.loadHTML(ClionEmbeddedPlatformioBundle.message("loading.platformio"))
    Disposer.register(myDisposable, it)
  }

  @Volatile
  var webClient: PlatformioWebSocketClient? = null

  private var projectLocationToOpen: String? = null
  fun getProjectLocationToOpen(): String? = projectLocationToOpen

  private var documentLocationToOpen: String? = null
  fun getDocumentLocationToOpen(): String? = documentLocationToOpen

  @Volatile
  var processHandler: KillableProcessHandler? = null

  init {
    title = ClionEmbeddedPlatformioBundle.message("dialog.title.platformio.home")
    init()
  }

  override fun createActions(): Array<Action> = arrayOf(cancelAction, helpAction)

  override fun createCenterPanel(): JComponent = browser.component

  override fun dispose() {
    ApplicationManager.getApplication().executeOnPooledThread {
      com.jetbrains.cidr.cpp.embedded.platformio.project.LOG.trace("Closing pio websocket")
      webClient?.closeBlocking()
      webClient = null
      com.jetbrains.cidr.cpp.embedded.platformio.project.LOG.trace("Destroy pio process")
      processHandler?.destroyProcess()
      processHandler = null
    }
    super.dispose()
  }

  @Volatile
  private var pioBuffer: StringBuilder? = StringBuilder()

  private fun writePioOutput(outputType: Key<*>, text: String) {
    if (outputType == ProcessOutputType.STDERR) {
      com.jetbrains.cidr.cpp.embedded.platformio.project.LOG.warn(text)
    }
    else {
      com.jetbrains.cidr.cpp.embedded.platformio.project.LOG.trace(text)
    }
    pioBuffer?.also {
      it.append(text)
      browser.loadHTML("<pre>$it</pre>")
    }
  }

  override fun beforeShowCallback() {
    super.beforeShowCallback()
    ApplicationManager.getApplication().executeOnPooledThread {
      val commandLine = PlatfromioCliBuilder(null)
        .withRedirectErrorStream(true)
        .withParams("home", "--port", portNumber.toString(), "--no-open").build()
      try {
        processHandler = object : KillableProcessHandler(commandLine) {
          override fun readerOptions(): BaseOutputReader.Options = BaseOutputReader.Options.forMostlySilentProcess()
        }
          .apply {
            addProcessListener(
              object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) =
                  writePioOutput(outputType, event.text)
              }
            )
            startNotify()
          }
        var connectable = false
        for (i in 0..100) {
          try {
            HttpRequests.request("http://127.0.0.1:$portNumber/wsrpc").connectTimeout(200).readTimeout(200).tryConnect()
          }
          catch (_: HttpRequests.HttpStatusException) {
            connectable = true
            break
          }
          catch (_: IOException) {
            Thread.sleep(100)
            continue
          }
          connectable = true
          break
        }
        if (connectable) {
          webClient = PlatformioWebSocketClient(port = portNumber,
                                                projectOpener = this::openProject,
                                                documentOpener = this::openDocument,
                                                diagnostics = this::setErrorText)
            .apply(PlatformioWebSocketClient::connect)
          pioBuffer = null
          browser.loadURL("http://127.0.0.1:$portNumber/")
        }
      }
      catch (e: Throwable) {
        notifyPlatformioNotFound(project)
        SwingUtilities.invokeLater {
          close(CANCEL_EXIT_CODE)
        }
      }
    }
  }

  private fun openDocument(location: String?) {
    if (location?.endsWith(PlatformioFileType.FILE_NAME) == true) {
      SwingUtilities.invokeAndWait {
        close(OK_EXIT_CODE)
        PlatformioUsagesCollector.FILE_OPEN_VIA_HOME_ID.log(project)
        documentLocationToOpen = location
      }
    }
    else {
      Messages.showMessageDialog(rootPane, ClionEmbeddedPlatformioBundle.message("dialog.message.cant.open.file", location),
                                 ClionEmbeddedPlatformioBundle.message("dialog.title.open.file"), AllIcons.General.WarningDialog)
    }
  }

  private fun openProject(location: String?) {
    if (location != null) {
      PlatformioUsagesCollector.PROJECT_OPEN_VIA_HOME_ID.log(project)
      SwingUtilities.invokeLater {
        close(OK_EXIT_CODE)
        service<TrustedPathsSettings>().addTrustedPath(location)
        projectLocationToOpen = location
      }
    }
  }

}