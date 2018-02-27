// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.cli

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.io.jsonRpc.JsonRpcServer
import org.jetbrains.io.jsonRpc.socket.RpcBinaryRequestHandler
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Irina.Chernushina on 1/26/2018.
 */
class VueCreateProjectProcess(private val folder: Path,
                              private val projectName: String,
                              private val templateName: String,
                              private val interpreterRef: NodeJsInterpreterRef,
                              private val packagePath: String,
                              parentDisposable: Disposable?) : Disposable {
  companion object {
    private const val DOMAIN = "vue-create-project"
    val LOG: Logger = Logger.getInstance(VueCreateProjectProcess::class.java)
  }
  private val processHandlerRef = AtomicReference<KillableProcessHandler>()
  private val questionRef = AtomicReference<Question>()
  @Volatile private var error: String? = null
  @Volatile private var processState = VueProjectCreationState.Starting

  private val serverDisposer = Disposable {}
  private val rpcServer: JsonRpcServer
  @Volatile private var lastMessage: String? = null
  @Volatile var listener: (() -> Unit)? = null

  init {
    if (parentDisposable != null) Disposer.register(parentDisposable, this)
    Disposer.register(this, serverDisposer)
    rpcServer = RpcBinaryRequestHandler.getRpcServerInstance()
    rpcServer.registerDomain(DOMAIN, NotNullLazyValue.createConstantValue(object {
      @Suppress("unused")
      fun question(serializedObject: String, validationError: String?) {
        processState = VueProjectCreationState.Process
        val question = deserializeQuestion(serializedObject, validationError)
        if (question == null) {
          LOG.info("Vue Create Project: Can not parse question: " + serializedObject)
          error = "Can not parse question: " + serializedObject
          processState = VueProjectCreationState.Error
          listener?.invoke()
          return
        }
        questionRef.set(question)
        listener?.invoke()
      }

      @Suppress("unused")
      fun error(message: String) {
        error = message
        processState = VueProjectCreationState.Error
        listener?.invoke()
      }

      @Suppress("unused")
      fun questionsFinished() {
        logProgress("questions finished")
        processState = VueProjectCreationState.QuestionsFinished
        sendCancel()
        Disposer.dispose(serverDisposer)
        listener?.invoke()
      }

      @Suppress("unused")
      fun notifyStarted() {
        logProgress("notify started")
        val packagePath = Paths.get(packagePath)
        rpcServer.send(DOMAIN, "start", packagePath.parent.normalize().toString(), packagePath.fileName.toString(), templateName, projectName)
      }
    }), false, serverDisposer)

    startProcess()
  }

  private fun startProcess() {
    val task: Task.Backgroundable = object : Task.Backgroundable(null, "Preparing Vue project generation service...",
                                                                 false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      override fun run(indicator: ProgressIndicator) {
        val handler = createPeerVueCliProcess(indicator)
        if (handler == null) {
          // if no peer, stop rpc server
          Disposer.dispose(serverDisposer)
        }
        else {
          processHandlerRef.set(handler)
        }
      }
    }
    if (ProjectManager.getInstance().openProjects.isEmpty()) {
      ApplicationManager.getApplication().executeOnPooledThread { task.run(EmptyProgressIndicator()) }
    } else {
      ProgressManager.getInstance().run(task)
    }
  }

  private fun deserializeQuestion(serializedObject: String, validationError: String?): Question? {
    val obj = JsonParser().parse(serializedObject) as? JsonObject ?: return null
    val type = obj["type"]?.asString ?: return null
    val questionType = QuestionType.read(type) ?: return null
    val choicesList = mutableListOf<Choice>()
    val choices = obj["choices"]?.asJsonArray
    if (choices != null) {
      for (i in 0 until choices.size()) {
        val choice = choices[i] as? JsonObject ?: return null
        val name = choice["name"]?.asString ?: return null
        val value = choice["value"]?.asString ?: return null
        choicesList.add(Choice(name, value))
      }
    }
    val message = obj["message"]?.asString ?: return null
    val defaultVal = obj["default"]?.asString ?: ""

    return Question(questionType, message, defaultVal, choicesList, validationError)
  }

  private fun logProgress(message: String, error: Boolean = false) {
    if (error) LOG.info("From service: " + message)
    else LOG.debug("From service: " + message)
  }

  fun waitForProcessTermination(indicator: ProgressIndicator) {
    val handler = processHandlerRef.get() ?: return
    val lastMessageCopy = lastMessage
    if (lastMessageCopy != null) indicator.text = filterProgressText(lastMessageCopy)
    handler.addProcessListener(object: ProcessAdapter() {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (event.text.any { Character.isLetter(it) }) {
          indicator.text = filterProgressText(event.text)
        }
      }
    })
    handler.waitFor()
  }

  private fun filterProgressText(text: String): String {
    val sb = StringBuilder()
    text.forEach { if (it.toInt() <= 255) sb.append(it) }
    return sb.toString().trim()
  }

  private fun createPeerVueCliProcess(indicator: ProgressIndicator): KillableProcessHandler? {
    val path = Paths.get(PathManager.getSystemPath(), "projectGenerators", "vue")
    val folder = path.toFile()
    if (Files.exists(path)) {
      indicator.text = "Clearing Vue project generation service folder..."
      FileUtil.delete(folder)
    }
    if (!FileUtil.createDirectory(folder)) {
      return reportError("Can not create service directory " + path)
    }
    val interpreter = interpreterRef.resolveAsLocal(ProjectManager.getInstance().defaultProject)
    val interpreterPath = interpreter.interpreterSystemDependentPath

    indicator.text = "Installing Vue project generation packages..."
    val installCommandLine = NodeCommandLineUtil.createNpmCommandLine(folder, interpreter, listOf("i", "ij-rpc-client"))
    val output = CapturingProcessHandler(installCommandLine).runProcess(TimeUnit.MINUTES.toMillis(5).toInt(), true)
    if (output.exitCode != 0) {
      return reportError("Can not install 'ij-rpc-client': " + output.stderr)
    }

    indicator.text = "Starting Vue project generation service..."
    val commandLine = createCommandLine(folder, interpreterPath) ?: return reportError("Can not run Vue project generation service")
    val processHandler: KillableProcessHandler?
    try {
      processHandler = KillableProcessHandler(commandLine)
    }
    catch (e: ExecutionException) {
      return reportError(e.message)
    }

    attachGenerationProcessListener(processHandler)

    processHandler.startNotify()
    return processHandler
  }

  private fun attachGenerationProcessListener(processHandler: KillableProcessHandler) {
    processHandler.addProcessListener(object : ProcessAdapter() {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        val isError = ProcessOutputType.isStderr(outputType)
        if (processState == VueProjectCreationState.Starting && isError) {
          error = event.text
          processState = VueProjectCreationState.Error
          listener?.invoke()
        }
        if (event.text.any { Character.isLetter(it) }) {
          lastMessage = event.text
        }
        logProgress(event.text, isError)
      }

      override fun processTerminated(event: ProcessEvent) {
        processState = VueProjectCreationState.Finished
        listener?.invoke()
        Disposer.dispose(this@VueCreateProjectProcess)
      }
    })
  }

  private fun reportError(message: String?): Nothing? {
    error = message
    processState = VueProjectCreationState.Error
    listener?.invoke()
    return null
  }

  private fun createCommandLine(folder: File, interpreterPath: String): GeneralCommandLine? {
    val serviceFolder = JSLanguageServiceUtil.getPluginDirectory(this.javaClass, "vueCliClient")
    if (serviceFolder == null) {
      LOG.info("Can not find generator script parent folder")
    }
    val targetName = "call-vue-cli-init.js"
    val targetPath = Paths.get(serviceFolder.path, targetName)
    if (!Files.exists(targetPath)) {
      LOG.info("Can not find generator script")
      return null
    }
    val copy = File(folder, targetName)
    FileUtil.copyFileOrDir(targetPath.toFile(), copy)
    val commandLine = GeneralCommandLine(interpreterPath)
    commandLine.workDirectory = this.folder.toFile()
//    commandLine.addParameter("--inspect-brk=61389")
    commandLine.addParameter(FileUtil.toSystemDependentName(copy.path))
    commandLine.addParameter(BuiltInServerManager.getInstance().port.toString())
    return commandLine
  }

  fun getState(): State {
    val currentProcessState = processState

    assert(currentProcessState != VueProjectCreationState.User)

    if (VueProjectCreationState.Starting == currentProcessState ||
        VueProjectCreationState.Finished == currentProcessState ||
        VueProjectCreationState.QuestionsFinished == currentProcessState)
      return VueCreateProjectProcess.State(currentProcessState, null, null)
    if (VueProjectCreationState.Error == currentProcessState) return State(currentProcessState, error, null)
    return State(currentProcessState, null, questionRef.get())
  }

  fun answer(answer: String) {
    assert(questionRef.get() != null)
    rpcServer.send(DOMAIN, "answer", answer)
  }

  fun answer(answer: List<String>) {
    assert(questionRef.get() != null)
    rpcServer.send(DOMAIN, "answerCheckbox", answer.toTypedArray())
  }

  fun cancel() {
    sendCancel()
    stopProcess()
    Disposer.dispose(this)
  }

  private fun sendCancel() {
    rpcServer.send(DOMAIN, "cancel")
  }

  override fun dispose() {
    logProgress("Dispose called")
    stopProcess()
  }

  private fun stopProcess() {
    if (processState != VueProjectCreationState.QuestionsFinished && processState != VueProjectCreationState.Finished) {
      val handler = processHandlerRef.get()
      handler?.destroyProcess()
      processState = VueProjectCreationState.Finished
    }
  }

  class State(val processState: VueProjectCreationState, val globalProblem: String?, val question: Question?)

  class Question(val type: QuestionType, val message: String, val defaultVal: String, val choices: List<Choice>, val validationError: String?)

  class Choice(val name: String, val value: String)

  enum class QuestionType {
    Input, Confirm, List, Checkbox;

    companion object {
      fun read(value: String): QuestionType? {
        if ("input" == value) return Input
        if ("confirm" == value) return Confirm
        if ("list" == value) return List
        if ("checkbox" == value) return Checkbox
        return null
      }
    }
  }
}

enum class VueProjectCreationState {
  Starting, Process, User, Error, QuestionsFinished, Finished
}