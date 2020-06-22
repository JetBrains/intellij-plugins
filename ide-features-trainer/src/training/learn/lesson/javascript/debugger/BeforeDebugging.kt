// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.debugger

import com.intellij.execution.RunManager
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.ui.treeStructure.Tree
import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.tree.DefaultMutableTreeNode

class BeforeDebugging(module: Module) : KLesson("Before Debugging: Run/Debug Configurations", module, JavaScriptLangSupport.lang) {

  companion object {
    val jsDebuggerSample = parseLessonSample("""
        function compareNumbers(a, b) {
            if (a === b) {
                return "Different!";
            } else {
                return "Equal!"
            }
        }

        console.log(compareNumbers(10, -20));
        """.trimIndent())
  }


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareSample(jsDebuggerSample)
        task("RunClass") {
          text("With WebStorm, you can run and debug all kinds of JavaScript apps right where you write your code.\n" +
               "In this module, we’ll go over some steps that will be helpful no matter what kind of code you debug. In our case, it will be a very basic <a href='https://nodejs.org/en/'>Node.js</a> app that should compare two numbers and return <strong>Different!</strong> or <strong>Equal!</strong> Please make sure Node.js is <a href='https://nodejs.org/en/download/'>installed</a> on your machine before moving forward (for a fresh install, you’ll need to restart WebStorm). Hit ${action(it)} if you’re ready to continue.")
          trigger(it)
        }
        task("HideActiveWindow") {
          text("Two things happened as we hit <action>RunClass</action>. First, we ran a file using Node.js and opened the <strong>Run</strong> tool window, which shows the results of running the code. Second, WebStorm created a temporary run/debug configuration so we could run a file like that. Let’s hide the tool window with ${action(it)} for now and get to know run/debug configurations better.")
          trigger(it)
        }
        task {
          text("So, these configurations serve as an entry point to running/debugging apps. They can be temporary or permanent. The main difference is that temporary ones are automatically deleted if the default limit of 5 configurations is reached. \n" +
            "Let’s see how you can turn a temporary configuration into a permanent one. Open the <strong>debugging.js</strong> drop-down menu located in the top right-hand corner and select <strong>Save 'debugging.js' Configuration</strong>. ")
          stateCheck {
            val selectedConfiguration = RunManager.getInstance(project).selectedConfiguration ?: return@stateCheck false
            !selectedConfiguration.isTemporary
          }

        }
        task {
          text("That’s it! Now, what if you want to adjust the settings of this new run/debug configuration or use another one? Open the <strong>debugging.js</strong> menu again and click <strong>Edit Configurations</strong>.")
          stateCheck {
            ((focusOwner as? Tree)?.model?.root as? DefaultMutableTreeNode)?.lastChild.toString() == "Templates"
          }
        }

        task {
          text("This is a place for managing run/debug configurations. To add a new one, hit <strong>+</strong>, select the desired configuration type, and specify the settings based on your project/configuration type. To fine-tune an existing one, you can click its name and update what’s needed.\n" +
            "Take some time to explore what can be found here and close the window once you’re ready to move next.")
          stateCheck {
            focusOwner is EditorComponentImpl
          }
        }
        task {
          text("That’s it for this lesson. To start the next one, click the button below or use ${action("learn.next.lesson")}.")
        }

      }
    }
  override val existedFile = "debugging.js"
}


