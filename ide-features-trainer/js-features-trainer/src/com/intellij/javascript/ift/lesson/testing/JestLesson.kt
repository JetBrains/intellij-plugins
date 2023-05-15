// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.lesson.testing

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.RunManager
import com.intellij.execution.testframework.TestRunnerBundle
import com.intellij.execution.testframework.sm.SmRunnerBundle
import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.javascript.ift.JsLessonsBundle
import com.intellij.javascript.ift.lesson.setLanguageLevel
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.ui.treeStructure.Tree
import training.dsl.*
import training.dsl.LessonUtil.highlightBreakpointGutter
import training.dsl.LessonUtil.productName
import training.learn.course.KLesson
import training.learn.js.textOnLine
import training.ui.LearningUiHighlightingManager
import java.awt.event.KeyEvent
import javax.swing.SwingConstants
import javax.swing.tree.DefaultMutableTreeNode

class JestLesson
  : KLesson("Fundamentals of Testing in WebStorm", JsLessonsBundle.message("js.testing.jest.title", productName)){
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
          val window = fileEditorManager.currentWindow!!
          val file = window.selectedFile!!

          WriteAction.run<Throwable> {
            fileEditorManager.createSplitter(SwingConstants.VERTICAL, window)
            val newWindow = fileEditorManager.getNextWindow(window)
            fileEditorManager.openFile(file = file.parent.findChild("sum.js")!!, window = newWindow)
            newWindow?.closeFile(file)
            fileEditorManager.currentWindow = window
          }
        }

        task("editRunConfigurations") {
          val editConfiguration = strong(ActionsBundle.message("group.RunMenu.text").dropMnemonic() + 
                                         " > " + 
                                         ExecutionBundle.message("edit.configuration.action").dropMnemonic())
          
          text(JsLessonsBundle.message("js.testing.jest.prepare",
                                       "https://jestjs.io/en/",
                                       "https://github.com/facebook/jest/tree/master/examples/getting-started",
                                       "https://jestjs.io/docs/en/getting-started",
                                       editConfiguration,
                                       icon(AllIcons.General.Add), strong("Jest"), strong("OK")))
          stateCheck {
            RunManager.getInstance(project).findConfigurationByName(TestRunnerBundle.message("all.tests.scope.presentable.text")) != null
          }
        }

        highlightButtonById("Run")
        task("Run") {
          text(JsLessonsBundle.message("js.testing.jest.run", strong(TestRunnerBundle.message("all.tests.scope.presentable.text")),
                                       strong(ExecutionBundle.message("run.configurable.display.name")), icon(AllIcons.RunConfigurations.TestState.Run)))
          trigger(it)
        }

        task {
          before {
            LearningUiHighlightingManager.clearHighlights()
          }
          text(JsLessonsBundle.message("js.testing.jest.navigate.1",
                                       strong(ExecutionBundle.message("tool.window.name.run")),
                                       icon(AllIcons.RunConfigurations.TestState.Run),
                                       icon(AllIcons.RunConfigurations.RerunFailedTests),
                                       icon(AllIcons.RunConfigurations.ShowPassed),
                                       "https://blog.jetbrains.com/webstorm/2018/10/testing-with-jest-in-webstorm/#run_tests_in_watch_mode"))
          text(JsLessonsBundle.message("js.testing.jest.navigate.2",
                                       strong(SmRunnerBundle.message("sm.test.runner.ui.tests.tree.presentation.labels.test.results")),
                                       strong("add"),
                                       shortcut(KeymapUtil.getKeyText(KeyEvent.VK_ENTER))))
          stateCheck {
            (focusOwner as? Tree)?.getSelectedNodes(DefaultMutableTreeNode::class.java, null)?.firstOrNull()?.toString() == "adds 1 + 2 to equal 3"
          }
        }
        
        task {
          text(JsLessonsBundle.message("js.testing.jest.fix.test.1", strong(".toBe")))
          text(JsLessonsBundle.message("js.testing.jest.fix.test.2", strong(".toBe"), strong("4"), strong("3")))
          stateCheck {
            textOnLine(5, "3")
          }
        }

        highlightBreakpointGutter(xRange = { IntRange(13, it - 17) }) { LogicalPosition(4, 0) }
        task {
          text(JsLessonsBundle.message("js.testing.jest.re.run.test.1", icon(AllIcons.RunConfigurations.TestState.Red2)))
          text(JsLessonsBundle.message("js.testing.jest.re.run.test.2", strong("Run adds 1 + 2 to equal 3")))
          stateCheck {
            RunManager.getInstance(project).findConfigurationByName("adds 1 + 2 to equal 3") != null
          }
        }

        highlightButtonById("Coverage")
        task("Coverage") {
          before {
            LearningUiHighlightingManager.clearHighlights()
          }
          text(JsLessonsBundle.message("js.testing.jest.success.run.coverage.1"))
          text(JsLessonsBundle.message("js.testing.jest.success.run.coverage.2", icon(AllIcons.General.RunWithCoverage)))
          checkToolWindowState("Coverage", true)
        }

        task("HideActiveWindow") {
          before {
            LearningUiHighlightingManager.clearHighlights()
          }
          text(JsLessonsBundle.message("js.testing.jest.coverage.result", shortcut(KeymapUtil.getShortcutText("HideActiveWindow"))))
          checkToolWindowState("Coverage", false)
        }

        text(JsLessonsBundle.message("js.testing.jest.end",
                                   "https://blog.jetbrains.com/webstorm/2018/10/testing-with-jest-in-webstorm/",
                                   "https://www.jetbrains.com/help/webstorm/unit-testing-javascript.html"))
      }
    }

  override val sampleFilePath: String = "sum.test.js"
}
