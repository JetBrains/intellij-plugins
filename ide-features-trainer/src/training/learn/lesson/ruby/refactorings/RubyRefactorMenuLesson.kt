// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.refactorings

import com.intellij.openapi.project.Project
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jList
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.fqn.FQN
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil
import training.commands.kotlin.TaskTestContext
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
        'default meow'
      end
    end

    class Cat < Animal
    end
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit
    get() = {
      prepareSample(sample)
      actionTask("Refactorings.QuickListPopupAction") {
        "RubyMine supports a variety of refactorings. Press ${action(it)} to see a partial list of them."
      }
      task("Push Members Down") {
        text("Some refactorings are seldom used and have no shortcut, but you can find them here. " +
             "Choose <strong>$it...</strong> now and complete the refactoring on <code>meow()</code>.")
        trigger("MemberPushDown") { checkMethodMoved(project) }
        test {
          ideFrame {
            jList("$it...").clickItem("$it...")
          }
          with(TaskTestContext.guiTestCase) {
            dialog(it) {
              button("Refactor").click()
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
      null)

    return barInDerived?.parentSymbol?.name == "Cat"
  }
}
