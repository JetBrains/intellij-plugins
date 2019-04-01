package training.learn.lesson.ruby

import com.intellij.testGuiFramework.framework.GuiTestUtil.shortcut
import com.intellij.testGuiFramework.framework.GuiTestUtil.typeText
import com.intellij.testGuiFramework.impl.jList
import com.intellij.testGuiFramework.util.Key
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RubyPostfixCompletionLesson(module: Module) : KLesson("Postfix Completion", module, "ruby") {
  private val sample = parseLessonSample("""class UsersController
  def create
    @user = User.new(user_params)
    @user.save<caret>
  end
end
""".trimIndent())
  private val result = parseLessonSample("""class UsersController
  def create
    @user = User.new(user_params)
    if @user.save
      <caret>
    end
  end
end
""".trimIndent()).text

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      task {
        text("The IDE can offer postfix shortcuts. Type <code>.if</code> and press <action>EditorEnter</action>.")
        trigger("EditorChooseLookupItem")
        test {
          ideFrame {
            typeText(".if")
            jList("if")
            shortcut(Key.ENTER)
          }
        }
      }
      task("@user.save") {
        text("Now choose the second item, <code>@user.save</code>.")
        stateCheck { editor.document.text == result }
        test {
          ideFrame {
            jList(it)
            shortcut(Key.DOWN)
            shortcut(Key.ENTER)
          }
        }
      }
    }

  override val existedFile: String
    get() = "app/Completions.rb"
}