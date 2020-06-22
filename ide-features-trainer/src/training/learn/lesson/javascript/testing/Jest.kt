// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.testing

import com.intellij.execution.RunManager
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.javascript.textOnLine
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.SwingConstants
import javax.swing.tree.DefaultMutableTreeNode

class Jest(module: Module) : KLesson("Fundamentals of Testing in WebStorm", module, JavaScriptLangSupport.lang){
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

        prepareRuntimeTask {
          val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
          val window: EditorWindow = fileEditorManager.currentWindow
          val file: VirtualFile = window.selectedFile

          fileEditorManager.createSplitter(SwingConstants.VERTICAL, window)
          val newWindow = fileEditorManager.getNextWindow(window)
          fileEditorManager.openFileWithProviders(file.parent.findChild("sum.js")!!, false, newWindow)
          newWindow.closeFile(file)
          fileEditorManager.currentWindow = window
        }

        task("editRunConfigurations") {
          text("""
            With WebStorm, all testing workflows become easier. Let’s see how. For this module, we’ll use <a href="https://jestjs.io/en/">Jest</a> and one of <a href="https://github.com/facebook/jest/tree/master/examples/getting-started">its sample projects</a> as an example, so please make sure you have Jest and npm/Yarn installed on your machine (see <a href="https://jestjs.io/docs/en/getting-started">this</a> webpage for more information).
            We’ll start by creating a run/debug configuration, which is going to be Jest-specific. On the main menu, select <strong>Run > Edit Configurations</strong>. Then click <icon>AllIcons.General.Add</icon>, add the <strong>Jest</strong> configuration with the default parameters, and hit <strong>OK</strong> to save it.
          """.trimIndent())
          stateCheck {
            RunManager.getInstance(project).findConfigurationByName("All Tests") != null
          }
        }

        task("Run") {
          text(""" 
            So, now the new <strong>All Tests</strong> configuration is selected by default. Let’s click the <strong>Run</strong> (<icon>AllIcons.RunConfigurations.TestState.Run</icon>) button right next to it.
          """.trimIndent())
          trigger(it)
        }

        task {
          text("""
            Now we can see the <strong>Run</strong> tool window with the test results and a stack trace for the failed tests coming from a test runner in it. Apart from simply tracking the test progress, you can do a lot of other things here. You can rerun all (<icon>AllIcons.RunConfigurations.TestState.Run</icon>) or only failed (<icon>AllIcons.RunConfigurations.RerunFailedTests</icon>) tests, view passed tests (<icon>AllIcons.RunConfigurations.ShowPassed</icon>), or enable the <a href="https://blog.jetbrains.com/webstorm/2018/10/testing-with-jest-in-webstorm/#run_tests_in_watch_mode">watch mode</a> to automatically rerun tests on changes. 
            You can also quickly find a specific test in <strong>Test Results</strong>. Let’s try it now: place the caret anywhere in the <strong>Test Results</strong> area, type <strong>add</strong>, and press <strong>Enter</strong> to jump to our test.
          """.trimIndent())
          stateCheck {
            (focusOwner as? Tree)?.getSelectedNodes(DefaultMutableTreeNode::class.java, null)?.firstOrNull()?.toString() == "adds 1 + 2 to equal 3"
          }
        }

        task {
          text("""That’s it! Use this feature whenever you need to quickly find your way through a lot of tests. Let’s now double-click <strong>adds 1 + 2 to equal 3</strong> that we’ve found.""".trimIndent())
          stateCheck {
            textAtCaretEqualsTo("toBe") && focusOwner is EditorComponentImpl
          }
        }

        task {
          text("""
            By double-clicking a test in <strong>Test Results</strong>, we’ve jumped straight to its location in the code. If you now hover over <strong>.toBe</strong>, you’ll see a popup explaining why the test failed. From there, you can also debug a test if needed. 
            Let’s now replace the incorrect value used for <strong>.toBe</strong>: on line 6, replace <strong>4</strong> with <strong>3</strong>.
          """.trimIndent())
          stateCheck {
            textOnLine(5, "3")
          }
        }

        task {
          text("""
            Now that we have the right value for the expected result, we can rerun our test. Let’s do it in a different way this time. See the <icon>AllIcons.RunConfigurations.TestState.Red2</icon> icon on the left of the test in the editor? This icon not only shows you the test status for the tests you’ve run recently, but also lets you quickly run and debug a specific test.
            Let’s click it and select <strong>Run adds 1 + 2 to equal 3</strong>.
          """.trimIndent())
          stateCheck {
            RunManager.getInstance(project).findConfigurationByName("adds 1 + 2 to equal 3") != null
          }
        }

        task("Coverage") {
          text("""
            Great job! Our test has successfully passed. 
            Let’s take a look at another handy tool. Click the <icon>AllIcons.General.RunWithCoverage</icon> icon located next to the run/debug configurations menu.
          """.trimIndent())
          trigger(it)
        }

        task("HideActiveWindow") {
          text("""
            This is how you can quickly build a code coverage report showing how many files were covered with tests, including the percentage of lines that were covered in those files. Now let’s close the coverage report with <shortcut>${KeymapUtil.getShortcutText("HideActiveWindow")}</shortcut>.  
          """.trimIndent())
          trigger(it)
        }

        task {
         text("""
           Congratulations! You’ve made it to the end of this module. Most of what we’ve learned also applies to the other test runners that WebStorm supports (except for code coverage and the watch mode). 
           For more tips and tricks about testing apps with Jest, please take a look at <a href="https://blog.jetbrains.com/webstorm/2018/10/testing-with-jest-in-webstorm/">this</a> blog post. If you use other test runners, you may want to explore our <a href="https://www.jetbrains.com/help/webstorm/unit-testing-javascript.html">web help</a>.
         """.trimIndent())
        }
      }
    }

  override val existedFile: String?
    get() = "sum.test.js"
}