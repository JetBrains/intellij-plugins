// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.refactorings

import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.ui.NameSuggestionsField
import org.jetbrains.ruby.ift.RubyLessonsBundle
import training.dsl.LessonContext
import training.dsl.LessonUtil.restoreIfModifiedOrMoved
import training.dsl.dropMnemonic
import training.dsl.parseLessonSample
import training.learn.LessonsBundle
import training.learn.course.KLesson
import javax.swing.JButton

class RubyRenameLesson
  : KLesson("Rename", LessonsBundle.message("rename.lesson.name")) {

  private val template = """
    class Championship
      attr_accessor :<name>

      def matches
        <name>*(<name>-1)/2
      end

      def add_new_team
        @<name> += 1
      end
    end

    def teams
      16
    end

    c = Championship.new
    c.<caret><name> = teams

    puts c.<name>
  """.trimIndent() + '\n'

  private val sample = parseLessonSample(template.replace("<name>", "teams"))

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      var replace: String? = null
      task("RenameElement") {
        text(RubyLessonsBundle.message("ruby.rename.start.refactoring", action(it), code("teams"), code("teams_number")))
        triggerByUiComponentAndHighlight(false, false) { ui: NameSuggestionsField ->
          ui.addDataChangedListener {
            replace = ui.enteredName
          }
          true
        }
        stateCheck {
          replace != null && ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.FIND)?.isVisible == true
        }
        restoreIfModifiedOrMoved()
        test {
          actions(it)
          dialog {
            type("teams_number")
            button("Refactor").click()
          }
        }
      }

      val confirmRefactoringButton = RefactoringBundle.message("usageView.doAction").dropMnemonic()
      task {
        triggerByUiComponentAndHighlight(highlightInside = false) { button: JButton ->
          button.text.contains(confirmRefactoringButton)
        }
      }

      task {
        val result = replace?.let { template.replace("<name>", it).replace("<caret>", "") }
        text(RubyLessonsBundle.message("ruby.rename.confirm", strong(confirmRefactoringButton)))
        stateCheck { editor.document.text == result }
        test {
          ideFrame {
            button(confirmRefactoringButton).click()
          }
        }
      }
    }
}
