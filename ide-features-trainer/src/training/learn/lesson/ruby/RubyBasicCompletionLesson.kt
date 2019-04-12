package training.learn.lesson.ruby

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.testGuiFramework.framework.GuiTestUtil.shortcut
import com.intellij.testGuiFramework.framework.GuiTestUtil.typeText
import com.intellij.testGuiFramework.impl.jList
import com.intellij.testGuiFramework.util.Key
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.v2.ClassModuleSymbol
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RubyBasicCompletionLesson(module: Module) : KLesson("Basic Completion", module, "ruby") {
  private val sample1 = parseLessonSample("""class Animal
  def speak
    'Hello!'
  end
end

class Cat < <caret>
end
""".trimIndent())

  private val sample2 = parseLessonSample("""class Animal
  def speak
    'Hello!'
  end
end

class Cat < Animal
  def meow
    'Meow'
  end

  def speak
    <caret>
  end
end
""".trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample1)
      task {
        text("By default, the IDE completes your code instantly. Start typing <code>An</code> right where " +
            "the caret is, and you will see the Lookup Menu with matching suggestions. Choose the first item " +
            "<code>Animal</code> from the Lookup menu by pressing <action>EditorEnter</action>.")
        trigger("EditorChooseLookupItem",
            { editor.document.text },
            { before, now -> checkHierarchy(project) && now != before })
        test {
          ideFrame {
            typeText("An")
            jList("Animal")
            shortcut(Key.ENTER)
          }
        }
      }
      Thread.sleep(500)
      prepareSample(sample2)
      task("CodeCompletion") {
        text("To activate Basic Completion explicitly, press ${action(it)}. " +
            "Select <code>meow</code> and press <action>EditorEnter</action>.")
        trigger(it)
        trigger("EditorChooseLookupItem") { textBeforeCaret(editor, "meow") }
        test {
          actions(it)
          ideFrame {
            jList("meow")
            shortcut(Key.ENTER)
          }
        }
      }
    }

  private fun checkHierarchy(project: Project): Boolean {
    val catSymbol = SymbolUtil.findConstantByFQN(
        project,
        Types.MODULE_OR_CLASS_OR_CONSTANT,
        FQN.of("Cat"),
        null)

    return catSymbol is ClassModuleSymbol &&
      catSymbol.getSuperClassSymbol(null)?.name == "Animal"
  }

  private fun textBeforeCaret(editor: Editor, text: String) : Boolean {
    val offset = editor.caretModel.offset
    if (offset < text.length) {
      return false
    }
    val subSequence = editor.document.charsSequence.subSequence(offset - text.length, offset)
    return subSequence.toString() == text
  }

  override val existedFile = RubyLangSupport.sandboxFile
}