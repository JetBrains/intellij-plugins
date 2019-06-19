package training.learn.lesson.ruby.refactorings

import com.intellij.testGuiFramework.impl.button
import com.intellij.ui.treeStructure.Tree
import training.commands.kotlin.TaskTestContext
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample
import java.util.concurrent.Future
import java.util.regex.Pattern

class RubyRenameLesson(module: Module) : KLesson("Rename", module, "ruby") {
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
  """.trimIndent()

  private val sample = parseLessonSample(template.replace("<name>", "teams"))

  private val replacePreviewPattern = Pattern.compile(".*Instance variable to be renamed to (\\w+).*")

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      lateinit var replace : Future<String>
      task("RenameElement") {
        text("Press ${action(it)} to rename the attribute accessor <code>teams</code> (e.g., to <code>teams_number</code>).")
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
      task("Do Refactor") {
        val result = template.replace("<name>", replace.get()).replace("<caret>", "")
        text("In order to be confident about the refactoring, RubyMine lets you preview it before confirming." +
            "Click <strong>$it</strong> to complete the refactoring.")
        stateCheck { editor.document.text == result }
        test {
          ideFrame {
            button(it).click()
          }
        }
      }
    }

  override val existedFile = RubyLangSupport.sandboxFile
}
