// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.javascript.completion

import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.wm.ToolWindowManager
import training.lang.JavaScriptLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.javascript.checkWordInSearchEverywhereInput
import training.learn.lesson.javascript.setLanguageLevel
import training.learn.lesson.javascript.shiftSymbol
import training.learn.lesson.javascript.textAtCaretEqualsTo
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class NavigationLesson(module: Module) : KLesson("Secrets of Efficient Navigation", module, JavaScriptLangSupport.lang) {

  private val thisOwnerName = "$" + "{this.ownerName}"
  private val thisName = "$" + "{this.name}"
  private val favoriteTreat = "$" + "{favoriteTreat}"

  val sample = parseLessonSample("""
        import {Pet} from './pet';
        
        export class Dog extends Pet {
            constructor(name, ownerName, breed) {
                super(name, ownerName);
                this.breed = breed;
            }
        
            giveTreat(favoriteTreat) {
                console.log(`$thisOwnerName gives $thisName $favoriteTreat`)
            }
        }
        
        let snoopy = new Dog('Snoopy', 'Charlie', 'Beagle');
        
        snoopy.giveTreat('pizza');

        """.trimIndent())


  override val lessonContent: LessonContext.() -> Unit
    get() {
      return {
        setLanguageLevel()
        prepareRuntimeTask {
          //by default in 2020.1 "Structure" is in "top-left" state, also the state can be changed by user
          val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Structure")
          toolWindow?.setSplitMode(true, null)
        }
        
        prepareSample(sample)
        
        task("RecentFiles") {
          text("Let’s start with navigating around a project. Press ${action(it)} to call up the <strong>Recent Files</strong> popup.")
          trigger(it)
        }
        task {
          text("With this popup, you can quickly jump between recent files and IDE tool windows. One of those tool windows can assist you with navigating around a smaller piece of a project, a single file. Click the <strong>Structure</strong> tab to learn more about it.")
          stateCheck {
            focusOwner?.javaClass.toString().contains("structureView.newStructureView.StructureViewComponent")
          }
        }
        task("ActivateStructureToolWindow") {
          text("So, the <strong>Structure</strong> tool window can help you examine a file faster. It provides an overview of its structure and lets you jump to a specific item simply by typing its name. Start typing <strong>giveTreat</strong> anywhere in the tool window area, hit <action>EditorEnter</action>, and then hit ${action(it)} to close the panel and jump to the code. ")
          stateCheck {
            textAtCaretEqualsTo("giveTreat") && focusOwner is EditorComponentImpl
          }
        }
        task("FindUsages") {
          text("As a result of our actions, the caret has moved to <strong>giveTreat</strong> (line 9). Let’s leave it there and press ${action(it)} to open another tool window that can help you quickly find usages of any item. ")
          stateCheck {
            textAtCaretEqualsTo("giveTreat")
          }
          trigger(it)
        }
        task("HideActiveWindow") {
          text("Great! Now you can see the usages of giveTreat across the whole project and the libraries. Let’s close the tool window with ${action(it)}.")
          trigger(it)
        }
        task("SearchEverywhere") {
          text("Now press <shortcut>${shiftSymbol()}</shortcut> twice to meet another feature that can help you search faster.")
          trigger(it)
        }
        task("GotoAction") {
          text("This is the <strong>Search Everywhere</strong> popup. It lets you instantly find any action, file, class or symbol, and shows all the matches in one place. If you want to run a more specific search, you can hit <action>EditorTab</action> to switch from <strong>All</strong> to any other tab, such as <strong>Actions</strong> or <strong>Files</strong>. Or you can use a shortcut to open a specific tab that you need. Let’s try the latter with ${action(it)}.")
          trigger(it)
        }
        task {
          text("Now that we’re on the <strong>Actions</strong> tab, let’s start typing <strong>Go to Declaration or Usages</strong> in the search bar to look up a shortcut for another useful navigation feature.")
          stateCheck {
            checkWordInSearchEverywhereInput("go to d")
          }
        }
        task("GotoDeclaration") {
          text("Notice the ${action(it)} next to <strong>Go to Declaration or Usages</strong> – it shows you usages for the definition and vice versa. Let’s close the popup, place the caret on <strong>snoopy</strong> (line 16), and hit ${action(it)} to look for its declaration.")
          stateCheck {
            textAtCaretEqualsTo("snoopy")
          }
          trigger(it)
        }
        task {
          text("Congratulations! You’ve made it to the end of <strong>Editor Basics</strong>. Print out the <a href=\"https://resources.jetbrains.com/storage/products/webstorm/docs/WebStorm_ReferenceCard.pdf\">keymap reference</a> to have all the shortcuts handy as you make yourself at home in WebStorm. Click the button below to move to the next module.")
        }
      }
    }
  override val existedFile = "navigation.js"
}