// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.refactorings

import com.intellij.refactoring.RefactoringBundle
import com.intellij.testGuiFramework.impl.button
import com.intellij.ui.treeStructure.Tree
import training.commands.kotlin.TaskTestContext
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class RubyRenameLesson(module: Module)
  : KLesson("Rename", LessonsBundle.message("rename.lesson.name"), module, "ruby") {

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

  private val replacePreviewPattern = Pattern.compile(".*Instance variable to be renamed to (\\w+).*")

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      lateinit var replace: Future<String>
      task("RenameElement") {
        text(LessonsBundle.message("ruby.rename.start.refactoring", action(it), code("teams"), code("teams_number")))
        replace = stateRequired {
          (focusOwner as? Tree)?.model?.root?.toString()?.let { root: String ->
            replacePreviewPattern.matcher(root).takeIf { m -> m.find() }?.group(1)
          }
        }
        test {
          actions(it)
          with(TaskTestContext.guiTestCase) {
            dialog {
              typeText("teams_number")
              button("Refactor").click()
            }
          }
        }
      }
      task(RefactoringBundle.message("usageView.doAction").dropMnemonic()) {
        var result = ""
        before {
          result = template.replace("<name>", replace.get(2, TimeUnit.SECONDS)).replace("<caret>", "")
        }
        text(LessonsBundle.message("ruby.rename.confirm", LessonUtil.productName, strong(it)))
        stateCheck { editor.document.text == result }
        test {
          ideFrame {
            button(it).click()
          }
        }
      }
    }
}
