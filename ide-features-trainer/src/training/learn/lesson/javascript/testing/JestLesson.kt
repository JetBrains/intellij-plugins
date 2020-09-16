// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.testing

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.RunManager
import com.intellij.execution.testframework.TestRunnerBundle
import com.intellij.execution.testframework.sm.SmRunnerBundle
import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.UIBundle
import com.intellij.ui.treeStructure.Tree
import training.lang.JavaScriptLangSupport
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil.productName
import training.learn.lesson.kimpl.dropMnemonic
import training.learn.lesson.kimpl.parseLessonSample
import java.awt.event.KeyEvent
import javax.swing.SwingConstants
import javax.swing.tree.DefaultMutableTreeNode

class JestLesson(module: Module)
  : KLesson("Fundamentals of Testing in WebStorm", LessonsBundle.message("js.testing.jest.title", productName), module,
            JavaScriptLangSupport.lang){
  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(parseLessonSample("""
          // Copyright 2004-present Facebook. All Rights Reserved.

          const sum = require('./sum');

          test('adds 1 + 2 to equal 3', () => {
            expect(sum(1, 2)).toBe(4);
          });
        """.trimIndent()))

        prepareRuntimeTask(ModalityState.current()) {
          val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
          val window: EditorWindow = fileEditorManager.currentWindow
          val file: VirtualFile = window.selectedFile

          WriteAction.run<Throwable> {
            fileEditorManager.createSplitter(SwingConstants.VERTICAL, window)
            val newWindow = fileEditorManager.getNextWindow(window)
            fileEditorManager.openFileWithProviders(file.parent.findChild("sum.js")!!, false, newWindow)
            newWindow.closeFile(file)
            fileEditorManager.currentWindow = window
          }
        }

        task("editRunConfigurations") {
          text(LessonsBundle.message("js.testing.jest.prepare", ActionsBundle.message("group.RunMenu.text").dropMnemonic(),
                                     ExecutionBundle.message(
                                       "edit.configuration.action").dropMnemonic(), strong("Jest"), strong("OK"),
                                     icon(AllIcons.General.Add)))
          stateCheck {
            RunManager.getInstance(project).findConfigurationByName(TestRunnerBundle.message("all.tests.scope.presentable.text")) != null
          }
        }

        task("Run") {
          text(LessonsBundle.message("js.testing.jest.run", strong(TestRunnerBundle.message("all.tests.scope.presentable.text")),
                                     ExecutionBundle.message(
                                       "run.configurable.display.name"), icon(AllIcons.RunConfigurations.TestState.Run)))
          trigger(it)
        }

        task {
          text(LessonsBundle.message("js.testing.jest.navigate",
                                     strong(UIBundle.message("tool.window.name.run")),
                                     icon(AllIcons.RunConfigurations.TestState.Run),
                                     icon(AllIcons.RunConfigurations.RerunFailedTests),
                                     icon(AllIcons.RunConfigurations.ShowPassed),
                                     strong(SmRunnerBundle.message("sm.test.runner.ui.tests.tree.presentation.labels.test.results")),
                                     strong("add"),
                                     shortcut(KeymapUtil.getKeyText(KeyEvent.VK_ENTER))))
          stateCheck {
            (focusOwner as? Tree)?.getSelectedNodes(DefaultMutableTreeNode::class.java, null)?.firstOrNull()?.toString() == "adds 1 + 2 to equal 3"
          }
        }

        task {
          text(LessonsBundle.message("js.testing.jest.double.click", strong("adds 1 + 2 to equal 3")))
          stateCheck {
            textAtCaretEqualsTo("toBe") && focusOwner is EditorComponentImpl
          }
        }

        task {
          text(LessonsBundle.message("js.testing.jest.fix.test", strong(SmRunnerBundle.message(
            "sm.test.runner.ui.tests.tree.presentation.labels.test.results")), strong(".toBe"), strong(".toBe"), strong("4"), strong("3")))
          stateCheck {
            textOnLine(5, "3")
          }
        }

        task {
          text(LessonsBundle.message("js.testing.jest.re.run.test", icon(AllIcons.RunConfigurations.TestState.Red2), strong(
            "Run adds 1 + 2 to equal 3")))
          stateCheck {
            RunManager.getInstance(project).findConfigurationByName("adds 1 + 2 to equal 3") != null
          }
        }

        task("Coverage") {
          text(LessonsBundle.message("js.testing.jest.success.run.coverage", icon(AllIcons.General.RunWithCoverage)))
          trigger(it)
        }

        task("HideActiveWindow") {
          text(LessonsBundle.message("js.testing.jest.coverage.result", shortcut(KeymapUtil.getShortcutText("HideActiveWindow"))))
          trigger(it)
        }

        task {
         text(LessonsBundle.message("js.testing.jest.end", productName))
        }
      }
    }

  override val existedFile: String?
    get() = "sum.test.js"
}