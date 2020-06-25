// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.run

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
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.psi.PsiDocumentManager
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.util.Key
import com.intellij.ui.tabs.impl.JBTabsImpl.Toolbar
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.*
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.XDebuggerManagerImpl
import com.intellij.xdebugger.impl.frame.XDebuggerFramesList
import org.fest.swing.timing.Timeout
import training.commands.kotlin.TaskRuntimeContext
import training.commands.kotlin.TaskTestContext
import training.keymap.KeymapUtil
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.lineWithBreakpoints
import training.learn.lesson.kimpl.subscribeForMessageBus
import training.ui.LearningUiHighlightingManager
import training.ui.LearningUiUtil
import java.awt.Rectangle
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import javax.swing.text.JTextComponent

class JavaDebugLesson(module: Module) : KLesson("Debug Workflow", module, "JAVA") {
  private val demoClassName = JavaRunLessonsUtils.demoClassName
  private val sample = JavaRunLessonsUtils.demoSample

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)

    var needToRun = false
    prepareRuntimeTask {
      runWriteAction {
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        breakpointManager.allBreakpoints.forEach { breakpointManager.removeBreakpoint(it) }

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

      lateinit var logicalPosition: LogicalPosition
      task {
        before {
          logicalPosition = editor.offsetToLogicalPosition(sample.startOffset)
        }
        triggerByPartOfComponent<EditorGutterComponentEx> l@{ ui ->
          if (CommonDataKeys.EDITOR.getData(ui as DataProvider) != editor) return@l null
          val y = editor.visualLineToY(editor.logicalToVisualPosition(logicalPosition).line)
          return@l Rectangle(18, y, ui.width - 18, editor.lineHeight)
        }
      }

      task("ToggleLineBreakpoint") {
        text("So there is a problem. Let's start investigation with placing breakpoint. To toggle a breakpoint you should click left editor gutter or just press ${action(it)}.")
        stateCheck {
          lineWithBreakpoints() == setOf(logicalPosition.line)
        }
        test { actions(it) }
      }

      highlightButtonById("Debug")

      lateinit var debugSession: XDebugSession
      task("Debug") {
        text("To start debug selected run configuration you need to click at ${icon(AllIcons.Actions.StartDebugger)}. " +
             "Or you can just press ${action(it)}.")
        addFutureStep {
          project.messageBus.connect(disposable).subscribe(XDebuggerManager.TOPIC, object : XDebuggerManagerListener {
            override fun processStarted(debugProcess: XDebugProcess) {
              debugSession = debugProcess.session
              debugSession.setBreakpointMuted(false)
              (debugSession as XDebugSessionImpl).setWatchExpressions(emptyList())
              debugSession.addSessionListener(object : XDebugSessionListener {
                override fun sessionPaused() {
                  invokeLater { completeStep() }
                }
              }, disposable)
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

      task("EditorEscape") {
        before {
          LearningUiHighlightingManager.clearHighlights()
        }
        text("Many trace actions will focus debug toolwindow. Lets return to the editor with ${action(it)}")
        stateCheck {
          focusOwner is EditorComponentImpl
        }
        test {
          sleep(500)
          GuiTestUtil.shortcut(Key.ESCAPE)
        }
      }

      waitBeforeContinue(500)

      highlightButtonById("XDebugger.NewWatch")

      task("Debugger.AddToWatch") {
        val position = sample.getPosition(1)
        val needAddToWatch = position.selection?.let { pair -> sample.text.substring(pair.first, pair.second) }
                             ?: error("Invalid sample data")
        caret(position)
        val hasShortcut = KeymapUtil.getShortcutByActionId(it) != null
        text("IDE has several ways to show values. For this step, we selected the call. Lets add it to <strong>Watches</strong>. " +
             "You can copy the expression into clipboard, use ${icon(AllIcons.General.Add)} button on the debug panel and past copied text. " +
             "Or you can just use action ${action(it)} ${if (hasShortcut) "" else " (consider to add a shortcut for it later)"}.")
        stateCheck {
          val watches = (XDebuggerManager.getInstance(project) as XDebuggerManagerImpl).watchesManager.getWatches("Application")
          watches.any { watch -> watch.expression == needAddToWatch }
        }
        test { actions(it) }
      }

      highlightButtonById("StepInto")

      actionTask("StepInto") {
        "Lets step into. You can use action ${action(it)} or the button ${icon(AllIcons.Actions.TraceInto)} at the debug panel."
      }

      task {
        text("In most cases you will want to skip argument calculating so Smart Step Into feature suggest by default the wrapping method." +
             "But here we need to choose the second one: ${code("extractNumber")}. " +
             "You can choose it by keyboard <raw_action>→</raw_action> and press ${action("EditorEnter")} or you can click the call by mouse.")
        stateCheck {
          val debugLine = debugSession.currentStackFrame?.sourcePosition?.line
          val sampleLine = editor.offsetToLogicalPosition(sample.getPosition(2).startOffset).line
          debugLine == sampleLine
        }
        test {
          sleep(500)
          GuiTestUtil.shortcut(Key.RIGHT)
          GuiTestUtil.shortcut(Key.ENTER)
        }
      }

      waitBeforeContinue(500)

      task("QuickEvaluateExpression") {
        before {
          LearningUiHighlightingManager.clearHighlights()
        }
        caret(sample.getPosition(2))
        text("Lets see what we are going to pass to ${code("Integer.parseInt")}. We selected the argument. " +
             "Invoke Quick Evaluate Expression ${action(it)}.")
        trigger(it)
        test { actions(it) }
      }

      task("EditorSelectWord") {
        text("Oh, we made a mistake in the array index! Lets fix it right now. " +
             "Close popup (${action("EditorEscape")}) and change 0 to 1.")
        val needed = sample.text.replace("[0]", "[1]")
        stateCheck {
          editor.document.text == needed
        }
        test {
          GuiTestUtil.shortcut(Key.ESCAPE)
          invokeLater {
            runWriteCommandAction(project) {
              val offset = sample.text.indexOf("[0]")
              editor.selectionModel.removeSelection()
              editor.document.replaceString(offset + 1, offset + 2, "1")
              PsiDocumentManager.getInstance(project).commitAllDocuments()
            }
          }
        }
      }

      highlightButtonById("CompileDirty")

      task("CompileDirty") {
        text("For big programs rerun can take too much time. " +
             "When you find some mistake in pure method you can just rebuild the project and apply <strong>Hot Swap</strong> JVM feature. " +
             "Let's build project: ${action(it)} or ${icon(AllIcons.Actions.Compile)}.")
        stateCheck {
          inHotSwapDialog()
        }
        test { actions(it) }
      }

      task {
        text("Confirm <strong>Hot Swap</strong> replacement")
        stateCheck {
          !inHotSwapDialog()
        }
        test {
          with(TaskTestContext.guiTestCase) {
            dialog(null, needToKeepDialog=true) {
              button("Yes").click()
            }
          }
        }
      }

      highlightButtonById("Debugger.PopFrame")

      actionTask("Debugger.PopFrame") {
        "We patched our method, but right now we are still executing old obsolete ${code("extractNumber")} and it will throw the exception again. " +
             "Let's drop the frame and return to the state before ${code("extractNumber")} call. " +
             "Click ${icon(AllIcons.Actions.PopFrame)} at the debug panel or use ${action(it)}."
      }

      highlightButtonById("StepOver")

      actionTask("StepOver") {
        "Let's check that the call of ${code("extractNumber")} will not throw an exception now. " +
             "Use Step Over action ${action(it)} or click the button ${icon(AllIcons.Actions.TraceOver)}."
      }

      highlightButtonById("Resume")

      actionTask("Resume") {
        "Seems no exceptions by now. Let's continue execution with ${action(it)} or" +
             "click the button ${icon(AllIcons.Actions.Resume)}."
      }

      highlightButtonById("XDebugger.MuteBreakpoints")

      actionTask("XDebugger.MuteBreakpoints") {
        "Ups, same breakpoint again. Note that ${code("extractNumber(s)")} in <strong>Watches</strong> has now another value. " +
             "Now we don't need to stop at this breakpoint. " +
             "So let's mute breakpoints by the button ${icon(AllIcons.Debugger.MuteBreakpoints)} or by action ${action(it)}."
      }

      waitBeforeContinue(500)

      caret(sample.getPosition(3))

      highlightButtonById("RunToCursor")

      actionTask("RunToCursor") {
        "Let's check the result of ${code("findAverage")}. " +
             "We've moved the editor cursor to the ${code("return")} statement. " +
             "Lets use <strong>Run to Cursor</strong> action ${action(it)} or click ${icon(AllIcons.Actions.RunToCursor)}. " +
             "Note that <strong>Run to Cursor</strong> works even if breakpoints are muted."
      }

      highlightButtonById("EvaluateExpression")

      actionTask("EvaluateExpression") {
        "It seems the ${code("result")} is not an average we want to find." +
             "We forgot to divide by length. Seems we need to return ${code("result/input.length")}. " +
             "Let's calculate this expresion. Invoke the <strong>Evaluate Expression</strong> action by pressing ${action(it)} or " +
             "click button ${icon(AllIcons.Debugger.EvaluateExpression)}. "
      }

      task("result/input.length") {
        text("Write ${code(it)}, completion works.")
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
        addFutureStep {
          debugSession.addSessionListener(object : XDebugSessionListener {
            override fun settingsChanged() {
              ApplicationManager.getApplication().invokeLater {
                completeStep()
              }
            }
          })
        }
        test { GuiTestUtil.shortcut(Key.ENTER) }
      }

      highlightButtonById("Stop")

      actionTask("Stop") {
        "It will be a correct answer! Lets close the dialog and stop debugging by ${action(it)}" +
             "or button ${icon(AllIcons.Actions.Suspend)}."
      }
    }

  private fun inHotSwapDialog(): Boolean {
    return Thread.currentThread().stackTrace.any { traceElement ->
      traceElement.className.contains("HotSwapUIImpl")
    }
  }

  private fun LessonContext.highlightButtonById(actionId: String) {
    val needToFindButton = ActionManager.getInstance().getAction(actionId)
    prepareRuntimeTask {
      LearningUiHighlightingManager.clearHighlights()
      ApplicationManager.getApplication().executeOnPooledThread {
        val result = LearningUiUtil.findAllShowingComponentWithTimeout(null, ActionButton::class.java,
                                                                       Timeout.timeout(1,
                                                                                       TimeUnit.SECONDS)) { ui ->
          ui.action == needToFindButton
          // Some buttons are duplicated to several tab-panels. It is a way to find an active one.
          && UIUtil.getParentOfType(Toolbar::class.java, ui)?.location?.x != 0
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
    return selectedConfiguration?.name == demoClassName
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

  override val testScriptProperties: TaskTestContext.TestScriptProperties
    get() = TaskTestContext.TestScriptProperties(duration = 20)

  override val fileName: String = "$demoClassName.java"
}
