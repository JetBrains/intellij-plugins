// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.lesson.editor

import com.intellij.ide.IdeBundle
import com.intellij.idea.ActionsBundle
import com.intellij.javascript.ift.JavaScriptLangSupport
import com.intellij.javascript.ift.JsLessonsBundle
import com.intellij.javascript.ift.lesson.setLanguageLevel
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.UIBundle
import training.learn.interfaces.Module
import training.learn.js.checkWordInSearchEverywhereInput
import training.learn.js.shiftSymbol
import training.learn.js.textAtCaretEqualsTo
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.checkToolWindowState
import training.learn.lesson.kimpl.parseLessonSample

class NavigationLesson(module: Module)
  : KLesson("Secrets of Efficient Navigation", JsLessonsBundle.message("js.editor.navigation.title"), module, JavaScriptLangSupport.lang) {

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
          text(
            JsLessonsBundle.message("js.editor.navigation.recent.files", action(it), strong(IdeBundle.message("title.popup.recent.files"))))
          trigger(it)
        }
        task {
          text(JsLessonsBundle.message("js.editor.navigation.choose.structure", strong(UIBundle.message("tool.window.name.structure"))))
          stateCheck {
            focusOwner?.javaClass.toString().contains("structureView.newStructureView.StructureViewComponent")
          }
        }
        task("ActivateStructureToolWindow") {
          text(JsLessonsBundle.message("js.editor.navigation.activate.structure", strong(UIBundle.message("tool.window.name.structure")), code("giveTreat"), action("EditorEnter"), action(it)))
          stateCheck {
            textAtCaretEqualsTo("giveTreat") && focusOwner is EditorComponentImpl
          }
        }
        task("FindUsages") {
          text(JsLessonsBundle.message("js.editor.navigation.find.usages", code("giveTreat"), action(it)))
          stateCheck {
            textAtCaretEqualsTo("giveTreat")
          }
          trigger(it)
        }
        task("HideActiveWindow") {
          text(JsLessonsBundle.message("js.editor.navigation.hide.tool.window", action(it)))
          checkToolWindowState("Find", false)
        }
        task("SearchEverywhere") {
          text(JsLessonsBundle.message("js.editor.navigation.search.everywhere", shortcut(shiftSymbol())))
          trigger(it)
        }
        task("GotoAction") {
          text(JsLessonsBundle.message("js.editor.navigation.search.everywhere.tabs", strong(ActionsBundle.message("action.SearchEverywhere.text")), action("EditorTab"), strong(IdeBundle.message("searcheverywhere.allelements.tab.name")), strong(IdeBundle.message("search.everywhere.group.name.actions")), strong(IdeBundle.message("search.everywhere.group.name.files")), action(it)))
          trigger(it)
        }
        task {
          text(
            JsLessonsBundle.message("js.editor.navigation.search.action", strong(IdeBundle.message("search.everywhere.group.name.actions")), strong(ActionsBundle.message("action.GotoDeclaration.text"))))
          stateCheck {
            checkWordInSearchEverywhereInput("go to d")
          }
        }
        task("GotoDeclaration") {
          text(JsLessonsBundle.message("js.editor.navigation.go.to.declaration", action(it), code("snoopy")))
          stateCheck {
            textAtCaretEqualsTo("snoopy")
          }
          trigger(it)
        }
        text(JsLessonsBundle.message("js.editor.navigation.keymap.reference",
                                   strong(JsLessonsBundle.message("js.editor.basics.module.name")),
                                   "https://resources.jetbrains.com/storage/products/webstorm/docs/WebStorm_ReferenceCard.pdf"))
      }
    }
  override val existedFile = "navigation.js"
}