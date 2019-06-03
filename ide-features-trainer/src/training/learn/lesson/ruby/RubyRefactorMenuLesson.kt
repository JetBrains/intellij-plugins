package training.learn.lesson.ruby

import com.intellij.openapi.project.Project
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jList
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context
import training.commands.kotlin.TaskTestContext
import training.lang.RubyLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.kimpl.KLesson
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.parseLessonSample

class RubyRefactorMenuLesson(module: Module) : KLesson("Refactoring Menu", module, "ruby") {
  private val sample = parseLessonSample("""
    class Animal
      def legs_number
        4
      end

      def <caret>meow
        "default meow"
      end
    end

    class Cat < Animal
    end
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      actionTask("Refactorings.QuickListPopupAction") {
        "RubyMine support a lot of refactorings. Press ${action(it)} to see the list of them."
      }
      task("Push Members Down") {
        text("Same rare refactorings have no shortcut but you could choose it here. " +
            "Choose <strong>$it...</strong> now and complete refactoring with <code>meow()</code> checked.")
        trigger("MemberPushDown") { checkMethodMoved(project) }
        test {
          ideFrame {
            jList("$it...").clickItem("$it...")
          }
          with(TaskTestContext.guiTestCase) {
            dialog(it) {
              button("Refactor").click()
              dialog("Problems Detected") {
                button("Continue").click()
              }
            }
          }
        }
      }
    }

  private fun checkMethodMoved(project: Project): Boolean {
    val derived = SymbolUtil.findConstantByFQN(
        project,
        Types.MODULE_OR_CLASS_OR_CONSTANT,
        FQN.of("Cat"),
        null) ?: return false

    val barInDerived = SymbolUtil.findMethod(
        derived,
        "meow",
        Types.METHODS,
        Context.INSTANCE.immutable(),
        null)

    return barInDerived?.parentSymbol?.name == "Cat"
  }

  override val existedFile = RubyLangSupport.sandboxFile
}
