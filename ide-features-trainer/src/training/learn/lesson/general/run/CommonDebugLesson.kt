// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.general.run

import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.icons.AllIcons
import com.intellij.ide.impl.DataManagerImpl
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.*
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.XDebuggerManagerImpl
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil
import com.intellij.xdebugger.impl.frame.XDebuggerFramesList
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import org.fest.swing.timing.Timeout
import training.commands.kotlin.TaskContext
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.keymap.KeymapUtil
import training.learn.CourseManager
import training.learn.interfaces.Module
import training.learn.lesson.LessonManager
import training.learn.lesson.kimpl.*
import training.ui.LearningUiHighlightingManager
import training.ui.LearningUiManager
import training.ui.LearningUiUtil
import training.util.WeakReferenceDelegator
import java.awt.Rectangle
import java.util.concurrent.TimeUnit
import javax.swing.JDialog
import javax.swing.text.JTextComponent

abstract class CommonDebugLesson(module: Module, id: String, languageId: String) : KLesson(id, "Debug workflow", module, languageId) {
  protected abstract val configurationName: String
  protected abstract val sample: LessonSample
  protected abstract val quickEvaluationArgument: String
  protected abstract val expressionToBeEvaluated: String
  protected abstract val confNameForWatches: String
  protected abstract val debuggingMethodName: String
  protected abstract val methodForStepInto: String
  protected abstract val stepIntoDirection: String

  protected var mayBeStopped: Boolean = false
  private var debugSession: XDebugSession? by WeakReferenceDelegator()

  private val incorrectBreakPointsMessage = "Breakpoints are set incorrect for this lesson."

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    prepareTask()

    toggleBreakpointTask()

    startDebugTask()

    returnToEditorTask()

    waitBeforeContinue(500)

    addToWatchTask()

    stepIntoTasks()

    waitBeforeContinue(500)

    quickEvaluateTask()

    fixTheErrorTask()

    applyProgramChangeTasks()

    stepOverTask()

    if (TaskTestContext.inTestMode) waitBeforeContinue(1000)

    resumeTask()

    muteBreakpointsTask()

    waitBeforeContinue(500)

    caret(sample.getPosition(3))

    runToCursorTask()

    if (TaskTestContext.inTestMode) waitBeforeContinue(1000)

    evaluateExpressionTasks()

