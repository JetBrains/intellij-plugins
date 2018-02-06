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
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputType
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NotNullLazyValue
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.io.jsonRpc.JsonRpcServer
import org.jetbrains.io.jsonRpc.socket.RpcBinaryRequestHandler
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference

class TestAction: DumbAwareAction("Start rpc server") {
  override fun actionPerformed(e: AnActionEvent?) {
    VueCreateProjectProcess(Paths.get("D:\\testProjects\\cl111"), "test", "webpack",
                            NodeJsLocalInterpreter("D:\\node894\\bin\\node.exe", null).toRef(),
                            "C:\\Users\\Irina.Chernushina\\AppData\\Roaming\\npm\\node_modules\\vue-cli", true, null)
  }
}

/**
 * @author Irina.Chernushina on 1/26/2018.
 */
class VueCreateProjectProcess(private val folder: Path,
                              private val projectName: String,
                              private val templateName: String,
                              private val interpreterRef: NodeJsInterpreterRef,
                              private val packagePath: String,
                              isTest: Boolean = false,
                              parentDisposable: Disposable?) : Disposable {
  companion object {
    private const val DOMAIN = "vue-create-project"
    val LOG: Logger = Logger.getInstance(VueCreateProjectProcess::class.java)
  }
  private val processHandlerRef = AtomicReference<KillableProcessHandler>()
  private val questionRef = AtomicReference<Question>()
  @Volatile private var error: String? = null
  @Volatile var processState = ProcessState.Starting

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
        processState = ProcessState.Working
        val question = deserializeQuestion(serializedObject, validationError)
        if (question == null) {
          LOG.info("Vue Create Project: Can not parse question: " + serializedObject)
          error = "Can not parse question: " + serializedObject
          processState = ProcessState.Error
          listener?.invoke()
          return
        }
        questionRef.set(question)
        listener?.invoke()
      }

      @Suppress("unused")
      fun error(message: String) {
        error = message
        processState = ProcessState.Error
        listener?.invoke()
      }

      @Suppress("unused")
      fun questionsFinished() {
        logProgress("questions finished")
        processState = ProcessState.QuestionsFinished
        sendCancel()
        Disposer.dispose(serverDisposer)
        listener?.invoke()
      }

      @Suppress("unused")
      fun notifyStarted() {
        logProgress("notify started")
        rpcServer.send(DOMAIN, "start", Paths.get(packagePath).parent.normalize().toString(), templateName, projectName)
      }
    }), false, serverDisposer)

    if (!isTest) {
      ApplicationManager.getApplication().executeOnPooledThread {
        val handler = createPeerVueCliProcess()
        if (handler == null) {
          // if no peer, stop rpc server
          Disposer.dispose(serverDisposer)
        } else {
          processHandlerRef.set(handler)
        }
      }
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
    if (lastMessage != null) indicator.text = lastMessage
    handler.addProcessListener(object: ProcessAdapter() {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (event.text.any { Character.isLetter(it) }) {
          indicator.text = event.text
        }
      }
    })
    handler.waitFor()
  }

  private fun createPeerVueCliProcess(): KillableProcessHandler? {
    val commandLine = createCommandLine()
    val processHandler: KillableProcessHandler?
    try {
      processHandler = KillableProcessHandler(commandLine)
    }
    catch (e: ExecutionException) {
      error = e.message
      processState = ProcessState.Error
      listener?.invoke()
      return null
    }

    processHandler.addProcessListener(object: ProcessAdapter() {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        if (event.text.any { Character.isLetter(it) }) {
          lastMessage = event.text
        }
        logProgress(event.text, ProcessOutputType.isStderr(outputType))
      }

      override fun processTerminated(event: ProcessEvent) {
        processState = ProcessState.Finished
        listener?.invoke()
        Disposer.dispose(this@VueCreateProjectProcess)
      }
    })

    processHandler.startNotify()
    return processHandler
  }

  private fun createCommandLine(): GeneralCommandLine {
    val serviceFolder = JSLanguageServiceUtil.getPluginDirectory(this.javaClass, "vueCliClient")
    val interpreter = interpreterRef.resolveAsLocal(ProjectManager.getInstance().defaultProject).interpreterSystemDependentPath
    val commandLine = GeneralCommandLine(interpreter)
    commandLine.workDirectory = folder.toFile()
//    commandLine.addParameter("--inspect-brk=61389")
    commandLine.addParameter(Paths.get(serviceFolder.path, "call-vue-cli-init.js").normalize().toString())
    commandLine.addParameter(BuiltInServerManager.getInstance().port.toString())
    return commandLine
  }

  fun getState(): State {
    val currentProcessState = processState
    if (ProcessState.Starting == currentProcessState || ProcessState.Finished == currentProcessState)
      return VueCreateProjectProcess.State(currentProcessState, null, null)
    if (ProcessState.QuestionsFinished == currentProcessState) {
      return VueCreateProjectProcess.State(currentProcessState, null, null)
    }
    if (ProcessState.Error == currentProcessState) return State(currentProcessState, error, null)
    return State(currentProcessState, null, questionRef.get())
  }

  fun answer(answer: String) {
    assert(questionRef.get() != null)
    rpcServer.send(DOMAIN, "answer", answer)
  }

  fun cancel() {
    sendCancel()
    Disposer.dispose(this)
  }

  private fun sendCancel() {
    rpcServer.send(DOMAIN, "cancel")
  }

  override fun dispose() {
    logProgress("Dispose called")
    if (processState != ProcessState.QuestionsFinished) {
      val handler = processHandlerRef.get()
      handler?.destroyProcess()
    }
  }

  class State(val processState: ProcessState, val globalProblem: String?, val question: Question?)

  class Question(val type: QuestionType, val message: String, val defaultVal: String, val choices: List<Choice>, val validationError: String?)

  class Choice(val name: String, val value: String)

  enum class ProcessState {
    Starting, Error, Working, QuestionsFinished, Finished
  }

  enum class QuestionType {
    Input, Confirm, List;

    companion object {
      fun read(value: String): QuestionType? {
        if ("input" == value) return Input
        if ("confirm" == value) return Confirm
        if ("list" == value) return List
        return null
      }
    }
  }
}