    stopTask()
  }

  private fun LessonContext.prepareTask() {
    var needToRun = false
    prepareRuntimeTask {
      runWriteAction {
        needToRun = !selectedNeedConfiguration() && !configureDebugConfiguration()
      }
    }

    if (needToRun) {
      // Normally this step should not be shown!
      task {
        text("Before debugging let's run the program and see what is going wrong ${action("RunClass")}.")
        addFutureStep {
          subscribeForMessageBus(RunManagerListener.TOPIC, object : RunManagerListener {
            override fun runConfigurationSelected(settings: RunnerAndConfigurationSettings?) {
              if (selectedNeedConfiguration()) {
                completeStep()
              }
            }
          })
        }
      }
    }
  }

  private fun LessonContext.toggleBreakpointTask() {
    lateinit var logicalPosition: LogicalPosition
    task {
      before {
        logicalPosition = editor.offsetToLogicalPosition(sample.startOffset)
      }
      triggerByPartOfComponent<EditorGutterComponentEx> l@{ ui ->
        if (CommonDataKeys.EDITOR.getData(ui as DataProvider) != editor) return@l null
        val y = editor.visualLineToY(editor.logicalToVisualPosition(logicalPosition).line)
        return@l Rectangle(20, y, ui.width - 26, editor.lineHeight)
      }
    }

    prepareRuntimeTask {
      runWriteAction {
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        breakpointManager.allBreakpoints.forEach { breakpointManager.removeBreakpoint(it) }
      }
    }

    task("ToggleLineBreakpoint") {
      text(
        "So there is a problem. Let's start investigation with placing breakpoint." +
        "To toggle a breakpoint you should click left editor gutter or just press ${action(it)}.")
      stateCheck {
        lineWithBreakpoints() == setOf(logicalPosition.line)
      }
      proposeRestore {
        val breakpoints = lineWithBreakpoints()
        if (breakpoints.isNotEmpty() && breakpoints != setOf(logicalPosition.line)) {
          incorrectBreakPointsMessage
        }
        else null
      }
      test { actions(it) }
    }

    prepareRuntimeTask {
      setRestoreForBreakpoints(logicalPosition)
    }
  }

  private fun LessonContext.startDebugTask() {
    highlightButtonById("Debug")

    mayBeStopped = false
    task("Debug") {
      text("To start debug selected run configuration you need to click at ${icon(AllIcons.Actions.StartDebugger)}. " +
           "Or you can just press ${action(it)}.")
      addFutureStep {
        project.messageBus.connect(lessonDisposable).subscribe(XDebuggerManager.TOPIC, object : XDebuggerManagerListener {
          override fun processStarted(debugProcess: XDebugProcess) {
            mayBeStopped = false
            val debugSession = debugProcess.session
            (this@CommonDebugLesson).debugSession = debugSession

            debugSession.setBreakpointMuted(false)
            (debugSession as XDebugSessionImpl).setWatchExpressions(emptyList())
            debugSession.addSessionListener(object : XDebugSessionListener {
              override fun sessionPaused() {
                invokeLater { completeStep() }
              }
            }, lessonDisposable)
            debugSession.addSessionListener(object : XDebugSessionListener {
              override fun sessionStopped() {
                val activeToolWindow = LearningUiManager.activeToolWindow
                if (activeToolWindow != null && !mayBeStopped && LessonManager.instance.currentLesson == this@CommonDebugLesson) {
                  val notification = TaskContext.RestoreNotification("Debug session has been stopped. Need to restart lesson.") {
                    CourseManager.instance.openLesson(activeToolWindow.project, this@CommonDebugLesson)
                  }
                  LessonManager.instance.setRestoreNotification(notification)
                }
              }
            }, lessonDisposable)
          }
        })
      }
      test { actions(it) }
    }

    task {
      stateCheck {
        focusOwner is XDebuggerFramesList
      }
    }
  }

  private fun LessonContext.returnToEditorTask() {
    task("EditorEscape") {
      before {
        LearningUiHighlightingManager.clearHighlights()
      }
      text("Many trace actions will focus debug toolwindow. Lets return to the editor with ${action(it)}")
      stateCheck {
        focusOwner is EditorComponentImpl
      }
      test {
        Thread.sleep(500)
        GuiTestUtil.shortcut(Key.ESCAPE)
      }
    }
  }

  private fun LessonContext.addToWatchTask() {
    highlightButtonById("XDebugger.NewWatch")

    task("Debugger.AddToWatch") {
      val position = sample.getPosition(1)
      val needAddToWatch = position.selection?.let { pair -> sample.text.substring(pair.first, pair.second) }
                           ?: error("Invalid sample data")
      caret(position)
      val hasShortcut = KeymapUtil.getShortcutByActionId(it) != null
      text("IDE has several ways to show values. For this step, we selected the call. Lets add it to <strong>Watches</strong>. " +
           "You can copy the expression into clipboard, use ${
             icon(AllIcons.General.Add)
           } button on the debug panel and paste copied text. " +
           "Or you can just use action ${action(it)} ${if (hasShortcut) "" else " (consider to add a shortcut for it later)"}.")
      stateCheck {
        val watches = (XDebuggerManager.getInstance(project) as XDebuggerManagerImpl).watchesManager.getWatches(confNameForWatches)
        watches.any { watch -> watch.expression == needAddToWatch }
      }
      test { actions(it) }
    }
  }

  private fun LessonContext.stepIntoTasks() {
    highlightButtonById("StepInto")

    actionTask("StepInto") {
      "Lets step into. You can use action ${action(it)} or the button ${icon(AllIcons.Actions.TraceInto)} at the debug panel."
    }

    task {
      before {
        LearningUiHighlightingManager.clearHighlights()
      }
      text("In most cases you will want to skip argument calculating so Smart Step Into feature suggest by default the wrapping method. " +
           "But here we need to choose the second one: ${code(methodForStepInto)}. You can choose it by keyboard " +
           "<raw_action>$stepIntoDirection</raw_action> and press ${action("EditorEnter")} or you can click the call by mouse.")
      stateCheck {
        val debugLine = debugSession?.currentStackFrame?.sourcePosition?.line
        val sampleLine = editor.offsetToLogicalPosition(sample.getPosition(2).startOffset).line
        debugLine == sampleLine
      }
      test {
        Thread.sleep(500)
        GuiTestUtil.shortcut(if (stepIntoDirection == "→") Key.RIGHT else Key.LEFT)
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
  }

  private fun LessonContext.quickEvaluateTask() {
    task("QuickEvaluateExpression") {
      before {
        LearningUiHighlightingManager.clearHighlights()
      }
      caret(sample.getPosition(2))
      text("Lets see what we are going to pass to ${code(quickEvaluationArgument)}. We selected the argument. " +
           "Invoke Quick Evaluate Expression ${action(it)}.")
      trigger(it)
      test { actions(it) }
    }
  }

  private fun LessonContext.fixTheErrorTask() {
    task {
      text("Oh, we made a mistake in the array index! Lets fix it right now. " +
           "Close popup (${action("EditorEscape")}) and change 0 to 1.")
      val needed = sample.text.replaceFirst("[0]", "[1]")
      stateCheck {
        editor.document.text == needed
      }
      test {
        GuiTestUtil.shortcut(Key.ESCAPE)
        invokeLater {
          WriteCommandAction.runWriteCommandAction(project) {
            val offset = sample.text.indexOf("[0]")
            editor.selectionModel.removeSelection()
            editor.document.replaceString(offset + 1, offset + 2, "1")
            PsiDocumentManager.getInstance(project).commitAllDocuments()
          }
        }
      }
    }
  }

  private fun LessonContext.stepOverTask() {
    highlightButtonById("StepOver")

    actionTask("StepOver") {
      "Let's check that the call of ${code("extract_number")} will not throw an exception now. " +
      "Use Step Over action ${action(it)} or click the button ${icon(AllIcons.Actions.TraceOver)}."
    }
  }

  private fun LessonContext.resumeTask() {
    highlightButtonById("Resume")

    actionTask("Resume") {
      "Seems no exceptions by now. Let's continue execution with ${action(it)} or" +
      "click the button ${icon(AllIcons.Actions.Resume)}."
    }
  }

  private fun LessonContext.muteBreakpointsTask() {
    highlightButtonById("XDebugger.MuteBreakpoints")

    actionTask("XDebugger.MuteBreakpoints") {
      "Ups, same breakpoint again. Now we don't need to stop at this breakpoint. " +
      "So let's mute breakpoints by the button ${icon(AllIcons.Debugger.MuteBreakpoints)} or by action ${action(it)}."
    }
  }

  private fun LessonContext.runToCursorTask() {
    highlightButtonById("RunToCursor")

    actionTask("RunToCursor") {
      "Let's check the result of ${code(debuggingMethodName)}. " +
      "We've moved the editor cursor to the ${code("return")} statement. " +
      "Lets use <strong>Run to Cursor</strong> action ${action(it)} or click ${icon(AllIcons.Actions.RunToCursor)}. " +
      "Note that <strong>Run to Cursor</strong> works even if breakpoints are muted."
    }
  }

  private fun LessonContext.evaluateExpressionTasks() {
    highlightButtonById("EvaluateExpression")

    actionTask("EvaluateExpression") {
      "It seems the ${code("result")} is not an average we want to find. " +
      "We forgot to divide by length. Seems we need to return ${code(expressionToBeEvaluated)}. " +
      "Let's calculate this expresion. Invoke the <strong>Evaluate Expression</strong> action by pressing ${action(it)} or " +
      "click button ${icon(AllIcons.Debugger.EvaluateExpression)}. "
    }

    task(expressionToBeEvaluated) {
      text("Type ${code(it)} into the <strong>Expression</strong> field, completion works.")
      stateCheck { checkWordInTextField(it) }
      test {
        type(it)
      }
    }

    task {
      before {
        LearningUiHighlightingManager.clearHighlights()
      }
      text("Click <strong>Evaluate</strong> or hit ${action("EditorEnter")}.")
      triggerByUiComponentAndHighlight(highlightBorder = false, highlightInside = false) { debugTree: XDebuggerTree ->
        val dialog = UIUtil.getParentOfType(JDialog::class.java, debugTree)
        val root = debugTree.root
        dialog?.title == "Evaluate" && root?.children?.size == 1
      }
      test { GuiTestUtil.shortcut(Key.ENTER) }
    }
  }

  private fun LessonContext.stopTask() {
    highlightButtonById("Stop")

    actionTask("Stop") {
      before { mayBeStopped = true }
      "It will be a correct answer! Lets close the dialog and stop debugging by ${action(it)}" +
      "or button ${icon(AllIcons.Actions.Suspend)}."
    }
  }

  private fun TaskRuntimeContext.setRestoreForBreakpoints(logicalPosition: LogicalPosition) {
    fun check() {
      invokeLater {
        if (lineWithBreakpoints() != setOf(logicalPosition.line)) {
          val notification = TaskContext.RestoreNotification(incorrectBreakPointsMessage) {
            runWriteAction {
              LessonManager.instance.clearRestoreMessage()
              val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
              breakpointManager.allBreakpoints.forEach { breakpointManager.removeBreakpoint(it) }
              FileDocumentManager.getInstance().getFile(editor.document)
              val line = logicalPosition.line
              val createPosition = XDebuggerUtil.getInstance().createPosition(virtualFile, line)
                                   ?: error("Can't create source position: $line at $virtualFile")
              XBreakpointUtil.toggleLineBreakpoint(project, createPosition, editor, false, false, true)
              //breakpointManager.addLineBreakpoint()
            }
          }
          LessonManager.instance.setRestoreNotification(notification)
        }
      }
    }
    project.messageBus.connect(lessonDisposable).subscribe(XBreakpointListener.TOPIC, object : XBreakpointListener<XBreakpoint<*>> {
      override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        check()
      }

      override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        check()
      }

      override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
        check()
      }
    })
  }

  protected abstract fun LessonContext.applyProgramChangeTasks()

  protected fun LessonContext.highlightButtonById(actionId: String) {
    val needToFindButton = ActionManager.getInstance().getAction(actionId)
    prepareRuntimeTask {
      LearningUiHighlightingManager.clearHighlights()
      ApplicationManager.getApplication().executeOnPooledThread {
        val result = LearningUiUtil.findAllShowingComponentWithTimeout(null, ActionButton::class.java,
                                                                       Timeout.timeout(1,
                                                                                       TimeUnit.SECONDS)) { ui ->
          ui.action == needToFindButton
          // Some buttons are duplicated to several tab-panels. It is a way to find an active one.
          && UIUtil.getParentOfType(JBTabsImpl.Toolbar::class.java, ui)?.location?.x != 0
        }

        invokeLater {
          for (button in result) {
            LearningUiHighlightingManager.highlightComponent(button)
          }
        }
      }
    }
  }

  private fun TaskRuntimeContext.selectedNeedConfiguration(): Boolean {
    val runManager = RunManager.getInstance(project)
    val selectedConfiguration = runManager.selectedConfiguration
    return selectedConfiguration?.name == configurationName
  }

  private fun TaskRuntimeContext.configureDebugConfiguration(): Boolean {
    val runManager = RunManager.getInstance(project)
    val dataContext = DataManagerImpl.getInstance().getDataContext(editor.component)
    val configurationsFromContext = ConfigurationContext.getFromContext(dataContext).configurationsFromContext

    val configuration = configurationsFromContext?.singleOrNull() ?: return false
    runManager.addConfiguration(configuration.configurationSettings)
    runManager.selectedConfiguration = configuration.configurationSettings
    return true
  }

  private fun TaskRuntimeContext.checkWordInTextField(expected: String): Boolean =
    (focusOwner as? JTextComponent)?.text?.replace(" ", "")?.toLowerCase() == expected.toLowerCase().replace(" ", "")
}
